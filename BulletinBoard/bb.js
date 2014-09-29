// PARAMETERS

var PORT = 3111;
var MANIFEST_FILE = process.env.HOME + '/.eVotingSystem/Public/Manifest/ElectionManifest.json';
var RESULT_DIR = process.env.HOME + '/.eVotingSystem/Public/Results/';


// LIBRARIES 

var express = require('express');
var bodyParser = require('body-parser');
var util = require('util');
var errorHandler = require('errorhandler')
var fs = require('fs');
var morgan = require('morgan')

var routes = require('./routes');


// INITIALIZATION (read the manifest file)

var manifset = null;
if (fs.existsSync(MANIFEST_FILE)) {
    manifest = JSON.parse(fs.readFileSync(MANIFEST_FILE));
}
else { // Initialization failed
    console.log('Cannot find an election manifest file.');
    console.log('Server not started.');
    process.exit(1);
}

var index = routes.index(manifest, RESULT_DIR);


// CREATE AND CONFIGURE THE APP

var app = express();

// view engine and location of the views
app.set('views', './views');
app.set('view engine', 'ejs');

// body parser (important for POST request)
app.use(bodyParser.urlencoded({ extended: true }));

// static content
app.use(express.static('./public')); // was: __dirname + '/public'
app.use(express.static(RESULT_DIR)); 

// error handling (not for production)
app.use(errorHandler({ dumpExceptions: true, showStack: true }));

// logging (onto console)
app.use( morgan(':remote-addr [:date] :method :url :status / :referrer ', {}) );


// ROUTES

app.get('', index);
app.get('/index.html', index);


// STARGING THE SERVER

var server = app.listen(PORT, function() {
    console.log('Bulleting Board running for election "%s" [%s]', manifest.title, manifest.electionID);
    console.log('Listening on %s, port %d', server.address().address, server.address().port);
});

