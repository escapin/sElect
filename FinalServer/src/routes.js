var fs = require('fs');
var config = require('../config');
var manifest = require('./manifest');
var server = require('./server');

var resultReady = false;
if (fs.existsSync(config.RESULT_FILE))
    resultReady = true;

// Processes data (partial result)
// Parameter res is optional. If present, the summary
// (success/failure) will be sent to it.
//
function processData(data, res) {
    server.processTally(data, function (err, result) {
        if (err) {
            console.log(' ...INTERNAL ERROR. Cannot process the tally: ', err);
            console.log('RESULT NOT SAVED');
            if (res) res.send({ ok: false, info: 'INTERNAL ERROR' });
        }
        else if (result.ok) { 
            var finalRes = result.data;
            // Save the result:
            fs.writeFileSync(config.RESULT_FILE, finalRes);
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
    console.log('Partial result coming. Processing...');
    processData(data, res);
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
            } else {
                res.status(404).send('404: Not found');
            }
        });
    }
}

// Reads data from the given file and  processes it as the partial result
exports.processFile = function processFile(dataFileName) {
    console.log('Processing the file ', dataFileName);
    var data = fs.readFileSync(dataFileName, {encoding:'utf8'});
    processData(data);
}
