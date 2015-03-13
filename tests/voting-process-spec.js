var crypto = require('cryptofunc');
var voterClient = require('voterClient');
var csCore = require('../CollectingServer/src/csCore.js');

var TAG_ACCEPTED = '00';  // (hex encoded) tag
var TAG_BALLOTS = '01';
var TAG_VOTERS = '10';

//////////////////////////////////////////////////////////////////////////////////////////
// Shortcuts

var pair = crypto.concatenate;
var unpair = crypto.deconcatenate;
var sign = crypto.sign;
var verif = crypto.verifsig;

//////////////////////////////////////////////////////////////////////////////////////////
// Parameters and keys

var electionID = '1441';
var NMixServ = 5;
var voters = ["a@ema.il", "b@ema.il", "c@ema.il", "d@ema.il"];

// Keys
var colServerKeys = crypto.sig_keygen();
var colServVerifKey = colServerKeys.verificationKey;
var mixServKeys = new Array(NMixServ);
for(var i=0; i<NMixServ; i++)
	mixServKeys[i] = crypto.pke_keygen();
var mixServEncKeys = mixServKeys.map(function(k){ return k.encryptionKey; });

// create the set of eligible voters
// TODO remove this; csCore.create should accept list of eligible voters (not a set)
var votersSet = {};
for(var i=0; i<voters.length; i++)
	votersSet[voters[i]] = true;

//////////////////////////////////////////////////////////////////////////////////////////
// Utils

function splitter(msg, callback) {
    if (msg.length !== 0)  {
        var p = crypto.deconcatenate(msg);
        callback(p.first);
        splitter(p.second, callback);
    }
}

//////////////////////////////////////////////////////////////////////////////////////////
// Test cases

describe( 'Voting process', function()
{
    it( 'works correctly', function()
    {  	
        // Create the collecting server (core)
        var cs = csCore.create(electionID, votersSet, colServerKeys.signingKey);

        // VOTING PHASE. Create voters and submit ballots:
        var receipt = new Array(voters.length);
    	for(i=0; i<voters.length;++i){
            // create a new voter object
            var voter = voterClient.create(electionID, colServVerifKey, mixServEncKeys);
            // create ballot (a receipt containing a ballot); i-th voter votes for i-th candidate:
    		receipt[i] = voter.createBallot(i);
    		expect (receipt[i].choice) .toBe(i);
            
            // submit the ballot
        	var csReply = cs.collectBallot(voters[i], receipt[i].ballot);
        	expect (csReply.ok).toBe(true);

            // check the acknowledgement (signature)
            receipt[i].signature = csReply.data;
            var receiptOK = voter.validateReceipt(receipt[i]);
            expect(receiptOK).toBe(true);
    	}
    });
});


xdescribe( 'Cs core', function ()
{
	it( 'get the Ballots', function()
	{  	
		var signedBallots = cs.getResult();
		var p = unpair(signedBallots);
		var tag_elID_ballots = p.first;
		var signature = p.second;
		expect(verif(colServVerifKey, tag_elID_ballots, signature)) .toBe(true);
		p = unpair(tag_elID_ballots);
		var tag = p.first;
		var elID_ballots = p.second;
		expect(tag) .toBe(TAG_BALLOTS);
		p = unpair(elID_ballots);
		var elID = p.first;
		var ballotsAsAMessage = p.second;
		expect(elID) .toBe(electionID);
		var result = [];
		//console.log();
		splitter(ballotsAsAMessage, function (item) {
			// FIXME: don't we require any conversion of the data we process?
			//item = (new Buffer(item, 'hex')).toString('utf8');
			result.push(item);
			//console.log(item);
		});
		var expectedBallots = [];
		for(i=0; i<receipt.length; ++i) {
			expectedBallots[i] = receipt[i].ballot;
		}	
		expect(result.length) .toBe(expectedBallots.length);
		
		expectedBallots.sort();
		//console.log();
		for(i=0; i<result.length; ++i) {
			expect(result[i]) .toBe(expectedBallots[i]);
			//console.log(expectedBallots[i]);
		}
	});
});
	
xdescribe( 'Cs core', function ()
{
	it( 'get the Voters\' list', function()
	{
		var signedVotersList = cs.getVotersList();
		var p = unpair(signedVotersList);
		var tag_elID_voters = p.first;
		var signature = p.second;
		expect(verif(colServVerifKey, tag_elID_voters, signature)) .toBe(true);
		p = unpair(tag_elID_voters);
		var tag = p.first;
		var elID_voters = p.second;
		expect(tag) .toBe(TAG_VOTERS);
		p = unpair(elID_voters);
		var elID = p.first;
		var votersAsAMessage = p.second;
		expect(elID) .toBe(electionID);
		votersList = [];
		console.log();
		splitter(votersAsAMessage, function (item) {
			//item = (new Buffer(item, 'hex')).toString('utf8');
			votersList.push(item);
			//console.log(item);
		});
		expect(votersList.length) .toBe(voters.length);
		
		voters.sort();
		//console.log();
		for(i=0; i<votersList.length; ++i) {
			expect(votersList[i]) .toBe(voters[i]);
			//console.log(voters[i]);
		}
		
	});
});

