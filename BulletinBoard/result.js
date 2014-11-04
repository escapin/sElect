var fs = require('fs');
var manifest = require('./manifest');
var config = require('./config');
var wrapper = require('./wrapper');

exports.result = null;  // this is where the result is stored (when ready)

// Use the wrapper object to check the signature on msg and parse
// it to a readable text format. Store the result in exports.result
function checkAndParse(msg) {
    wrapper.finalResultAsText(msg, function (err, result) {
        if (err) {
            console.log(' ...Internal error:', error);
        } 
        else if (!result.ok) {
            console.log(' ...Result not ok (perhaps wring signature)');
        }
        else {
            exports.result = result.data.split('\n').map( function (line) {
                t = line.split(/ +/);
                var vote = manifest.choices[t[0]];
                return {vote:vote, nonce:t[1]};
            });
            console.log('Redable retuls:', exports.result);
        }
    });
}

var fileProcessed = false;

// Check whether there is the result file and, if so, parse it
function loadResult() {
    if (fileProcessed || exports.result) return; 

    console.log('Looking for the result file');
    fs.exists(config.RESULT_FILE, function(exists) {
        if(exists) {
            if (fileProcessed) return;
            fileProcessed = true;
            console.log('Result file found. Processing...');
            fs.readFile(config.RESULT_FILE, {encoding:'ascii'}, function (err, msg_res) { 
                if (err) { 
                    console.log('Error:', err); 
                    return; 
                }
                console.log('Result:', msg_res);
                console.log('Obtain readable results...');
                checkAndParse(msg_res);
            });
        }
        else {
            console.log('Result file not found');
        }
    });
}

// TODO Once a (wrong) file is processed, this routine does not check for a new file


exports.loadResult = loadResult;

