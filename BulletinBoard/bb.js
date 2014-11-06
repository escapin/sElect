var express = require('express');
var bodyParser = require('body-parser');
var errorHandler = require('errorhandler')
var morgan = require('morgan')

var config = require('./config');
var manifest = require('./manifest');
var routes = require('./routes');
var result = require('./result');

// CREATE AND CONFIGURE THE APP
var app = express();

app.set('views', './views');    // view engine and location of the views
app.set('view engine', 'ejs'); 
app.use(bodyParser.urlencoded({ extended: true })); // body parser (important for POST request)
app.use(express.static('./public')); // static content // was: __dirname + '/public'
app.use(errorHandler({ dumpExceptions: true, showStack: true })); // error handling (not for production)
app.use( morgan(':remote-addr [:date] :method :url :status / :referrer ', {}) );

// ROUTES
app.get('', routes.index);
app.get('/index.html', routes.index);
app.get('/SignedFinalResult', routes.serveFile(config.RESULT_FILE));
app.get('/SignedPartialResult', routes.serveFile(config.PARTIAL_RESULT_FILE));

// SET THE BACKROUD CHECK FOR THE RESULT FILE
setInterval( result.loadResult, 5000);
// TODO: 
// (1) We could make sure that this is alive on every request (just in case)
// (2) Remove this when the result is read.

// STARGING THE SERVER
var server = app.listen(config.port, function() {
    console.log('Bulleting Board running for election "%s" [%s]', manifest.title, manifest.hash);
    console.log('Listening on %s, port %d\n', server.address().address, server.address().port);
});

