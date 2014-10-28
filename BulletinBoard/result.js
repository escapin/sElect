var fs = require('fs');
var manifest = require('./manifest');
var config = require('./config');
var wrapper = require('./wrapper');

exports.result = null;  // this is where the result is stored (when ready)

var inLoadResult = false;

function loadResult() {
    if (inLoadResult || exports.result) return; 
    inLoadResult = true;

    console.log('Looking for the result file');
    fs.exists(config.RESULT_FILE, function(exists) {
        if(exists) {
            console.log('Result file found. Processing...');
            fs.readFile(config.RESULT_FILE, {encoding:'ascii'}, function (err, msg_res) { 
                if (err) { 
                    console.log('Error:', err); 
                    inLoadResult = false; return; 
                }
                console.log('Result:', msg_res);
                console.log('Obtain readable results...');
                wrapper.finalResultAsText(msg_res, function (err, result) {
                    if (err) {
                        console.log(' ...Internal error:', error);
                    } else if (!result.ok) {
                        console.log(' ...Result not ok (perhaps wring signature)');
                    }
                    else {
                        exports.result = result.data.split('\n').map( function (line) {
                            t = line.split(/ +/);
                            var vote = manifest.choicesList[t[0]];
                            return {vote:vote, nonce:t[1]};
                        })
                        console.log('Redable retuls:', exports.result);
                    }
                    inLoadResult = false;
                });
            });
        }
        else {
            console.log('Result file not found');
            inLoadResult = false;
        }
    });
}

exports.loadResult = loadResult;

