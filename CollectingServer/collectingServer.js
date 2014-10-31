var express = require('express');
var https = require('https');
var bodyParser = require('body-parser');
var morgan = require('morgan'); // logging
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
app.set('views', './views');    // location of the views
app.set('view engine', 'ejs');  // view engine
app.use(bodyParser.json()); 
app.use(express.static('./public')); // static content
// app.use( morgan(':remote-addr [:date] :method :url :status / :referrer ', {}) ); // logging


// ROUTES
app.post('/otp', routes.otp);
app.post('/cast', routes.cast);

app.get('/', routes.info);
app.get('/close', routes.close); // for testing
app.get('/result.msg', routes.serveFile(config.RESULT_FILE));
app.get('/manifest', routes.serveFile(config.MANIFEST_FILE));

// STARGING THE SERVER
var tls_options = {
    key:  fs.readFileSync(config.TLS_KEY_FILE),
    cert: fs.readFileSync(config.TLS_CERT_FILE)
};

var server = https.createServer(tls_options, app).listen(config.port, function() {
    console.log('Collecting Server running for election "%s" [%s]', manifest.title, manifest.electionID);
    console.log('HTTPS server listening on %s, port %d\n', server.address().address, server.address().port);
});


