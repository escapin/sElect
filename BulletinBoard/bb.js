var express = require('express');
var bodyParser = require('body-parser');
var util = require('util');
var errorHandler = require('errorhandler')
var fs = require('fs');
var morgan = require('morgan')

var config = require('./config');
var manifest = require('./manifest');
var routes = require('./routes');
var result = require('./result');

// CREATE AND CONFIGURE THE APP

var app = express();

// view engine and location of the views
app.set('views', './views');
app.set('view engine', 'ejs');

// body parser (important for POST request)
app.use(bodyParser.urlencoded({ extended: true }));

// static content
app.use(express.static('./public')); // was: __dirname + '/public'
// app.use(express.static(RESULT_DIR)); 

// error handling (not for production)
app.use(errorHandler({ dumpExceptions: true, showStack: true }));

// logging (onto console)
app.use( morgan(':remote-addr [:date] :method :url :status / :referrer ', {}) );


// ROUTES

app.get('', routes.index);
app.get('/index.html', routes.index);


// SET THE BACKROUD CHECK FOR THE RESULT FILE

setInterval( result.loadResult, 5000);
// TODO: 
// (1) We could make sure that shit is alive on every
// request (just in case)
// (2) Remove this when the result is read.

// STARGING THE SERVER

var server = app.listen(config.port, function() {
    console.log('Bulleting Board running for election "%s" [%s]', manifest.title, manifest.electionID);
    console.log('Listening on %s, port %d\n', server.address().address, server.address().port);
});

