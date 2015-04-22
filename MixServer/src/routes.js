var fs = require('fs');
var request = require('request-json');
var path = require('path');
var config = require('./config');
var manifest = require('./manifest');
var mixCore = require('./mixCore');


var CURRENT_DIR = process.cwd();

///////////////////////////////////////////////////////////////////////////////////////
//State
 
var resultReady = false;
if (fs.existsSync(config.OUTPUT_FILE))
    resultReady = true;

var chainIndex = retreiveChainIndex();
exports.chainIndex = chainIndex;

var encKey      = manifest.mixServers[chainIndex].encryption_key;
var decKey      = config.decryption_key;
var verifKey    = manifest.mixServers[chainIndex].verification_key;
var signKey     = config.signing_key;
var precServVerifKey = (chainIndex === 0)? 
			manifest.collectingServer.verification_key :
			manifest.mixServers[chainIndex-1].verification_key;
var numberOfVoters = manifest.voters.length;
var mix = mixCore.create(encKey, decKey, verifKey, signKey, 
		precServVerifKey, manifest.hash, config.class_paths);

//////////////////////////////////////////////////////////////////////////////////////

function retreiveChainIndex(){
	for(var i=0; i<manifest.mixServers.length; ++i)
		if(manifest.mixServers[i].encryption_key === config.encryption_key &&
			manifest.mixServers[i].verification_key === config.verification_key)
			return i;
	return -1;
}

// Processes the ballots
// Parameter 'res' is optional. If present, the summary
// (success/failure) will be sent to it.
// Function 'sendResult' is optional. If present, it will
// send the innerBallots to the next server.
//
function processBallots(inputFile_path, outputFile_path, res, sendResult) {
    mix.processBallots(inputFile_path, outputFile_path, 
    		function(err, stdout, stderr){
				console.log(stdout);
				console.log(stderr);
				if(err){
					console.log(' ...INTERNAL ERROR. Cannot process the ballots: ', err);
		            console.log('RESULT NOT SAVED');
		            if (res) res.send({ ok: false, info: 'INTERNAL ERROR' });
				}
	    	},
	    	function (code) {
	    		console.log("[MixServer.routes] MixServer.java exited with code " + code);
	    		if(code!==0){
	    			var info, out;
	    			switch(code){
	    				case 10:
	    					out='***MixServerWrapper*** \t Wrong Number of Arguments';
	    					break;
	    				case 11:
	    					out='***MixServerWrapper*** \t [IOException] reading the file';
	    					break;
	    				case 12:
	    					out='***MixServerWrapper*** \t [IOException] writing the file';
	    					break;
	    				case 1:
	    					info='MalformedData: Wrong signature';
	    					break;
	    				case 2:
	    					info='MalformedData: Wrong tag';
	    					break;
	    				case 3:
	    					info='MalformedData: Wrong election ID';
	    					break;
	    				case -1:
	    					info='ServerMisbehavior: Too many entries';
	    					break;
	    				case -2:
	    					info='ServerMisbehavior: Ballots not sorted';
	    					break;
	    				case -3:
	    					info='ServerMisbehavior: Duplicate ballots';
	    					break;
	    				default:
	    					info='Unknown Error';
	    			}
	    			if(info) {
	    				console.log(info);
	    				if (res) res.send({ ok: false, info: info });
	    			}
	    			else if(out){
	    				console.log(out);
	    				if (res) res.send({ ok: false, info: 'INTERNAL ERROR' });
	    			}	
	    		} else { // code === 0 --> everything went fine
                    console.log('RESULTREADY IS SET TO TRUE')
                    resultReady = true;
	    			if (res) res.send({ ok: true, info: 'Data accepted'});
	    			if(sendResult){
	    				// retrieve the results from the file
	    				var innerBallots = dataFromFile(config.OUTPUT_FILE);
	    				sendResult(innerBallots);
	    			}
	    		}	
	    	});
    
//    function (err, result) {
//        if (err) {
//            
//        }
//        else if (!result.ok) {
//            console.log('ERROR:', result.data);
//            console.log('RESULT NOT SAVED');
//            if (res) res.send({ ok: false, info: result.data });
//        }
//        else {
//            var innerBallots = result.data;
//            // save the  inner ballots
//            dataToFile(innerBallots, config.OUTPUT_FILE, function (err) {
//            	if (err)
//            		console.log('Problems with saving result in ', config.OUTPUT_FILE);
//            	else {
//            		console.log('Result saved in', config.OUTPUT_FILE);
//            		resultReady = true;
//            	}
//            });
//            if (res) res.send({ ok: true, info: 'Data accepted'});
//            // send the inner ballots to the next server, if it exists
//            if (sendResultToNextMix) sendResult(innerBallots);
//        }
//    });
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


//function saveResult(innerBallots) {
//	fs.writeFile(config.OUTPUT_FILE, innerBallots, function (err) {
//        if (err)
//            console.log('Problems with saving result', config.OUTPUT_FILE);
//        else {
//            console.log('Result saved in', config.OUTPUT_FILE);
//            resultReady = true;
//        }
//    });
//}


/* no callback: synchronous version */
function dataFromFile(path, callback){
	if(callback)
		fs.readFile(path, {encoding:'utf8'}, callback);
	/* function (err, data) {
	 * 		if (err) throw err;
	 *		  	console.log(data);
	 *	});
	 */
	else
		return fs.readFileSync(path, {encoding:'utf8'});
}

/* no callback: synchronous version */
function dataToFile(data, path, callback){
	if (callback)
		fs.writeFile(path, data, {encoding:'utf8'}, callback);
	/* 	function (err) {
     *   if (err)
     *       console.log('Problems with saving data in ', path);
     *   else
     *       console.log('Data written in ', path);
     *	});
     */
	else
		fs.writeFileSync(path, data, {encoding:'utf8'});
	
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
    // save the ballots in the input file
    dataToFile(data, config.INPUT_FILE);
    // process the ballots and ...
    if(chainIndex+1 >= manifest.mixServers.length) // ... no other mix servers
    	processBallots(config.INPUT_FILE, config.OUTPUT_FILE, res);
    else // ... send the result to the next mix server
    	processBallots(config.INPUT_FILE, config.OUTPUT_FILE, res, sendResultToNextMix);
}

// Reads data from the given file and  processes it as the ballots
exports.processFile = function processFile(dataFileName) {
    console.log('Processing the file ', dataFileName);
    //var data = dataFromFile(dataFileName);
    processBallots(dataFileName, config.OUTPUT_FILE);
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
