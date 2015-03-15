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

console.log('************ Initialisation');

var electionID = 'eeee';
var NMixServ = 5;
var NVoters = 1000;
var voters = new Array(NVoters);
for (var i=0; i<NVoters; ++i) {
    voters[i] = 'aa' + crypto.int32ToHexString(i);
}

// Keys
var colServerKeys = crypto.sig_keygen();
var colServVerifKey = colServerKeys.verificationKey;
var mixServKeys = new Array(NMixServ);
for(var i=0; i<NMixServ; i++)
	mixServKeys[i] = crypto.pke_keygen();
var mixServEncKeys = mixServKeys.map(function(k){ return k.encryptionKey; });



console.log('************ Initialisation done');

//////////////////////////////////////////////////////////////////////////////////////////
// Test cases

describe( 'Voting process', function()
{
    var cs = csCore.create(electionID, voters, colServerKeys.signingKey);
    var receipts = new Array(voters.length);


    it ( 'eligibleVoters field works as expected', function()
    {
    	//console.log(cs.eligibleVoters);
    	var listElVoters = Object.keys(cs.eligibleVoters);
    	listElVoters.sort();
    	voters.sort();
    	for(var i=0; i<listElVoters.length; ++i){
    		expect(listElVoters[i]) .toBe(voters[i]);
    	}
    });
    
    
    it( 'Voting phase works as expected', function()
    {  	
        console.log('************ Testing the voting phase');

    	for(i=voters.length-1; i>=0; --i){
            // create a new voter object
            var voter = voterClient.create(electionID, colServVerifKey, mixServEncKeys);
            // create ballot (a receipt containing a ballot); i-th voter votes for i-th candidate:
    		receipts[i] = voter.createBallot(i);
    		expect (receipts[i].choice) .toBe(i);
            
            // submit the ballot
        	var csReply = cs.collectBallot(voters[i], receipts[i].ballot);
        	expect (csReply.ok).toBe(true);

            // check the acknowledgement (signature)
            receipts[i].signature = csReply.data;
            var receiptOK = voter.validateReceipt(receipts[i]);
            expect(receiptOK).toBe(true);
    	}
    });

    it( 'The collecting server produces correct list of voters', function()
    {
        console.log('************ Checking the list of voters');

		var signedVotersList = cs.getVotersList();
		var p = unpair(signedVotersList);
		var data = p.first; // tag,elID,voters
		var signature = p.second;
        // check the signature
		expect(verif(colServVerifKey, data, signature)) .toBe(true);
       
        var data = crypto.splitter(data);

		expect(data.nextMessage()) .toBe(TAG_VOTERS); // check the tag
		expect(data.nextMessage()) .toBe(electionID); // check the election id
        // The rest of data is a list of voter id-s.
        // It should correspond to the initial list 'voters' (which was sorted)
        for (var i=0; !data.empty(); ++i) {
            expect(data.nextMessage()).toBe(voters[i]);
        }
    });

    it( 'The collecting server produces correct list of ballots', function()
    {  	
        console.log('************ Checking the list of ballots');

		var signedBallots = cs.getResult();
		var p = unpair(signedBallots);
		var data = p.first; // tag,elID,voters
		var signature = p.second;
        // check the signature
		expect(verif(colServVerifKey, data, signature)) .toBe(true);
       
        var data = crypto.splitter(data);

		expect(data.nextMessage()) .toBe(TAG_BALLOTS); // expected the tag
		expect(data.nextMessage()) .toBe(electionID);  // expected the election id

        // The rest of data is a list of ballots 
        // Lets take this list
        var ballotsInResult = [];
        for (var i=0; !data.empty(); ++i) {
            ballotsInResult.push(data.nextMessage());
        }
        // It should be equal to the sorted list of cast ballots
        var castBallots = receipts.map(function(rec){ return rec.ballot }).sort();
		expect(castBallots.length).toBe(ballotsInResult.length);
		for(i=0; i<castBallots.length; ++i) {
			expect(ballotsInResult[i]).toBe(castBallots[i]);
		}

    });

});
	

