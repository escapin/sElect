var express = require('express');
var bodyParser = require('body-parser');
var morgan = require('morgan'); // logging

var config = require('./config');
var routes = require('./routes');

// Check the manifest 
if (!config.manifest) { // there is no manifest
    console.log('ERROR: Cannot find an election manifest file.');
    console.log('Server not started.');
    process.exit(1);
}

// CREATE AND CONFIGURE THE APP
var app = express();
app.use(bodyParser.json()); 
app.use( morgan(':remote-addr [:date] :method :url :status / :referrer ', {}) ); // logging (onto console)

// ROUTES
app.post('/otp', routes.otp);
app.post('/cast', routes.cast);

// STARGING THE SERVER
var server = app.listen(config.port, function() {
    console.log('sElect Collecting Server running for election "%s" [%s]', config.manifest.title, config.manifest.electionID);
    console.log('Listening on %s, port %d', server.address().address, server.address().port);
});

