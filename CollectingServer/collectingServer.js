var express = require('express');
var cors = require('cors');
var https = require('https');
var bodyParser = require('body-parser');
var morgan = require('morgan'); // logging
var winston = require('winston');
var basicAuth = require('basic-auth-connect');
var fs = require('fs');
var mkdirp = require('mkdirp');
var bcrypt = require("bcryptjs");

var config = require('./config');
var manifest = require('./src/manifest')

// LOGGING (to a file in addition to the console)
winston.add(winston.transports.File, { filename: config.LOG_FILE });

// create the folder where the data will be stored
mkdirp.sync(config.DATA_FOLDER);

// CHECK IF THE RESULT ALREADY EXISTS
var cmdlineOption = process.argv[2];
var resultFileExists = fs.existsSync(config.RESULT_FILE);
if (resultFileExists && cmdlineOption !== '--serveResult') {
    console.log('ERROR: The file with result (%s) already exists.', config.RESULT_FILE);
    console.log('Remove this file or run the server with --serveResult option.');
    console.log('SERVER NOT STARTED.');
    process.exit(1);
}
if (cmdlineOption === '--serveResult' && !resultFileExists) {
    console.log('ERROR: The file with result does not exist.');
    console.log('SERVER NOT STARTED.');
    process.exit(1);
}

// CHECK IF THE LOG WITH ACCEPTED BALLOTS EXISTS
var logFileExists = fs.existsSync(config.ACCEPTED_BALLOTS_LOG_FILE);
if (logFileExists && !resultFileExists && cmdlineOption !== '--resume') {
    console.log('ERROR: Log file with accepted ballots (%s) exists.', config.ACCEPTED_BALLOTS_LOG_FILE);
    console.log('Remove this file or run the server with --resume option.');
    console.log('SERVER NOT STARTED.');
    process.exit(1);
}
if (cmdlineOption === '--resume' && !logFileExists) {
    console.log('ERROR: Log file with accepted ballots does not exist');
    console.log('Server cannot be resumed.');
    process.exit(1);
}

// CHECK FOR WRONG OPTIONS
if (cmdlineOption && cmdlineOption!=='--resume' && cmdlineOption!=='--serveResult') {
    console.log('ERROR: Wrong option');
    console.log('SERVER NOT STARTED.');
    process.exit(1);
}


// CREATE AND CONFIGURE THE APP
var routes = require('./src/routes');
var app = express();
app.use(cors()); // enable all CORS request
app.set('views', './src/views');    // location of the views
app.set('view engine', 'ejs');  // view engine
app.use(bodyParser.urlencoded({extended:true}));
app.use(bodyParser.json()); 
app.use(express.static('./public')); // static content
app.use( morgan('*** :remote-addr [:date] :method :url :status / :referrer [:response-time ms]', {}) ); // logging
//app.use('/admin/*', basicAuth('admin', config.serverAdminPassword)); // authentication for the admin panel only
if(config.serverAdminPassword != ""){
	app.use('/admin/*', basicAuth(function(username,password){
		var salt = bcrypt.getSalt(config.serverAdminPassword);
		var hash = bcrypt.hashSync(password, salt);
        return ((username === 'admin') && (hash === config.serverAdminPassword))
    }));
}

// Insert some delay (for testing only):
// app.use(function(req, res, next) { setTimeout(next, 300); });

// ROUTES
app.post('/otp', routes.otp);
app.post('/cast', routes.cast);

app.get('/', routes.info);
app.get('/status', routes.info);
app.get('/admin/panel', routes.controlPanel);
app.get('/admin/close', routes.close);
app.get('/result.msg', routes.serveFile(config.RESULT_FILE));
if(manifest.publishListOfVoters)
	app.get('/votersList.msg', routes.serveFile(config.VOTERSLIST_FILE));
app.get('/manifest', routes.serveFile(config.MANIFEST_FILE));

// STARTING THE SERVER

if (config.useTLS) {
	key = fs.readFileSync(config.TLS_KEY_FILE, 'utf8');
	cert = fs.readFileSync(config.TLS_CERT_FILE, 'utf8');
    var tls_options = {
			key:  key,
			cert: cert
    	};
    app = https.createServer(tls_options, app);
}
var server = app.listen(config.port, function() {
    console.log('Collecting Server running for election "%s" [%s]', manifest.title, manifest.hash);
    winston.info('SERVER STARTED');
    console.log('Server listening on %s, port %d', server.address().address, server.address().port);
    if (config.useTLS) 
        console.log('Using TLS');
});

