var mixCore = function() {
var exports = {};
//////////////////////////////////////////////////////////////////////////////////////////////

var java = require("java");

exports.create = functon(config, manifest)
{
	// CONSTRUCTOR 
	// Add (java) class paths, as specified in the config:
	for (var i=0; i<config.class_paths.length; ++i) {
	    var path = config.class_paths[i];
	    java.classpath.push(path);
	}
	var chainIndex = -1
	for(var i=0; i<manifest.mixServers.length; ++i)
		if(manifest.mixServers[i].encryption_key === config.encryption_key &&
			manifest.mixServers[i].verification_key === config.verification_key){
			chainIndex = i;
			break;
		}
	// exports.chainIndex = chainIndex;
	
	// Cryptographic Keys:
	var encKey      = manifest.mixServers[chainIndex].encryption_key;
	var decKey      = config.decryption_key;
	var verifKey    = manifest.mixServers[chainIndex].verification_key;
	var signKey     = config.signing_key;
	if (chainIndex == 0)
		var precServVerifKey  = manifest.collectingServer.verification_key;
	else
		var precServVerifKey  = manifest.mixServers[chainIndex-1].verification_key;
	
	// Create the list (map) of eligible voters. Usage if (eligibleVoters[v]) ...
	var numberOfVoters = manifest.voters.length;
	
	// Create an instance of MixServerWrapper:
	var mixServWrapper = java.newInstanceSync("selectvoting.system.wrappers.MixServerWrapper",
		                                      encKey, decKey, verifKey, signKey, precServVerifKey,
		                                      manifest.hash, numberOfVoters);
	
	// module.exports = mixServWrapper;
	
	function processBallots(data) {
		// since we are using the sync method, an exception 
		// will be throw if an error occurs
		var result = java.callMethodSync(mixServWrapper, "processBallots", data);
		return result;
	}
	
	return {chainIndex: chainIndex, processBallots: processBallots};
}

//////////////////////////////////////////////////////////////////////////////////////////////
return exports;
}();

if (typeof(module)!=='undefined' && module.exports) {
    module.exports = mixCore;
}

