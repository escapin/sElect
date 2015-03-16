var java = require("java");
var crypto = require('cryptofunc');
var voterClient = require('voterClient');
var csCore = require('../CollectingServer/src/csCore.js');

//////////////////////////////////////////////////////////////////////////////////////////
// Shortcuts

var pair = crypto.concatenate;
var unpair = crypto.deconcatenate;
var sign = crypto.sign;
var verif = crypto.verifsig;

//////////////////////////////////////////////////////////////////////////////////////////
// Configuration

java.classpath.push('../bin');
java.classpath.push('../lib/bcprov-jdk16-1.46.jar');

//////////////////////////////////////////////////////////////////////////////////////////
// Parameters and keys

var TAG_VOTERS = '10';

var electionID = 'eeee';
var NMixServ = 3;
var NVoters = 2000;
var voters = new Array(NVoters);

console.log('************ Initialisation');

// voters identifiers
for (var i=0; i<NVoters; ++i) {
    voters[i] = 'aa' + crypto.int32ToHexString(i);
}

// Keys
var colServerKeys = crypto.sig_keygen();
var colServVerifKey = colServerKeys.verificationKey;
var mixServKeys = new Array(NMixServ);
var mixServSigKeys = new Array(NMixServ);
for(var i=0; i<NMixServ; i++) {
	mixServKeys[i] = crypto.pke_keygen();
    mixServSigKeys[i] = crypto.sig_keygen();
}
var mixServEncKeys = mixServKeys.map(function(k){ return k.encryptionKey; });
var mixServVerifKeys = mixServSigKeys.map(function(k){ return k.verificationKey; });

// create the set of eligible voters
// TODO remove this; csCore.create should accept list of eligible voters (not a set)
var votersSet = {};
for(var i=0; i<voters.length; i++)
	votersSet[voters[i]] = true;



// Create mix servers
var mixServers = new Array(NMixServ);
for (i=0; i<NMixServ; ++i) {
    var precServVerifKey = (i==0 ? colServVerifKey : mixServSigKeys[i-1].verificationKey );
    mixServers[i] = java.newInstanceSync("selectvoting.system.wrappers.MixServerWrapper",
                              mixServKeys[i].encryptionKey, 
                              mixServKeys[i].decryptionKey, 
                              mixServSigKeys[i].verificationKey,
                              mixServSigKeys[i].signingKey,
                              precServVerifKey,
                              electionID, NVoters);
}



//////////////////////////////////////////////////////////////////////////////////////////
// Test cases

describe( 'Voting process', function()
{
    var cs = csCore.create(electionID, votersSet, colServerKeys.signingKey);
    var receipts = new Array(voters.length);
    var voter = voterClient.create(electionID, colServVerifKey, mixServEncKeys, mixServVerifKeys);

    it( 'Ballot creation works as expected', function()
    {
        console.log('************ Ballot creation');
    	for(i=voters.length-1; i>=0; --i){
            // create ballot (a receipt containing a ballot); i-th voter votes for i-th candidate:
    		receipts[i] = voter.createBallot(i);
    		expect (receipts[i].choice) .toBe(i);
        }
    });

    it( 'Ballot casting works as expected', function()
    {
        console.log('************ Ballot casting');

        for(i=voters.length-1; i>=0; --i){
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

		expect(data.nextMessage()) .toBe(voterClient.TAG_BALLOTS); // expected the tag
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


    function mix(i, inputData, done) {
        if (i >= NMixServ)  {
            done();
            return;
        }

        console.log('************ Mixing', i);

        mixServers[i].processBallots(inputData, function (err, result) {
            if (err) {
                console.log('UNEXPECTED ERROR', err);
                done(err);
            }
            else if (!result.ok) {
                console.log('ERROR:', result.data);
                done()
            }
            else { // mixing completed successfuly; call the next mix server
                mixServers[i].result = result.data;
                mix(i+1, result.data, done);
            }
        });
    }


    it( 'Mixing works as expected', function(done)
    {  	
        var data = cs.getResult();
        mix(0, data, done);
    }, 100000);

    it( 'Voter verification works as expected', function()
    {  	
        console.log('************ Verification');

        // Voter who cast his ballot should verify that
        // everything went fine:
        var n = 1; // the index of the voter to check result
        var res = voter.checkColServerResult(cs.getResult(), receipts[n]);
        expect(res.ok).toBe(true);
        for (var i=0; i<NMixServ; ++i) {
            var res = voter.checkMixServerResult(i, mixServers[i].result, receipts[n]);
            expect(res.ok).toBe(true);
        }

        // The same, but now for wrong data:
        res = voter.checkColServerResult(mixServers[0].result, receipts[n]);
        expect(res.ok).toBe(false);
        expect(res.descr).toBe('Wrong signature');

        // Let us now take a voter whose ballot was ignored (was
        // not cast):
        var rec = voter.createBallot(i);
        var res = voter.checkColServerResult(cs.getResult(), rec);
        expect(res.ok).toBe(false);
        expect(res.descr).toBe('Ballot not found');
        for (var i=0; i<NMixServ; ++i) {
            var res = voter.checkMixServerResult(i, mixServers[i].result, rec);
            expect(res.ok).toBe(false);
            expect(res.descr).toBe('Ballot not found');
        }
    });
});
	

