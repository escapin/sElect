var java = require("java");
var config = require("./config");
var manifest = require("./manifest")

// Add (java) class paths, as specified in the config:
for (var i=0; i<config.class_paths.length; ++i) {
    var path = config.class_paths[i];
    java.classpath.push(path);
}

// Cryptographic Keys:
var csVerifKey  = manifest.collectingServer.verification_key;
var encKey      = manifest.finalServer.encryption_key;
var decKey      = config.decryption_key;
var verifKey    = manifest.finalServer.verification_key;
var signKey     = config.signing_key;


// Create the list (map) of eligible voters. Usage if (eligibleVoters[v]) ...
var numberOfVoters = +manifest.voters.length;

// Create an instance of FinalServerWrapper:
var fsWrapper = java.newInstanceSync("selectvoting.system.wrappers.FinalServerWrapper", 
                                      encKey, decKey, verifKey, signKey, csVerifKey, 
                                      manifest.hash, numberOfVoters);

module.exports = fsWrapper;
