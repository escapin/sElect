var fs = require('fs');
var request = require('request');
var manifest = require('./manifest');
var config = require('../config');
// var wrapper = require('./wrapper');
var crypto = require('cryptofunc');

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

function parsePartialResult(signedFinalResult) {
    var p = crypto.deconcatenate(signedFinalResult);
    var result = p.first;
    var signature = p.second;
    
    // Verify the signature
    var sig_ok = crypto.verifsig(manifest.collectingServer.verification_key, result, signature);
    if (!sig_ok) {
        console.log('ERROR: Wrong signature');
        return;
    }
    
    // Check the tag:
    p = crypto.deconcatenate(result);
    if (p.first !== TAG_BALLOTS) {
        console.log('ERROR: Wrong tag');
        return;
    }
    var payload = p.second;

    // Check the election id
    p = crypto.deconcatenate(payload);
    if (manifest.hash.toUpperCase() !== p.first.toUpperCase()) {
        console.log('ERROR: Wrong election ID');
        return;
    }

    // Get the voters as a message
    p = crypto.deconcatenate(p.second); 
    votersMsg = p.second;
    // And collect them
    var t = [];
    console.log("Fetching the list of voters.");
    splitter(votersMsg, function (item) {
        item = (new Buffer(item, 'hex')).toString('utf8');
        t.push(item);
        console.log("\t" + item);
    });
    exports.voters = t;
}

// expected data format:
//		SIGN_lastMixServer[elID, nonce, choice]
//
function parseFinalResult(signedFinalResult) {
    var p = crypto.deconcatenate(signedFinalResult);
    var result = p.first;
    var signature = p.second;
    
    // Verify the signature
    var sig_ok = crypto.verifsig(manifest.mixServers[manifest.mixServers.length-1].verification_key, result, signature);
    if (!sig_ok) {
        console.log('ERROR: Wrong signature');
        return;
    }

    p = crypto.deconcatenate(result);
    // Check the tag:
    if (p.first !== TAG_BALLOTS) {
        console.log('ERROR: Wrong tag');
        return;
    }
    
    var elID_entriesAsAMessage = p.second;
    // Check the election id
    p = crypto.deconcatenate(elID_entriesAsAMessage);
    if (manifest.hash.toUpperCase() !== p.first.toUpperCase()) {
        console.log('ERROR: Wrong election ID');
        return;
    }

    // Get the [nonce,choice] pairs
    var t = [];
    var ccount = manifest.choices.map(function(x) {return 0;}); // initialize the counters for choices with 0's
    console.log("Fetching the voters' choices.");
    splitter(p.second, function(item) {
        p = crypto.deconcatenate(item);
        var choice = +p.second
        // add the [nonce,choice] pair to the list of votes
        t.push({nonce: p.first, vote: manifest.choices[choice]});
        console.log("\tnonce: " + p.first + "\tvote: " + manifest.choices[choice]);
        // add one vote for choice 
        ++ccount[choice];
    });

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

    // Load /parse the result from the collecting server
    if (exports.voters === null) {
        fetchData(manifest.collectingServer.URI + '/result.msg', function (err, data) {
            if (!err) {
                console.log('PARTIAL RESULT FILE FETCHED');
                parsePartialResult(data); 
            }
        });
    }

    // Load /parse the final result (from the last mix server)
    if (exports.result === null) {
        fetchData(manifest.mixServers[+manifest.mixServers.length-1].URI + '/result.msg', function (err, data) {
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
        loadFileAndContinue(config.PARTIAL_RESULT_FILE, parsePartialResult);
    }
    */
}


