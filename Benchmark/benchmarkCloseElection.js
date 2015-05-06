var fs = require('fs');
var path = require('path');
var crypto = require('cryptofunc');
var cryptoUtils = require('cryptoUtils');
var voterClient = require('voterClient');
var csCore = require('../CollectingServer/src/csCore.js');
var mixCore = require('../MixServer/src/mixCore.js');

var mkdirp = require('mkdirp');
var Benchmark = require('benchmark');



////////////////////////////////////////
// PARAMETERS OF THE TEST
var NMixServ = 3;
var NVoters = 50;
////////////////////////////////////////



var TAG_VOTERS = '10';
var TAG_BALLOTS = '01';
//console.log('CWD:\t' + process.cwd());
var PARTIALRESULT_dir = path.join(process.cwd(), '_data_Test');



var electionID = 'eeee';
// keys
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
// java classpaths for mix server
var classpaths = ["../bin", "../lib/*"];


var mixServer = new Array(NMixServ);
var intermediateResult = new Array(NMixServ);


// compiled/called before the test loop 
// we create the ballots and we submit them to the collecting Server
function setup(){
	console.log('****** SET UP PHASE');
	
	
	console.log('************ Set up the voters');
	var voters = new Array(NVoters);
	for (var i=NVoters-1; i>=0; --i) {
	    voters[i] = 'abc'  + i + '@ema.il';
	}
	// the object performing the voting 
	var voter = voterClient.create(electionID, colServVerifKey, mixServEncKeys, mixServVerifKeys);
	
	
	console.log('************ Set up the Collecting and the Mix Servers');
	// THE COLLECTING SERVER
	cs = csCore.create(electionID, colServSigKeys.signingKey); // *global var*
	// THE MIX SERVERS
	
	for (i=0; i<NMixServ; ++i) {
	    var precServVerifKey = (i==0 ? colServVerifKey : mixServSigKeys[i-1].verificationKey );
		mixServer[i] = mixCore.create(	mixServPkeKeys[i].encryptionKey,
										mixServPkeKeys[i].decryptionKey,
										mixServSigKeys[i].verificationKey,
										mixServSigKeys[i].signingKey,
										precServVerifKey, electionID,
										classpaths);
	}
	
	
	console.log('************ Create the folder where the partial result will be stored');
	deleteFolderRecursive(PARTIALRESULT_dir);
    mkdirp(PARTIALRESULT_dir, function (err) {
        if (err) 
        	console.error("Error: ", err);
		//else 
        //	console.log("\tFolder '" + PARTIALRESULT_dir + "' created.");
    });
	
    
    console.log('************ Create the ballots');
    var receipts = new Array(voters.length);
    for(i=voters.length-1; i>=0; --i){
        // create ballot (a receipt containing a ballot); i-th voter votes for i-th candidate:
		receipts[i] = voter.createBallot(i);
		if(receipts[i].choice!==i){
        	console.log("\tSomething wrong with the receipt of the " + i + "-th voter");
        	console.log("\tAborting the whole process...");
        	process.exit(-1);
        }
			
    }
	
    
    console.log('************ Cast the ballots');
    for(i=voters.length-1; i>=0; --i){
        // submit the ballot
    	var csReply = cs.collectBallot(voters[i], receipts[i].ballot);
        // store the acknowledgement (signature) in the receipt
        receipts[i].signature = csReply.data;
	}
    
    
    console.log('************ Check the acknowledgment in the receipt');
    for(i=voters.length-1; i>=0; --i){
        // check the acknowledgement (signature in the receipt)
        var receiptOK = voter.validateReceipt(receipts[i]);
        if(receiptOK===false) {
        	console.log("\tSomething wrong with the receipt of the " + i + "-th voter");
        	console.log("\tAborting the whole process...");
        	process.exit(-1);
        }
	}
}

// the test to benchmark:
// from closing the election 'till the last mix server processed all the ballots
function fromClosingElection(){
	//console.log('************ Get the partial result from the collecting server');
	var signedBallots = cs.getResult();
	
	finalResult = mix(0, signedBallots); // *global var*
}

function mix(i, inputData) {
    if (i >= NMixServ)  {
        return inputData;
    }

    //console.log('************ Mixing', i);
    
    var nonce = crypto.nonce(5);
    
    var inputFile_path = path.join(PARTIALRESULT_dir,'partialResult0' + i + '_' + nonce + '_input.msg'); 
	dataToFile(inputData, inputFile_path);
	var outputFile_path = path.join(PARTIALRESULT_dir, 'partialResult0' + i + '_' + nonce + '_output.msg');
	
	mixServer[i].processBallots(inputFile_path, outputFile_path, 
    	function(err, stdout, stderr){
			console.log(stdout);
			console.log(stderr);
		},
		function(code){
			if(code!==0){
				console.log("\tSomething got wrong while the " + i + "-th " +
						"mix server was processing the ballots");
				switch(code){
					case 10:
						console.log('***MixServerWrapper*** \t Wrong Number of Arguments');
						break;
					case 11:
						console.log('***MixServerWrapper*** \t [IOException] reading the file');
						break;
					case 12:
						console.log('***MixServerWrapper*** \t [IOException] writing the file');
						break;
					case 1:
						console.log('***MalformedData*** \t Wrong signature');
						break;
					case 2:
						console.log('***MalformedData*** \t Wrong tag');
						break;
					case 3:
						console.log('***MalformedData*** \t Wrong election ID');
						break;
					case -1:
						console.log('***ServerMisbehavior*** \t Too many entries');
						break;
					case -2:
						console.log('***ServerMisbehavior*** \t Ballots not sorted');
						break;
					case -3:
						console.log('***ServerMisbehavior*** \t Duplicate ballots');
						break;
					default:
						console.log('***Unknown Errror***');
				}
				console.log("\tAborting the whole process...");
	        	process.exit(-1);
			}
			intermediateResult[i] = dataFromFile(outputFile_path);
			//console.log(intermediateResult[i]);
			mix(i+1, intermediateResult[i]);
		});
}




var bench = new Benchmark('fromClosingElection', fromClosingElection, {

  // displayed by Benchmark#toString if `name` is not available
  //'id': 'xyz',

  // called when the benchmark starts running
  //'onStart': onStart,

  // called after each run cycle
  //'onCycle': function() { console.log('************** \n Cycle '); },

  // called when aborted
  'onAbort': function() { console.log('Process Aborted!'); },

  // called when a test errors
  //'onError': onError,

  // called when reset
  //'onReset': onReset,

  // called when the benchmark completes running
//  'onComplete': function() {
//	  	var times = this.times;
//	  	console.log(times);
//	  	console.log('Time elapsed: ' + times.elapsed);
//	  },

  // compiled/called before the test loop
  'setup': setup(),
  

  // compiled/called after the test loop
  'teardown': function() {
	  	console.log(this.times);
	  }
})
.run({ 'async': true });


function deleteFolderRecursive(path) {
  if( fs.existsSync(path) ) {
    fs.readdirSync(path).forEach(function(file,index){
      var curPath = path + "/" + file;
      if(fs.lstatSync(curPath).isDirectory()) { // recurse
        deleteFolderRecursive(curPath);
      } else { // delete file
        fs.unlinkSync(curPath);
      }
    });
    fs.rmdirSync(path);
  }
};

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