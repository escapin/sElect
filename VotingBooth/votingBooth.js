var express = require('express');
var bodyParser = require('body-parser');
var errorHandler = require('errorhandler');
var morgan = require('morgan'); // logging

var config = require('./config');
var manifest = require('./manifest');
var routes = require('./routes');
var _voter = require('./protocol/voter');

// Check the manifest 

if (!manifest) { // there is no manifest
    console.log('ERROR: Cannot find an election manifest file.');
    console.log('Server not started.');
    process.exit(1);
}

// CREATE AND CONFIGURE THE APP

var app = express();
app.set('views', './views');    // location of the views
app.set('view engine', 'ejs');  // view engine
app.use(bodyParser.urlencoded({ extended: true })); //for POST requests
app.use(express.static('./public')); // static content
app.use(errorHandler({ dumpExceptions: true, showStack: true })); // error handling (not for production)
app.use( morgan(':remote-addr [:date] :method :url :status / :referrer ', {}) ); // logging (onto console)


// ROUTES

app.get('', routes.welcome);
app.get('/index.html', routes.welcome);
app.get('/welcome', routes.welcome);
app.post('/welcome', routes.prompt_for_otp);
app.post('/select', routes.select);
app.post('/cast', routes.cast);

// STARGING THE SERVER

var server = app.listen(config.port, function() {
    console.log('Voting Booth running for election "%s" [%s]', manifest.title, manifest.electionID);
    console.log('Listening on %s, port %d\n', server.address().address, server.address().port);
});

