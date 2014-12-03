var express = require('express');
var cors = require('cors');
var https = require('https');
var bodyParser = require('body-parser');
var morgan = require('morgan');
var fs = require('fs');

var config = require('./config');
var manifest = require('./manifest')
var routes = require('./routes');


// Display the error message and halt the process.
function error(info) {
    console.log('ERROR:', info);
    console.log('SERVER NOT STARTED.');
    process.exit(1);
}

// CHECK IF THE RESULT ALREADY EXISTS
var cmdlineOption = process.argv[2];
var resultFileExists = fs.existsSync(config.RESULT_FILE);
if (resultFileExists && cmdlineOption !== '--serveResult') {
    error('The file with result already exists.\nRemove this file or run the server with --serveResult option.');
}
if (cmdlineOption === '--serveResult' && !resultFileExists) {
    error('The file with result does not exist.');
}

// PROCESSING DATA (THE PARTIAL RESULT) FROM A FILE
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
app.get('/result.msg', routes.serveFile(config.RESULT_FILE));

// STARTING THE SERVER
if (config.useTLS) {
    var tls_options = {
        key:  fs.readFileSync(config.TLS_KEY_FILE),
        cert: fs.readFileSync(config.TLS_CERT_FILE)
    };
    app = https.createServer(tls_options, app)
}

var server = app.listen(config.port, function() {
    console.log('Final Server running for election "%s" [%s]', manifest.title, manifest.hash);
    if (config.useTLS) 
        console.log('Using TLS');
});

