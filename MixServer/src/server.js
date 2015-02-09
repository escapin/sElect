var java = require("java");
var config = require("../config");
var manifest = require("./manifest")

// Add (java) class paths, as specified in the config:
for (var i=0; i<config.class_paths.length; ++i) {
    var path = config.class_paths[i];
    java.classpath.push(path);
}

// resume the index in the chain of mix servers
var index 		= config.chainIndex;
if (index<0 || index >= +manifest.mixServers.length) {
	console.log('ERROR: Mix server index out of range.');
    console.log('SERVER NOT STARTED.');
    process.exit(1);
}
// Cryptographic Keys:
var encKey      = manifest.mixServers[index].encryption_key;
var decKey      = config.decryption_key;
var verifKey    = manifest.mixServers[index].verification_key;
var signKey     = config.signing_key;
if (index == 0)
	var precServVerifKey  = manifest.collectingServer.verification_key;
else
	var precServVerifKey  = manifest.mixServers[index-1].verification_key;

// Create the list (map) of eligible voters. Usage if (eligibleVoters[v]) ...
var numberOfVoters = +manifest.voters.length;

// Create an instance of MixServerWrapper:
var mixServWrapper = java.newInstanceSync("selectvoting.system.wrappers.MixServerWrapper", 
                                      encKey, decKey, verifKey, signKey, precServVerifKey, 
                                      manifest.hash, numberOfVoters);

module.exports = mixServWrapper;
