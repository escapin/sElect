var fs = require('fs');
var path = require('path');
var crypto = require('cryptofunc');
var voterClient = require('voterClient');
var csCore = require('../CollectingServer/src/csCore.js');
var mixCore = require('../MixServer/src/mixCore.js');

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

var mixServPkeKeys = new Array(NMixServ);
var mixServSigKeys = new Array(NMixServ);
for(var i=0; i<NMixServ; i++) {
	mixServPkeKeys[i] = crypto.pke_keygen();
	mixServSigKeys[i] = crypto.sig_keygen();
}
var mixServEncKeys = mixServPkeKeys.map(function(k){ return k.encryptionKey; });

console.log('************ Initialisation done');

//////////////////////////////////////////////////////////////////////////////////////////
// Test cases

describe( 'Voting process', function()
{

	var cs = csCore.create(electionID, voters, colServerKeys.signingKey);
    var receipts = new Array(voters.length);
    
    //console.log('Test: Current directory --> ' + process.cwd());
    
    ///////////////////////////////////////////////////////////////
    // FIXME: just a quick fix for when we execute the test from the root dir --> to be optimize
    var class_path = ["bin", "lib/bcprov-jdk16-1.46.jar"]; // for java in MixServer
    for(var i=0;i<class_path.length; ++i)
    		if(!fs.existsSync(class_path[i]))
    			class_path[i] = path.join("../", class_path[i]);
    // console.log(class_path);
    /////////////////////////////////////////////////////////////
    
    var mixServer = new Array(NMixServ);
    var precServVerifKey = colServVerifKey;
    for(var i=0; i<mixServer.length; ++i) {
    	mixServer[i] = mixCore.create(	mixServPkeKeys[i].encryptionKey,
    									mixServPkeKeys[i].decryptionKey,
    									mixServSigKeys[i].verificationKey,
    									mixServSigKeys[i].signingKey,
    									precServVerifKey, electionID,
    									voters.length, class_path);
    	// console.log(mixServer[i].verifKey);
    	precServVerifKey = mixServer[i].verifKey;
    }

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
    
    it( 'The mix servers process the data correctly', function()
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
    	
    	// let's test the whole chain now
    	var inputData = signedBallots;
    	for(var i=0; i<mixServer.length; ++i){
    		result = mixServer[i].processBallots(inputData);
    		expect(result.ok) .toBe(true);
    		inputData = result.data;
    	}
    	var finalResult = result.data;
    	
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
});

function bijection(arr1, arr2){
	if(!Array.isArray(arr1) || !Array.isArray(arr2) || arr1.length!=arr2.length)
		return false;
	arr1.sort();
	arr2.sort();
	for(var i=0; i<arr1.sort(); ++i)
		if(arr1[i] === arr2[i])
			return false;
	return true;
}