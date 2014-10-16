var express = require('express');
var bodyParser = require('body-parser');
var morgan = require('morgan'); // logging

var config = require('./config');
var manifest = require('./manifest')
var routes = require('./routes');


// CREATE AND CONFIGURE THE APP
var app = express();
app.use(bodyParser.json()); 
app.use( morgan(':remote-addr [:date] :method :url :status / :referrer ', {}) ); // logging (onto console)

// ROUTES
app.post('/data', routes.process);

// STARTING THE SERVER
var server = app.listen(config.port, function() {
    console.log('Final Server running for election "%s" [%s]', manifest.title, manifest.electionID);
    console.log('Listening on %s, port %d\n', server.address().address, server.address().port);
});
