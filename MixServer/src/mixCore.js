var mixCore = function() {
var exports = {};
//////////////////////////////////////////////////////////////////////////////////////////////

var java = require("java");

exports.create = function(encKey, decKey, verifKey, signKey, precServVerifKey, 
		electionID, numberOfVoters, class_path)
{
	//console.log('Mix: Current directory --> ' + process.cwd());
	// CONSTRUCTOR 
	// Add (java) class paths
	for (var i=0; i<class_path.length; ++i)
	    java.classpath.push(class_path[i]);
//	var chainIndex = -1
//	for(var i=0; i<manifest.mixServers.length; ++i)
//		if(manifest.mixServers[i].encryption_key === config.encryption_key &&
//			manifest.mixServers[i].verification_key === config.verification_key){
//			chainIndex = i;
//			break;
//		}
	// exports.chainIndex = chainIndex;
	
//	// Cryptographic Keys:
//	var encKey      = manifest.mixServers[chainIndex].encryption_key;
//	var decKey      = config.decryption_key;
//	var verifKey    = manifest.mixServers[chainIndex].verification_key;
//	var signKey     = config.signing_key;
//	if (chainIndex == 0)
//		var precServVerifKey  = manifest.collectingServer.verification_key;
//	else
//		var precServVerifKey  = manifest.mixServers[chainIndex-1].verification_key;
	
	// Create the list (map) of eligible voters. Usage if (eligibleVoters[v]) ...
	//	var numberOfVoters = manifest.voters.length;
	
	
//	console.log("encKey:\n" + encKey + "\n\n" + 
//			"decKey:\n" +  decKey + "\n\n" + 
//			"verifKey:\n" + verifKey + "\n\n" + 
//			"signKey:\n" + signKey + "\n\n" + 
//			"precServVerifKey:\n" + precServVerifKey + "\n\n" + 
//			"electionID:\t" + electionID + "\n\n" + 
//			"numberOfVoters:\t" + numberOfVoters + "\n\n\n");
	
	function processBallots(data) {
		// WARN: if the wrapper is created outside the function a fatal error happens
		// Create an instance of MixServerWrapper:
		var mixServWrapper = java.newInstanceSync("selectvoting.system.wrappers.MixServerWrapper",
			                                      encKey, decKey, verifKey, signKey, precServVerifKey,
			                                      electionID, numberOfVoters);
		// module.exports = mixServWrapper;
		// since we are using the sync method, an exception 
		// will be throw if an error occurs
		var result = java.callMethodSync(mixServWrapper, "processBallots", data);
		return result;
	}
	
	return {encKey : encKey, verifKey : verifKey, electionID : electionID, 
			processBallots: processBallots};
}

//////////////////////////////////////////////////////////////////////////////////////////////
return exports;
}();

if (typeof(module)!=='undefined' && module.exports) {
    module.exports = mixCore;
}

