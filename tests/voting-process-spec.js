var fs = require('fs');
var path = require('path');
// var java = require("java");
var crypto = require('cryptofunc');
var voterClient = require('voterClient');
var csCore = require('../CollectingServer/src/csCore.js');
var mixCore = require('../MixServer/src/mixCore.js');

//////////////////////////////////////////////////////////////////////////////////////////
// Shortcuts

var pair = crypto.concatenate;
var unpair = crypto.deconcatenate;
var sign = crypto.sign;
var verif = crypto.verifsig;

//////////////////////////////////////////////////////////////////////////////////////////
// Configuration

// java.classpath.push('../bin');
// java.classpath.push('../lib/bcprov-jdk16-1.46.jar');

//////////////////////////////////////////////////////////////////////////////////////////
// Parameters and keys

var TAG_VOTERS = '10';
var TAG_BALLOTS = '01';

var electionID = 'eeee';
var NMixServ = 3;
var NVoters = 10;
var voters = new Array(NVoters);

console.log('************ Initialisation');

// voters identifiers
for (var i=0; i<NVoters; ++i) {
    voters[i] = 'aa' + crypto.int32ToHexString(i);
}

// Keys
var colServSigKeys = crypto.sig_keygen();
var colServVerifKey = colServSigKeys.verificationKey;
//<<<<<<< HEAD
//=======
//var mixServKeys = new Array(NMixServ);
//var mixServSigKeys = new Array(NMixServ);
//for(var i=0; i<NMixServ; i++) {
//	mixServKeys[i] = crypto.pke_keygen();
//    mixServSigKeys[i] = crypto.sig_keygen();
//}
//var mixServEncKeys = mixServKeys.map(function(k){ return k.encryptionKey; });
//
//>>>>>>> verif

var mixServPkeKeys = new Array(NMixServ);
var mixServSigKeys = new Array(NMixServ);
for(var i=0; i<NMixServ; i++) {
	mixServPkeKeys[i] = crypto.pke_keygen();
	mixServSigKeys[i] = crypto.sig_keygen();
}
var mixServEncKeys = mixServPkeKeys.map(function(k){ return k.encryptionKey; });
var mixServVerifKeys = mixServSigKeys.map(function(k){ return k.verificationKey; });



//////////////////////////////////////////////////////////////////////////////////////////
// Test cases

describe( 'Voting process', function()
{

	var cs = csCore.create(electionID, voters, colServSigKeys.signingKey);
    var receipts = new Array(voters.length);
    var voter = voterClient.create(electionID, colServVerifKey, mixServEncKeys, mixServVerifKeys);
    
    //console.log('Test: Current directory --> ' + process.cwd());
    
    ///////////////////////////////////////////////////////////////
    // FIXME: just a quick fix for when we execute the test from the root dir --> to be optimize
    var class_path = ["bin", "lib/bcprov-jdk16-1.46.jar"]; // for java in MixServer
    for(var i=0;i<class_path.length; ++i)
    		if(!fs.existsSync(class_path[i]))
    			class_path[i] = path.join("../", class_path[i]);
    // console.log(class_path);
    /////////////////////////////////////////////////////////////
    
    // Create mix servers
    var mixServer = new Array(NMixServ);
    for (i=0; i<NMixServ; ++i) {
        var precServVerifKey = (i==0 ? colServVerifKey : mixServSigKeys[i-1].verificationKey );
    	mixServer[i] = mixCore.create(	mixServPkeKeys[i].encryptionKey,
    									mixServPkeKeys[i].decryptionKey,
    									mixServSigKeys[i].verificationKey,
    									mixServSigKeys[i].signingKey,
    									precServVerifKey, electionID,
    									NVoters, class_path);
//        mixServer[i] = java.newInstanceSync("selectvoting.system.wrappers.MixServerWrapper",
//                                  mixServKeys[i].encryptionKey, 
//                                  mixServKeys[i].decryptionKey, 
//                                  mixServSigKeys[i].verificationKey,
//                                  mixServSigKeys[i].signingKey,
//                                  precServVerifKey,
//                                  electionID, NVoters);
    }
    var intermediateResult = new Array(NMixServ);
    
    it ( 'eligibleVoters field works as expected', function()
    {
    	var listElVoters = Object.keys(cs.eligibleVoters);
    	expect(bijection(listElVoters, voters)).toBe(true);
//    	listElVoters.sort();
//    	voters.sort();
//    	for(var i=0; i<listElVoters.length; ++i){
//    		expect(listElVoters[i]) .toBe(voters[i]);
//    	}
    });
    
    
//    it( 'Voting phase works as expected', function()
//    {  	
//        console.log('************ Testing the voting phase');
//
//    var cs = csCore.create(electionID, votersSet, colServSigKeys.signingKey);
//    var receipts = new Array(voters.length);
    

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
    
    it( 'The first mix server process the data correctly', function()
    {
    	console.log('************ Testing the mix servers');

    	var signedBallots = cs.getResult();

    	// result to the first mix server
    	var result=mixServer[0].processBallots(signedBallots);
    	expect(result.ok) .toBe(true);
    	
    	// let's give him trash
    	result=mixServer[0].processBallots(crypto.nonce(2000));
    	expect(result.ok) .toBe(false);
    	expect(result.data) .toBe("Wrong signature");
    	
    });
    
    //	var signedBallots = cs.getResult();
	// let's test the whole chain now
    //	var inputData = signedBallots;
    //	for(var i=0; i<mixServer.length; ++i){
    //		result = mixServer[i].processBallots(inputData);
    //		expect(result.ok) .toBe(true);
    //		inputData = result.data;
    //	}
    // var finalResult = result.data;
    
    it( 'Mixing works as expected', function(done)
    {  	
    	var data = cs.getResult();
    	mix(0, data, done);
    }, 100000); // timeout in ms
    
    function mix(i, inputData, done) {
        if (i >= NMixServ)  {
            done();
            return;
        }

        console.log('************ Mixing', i);

        var result = mixServer[i].processBallots(inputData);
        expect(result.ok) .toBe(true);
        intermediateResult[i] = result.data;
        mix(i+1, result.data, done);
//        mixServer[i].processBallots(inputData, function (err, result) {
//            if (err) {
//                console.log('UNEXPECTED ERROR', err);
//                done(err);
//            }
//            else if (!result.ok) {
//                console.log('ERROR:', result.data);
//                done()
//            }
//            else { // mixing completed successfully; call the next mix server
//            	expect(result.ok) .toBe(true);
//            	intermediateResult[i] = result.data;
//                mix(i+1, result.data, done);
//            }
//        });
    }
    	
    it( 'Final results as expected', function()
    {
    	console.log('************ Verification');
    	
    	var finalResult = intermediateResult[NMixServ-1];
    	var p = unpair(finalResult);
    	var tag_elID_ballots = p.first; 	// [tag, elID, ballotsAsAMessage]
		var signature = p.second;
        // check the signature
		expect(verif(mixServer[mixServer.length-1].verifKey, tag_elID_ballots, signature)) .toBe(true);
       
        var data = crypto.splitter(tag_elID_ballots);

		expect(data.nextMessage()) .toBe(TAG_BALLOTS); // expected the tag
		expect(data.nextMessage()) .toBe(electionID);  // expected the election id
        
        // The rest of data is a list of (electionID, receiptID,choice)
        // Lets take this list
        var receiptIDs = [];
        var choices = [];
        for (var i=0; !data.empty(); ++i) {
        	p = unpair(data.nextMessage());
        	expect(p.first).toBe(electionID);
        	var receipt_choice = p.second;
        	p = unpair(receipt_choice);
        	var receiptID = p.first;
        	var choice = crypto.hexStringToInt(p.second);
        	//console.log(receipt + "\t" + choice);
        	receiptIDs.push(receiptID);
            choices.push(choice);
        }
        originalReceiptIDs = receipts.map(function(rec){ return rec.receiptIDs });
        originalChoices = receipts.map(function(rec){ return rec.receiptID});
        
        expect(bijection(originalReceiptIDs, receiptIDs)) .toBe(true);
        expect(bijection(originalChoices, choices)) .toBe(true);
    });
    	
    it( 'Voter verification works as expected', function()
    {  	
        console.log('************ Verification');

        // Voter who cast his ballot should verify that
        // everything went fine:
        var n = 1; // the index of the voter to check result
        var res = voter.checkColServerResult(cs.getResult(), receipts[n]);
        expect(res.ok).toBe(true);
        for (var i=0; i<NMixServ; ++i) {
            var res = voter.checkMixServerResult(i, intermediateResult[i], receipts[n]);
            expect(res.ok).toBe(true);
        }

        // The same, but now for wrong data:
        res = voter.checkColServerResult(intermediateResult[0], receipts[n]);
        expect(res.ok).toBe(false);
        expect(res.descr).toBe('Wrong signature');

        // Let us now take a voter whose ballot was ignored (was
        // not cast):
        var rec = voter.createBallot(i);
        var res = voter.checkColServerResult(cs.getResult(), rec);
        expect(res.ok).toBe(false);
        expect(res.descr).toBe('Ballot not found');
        for (var i=0; i<NMixServ; ++i) {
            var res = voter.checkMixServerResult(i, intermediateResult[i], rec);
            expect(res.ok).toBe(false);
            expect(res.descr).toBe('Ballot not found');
        }
    });


});

function bijection(arr1, arr2){
	//if(!Array.isArray(arr1) || !Array.isArray(arr2) || arr1.length!=arr2.length)
	if(arr1.length!=arr2.length)
		return false;
	arr1.sort();
	arr2.sort();
	for(var i=0; i<arr1.sort(); ++i)
		if(arr1[i] === arr2[i])
			return false;
	return true;
}
