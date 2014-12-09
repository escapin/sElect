var express = require('express');
var bodyParser = require('body-parser');
var errorHandler = require('errorhandler')
var morgan = require('morgan')

var config = require('./config');
var manifest = require('./src/manifest');
var routes = require('./src/routes');
var result = require('./src/result');

// CREATE AND CONFIGURE THE APP
var app = express();

app.set('views', './src/views');    // view engine and location of the views
app.set('view engine', 'ejs'); 
app.use(bodyParser.urlencoded({ extended: true })); // body parser (important for POST request)
app.use(express.static('./public')); // static content // was: __dirname + '/public'
app.use(errorHandler({ dumpExceptions: true, showStack: true })); // error handling (not for production)
app.use( morgan(':remote-addr [:date] :method :url :status / :referrer ', {}) );

// ROUTES
app.get('', routes.summary);
app.get('/summary', routes.summary);
app.get('/votes', routes.votes);
app.get('/voters', routes.voters);
app.get('/details', routes.details);

app.get('/SignedFinalResult', routes.serveFile(config.RESULT_FILE));
app.get('/SignedPartialResult', routes.serveFile(config.PARTIAL_RESULT_FILE));
app.get('/ElectionManifest.json', routes.serveFile(config.MANIFEST_FILE));

// SET THE BACKROUD CHECK FOR THE RESULT FILE
setInterval( result.loadResult, 5000);
result.loadResult();
// TODO: 
// (1) We could make sure that this is alive on every request (just in case)
// (2) Remove this when the result is read.

// STARGING THE SERVER
var server = app.listen(config.port, function() {
    console.log('Bulleting Board running for election "%s" [%s]', manifest.title, manifest.hash);
    console.log('Listening on %s, port %d\n', server.address().address, server.address().port);
});

