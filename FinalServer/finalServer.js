var express = require('express');
var https = require('https');
var bodyParser = require('body-parser');
var morgan = require('morgan');
var fs = require('fs');

var config = require('./config');
var manifest = require('./manifest')
var routes = require('./routes');

// CHECK IF THE RESULT ALREADY EXISTS
var resultFileExists = fs.existsSync(config.RESULT_FILE);
if (resultFileExists && process.argv[2] !== '--onlyServeResult') {
    console.log('ERROR: The file with result already exists.');
    console.log('Remove this file or run the server with --onlyServeResult option.');
    console.log('Server not started.');
    process.exit(1);
}
if (process.argv[2] === '--onlyServeResult' && !resultFileExists) {
    console.log('ERROR: The file with result does not exist.');
    console.log('Server not started.');
    process.exit(1);
}

// CREATE AND CONFIGURE THE APP
var app = express();

app.set('views', './views');    // view engine and location of the views
app.set('view engine', 'ejs'); 
app.use(bodyParser.json()); 
app.use(express.static('./public')); // static content
app.use( morgan(':remote-addr [:date] :method :url :status / :referrer ', {}) ); // logging

// ROUTES
app.get('/', routes.statusPage);
app.post('/data', routes.process);
app.get('/manifest', routes.serveFile(config.MANIFEST_FILE));
app.get('/result.msg', routes.serveFile(config.RESULT_FILE));

// STARTING THE SERVER
var tls_options = {
    key:  fs.readFileSync(config.TLS_KEY_FILE),
    cert: fs.readFileSync(config.TLS_CERT_FILE)
};

var server = https.createServer(tls_options, app).listen(config.port, function() {
    console.log('Final Server running for election "%s" [%s]', manifest.title, manifest.electionID);
    console.log('HTTPS server listening on %s, port %d\n', server.address().address, server.address().port);
});

