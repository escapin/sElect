// LIBRARIES 
var express = require('express');
var bodyParser = require('body-parser');
var util = require('util');
var errorHandler = require('errorhandler')
var fs = require('fs');
var routes = require('./routes');

// PARAMETERS
var MANIFEST_FILE = 'public/ElectionManifest.json';

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

var index = routes.index(manifest);

// CREATE THE APP

var app = express();


// CONFIGURATION

app.set('views', './views');
app.set('view engine', 'ejs');

app.use(bodyParser.urlencoded({ extended: true }));
app.use(express.static('./public')); // was: __dirname + '/public'
// app.use(express.static(__dirname + './public'));

// Error handling (not for production)
app.use(errorHandler({ dumpExceptions: true, showStack: true }));


// ROUTES

app.get('', index);
app.get('/index.html', index);


// STARGING THE SERVER

var server = app.listen(3000, function() {
    console.log('Listening on %s, port %d', server.address().address, server.address().port);
});

