var mixCore = function() {
var exports = {};
//////////////////////////////////////////////////////////////////////////////////////////////

var child_process = require('child_process');

exports.create = function(encKey, decKey, verifKey, signKey, precServVerifKey, electionID, classpaths)
{
	
	var classpathString = '"';
	for (var i in classpaths){
		classpathString += classpaths[i] + ':';
	}
	classpathString+='"';
	
	
	function processBallots(inputFile_path, outputFile_path, callbackExec, onClose) {
		
		mixServer = child_process.exec('java -cp ' + classpathString + " " +
 		                    		                'selectvoting.system.wrappers.MixServerWrapperMain' + " " +
		                    		                encKey + " " + decKey + " " + verifKey + " " + signKey + " " + precServVerifKey  
		                    		                + " " + electionID + " " + inputFile_path + " " + outputFile_path, 
		                    		                	callbackExec);
		
		if(onClose)
			mixServer.on('close', onClose);
		
	}
	
	return {encKey : encKey, verifKey : verifKey, precServVerifKey: precServVerifKey,
			electionID : electionID, processBallots: processBallots};
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

