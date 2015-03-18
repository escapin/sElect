var fs = require('fs');
var request = require('request');
var manifest = require('./manifest');
var config = require('../config');
// var wrapper = require('./wrapper');
var crypto = require('cryptofunc');

var TAG_VOTERS = '10';
var TAG_BALLOTS = '01';

exports.result = null;  // this is where the result is stored (when ready)
exports.voters = null;  // this is where the list of voters is stored
exports.summary = null; // the summary of the election


function splitter(msg, callback) {
    if (msg.length !== 0)  {
        var p = crypto.deconcatenate(msg);
        callback(p.first);
        splitter(p.second, callback);
    }
}

function parseVotersList(signedVotersList) {
	var p = crypto.deconcatenate(signedVotersList);
	var data = p.first; // tag,elID,voters
	var signature = p.second;
    
    // Verify the signature
    var sig_ok = crypto.verifsig(manifest.collectingServer.verification_key, data, signature);
    if (!sig_ok) {
        console.log('ERROR: Wrong signature');
        return;
    }
    
    var data = crypto.splitter(data);
    
    // Check the tag:
    if (data.nextMessage() !== TAG_VOTERS) {
        console.log('ERROR: Wrong tag');
        return;
    }
    if (data.nextMessage().toUpperCase() !== manifest.hash.toUpperCase()) {
        console.log('ERROR: Wrong election ID');
        return;
    }

    // The rest of data is a list of voterIDs.
    var t = [];
	console.log("Fetching the list of voters.");
    for (var i=0; !data.empty(); ++i) {
    	var voterID = new Buffer(data.nextMessage(), 'hex').toString('utf8');
    	//var voterID = data.nextMessage();
    	t.push(voterID);
    	console.log("\t" + voterID);	
    }
    exports.voters = t;
    
//    // Get the voters as a message
//    p = crypto.deconcatenate(p.second); 
//    votersMsg = p.second;
//    // And collect them
//    

//    splitter(votersMsg, function (item) {
//        item = (new Buffer(item, 'hex')).toString('utf8');
//        t.push(item);

//    });
//    exports.voters = t;
}

// expected data format:
//		SIGN_lastMixServer[elID, nonce, choice]
//
function parseFinalResult(signedFinalResult) {
    var p = crypto.deconcatenate(signedFinalResult);
    var tag_elID_ballots = p.first; 	// [tag, elID, ballotsAsAMessage]
	var signature = p.second;
    
    // Verify the signature
    var sig_ok = crypto.verifsig(manifest.mixServers[manifest.mixServers.length-1].verification_key, tag_elID_ballots, signature);
    if (!sig_ok) {
        console.log('ERROR: Wrong signature');
        return;
    }
    
    var data = crypto.splitter(tag_elID_ballots);
    
    // Check the tag:
    if (data.nextMessage() !== TAG_BALLOTS) {
        console.log('ERROR: Wrong tag');
        return;
    }
    
    // Check the election id
    if(data.nextMessage().toUpperCase() !== manifest.hash.toUpperCase()) {
    	console.log('ERROR: Wrong election ID');
        return;
    }
    
    var t = [];
    var ccount = manifest.choices.map(function(x) {return 0;}); // initialize the counters for choices with 0's
    console.log("Fetching the voters' choices.");
    for (var i=0; !data.empty(); ++i) {
    	p = crypto.deconcatenate(data.nextMessage());
    	// Check the election id
    	if(p.first.toUpperCase() !== manifest.hash.toUpperCase()) {
        	console.log('ERROR: Wrong election ID');
            return;
        }
    	var receipt_nonce = p.second;
    	p = crypto.deconcatenate(receipt_nonce);
    	var receiptID = p.first;
    	var choice = crypto.hexStringToInt(p.second);
    	t.push({receiptID: receiptID, vote: manifest.choices[choice]});
    	console.log("\t" + receiptID + "\t" + choice);
    	// add one vote for choice 
        ++ccount[choice];
    }
    
    // format the summary (number votes for different candidates) 
    var summary = [];
    for (var i=0; i<ccount.length; ++i) {
        summary.push({choice : manifest.choices[i],  votes : ccount[i] });
    }

    exports.summary = summary;
    exports.result = t;
}

var fileProcessed = false;

// Check whether there is the final result file and, if so, parse it
function loadFileAndContinue(filename, cont) {
    console.log('Looking for file',  filename);
    fs.exists(filename, function(exists) {
        if(exists) {
            console.log('Processing', filename);
            fs.readFile(filename, {encoding:'ascii'}, function (err, data) { 
                if (err) { 
                    console.log('Error:', err); 
                    return; 
                }
                cont(data);
            });
        }
        else {
            console.log(filename, 'not found');
        }
    });
}


function fetchData(url, cont) {
    request(url, function (err, response, body) {
        if (!err && response.statusCode == 200) {
            cont(null, body);
        }
        else {
            cont('Cannot fetch the page');
        }
    });

}

exports.loadResult = function () {

    // Load /parse the voters list from the collecting server
    if (exports.voters === null) {
        fetchData(manifest.collectingServer.URI + '/votersList.msg', function (err, data) {
            if (!err) {
                console.log('VOTERS LIST FILE FETCHED');
                parseVotersList(data); 
            }
        });
    }

    // Load /parse the final result (from the last mix server)
    if (exports.result === null) {
        fetchData(manifest.mixServers[manifest.mixServers.length-1].URI + '/result.msg', function (err, data) {
            if (!err) {
                console.log('FINAL RESULT FILE FETCHED');
                parseFinalResult(data); 
            }
        });
    }

    /*
    // load / parse the final result
    if (exports.result === null) {
        loadFileAndContinue(config.RESULT_FILE, parseFinalResult);
    }

    // load /parse the partial result
    if (exports.voters === null) {
        loadFileAndContinue(config.PARTIAL_RESULT_FILE, parseVotersList);
    }
    */
}


