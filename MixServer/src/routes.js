var fs = require('fs');
var request = require('request-json');
var config = require('../config');
var manifest = require('./manifest');
var server = require('./server');

var resultReady = false;
if (fs.existsSync(config.RESULT_FILE))
    resultReady = true;

function retreiveChainIndex(){
	for(var i=0; i<manifest.mixServers.length; ++i)
		if(manifest.mixServers[i].encryption_key === config.encryption_key &&
			manifest.mixServers[i].verification_key === config.verification_key)
			return i;
	return -1;
}

var chainIndex = retreiveChainIndex();
exports.chainIndex = chainIndex;

function saveResult(innerBallots) {
	fs.writeFile(config.RESULT_FILE, innerBallots, function (err) {
        if (err)
            console.log('Problems with saving result', config.RESULT_FILE);
        else {
            console.log('Result saved in', config.RESULT_FILE);
            resultReady = true;
        }
    });
}

// Processes the ballots
// Parameter 'res' is optional. If present, the summary
// (success/failure) will be sent to it.
// Function 'sendResult' is optional. If present, it will
// send the innerBallots to the next server.
//
function processBallots(data, res, sendResult) {
    server.processBallots(data, function (err, result) {
        if (err) {
            console.log(' ...INTERNAL ERROR. Cannot process the ballots: ', err);
            console.log('RESULT NOT SAVED');
            if (res) res.send({ ok: false, info: 'INTERNAL ERROR' });
        }
        else if (!result.ok) {
            console.log('ERROR:', result.data);
            console.log('RESULT NOT SAVED');
            if (res) res.send({ ok: false, info: result.data });
        }
        else {
            var innerBallots = result.data;
            // save the  inner ballots
            saveResult(innerBallots);
            if (res) res.send({ ok: true, info: 'Data accepted'});
            // send the inner ballots to the next server, if it exists
            if (sendResult) sendResult(innerBallots);
        }
    });
}

var mixserv_options = {};
if (config.ignore_fin_serv_cert)
    mixserv_options = {rejectUnauthorized: false};

// Send result to the next mix server, if it is listed in the manifest.
// Print only a message error, otherwise.
function sendResultToNextMix(innerBallots) {
	var nextIndex 	= chainIndex+1;
	if(nextIndex >= manifest.mixServers.length)
		console.log('Warning: result not sent. No other mix servers.');
	else {
		console.log('Sending result to the next mix server (#%d)', nextIndex);
		var nextMixServ = request.newClient(manifest.mixServers[nextIndex].URI, mixserv_options);
		var data = {data: innerBallots}
		// one could add something like {timeout:10000} to the request below, after 'data'
		nextMixServ.post('data', data, function(err, res, body) {
			if (err)
				console.log(' ...Error: Cannot send the result to the next mix server: ', err);
			else {
				console.log(' ...Result sent to the next mix server (#%d).', nextIndex);
				console.log(' ...Response:', body);
			}
		});
	}
}


// Get the data (in 'req'), processes it as the ballots,
// and send it to the next mix server
exports.process = function process(req, res)
{
    if (resultReady) {
        console.log('ERROR: result already exists');
        res.send({ok: false, info: 'result already exists'})
        return;
    }
    var data = req.body.data;
    console.log('Ballots coming. Processing...');
    // process the ballots and ...
    if(chainIndex+1 >= manifest.mixServers.length) // ... no other mix servers
    	processBallots(data, res);
    else // ... send the result to the next mix server
    	processBallots(data, res, sendResultToNextMix);
}

// Reads data from the given file and  processes it as the ballots
exports.processFile = function processFile(dataFileName) {
    console.log('Processing the file ', dataFileName);
    var data = fs.readFileSync(dataFileName, {encoding:'utf8'});
    processBallots(data);
}


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
