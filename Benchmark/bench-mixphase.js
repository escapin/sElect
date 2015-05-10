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

var params = {	NVoters: 50, 
				NMixServ: 3}; // default params

if(process.argv.length!==2 && process.argv.length!==4){ 
	console.log("ERROR in command line.\n" +
			"Usage: \n" +
			"\t node bench-mixphase.js \n" +
			"\t node bench-mixphase.js NVoters NMixServ");
	process.exit(-1);
}
else if (process.argv.length===4){ // custom parameters 
	params.NVoters = process.argv[2];
	params.NMixServ = process.argv[3];
}
//console.log(JSON.stringify(params));
////////////////////////////////////////


var TAG_VOTERS = '10';
var TAG_BALLOTS = '01';
//console.log('CWD:\t' + process.cwd());
var PARTIALRESULT_dir = path.join(process.cwd(), '_data_Test');



var electionID = 'eeee';
// keys
var colServSigKeys = crypto.sig_keygen();
var colServVerifKey = colServSigKeys.verificationKey;

var mixServPkeKeys = new Array(params.NMixServ);
var mixServSigKeys = new Array(params.NMixServ);
for(var i=0; i<params.NMixServ; i++) {
	mixServPkeKeys[i] = crypto.pke_keygen();
	mixServSigKeys[i] = crypto.sig_keygen();
}
var mixServEncKeys = mixServPkeKeys.map(function(k){ return k.encryptionKey; });
var mixServVerifKeys = mixServSigKeys.map(function(k){ return k.verificationKey; });
// java classpaths for mix server
var classpaths = ["../bin", "../lib/*"];


var cs = csCore.create(electionID, colServSigKeys.signingKey);
var mixServer = new Array(params.NMixServ);
var intermediateResult = new Array(params.NMixServ);

// compiled/called before the test loop 
// we create the ballots and we submit them to the collecting Server
function onStart(){
	console.log('****** SETUP THE BENCHMARK');
	
	console.log('************ Set up the ' + params.NMixServ + '  Mix Servers');
	// THE MIX SERVERS
	for (i=0; i<params.NMixServ; ++i) {
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
	
    
    console.log('************ Set up the voters');
	var voters = new Array(params.NVoters);
	for (var i=params.NVoters-1; i>=0; --i) {
	    voters[i] = 'abc'  + i + '@ema.il';
	}
	// the object performing the voting 
	var voter = voterClient.create(electionID, colServVerifKey, mixServEncKeys, mixServVerifKeys);
    
    console.log('************ Create and cast the ballots');
    var receipts = new Array(voters.length);
    for(i=0; i<voters.length; ++i){
        // create ballot (a receipt containing a ballot); i-th voter votes for i-th candidate:
		receipts[i] = voter.createBallot(i);
		if(receipts[i].choice!==i){
			console.log();
        	console.log("\tSomething wrong with the receipt of the " + i + "-th voter");
        	console.log("\tAborting the whole process...");
        	process.exit(-1);
        }
		// submit the ballot
    	var csReply = cs.collectBallot(voters[i], receipts[i].ballot);
        // store the acknowledgement (signature) in the receipt
        receipts[i].signature = csReply.data;
        
		process.stdout.write('\r>>> Ballot #' + (i+1) + ' casted');
    }
	console.log();
    
    
//    console.log('************ Check the acknowledgment in the receipt');
//    for(i=0; i<voters.length; ++i){
//        // check the acknowledgement (signature in the receipt)
//        var receiptOK = voter.validateReceipt(receipts[i]);
//        if(receiptOK===false) {
//        	console.log("\tSomething wrong with the receipt of the " + i + "-th voter");
//        	console.log("\tAborting the whole process...");
//        	process.exit(-1);
//        }
//	}
    
    console.log();
    console.log('****** STARTING THE BENCHMARK');
}

// the test to benchmark:
// from closing the election 'till the last mix server processed all the ballots
function mixPhase(deferred){
	//console.log('************ Get the partial result from the collecting server');
	var signedBallots = cs.getResult();
	mix(0, signedBallots, deferred);
}

function mix(i, inputData, deferred) {
    if (i >= params.NMixServ) {
    	deferred.resolve();
        return;
    }

    //console.log('\n************ Mixing', i);
    
    var nonce = crypto.nonce(5);
    
    var inputFile_path = path.join(PARTIALRESULT_dir,'partialResult0' + i + '_' + nonce + '_input.msg'); 
	dataToFile(inputData, inputFile_path);
	var outputFile_path = path.join(PARTIALRESULT_dir, 'partialResult0' + i + '_' + nonce + '_output.msg');
	
	mixServer[i].processBallots(inputFile_path, outputFile_path, 
    	function(err, stdout, stderr){
			//console.log(stdout);
			if(err){
				console.log(' ...INTERNAL ERROR. Cannot process the ballots: ', err);
				console.log('RESULT NOT SAVED');
				console.log(stderr);
			}
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
			//console.log("Submitting to the next mix server");
			
			mix(i+1, intermediateResult[i], deferred);
		});
}

var nCycles = 0;
var bench = new Benchmark('mixPhase', {

	// a flag to indicate the benchmark is deferred
	'defer': true,
	
	'fn': function(deferred) {
		mixPhase(deferred);
	 },

	// called when the benchmark starts running
	'onStart': onStart(),

	// called after each run cycle
	'onCycle':  function() {
	  ++nCycles;
	  process.stdout.write('\r>>> Cycles: ' + nCycles );
  	},

  	// called when aborted
  	'onAbort': function() { console.log('Process Aborted!'); },

  	// called when a test errors
  	'onError': function() { console.log('Test Error!'); },

  	// called when the benchmark completes running
  	'onComplete': onComplete,

  	// compiled/called before the test loop
  	// 'setup': setup,
  
  	//  compiled/called after the test loop
  	//'teardown': function() { console.log("\n"); }
});

//bench.run({ 'async': true });
bench.run();



function onComplete(){
	console.log(); // new line after the n-th cycle
	console.log("****** BENCHMARK COMPLETED");
	console.log();
	
	console.log('***********************************\n' +
			'* PARAMETER:\n' +
			'*  - Number of voters:\t\t ' + params.NVoters + '\n' +
			'*  - Number of mix servers:\t ' + params.NMixServ);

	/**********************************	
	 *  cycle: 		time taken to complete the last cycle (secs)
	 *	elapsed:	the time taken to complete the benchmark (secs)
	 *	period:		the time taken to execute the test once (secs)
	 *	timeStamp:	a timestamp of when the benchmark started (ms)
	**********************************/
	
	console.log(	'***********************************\n' +
		'* TIMES:\n' +
		'*  - time taken to complete the last cycle \t-->\t cycle (secs):\t\t' + bench.times.cycle + '\n' +
		'*  - time taken to complete the benchmark \t-->\t elapsed (secs):\t' + bench.times.elapsed + '\n' +
		'*  - the time taken to execute the test once \t-->\t period (secs):\t\t' + bench.times.period + '\n' +
		'*  - timestamp of when the benchmark started \t-->\t timeStamp (ms):\t' + bench.times.timeStamp + '\n' +
					'***********************************');
	
	
	console.log("\n************* As a JSON Object *************");
	output={};
	output=jsonConcat(output, params);
	output=jsonConcat(output, bench.times);
	console.log(JSON.stringify(output));
}


function jsonConcat(output, toBeConcat) {
 for (var key in toBeConcat) {
	 output[key] = toBeConcat[key];
 }
 return output;
}

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