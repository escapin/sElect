var express = require('express');
var https = require('https');
var bodyParser = require('body-parser');
var morgan = require('morgan'); // logging
var fs = require('fs');

var config = require('./config');
var manifest = require('./manifest')
var routes = require('./routes');


// CREATE AND CONFIGURE THE APP
var app = express();
app.set('views', './views');    // location of the views
app.set('view engine', 'ejs');  // view engine
app.use(bodyParser.json()); 
// app.use( morgan(':remote-addr [:date] :method :url :status / :referrer ', {}) ); // logging (onto console)

// ROUTES
app.post('/otp', routes.otp);
app.post('/cast', routes.cast);

app.get('/', routes.info);
app.get('/close', routes.close); // for testing

// STARGING THE SERVER
var tls_options = {
    key:  fs.readFileSync(config.TLS_KEY_FILE),
    cert: fs.readFileSync(config.TLS_CERT_FILE)
};

var server = https.createServer(tls_options, app).listen(config.port, function() {
    console.log('Collecting Server running for election "%s" [%s]', manifest.title, manifest.electionID);
    console.log('HTTPS server listening on %s, port %d\n', server.address().address, server.address().port);
});


