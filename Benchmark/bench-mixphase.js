var fs = require('fs');
var path = require('path');
var crypto = require('cryptofunc');
var cryptoUtils = require('cryptoUtils');
var voterClient = require('voterClient');
//var csCore = require('../CollectingServer/src/csCore.js');
var mixCore = require('../MixServer/src/mixCore.js');

var mkdirp = require('mkdirp');
var Benchmark = require('benchmark');
var SortedList = require('sortedlist');


//SHORTCUTS
var pair = crypto.concatenate;
var unpair = crypto.deconcatenate;
var sign = crypto.sign;

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


//var TAG_VOTERS = '10';
var TAG_BALLOTS = '01';
//console.log('CWD:\t' + process.cwd());
var PARTIALRESULT_dir = path.join(process.cwd(), '_data_Test');
var CSRESULTS_dir = path.join(process.cwd(), '_CollectingServer_results');

var electionID = 'eeee';
var classpaths = ["../bin", "../lib/*"];

//var cs;
var mixServer;
var signedBallots;

// compiled/called before the test loop 
// we create the ballots and we submit them to the collecting Server
function onStart(){
	console.log('****** SETUP PHASE');
	
	console.log('************ Set up the keys');
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
	
	
	//console.log('************ Set up the Collecting Server');
	//cs = csCore.create(electionID, colServSigKeys.signingKey);
	
	
	console.log();
    console.log('****** VOTING PHASE');
    
    var voterObj = voterClient.create(electionID, colServVerifKey, mixServEncKeys, mixServVerifKeys);
    var sortedBallots = SortedList.create();
	for (var i=0; i<params.NVoters; ++i) {
	    var voter = 'abc'  + i + '@ema.il';
	    var receipt = voterObj.createBallot(i);
	    sortedBallots.insertOne(receipt.ballot);
	    //var csReply = cs.collectBallot(voter, receipt.ballot); // cast the vote
	    process.stdout.write('\r>>> Ballot #' + (i+1) + ' created');
	}
    console.log();
    
    console.log();
    console.log('************ Concatenating the casted ballots as an unique message');
    //signedBallots = cs.getResult();
    var ballotsArray=sortedBallots.toArray();
    var ballotsAsAMessage = '';
	var last;
    for(var i=ballotsArray.length-1; i>=0; --i){
    	// inverse order necessary to make the implementation of crypto.concatenate work
    	var current = ballotsArray[i];
    	if(last === undefined || current!==last)
    		ballotsAsAMessage = pair(current, ballotsAsAMessage);
		last = current;
		process.stdout.write('\r>>> Ballot #' + (ballotsArray.length-i) + ' processed');
    }
    console.log();
    console.log();
    console.log("Length of one ballot (the last one): " + ballotsArray[ballotsArray.length-1].length + " byte.");
    
    console.log();
    console.log('************ Creating message with the ballots signed as by the Collecting Server');
    console.log('>>> (TAG, elID, ballotsAsAMessage)');
    var tag_elID_ballots = pair(TAG_BALLOTS, pair(electionID, ballotsAsAMessage));
    console.log('>>> Generating the signature');
    var signature = sign(colServSigKeys.signingKey, tag_elID_ballots);
    console.log('>>> signedBallots=(TAG, elID, ballotsAsAMessage, signature)');
    signedBallots = pair(tag_elID_ballots, signature);
    
	console.log();
	var signedBallotsFILE= path.join(CSRESULTS_dir,'signedBallots' + params.NVoters + '.msg');
	console.log('************ Save the message with the ballots signed by the Collecting Server in \n', signedBallotsFILE);
	mkdirp(CSRESULTS_dir, function (err) {
        if (err)
        	console.error("Error: ", err);
		//else 
        //	console.log("\tFolder '" + CSRESULTS_dir + "' created.");
    });
	dataToFile(signedBallots, signedBallotsFILE);
    
	console.log();
    console.log('************ Set up the ' + params.NMixServ + '  Mix Servers');
	mixServer = new Array(params.NMixServ);
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
    
    console.log('****** MIXING PHASE: starting the benchmark');
}

// the test to benchmark:
// from closing the election 'till the last mix server processed all the ballots
function mixPhase(deferred){
	mix(0, signedBallots, deferred);
}

function mix(i, inputData, deferred) {
    if (i >= params.NMixServ) {
    	deferred.resolve();
        return;
    }

    //console.log('\n************ Mixing', i);
    
    var inputFile_path = path.join(PARTIALRESULT_dir,'partialResult0' + i + '_input.msg');
	dataToFile(inputData, inputFile_path);
	var outputFile_path = path.join(PARTIALRESULT_dir, 'partialResult0' + i + '_output.msg');
	
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
			var intermediateResult = dataFromFile(outputFile_path);
			//console.log(intermediateResult);
			//console.log("Submitting to the next mix server");
			
			mix(i+1, intermediateResult, deferred);
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
	 *  cycle: 		the time taken to complete the last cycle (secs)
	 *	elapsed:	the time taken to complete the benchmark (secs)
	 *	period:		the time taken to execute the test once (secs)
	 *	timeStamp:	a timestamp of when the benchmark started (ms)
	**********************************/
	
	console.log(	'***********************************\n' +
		'* TIMES:\n' +
		'*  - the time taken to complete the last cycle \t-->\t cycle (secs):\t\t' + bench.times.cycle + '\n' +
		'*  - the time taken to complete the benchmark \t-->\t elapsed (secs):\t' + bench.times.elapsed + '\n' +
		'*  - the time taken to execute the test once \t-->\t period (secs):\t\t' + bench.times.period + '\n' +
		'*  - a timestamp of when the benchmark started \t-->\t timeStamp (ms):\t' + bench.times.timeStamp + '\n' +
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