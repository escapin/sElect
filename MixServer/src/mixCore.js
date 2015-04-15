var mixCore = function() {
var exports = {};
//////////////////////////////////////////////////////////////////////////////////////////////

var child_process = require('child_process');

exports.create = function(encKey, decKey, verifKey, signKey, precServVerifKey, 
		electionID, numberOfVoters, classpaths)
{
	
	var classpathString = '"';
	for (var i in classpaths){
		classpathString += classpaths[i] + ':';
	}
	classpathString+='"';
	
	
	function processBallots(inputFile_path, outputFile_path, callbackExec, onClose) {
//		console.log('\n\n\n\t\t\t' + encKey + '\n\n\n');
//		var spawn = require('child_process'),
//		ls    = spawn('ls', ['-lh', '/usr']);
//		java = spawn('java', ['-version']);
//		mixServer = spawn('java', 
//								['-cp', '.:../../lib/*',
//		                           'selectvoting.system.wrappers.MixServerWrapperMain',
//		                           encKey, decKey, verifKey, signKey, precServVerifKey ,
//		                           electionID, numberOfVoters, inputFile_path, outputFile_path
//		                           ],
////		                           );
//		                           {cwd: '../../bin', 	// the working dir of the process: java bin
//									env: process.env,
//									stdio: ['pipe', 'pipe', 'pipe']
//		                           }
//						);
//		java.stdout.on('data', function (data){
//			console.log('stdout: ' + data);
//		});
		
		
		mixServer = child_process.exec('java -cp ' + classpathString + " " +
 		                    		                'selectvoting.system.wrappers.MixServerWrapperMain' + " " +
		                    		                encKey + " " + decKey + " " + verifKey + " " + signKey + " " + precServVerifKey  
		                    		                + " " + electionID + " " + numberOfVoters + " " + inputFile_path + " " + outputFile_path, 
		                    		                	callbackExec);
		
//FIME: why doesn't this version work?
//		mixServer = child_process.execFile('java', ['-cp', classpathString,
//		                'selectvoting.system.wrappers.MixServerWrapperMain',
//		                encKey, decKey, verifKey, signKey, precServVerifKey ,
//		                electionID, numberOfVoters, inputFile_path, outputFile_path
//		                ],
//		                {},
//		                // {cwd: '../bin'}, // the working dir of the process: java bin
//				        callbackExec);
		if(onClose)
			mixServer.on('close', onClose);
		
//		function (err, stdout, stderr){
//     	   if (err) {
//console.log("child processes failed with error code: " + err.code);
//}
//console.log(stdout);
//console.log(stderr);
//}
	    
//		ls.stdout.on('data', function (data) {
//		  console.log('stdout: ' + data);
//		});
//
//		ls.stderr.on('data', function (data) {
//		  console.log('stderr: ' + data);
//		});
//
//		ls.on('close', function (code) {
//		  console.log('child process exited with code ' + code);
//		});
		
		
//		mixServer.stdout.on('data', function (data) {
//		  console.log('stdout: ' + data);
//		});
//		mixServer.on('close', exitCodeHandler);
//		mixServer.on('close', function (code) {
//			console.log("Program exited with code " + code);
//		});
	}
	
	return {encKey : encKey, verifKey : verifKey, precServVerifKey: precServVerifKey,
			electionID : electionID, numberOfVoters: numberOfVoters,
			processBallots: processBallots};
}


exports.exitCodeHandler = function exitCodeHandler(code) {
	if (code !== 0) {
		console.log('[mixCore.js] MixWrapper process exited with code ' + code);
	    process.stdout.write('[mixCore.js] This error code corresponds to:\t');
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
	    return code;
	}
	console.log('[mixCore.js] MixWrapper process ended correctly');
	return 0;
}

//////////////////////////////////////////////////////////////////////////////////////////////
return exports;
}();

if (typeof(module)!=='undefined' && module.exports) {
    module.exports = mixCore;
}

