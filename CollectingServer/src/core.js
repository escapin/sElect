//var java = require("java");
var config = require('../config');
var manifest = require('./manifest');
var HashMap = require('hashmap');
var selectUtils = require('selectUtils');
var crypto = require('cryptofunc');

var TAG_ACCEPTED = '00';  // (hex encoded) tag
var TAG_BALLOTS = '01';
var TAG_VOTERS = '10';

// shortcut
var pair = crypto.concatenate;
var unpair = crypto.deconcatenate;
var sign = crypto.sign;
var equals = selectUtils.equals;
var elID = manifest.hash;
var hexToBytes = crypto.hexToBytes;
var bytesToHex = crypto.bytesToHex;

// Cryptographic Keys:
//var colSerEncKey = manifest.collectingServer.encryption_key;
//var colSerDecKey = config.decryption_key;
//var colSerVerKey = manifest.collectingServer.verification_key;
var colSerSigKey = config.signing_key;



////Add (java) class paths, as specified in the config:
//for (var i=0; i<config.class_paths.length; ++i) {
//  var path = config.class_paths[i];
//  java.classpath.push(path);
//}

//Create the list (map) of eligible voters. Usage if (eligibleVoters[v]) ...
var eligibleVoters = {};
//var listOfEligibleVoters = [];
for( var i=0; i<manifest.voters.length; ++i ) {
    var voter_id = manifest.voters[i].email;
    eligibleVoters[voter_id] = true;
    //listOfEligibleVoters.push(voter_id);
}
module.exports.eligibleVoters = eligibleVoters;

var storedBallots = new HashMap();
var numberOfCastBallot = 0;
// 'ballot': the n-time encrypted ballot
// 'receipt' format: signatureOf[TAG_ACCEPTED, electionID, ballot]
exports.collectBallot = function collectBallot(voterID, ballot) {
	if(!eligibleVoters[voterID])
		return {ok: false; data: "Wrong voter ID"};
	
	var ballotAlreadyStored = storedBallots.get(voterID);
	if(ballotAlreadyStored === ballot)
		return {ok: false; data: "Voter already voted"};

	if(ballotAlreadyStored === undefined || ballotAlreadyStored.lenght==0){
		storedBallots.set(voterID, ballot);
		numberOfCastBallot++;
	}

	// generate the receipt
	var tag_elID_ballot = pair(TAG_ACCEPTED, pair(elID, ballot));
	var receipt = sign(colSerSigKey, tag_elID_ballot);

	return {ok: true; data: bytesToHex(receipt)};
}

// the list of sorted ballots with duplicates elimination
// return format: Sign[TAG_BALLOTS, elID, ballotsAsAMessage]
exports.getBallots = function getBallots() {
	var listOfBallots = storedBallots.values();
	var ballotsAsAMessage = hexArray2sortedBytesWithDuplicateElimination(listOfBallots);
	var tag_elID_ballots = pair(TAG_BALLOTS, pair(elID, ballotsAsAMessage));
	var signature = sign(colSerSigKey, tag_elID_ballots);
	var signedResult = pair(tag_elID_ballots, signature);
	return signedResult;
}



// the list of sorted voters
//return format: Sign[TAG_VOTERS, elID, votersAsAMessage]
exports.getVoters = function getVoters(){
	var listOfVoters = storedBallots.keys();
	var votersAsAMessage = hexArray2sortedBytesWithDuplicateElimination(listOfVoters);
	var tag_elID_voters = pair(TAG_VOTERS, pair(elID, votersAsAMessage));
	var signature = sign(colSerSigKey, tag_elID_voters);
	var signedResult = pari(tag_elID_voters, signature);
	return signedResult;
}

// require: an array of hexadecimal strings
function hexArray2sortedBytesWithDuplicateElimination(stringArray) {
	stringArray.sort(); // lexicographic order
	var result = hexToBytes('');
	var last;
	for(var i=0; i<stringArray.length; i++){
		var current = hexToBytes(stringArray[i]);
		if(last === undefined || current!==last)
			result = pair(result, current);
		last = current;
	}
	return result;
}








//var voterIdentifiers = java.newArray("java.lang.String", listOfEligibleVoters); 

// Create the instance of CollectingServerWrapper:
//var csWrapper = java.newInstanceSync("selectvoting.system.wrappers.CollectingServerWrapper", 
//                                      colSerEncKey, colSerDecKey, colSerVerKey, colSerSigKey, 
//                                      manifest.hash, voterIdentifiers);

// Export the wrapper:
//module.exports = csWrapper;


