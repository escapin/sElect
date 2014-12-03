var fs = require('fs');
var request = require('request');
var manifest = require('./manifest');
var config = require('./config');
// var wrapper = require('./wrapper');
var crypto = require('cryptofunc');

exports.result = null;  // this is where the result is stored (when ready)
exports.voters = null;  // this is where the list of voters is stored

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
    if (p.first !== '01') {
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

    // Get the voters as a mesage
    p = crypto.deconcatenate(p.second); 
    votersMsg = p.second;
    // And collect them
    var t = [];
    splitter(votersMsg, function (item) {
        item = (new Buffer(item, 'hex')).toString('utf8');
        t.push(item);
    });
    
    exports.voters = t;
}

function parseFinalResult(signedFinalResult) {
    var p = crypto.deconcatenate(signedFinalResult);
    var result = p.first;
    var signature = p.second;
    
    // Verify the signature
    var sig_ok = crypto.verifsig(manifest.finalServer.verification_key, result, signature);
    if (!sig_ok) {
        console.log('ERROR: Wrong signature');
        return;
    }

    // Check the election id
    p = crypto.deconcatenate(result);
    if (manifest.hash.toUpperCase() !== p.first.toUpperCase()) {
        console.log('ERROR: Wrong election ID');
        return;
    }

    // Get the choice/nonce pairs
    var t = [];
    var ccount = manifest.choices.map(function(x) {return 0;}); // initialize the counters for choices with 0's
    splitter(p.second, function(item) { 
        p = crypto.deconcatenate(item);
        var choice = +p.second
        // add the choice/nonce pair to the list of votes
        t.push({nonce: p.first, vote: manifest.choices[choice]});
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

    // Load /parse the partial result
    if (exports.voters === null) {
        console.log('...TRY TO GET THE PARTIAL RESULT...');
        fetchData(manifest.collectingServer.URI + '/result.msg', function (err, data) {
            if (err) console.log('ERROR when fetchin the partial result:', err);
            else     parsePartialResult(data); 
        });
    }

    // Load /parse the final result
    if (exports.result === null) {
        console.log('...TRY TO GET THE FINAL RESULT...');
        fetchData(manifest.finalServer.URI + '/result.msg', function (err, data) {
            if (err) console.log('ERROR when fetchin the final result:', err);
            else     parseFinalResult(data); 
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


