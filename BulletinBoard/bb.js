var express = require('express');
var bodyParser = require('body-parser');
var errorHandler = require('errorhandler');
var morgan = require('morgan');
var mkdirp = require('mkdirp');

var config = require('./config');
var manifest = require('./src/manifest');
var routes = require('./src/routes');
var result = require('./src/result');

//create the folder where the data coming from the other servers will be stored
mkdirp(config.DATA_FOLDER, function (err) {
    if (err) 
    	console.error("Error: ", err);
//    else 
//    	console.log("Folder '" + config.DATA_FOLDER + "' created.");
});

// CREATE AND CONFIGURE THE APP
var app = express();

app.set('views', './src/views');    // view engine and location of the views
app.set('view engine', 'ejs'); 
app.use(bodyParser.urlencoded({ extended: true })); // body parser (important for POST request)
app.use(express.static('./public')); // static content // was: __dirname + '/public'
app.use(errorHandler({ dumpExceptions: true, showStack: true })); // error handling (not for production)
app.use( morgan(':remote-addr [:date] :method :url :status / :referrer ', {}) );


// ROUTES
app.get('/', routes.summary);
app.get('/summary', routes.summary);
app.get('/votes', routes.votes);
if (manifest.publishListOfVoters)
    app.get('/voters', routes.voters);
app.get('/details', routes.details);

app.get('/ElectionManifest.json', routes.serveFile(config.MANIFEST_FILE));
app.get('/SignedFinalResult', routes.serveFile(config.RESULT_FILE));
// app.get('/SignedPartialResult', routes.serveFile(config.PARTIAL_RESULT_FILE));
if (manifest.publishListOfVoters)
    app.get('/VotersList', routes.serveFile(config.VOTERSLIST_FILE));
app.get('/ResultsCollectingServer', routes.serveFile(config.RESULTCS_FILE));
for(i=0; i<manifest.mixServers.length; ++i){
	var resource = '/ResultsMixServer' + i;
	var file = config.RESULTMIX_FILE.replace('%d', i);
	app.get(resource, routes.serveFile(file));
}


// SET THE BACKGROUD CHECK FOR THE RESULT FILE
//setInterval( result.loadResult, 5000);
//result.loadResult();
setInterval(result.fetchAndSaveData, 5000);
result.fetchAndSaveData();
// TODO:
// (1) We could make sure that this is alive on every request (just in case)
// (2) Remove this when the result is read.

// STARGING THE SERVER
var server = app.listen(config.port, function() {
    console.log('Bulleting Board running for election "%s" [%s]', manifest.title, manifest.hash);
    console.log('Listening on %s, port %d\n', server.address().address, server.address().port);
});

