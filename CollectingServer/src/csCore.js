var csCore = function() {
var exports = {};
//////////////////////////////////////////////////////////////////////////////////////////////

var HashMap = require('hashmap');
var crypto = require('cryptofunc');
var cryptoUtils = require('cryptoUtils');

// (hex encoded) tags
var TAG_ACCEPTED = '00'; 
var TAG_BALLOTS = '01';
var TAG_VOTERS = '10';

// SHORTCUTS
var pair = crypto.concatenate;
var unpair = crypto.deconcatenate;
var sign = crypto.sign;

// return a new csCore instance
exports.create = function(electionID, signKey)
{
	// PRIVATE FIELDS
	var storedBallots = new HashMap();
	
	// 'ballot': the n-time encrypted ballot
	// 'receipt' format: signatureOf[TAG_ACCEPTED, electionID, ballot]
	function collectBallot(voterID, ballot) {
		if(storedBallots.has(voterID))
			return {ok: false, data: "Voter already voted"};

        // store the ballot
        storedBallots.set(voterID, ballot);
		
		// generate the receipt
		var tag_elID_ballot = pair(TAG_ACCEPTED, pair(electionID, ballot));
		var signature = sign(signKey, tag_elID_ballot);
		
		return {ok: true, data: signature};
	}

	// the list of sorted ballots with duplicates elimination
	// return format: Sign[TAG_BALLOTS, electionID, ballotsAsAMessage]
	function getResult() {
		var listOfBallots = storedBallots.values();
		var ballotsAsAMessage = array2sortedMsgWithDuplicateElimination(listOfBallots);
		var tag_elID_ballots = pair(TAG_BALLOTS, pair(electionID, ballotsAsAMessage));
		var signature = sign(signKey, tag_elID_ballots);
		var signedResult = pair(tag_elID_ballots, signature);
		return signedResult;
	}

	// the list of sorted voters
	//return format: Sign[TAG_VOTERS, electionID, votersAsAMessage]
	function getVotersList(){
		var listOfVotersMsg = storedBallots.keys().map(function(v) {return cryptoUtils.stringToMessage(v);});
		var votersAsAMessage = array2sortedMsgWithDuplicateElimination(listOfVotersMsg);
		var tag_elID_voters = pair(TAG_VOTERS, pair(electionID, votersAsAMessage));
		var signature = sign(signKey, tag_elID_voters);
		var signedResult = pair(tag_elID_voters, signature);
		return signedResult;
	}

	// require: an array of hexadecimal strings
	function array2sortedMsgWithDuplicateElimination(msgArray) {
		msgArray.sort(); // lexicographic order
		var result = '';
		var last;
        // TODO: (if this is visibly slow) reimplement it in a more efficent way
		for(var i=msgArray.length-1; i>=0; --i){
		// inverse order necessary to make the implementation of crypto.concatenate work
			var current = msgArray[i];
			if(last === undefined || current!==last)
				result = pair(current, result);
			last = current;
		}
		return result;
	}
	
	
	return { electionID: electionID, 
			 signKey: signKey, 
			 collectBallot: collectBallot, 
             getResult: getResult, 
             getVotersList: getVotersList };
}

//////////////////////////////////////////////////////////////////////////////////////////////
return exports;
}();

if (typeof(module)!=='undefined' && module.exports) {
    module.exports = csCore;
}

