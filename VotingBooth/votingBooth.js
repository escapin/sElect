var express = require('express');
var https = require('https');
var bodyParser = require('body-parser');
var errorHandler = require('errorhandler');
var morgan = require('morgan'); // logging
var fs = require('fs');

var config = require('./config');
var manifest = require('./manifest');
var routes = require('./routes');

// CREATE AND CONFIGURE THE APP

var app = express();
app.set('views', './views');    // location of the views
app.set('view engine', 'ejs');  // view engine
app.use(bodyParser.urlencoded({ extended: true })); //for POST requests
app.use(express.static('./public')); // static content
app.use(errorHandler({ dumpExceptions: true, showStack: true })); // error handling (not for production)
// app.use( morgan(':remote-addr [:date] :method :url :status / :referrer ', {}) ); // logging (onto console)


// ROUTES

app.get('', routes.welcome);
app.get('/index.html', routes.welcome);
app.get('/welcome', routes.welcome);
app.post('/welcome', routes.prompt_for_otp);
app.post('/select', routes.select);
app.post('/cast', routes.cast);

// STARGING THE SERVER

var tls_options = {
    key:  fs.readFileSync(config.TLS_KEY_FILE),
    cert: fs.readFileSync(config.TLS_CERT_FILE)
};

var server = https.createServer(tls_options, app).listen(config.port, function() {
    console.log('Voting Booth running for election "%s" [%s]', manifest.title, manifest.hash);
    console.log('HTTPS server listening on %s, port %d\n', server.address().address, server.address().port);
});

