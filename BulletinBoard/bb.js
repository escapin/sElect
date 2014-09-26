// LIBRARIES 
var express = require('express');
var bodyParser = require('body-parser');
var util = require('util');
var errorHandler = require('errorhandler')
var routes = require('./routes');


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

app.get('', routes.index);
app.get('/index.html', routes.index);


// STARGING THE SERVER

var server = app.listen(3000, function() {
    console.log('Listening on %s, port %d', server.address().address, server.address().port);
});

