var express = require('express');
var cors = require('cors');
var https = require('https');
var bodyParser = require('body-parser');
var morgan = require('morgan');
var fs = require('fs');
var mkdirp = require('mkdirp');

var config = require('./src/config');
var manifest = require('./src/manifest')
var routes = require('./src/routes');

//create the folder where the data will be stored
mkdirp(config.DATA_FOLDER, function (err) {
    if (err) 
    	console.error("Error: ", err);
//    else 
//    	console.log("Folder '" + config.DATA_FOLDER + "' created.");
});

// Display the error message and halt the process.
function error(info) {
    console.log('ERROR:', info);
    console.log('SERVER NOT STARTED.');
    process.exit(1);
}

// CHECK IF THE RESULT ALREADY EXISTS
var cmdlineOption = process.argv[2];
var resultFileExists = fs.existsSync(config.OUTPUT_FILE);
if (resultFileExists && cmdlineOption !== '--serveResult') {
    error('The file with result (' +config.OUTPUT_FILE+ ') already exists.\nRemove this file or run the server with --serveResult option.');
}
if (cmdlineOption === '--serveResult' && !resultFileExists) {
    error('The file with result does not exist.');
}

// PROCESSING DATA (THE BALLOTS) FROM A FILE
if (cmdlineOption==='--processData') {
    var dataFileName = process.argv[3];
    if (!dataFileName)
        error('File name not given');
    if (!fs.existsSync(dataFileName))
        error('The file does not exist.');
    routes.processFile(dataFileName);
}

// CHECK FOR WRONG OPTIONS
if (cmdlineOption && cmdlineOption!=='--processData' && cmdlineOption!=='--serveResult') {
    error('Wrong option');
}

// CREATE AND CONFIGURE THE APP
var app = express();
app.use(cors()); // enable all CORS request
app.set('views', './views');    // view engine and location of the views
app.set('view engine', 'ejs');
app.use(bodyParser.json());
app.use(express.static('./public')); // static content
app.use( morgan(':remote-addr [:date] :method :url :status / :referrer ', {}) ); // logging

// ROUTES
app.get('/', routes.statusPage);
app.get('/status', routes.statusPage);
app.post('/data', routes.process);
app.get('/manifest', routes.serveFile(config.MANIFEST_FILE));
app.get('/result.msg', routes.serveFile(config.OUTPUT_FILE));

// STARTING THE SERVER
if (config.useTLS) {
	// The file containing the chain of trust is optional:
	// if this is omitted several well known "root" CAs will 
	// be used, like VeriSign
	ca = [];
	try{
		chain = fs.readFileSync(config.TLS_CHAINTRUST_FILE, 'utf8');		
		chain = chain.split("\n");
		cert = [];
		for (line in chain){
			if(line.length!==0)
				cert.push(line);
			if (line === "/-END CERTIFICATE-/"){
				ca.push(cert.join("\n"));
				cert = [];
			}
		}
	} catch(err){
//		winston.info("Problems opening the file. " + config.TLS_CHAINTRUST_FILE +
//				"\n\tSince this file is optional, probably it does not exist." +
//				"\n\t" + err);
		console.log("WARNING: The file '" + config.TLS_CHAINTRUST_FILE + 
				"' containg the chain of trust is not present.");
				
	}
	
	key = fs.readFileSync(config.TLS_KEY_FILE, 'utf8');
	cert = fs.readFileSync(config.TLS_CERT_FILE, 'utf8');
	if(ca.length!==0)
		var tls_options = {
			ca: ca,
			key:  key,
			cert: cert
    	};
	else
		var tls_options = {
			key:  key,
			cert: cert
    	};
	app = https.createServer(tls_options, app)
}

var server = app.listen(config.port, function() {
    console.log('Mix Server #%s running for election "%s" [%s]', routes.chainIndex, manifest.title, manifest.hash);
    console.log('Server listening on %s, port %d', server.address().address, server.address().port);
    if (config.useTLS)
        console.log('Using TLS');
});

