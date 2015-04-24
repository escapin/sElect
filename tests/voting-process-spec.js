var fs = require('fs');
var path = require('path');
var crypto = require('cryptofunc');
var cryptoUtils = require('cryptoUtils');
var voterClient = require('voterClient');
var csCore = require('../CollectingServer/src/csCore.js');
var mixCore = require('../MixServer/src/mixCore.js');
var mkdirp = require('mkdirp');

//////////////////////////////////////////////////////////////////////////////////////////
// Shortcuts

var pair = crypto.concatenate;
var unpair = crypto.deconcatenate;
var sign = crypto.sign;
var verif = crypto.verifsig;


//////////////////////////////////////////////////////////////////////////////////////////

// Parameters and keys

var TAG_VOTERS = '10';
var TAG_BALLOTS = '01';
//console.log('CWD:\t' + process.cwd());
var PARTIALRESULT_dir = path.join(process.cwd(), '_data_Test');
var TIMEOUT = 100000;

var electionID = 'eeee';
var NMixServ = 5;
var NVoters = 50;
var voters = new Array(NVoters);


console.log('************ Initialisation');

// voters identifiers
for (var i=NVoters-1; i>=0; --i) {
    voters[i] = 'abc'  + i + '@ema.il';
}

// Keys
var colServSigKeys = crypto.sig_keygen();
var colServVerifKey = colServSigKeys.verificationKey;

var mixServPkeKeys = new Array(NMixServ);
var mixServSigKeys = new Array(NMixServ);
for(var i=0; i<NMixServ; i++) {
	mixServPkeKeys[i] = crypto.pke_keygen();
	mixServSigKeys[i] = crypto.sig_keygen();
}
var mixServEncKeys = mixServPkeKeys.map(function(k){ return k.encryptionKey; });
var mixServVerifKeys = mixServSigKeys.map(function(k){ return k.verificationKey; });

var classpaths = ["../bin", "../lib/*"];

//////////////////////////////////////////////////////////////////////////////////////////
// Test cases

describe( 'Voting process', function()
{

	var cs = csCore.create(electionID, colServSigKeys.signingKey);
    var receipts = new Array(voters.length);
    var voter = voterClient.create(electionID, colServVerifKey, mixServEncKeys, mixServVerifKeys);
    
    //console.log('Test: Current directory --> ' + process.cwd());
    
    // create the folder where the data processed 
    // by the mix servers will be stored 
    mkdirp(PARTIALRESULT_dir, function (err) {
        if (err) 
        	console.error("Error: ", err);
//		else 
//        	console.log("Folder '" + config.DATA_FOLDER + "' created.");
    });

    
    // Create mix servers
    var mixServer = new Array(NMixServ);
    for (i=0; i<NMixServ; ++i) {
        var precServVerifKey = (i==0 ? colServVerifKey : mixServSigKeys[i-1].verificationKey );
    	mixServer[i] = mixCore.create(	mixServPkeKeys[i].encryptionKey,
    									mixServPkeKeys[i].decryptionKey,
    									mixServSigKeys[i].verificationKey,
    									mixServSigKeys[i].signingKey,
    									precServVerifKey, electionID,
    									classpaths);
    }
    var intermediateResult = new Array(NMixServ);
    
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

            // store the acknowledgement (signature) in the receipt
            receipts[i].signature = csReply.data;
    	}
    });

    it( 'Acknowledge verification works as expected', function()
    {
        console.log('************ Ballot casting');

        for(i=voters.length-1; i>=0; --i){
            // check the acknowledgement (signature in the receipt)
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
        // The rest of data is a list of voterIDs.
        var votersBack = new Array(NVoters);
        for (var i=0; !data.empty(); ++i) {
        	votersBack[i] = cryptoUtils.messageToString(data.nextMessage());
        	//console.log('\t' + votersBack[i]);
        }
        expect(bijection(votersBack, voters)).toBe(true);
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
    	console.log('************ Testing a mix server with trash');

    	// let's first give him trash
    	var signedBallots = crypto.nonce(2000); 
    	var inputFile_path = path.join(PARTIALRESULT_dir, 'partialResultCRAP_input.msg'); 
    	dataToFile(signedBallots, inputFile_path);
    	var outputFile_path = path.join(PARTIALRESULT_dir, 'partialResultCRAP_output.msg');
    	mixServer[0].processBallots(inputFile_path, outputFile_path,
    		function(err, stdout, stderr){
				expect (err===null) .toBe(false);
				expect (err.code) .toBe(1); // wrong signature
			},
			function (code) {
				expect (code) .toBe (1); // wrong signature
				//console.log("Program exited with code " + code);
			});
    	
    }, TIMEOUT);
    
    it( 'The first mix server process the data correctly', function()
    {
    	console.log('************ Testing a mix server with correct data');
    	// now seriously
    	var signedBallots = cs.getResult();
    	// write the result on a file
    	var inputFile_path = path.join(PARTIALRESULT_dir, 'partialResult00_input.msg'); 
    	dataToFile(signedBallots, inputFile_path);
    	var outputFile_path = path.join(PARTIALRESULT_dir, 'partialResult00_output.msg');
    	
    	// result to the first mix server
    	mixServer[0].processBallots(inputFile_path, outputFile_path,
    		function(err, stdout, stderr){
    			console.log(stdout);
    			console.log(stderr);
    			//console.log(outputFile_path);
    			dataFromFile(outputFile_path, function (err, data) {
    				if (err){ 
    		        	console.log('Problems with reading data from ', outputFile_path);
    		            console.log('Error: ', err); 
    		            return; 
    		        }
    		        //console.log(data);
    		    });
    		},
    		function (code) {
    			expect (code) .toBe (0);
    			//console.log("Program exited with code " + code);
    		});
    }, TIMEOUT);
    
    it( 'Mixing works as expected', function(done)
    {  	
    	console.log('************ Testing a mix servers\' chain');
    	var data = cs.getResult();
    	mix(0, data, done);
    }, TIMEOUT); // timeout in ms
    
    function mix(i, inputData, done) {
        if (i >= NMixServ)  {
            done();
            return;
        }

        console.log('************ Mixing', i);
        
        var inputFile_path = path.join(PARTIALRESULT_dir,'partialResult0' + i + '_input.msg'); 
    	dataToFile(inputData, inputFile_path);
    	var outputFile_path = path.join(PARTIALRESULT_dir, 'partialResult0' + i + '_output.msg');
    	
    	mixServer[i].processBallots(inputFile_path, outputFile_path, 
        	function(err, stdout, stderr){
				console.log(stdout);
				console.log(stderr);
			},
			function(code){
				expect(code) .toBe(0);
				intermediateResult[i] = dataFromFile(outputFile_path);
				//console.log(intermediateResult[i]);
				mix(i+1, intermediateResult[i], done);
			});
    }
    	
    it( 'Final results as expected', function()
    {
    	console.log('************ Final Result');
    	
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
        expect(res.blamingData).not.toBe(undefined);
        expect(res.blame).toBe(true);
        for (var i=0; i<NMixServ; ++i) {
            var res = voter.checkMixServerResult(i, intermediateResult[i], rec);
            expect(res.ok).toBe(false);
            expect(res.descr).toBe('Ballot not found');
            expect(res.blame).toBe(true);
            expect(res.blamingData).not.toBe(undefined);
        }
    });

});


/* no callback: synchronous version */
function dataFromFile(path, callback){
	if(callback)
		fs.readFile(path, {encoding:'utf8'}, callback);
	/* function (err, data) {
	 * 		if (err) throw err;
	 *		  	console.log(data);
	 *	});
	 */
	else
		return fs.readFileSync(path, {encoding:'utf8'});
}

/* no callback: synchronous version */
function dataToFile(data, path, callback){
	if (callback)
		fs.writeFile(path, data, {encoding:'utf8'}, callback);
	/* 	function (err) {
     *   if (err)
     *       console.log('Problems with saving data in ', path);
     *   else
     *       console.log('Data written in ', path);
     *	});
     */
	else
		fs.writeFileSync(path, data, {encoding:'utf8'});
}

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
