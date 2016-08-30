var fs = require('fs');
var request = require('request');
var manifest = require('./manifest');
var config = require('../config');
// var wrapper = require('./wrapper');
var crypto = require('cryptofunc');
var cryptoUtils = require('cryptoUtils');
var strHexConversion = require('strHexConversion');

var TAG_VOTERS = '10';
var TAG_BALLOTS = '01';
var TAG_VERIFCODEAUTO = '02';
var TAG_VERIFCODEUSER = '03';

var NMixServers = manifest.mixServers.length;

exports.finalResult = null;	// this is where the result is stored (when ready)
exports.voters = null;		// this is where the list of voters is stored
exports.summary = null;		// the summary of the election


function splitter(msg, callback) {
    if (msg.length !== 0)  {
        var p = crypto.deconcatenate(msg);
        callback(p.first);
        splitter(p.second, callback);
    }
}

// expected data format:
// 		SIGN_collectinServer[electionID, receiptID, votersList]
//
exports.parseVotersList = function(signedVotersList) {
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
	console.log("*** Parsing the list of voters.");
    for (var i=0; !data.empty(); ++i) {
    	var voterID = cryptoUtils.messageToString(data.nextMessage());
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
//		SIGN_lastMixServer[electionID, receiptID, ballotsAsAMsg]
//
exports.parseFinalResult = function(signedFinalResult) {
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
    console.log("*** Parsing the voters' choices.");
    for (var i=0; !data.empty(); ++i) {
    	p = crypto.deconcatenate(data.nextMessage());
    	// Check the election id
    	if(p.first.toUpperCase() !== manifest.hash.toUpperCase()) {
        	console.log('ERROR: Wrong election ID');
            return;
        }
    	var choice_verifCode = p.second;
    	p = crypto.deconcatenate(choice_verifCode);
    	var choice = crypto.hexStringToInt(p.first);
    	var verificationCode = p.second;
    	
    	// Verification code
    	p = crypto.deconcatenate(verificationCode);
    	var receiptID;
    	var userCode='';
    	if(p.first === TAG_VERIFCODEAUTO)
    		receiptID = p.second;
    	else if (p.first === TAG_VERIFCODEUSER){
    		p = crypto.deconcatenate(p.second)
    		receiptID = p.first;
    		userCode = strHexConversion.hexDecode(p.second);
    	}
    	else 
    		console.log('ERROR: Wrong verification code format');
    	
    	t.push({userCode: userCode, receiptID: receiptID, vote: manifest.choices[choice]});
    	if(userCode !== '')
    		console.log(userCode + "\t" + receiptID + "\t" + choice);
    	else
    		console.log(receiptID + "\t" + choice);
    	
    	// add one vote for choice 
        ++ccount[choice];
    }
    
    // format the summary (number votes for different candidates) 
    var summary = [];
    for (var i=0; i<ccount.length; ++i) {
        summary.push({choice : manifest.choices[i],  votes : ccount[i] });
    }

    exports.summary = summary;
    exports.finalResult = t;
}


//var fileProcessed = false;

function fetchData(url, cont) {
    request(url, function (err, response, body) {
        if (!err && response.statusCode == 200) {
            cont(null, body);
        }
        else {
        	var info = 'Cannot fetch the page: ' + url + '\t\(' + err + ')';
            cont(info);
        }
    });

}
//Save data in a file
function saveData(data, file) {
    fs.writeFile(file, data, function (err) {
        if (err) 
            console.log('Problems with saving the data:\n', data);
        else {
            console.log('Result saved in: ', file);
            resultReady = true;
        }
    });
}

exports.fetchAndSaveData = function() {
	// fetch the results of the mix servers
	for(var i=NMixServers-1; i>=0; --i) {
		var mixServer_path = config.RESULTMIX_FILE.replace('%d', i);
		// console.log(mixServer_path);
		fs.exists(mixServer_path, function(j){
		      return function(exists){
		    	  if(!exists){
		    		  // fetch the results from the i-th mix server
		    		  fetchData(manifest.mixServers[j].URI + '/result.msg', function(k){
		    			  return function (err, data) {
		    				  if (!err) {
		    					  console.log('** I) \t Results of the #%sth mix server fetched', k);
		    					  // recreate the path where to save the result because the for loop could be
		    					  // in a different iteration than the callback
		    					  var mixServer_path = config.RESULTMIX_FILE.replace('%d', k);
		    					  saveData(data, mixServer_path);
		    					  if(k===NMixServers-1 && exports.finalResult === null) // shows the final results
		    						  exports.parseFinalResult(data);
		    				  }
		    				  else {
		    					//console.log("** I) \t%s  (MixServer #%s)", err, k);
		    				  }
		    			  }
		    		  }(j));
		    	  }
		      }
		}(i));
	}

	// fetch the results of the collecting server
	fs.exists(config.RESULTCS_FILE, function (exists) {
		if(!exists){
			// fetch the results form the Collecting Server
			fetchData(manifest.collectingServer.URI + '/result.msg', function (err, data) {
				if (!err) {
					console.log('** II)\tResults of the Collecting Server fetched');
					saveData(data, config.RESULTCS_FILE);
				}
				else {
					console.log("** II)\t%s  (Collecting Server: result)", err);
				}
			});
		}
	});
	
	if(manifest.publishListOfVoters){
		// fetch the votersList
		fs.exists(config.VOTERSLIST_FILE, function (exists) {
			if(!exists){
				// fetch the votersList from the Collecting Server
				fetchData(manifest.collectingServer.URI + '/votersList.msg', function (err, data) {
					if (!err) {
						console.log('** III)\t Voters list fetched');
						saveData(data, config.VOTERSLIST_FILE);
						if (exports.voters === null)
							exports.parseVotersList(data);
					}
					else {
						console.log("** III)\t%s  (Collecting Server: list of voters)", err);
					}
				});
			}
		});
	}
}


/////////////////////////////////////////////
//Check whether there is the file and, if so, parse it
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

exports.loadResult = function () {
	
	// Load /parse the voters list from the file
    if (exports.voters === null) {
        loadFileAndContinue("" + config.FILE_FOLDER + config.VOTERSLIST + config.EXT, 
        		parseVotersList);
    }
	
    // load / parse the final result from the file
    if (exports.finalResult === null) {
        loadFileAndContinue("" + config.FILE_FOLDER + config.RESULTMIX + (NMixServers-1) + config.EXT, 
        		parseFinalResult);
    }

//    // Load /parse the voters list from the collecting server
//    if (exports.voters === null) {
//        fetchData(manifest.collectingServer.URI + '/votersList.msg', function (err, data) {
//            if (!err) {
//                console.log('VOTERS LIST FILE FETCHED');
//                parseVotersList(data); 
//            }
//        });
//    }
//
//    // Load /parse the final result (from the last mix server)
//    if (exports.finalResult === null) {
//        fetchData(manifest.mixServers[manifest.mixServers.length-1].URI + '/result.msg', function (err, data) {
//            if (!err) {
//                console.log('FINAL RESULT FILE FETCHED');
//                parseFinalResult(data); 
//            }
//        });
//    }

}


