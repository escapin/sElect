var java = require("java");
var config = require("../config");
var manifest = require("./manifest")

// Add (java) class paths, as specified in the config:
for (var i=0; i<config.class_paths.length; ++i) {
    var path = config.class_paths[i];
    java.classpath.push(path);
}

// Cryptographic Keys:
var colSerEncKey = manifest.collectingServer.encryption_key;
var colSerDecKey = config.decryption_key;
var colSerVerKey = manifest.collectingServer.verification_key;
var colSerSigKey = config.signing_key;

// Create the list (map) of eligible voters. Usage if (eligibleVoters[v]) ...
var eligibleVoters = {};
var listOfEligibleVoters = [];
for( var i=0; i<manifest.voters.length; ++i ) {
    var voter_id = manifest.voters[i].email;
    eligibleVoters[voter_id] = true;
    listOfEligibleVoters.push(voter_id);
}
var voterIdentifiers = java.newArray("java.lang.String", listOfEligibleVoters); 

// Create the instance of CollectingServerWrapper:
var csWrapper = java.newInstanceSync("selectvoting.system.wrappers.CollectingServerWrapper", 
                                      colSerEncKey, colSerDecKey, colSerVerKey, colSerSigKey, 
                                      manifest.hash, voterIdentifiers);

// Export the wrapper:
module.exports = csWrapper;
module.exports.eligibleVoters = eligibleVoters;

