var fs = require('fs');
var config = require('../config');
var manifest = require('./manifest');
var server = require('./server');

var resultReady = false;
if (fs.existsSync(config.RESULT_FILE))
    resultReady = true;

// Processes the ballots 
// Parameter res is optional. If present, the summary
// (success/failure) will be sent to it.
//
function processBallots(data, res) {
    server.processBallots(data, function (err, result) {
        if (err) {
            console.log(' ...INTERNAL ERROR. Cannot process the ballots: ', err);
            console.log('RESULT NOT SAVED');
            if (res) res.send({ ok: false, info: 'INTERNAL ERROR' });
        }
        else if (result.ok) {
            var innerBallots = result.data;
            // Save the inner ballots:
            fs.writeFileSync(config.RESULT_FILE, innerBallots);
            console.log('Result saved in', config.RESULT_FILE);
            resultReady = true;
            if (res) res.send({ ok: true, info: 'Data accepted'});
        }
        else {
            console.log('ERROR:', result.data);
            console.log('RESULT NOT SAVED');
            if (res) res.send({ ok: false, info: result.data });
        }
    });
}

exports.process = function process(req, res) 
{
    if (resultReady) {
        console.log('ERROR: result already exists');
        res.send({ok: false, info: 'result already exists'})
        return;
    }
    var data = req.body.data;
    console.log('Ballots coming. Processing...');
    processBallots(data, res);
};

exports.statusPage = function statusPage(req, res) {
    var status = resultReady ? 'result ready' : 'waiting for data';
    res.send({electionID : manifest.hash, status : status });
}

// Serve a particular static file
exports.serveFile = function serveFile(path) {
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

// Reads data from the given file and  processes it as the ballots
exports.processFile = function processFile(dataFileName) {
    console.log('Processing the file ', dataFileName);
    var data = fs.readFileSync(dataFileName, {encoding:'utf8'});
    processBallots(data);
}
