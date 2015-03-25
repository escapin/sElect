var mixCore = function() {
var exports = {};
//////////////////////////////////////////////////////////////////////////////////////////////


exports.create = function(encKey, decKey, verifKey, signKey, precServVerifKey, 
		electionID, numberOfVoters)
{
	
	function processBallots(inputFile_path, outputFile_path) {
		var spawn = require('child_process').spawn,
			mixServer = spawn('java', ['-cp', '.:../lib/*',
			                           'selectvoting.system.wrappers.MixServerWrapperMain',
			                           encKey, decrKey, verifKey, signKey, precServVerifKey,
			                           electionID, numberOfVoters, inputFile_path, outputFile_path
			                           ],
			                           {cwd: '../../bin/', 	// the working dir of the process: java bin
										env: process.env});
		mixServer.on('close', function (code) {
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
				    	// FIXME: should we do something else if a ServerMisbehavior occurs?
				    default:
				    	console.log('***Unknown Errror***');
			    }
			    process.exit(code);
			 }
			console.log('[mixCore.js] MixWrapper process ended correctly');
		});
	}
	
	return {encKey : encKey, verifKey : verifKey, precServVerifKey: precServVerifKey,
			electionID : electionID, numberOfVoters: numberOfVoters,
			processBallots: processBallots};
}

//////////////////////////////////////////////////////////////////////////////////////////////
return exports;
}();

if (typeof(module)!=='undefined' && module.exports) {
    module.exports = mixCore;
}

