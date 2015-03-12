var crypto = require('cryptofunc');
var voterClient = require('voterClient');
var csCore = require('../CollectingServer/src/csCore.js');


var TAG_ACCEPTED = '00';  // (hex encoded) tag
var TAG_BALLOTS = '01';
var TAG_VOTERS = '10';

// shortcut
var pair = crypto.concatenate;
var unpair = crypto.deconcatenate;
var sign = crypto.sign;
var verif = crypto.verifsig;

var electionID = '0001';
var num_mixSer = 5;
var colSer_sk = crypto.sig_keygen();
var mixSer_ek = [];
for(var i=0; i<num_mixSer; i++)
	mixSer_ek = crypto.rsa_keygen();

var colServVerifKey = colSer_sk.verificationKey;
var mixServEncKeys = [];
for(i=0; i<mixSer_ek.length; ++i)
	mixServEncKeys.push(mixSer_ek[i].encryptionKey);
var voter = voterClient.create(electionID, colServVerifKey, mixServEncKeys);

var voters = ["a@ema.il", "b@ema.il", "c@ema.il", "d@ema.il"];
var eligibleVoters = {};
for(var i=0; i<voters.length; i++)
	eligibleVoters[voters[i]]=true;
var colSerSigKey = colSer_sk.signingKey;
var cs = csCore.create(electionID, eligibleVoters, colSerSigKey);
var voterReply = [];

describe( 'Cs core', function()
{
    it( 'collects the ballots created by the voter clients', function()
    {  	
    	for(i=0; i<voters.length;++i){
    		// FIXME: it seems to be quite demanding
    		voterReply[i] = voter.createBallot(i);
    		expect (voterReply[i].choice) .toBe(i);
        	var csReply = cs.collectBallot(voters[i], voterReply[i].ballot);
        	expect (csReply.ok) .toBe(true);
        	// FIXME: the cs replies only with the signature
        	var receipt = {	electionID: voterReply[i].electionID, ballot: voterReply[i].ballot, 
					signature: csReply.data};
        	var receipt_correct = voter.validateReceipt(receipt);
        	expect (receipt_correct) .toBe(true);
    	}
    });
});

function splitter(msg, callback) {
    if (msg.length !== 0)  {
        var p = crypto.deconcatenate(msg);
        callback(p.first);
        splitter(p.second, callback);
    }
}

describe( 'Cs core', function ()
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
		for(i=0; i<voterReply.length; ++i) {
			expectedBallots[i] = voterReply[i].ballot;
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
	
describe( 'Cs core', function ()
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

