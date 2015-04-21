var express = require('express');
var cors = require('cors');
var https = require('https');
var bodyParser = require('body-parser');
var fs = require('fs');

var config = require('./config');

// CREATE AND CONFIGURE THE APP
var app = express();
app.use(cors()); // enable all CORS request
app.use(bodyParser.urlencoded({extended:true}));
app.use(bodyParser.json());

// INITIALIZATION

var logFile = fs.createWriteStream(config.logFile, {flags:'a', encoding:'utf8'});

// ROUTES
//
// status:
app.get('/', function (req, res) { res.send({status:'Logger is up'}) });
// serving the log file
app.get('/fullLog.log', serveFile(config.logFile));
// logging:
app.post('/log', function (req, res) {
    var body = req.body;
    body.time = (new Date).toTimeString();
    body.ip = req.ip;
    body.userAgent = req.headers['user-agent']

    // Logging onto the console
    console.log(body);

    // Logging into the file
    logFile.write(JSON.stringify(body)+'\n');

    res.send({ ok: true }); 
});

// STARTING THE SERVER
var server = app.listen(config.port, function() {
    console.log('Logging server listening on %s, port %d', server.address().address, server.address().port);
});

// Serve a particular static file
function serveFile(path) {
    return function (req, res) {
        fs.exists(path, function(exists) {
            if (exists) {
                fs.createReadStream(path).pipe(res);
                // pull out all the content of the file in path
                // and write it to res
            } else {
                res.status(404).send('404: Not found');
            }
        });
    }
}
