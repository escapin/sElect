var java = require("java");
var config = require("./config");
var manifest = require("./manifest")

// Add (java) class paths, as specified in the config:
for (var i=0; i<config.class_paths.length; ++i) {
    var path = config.class_paths[i];
    java.classpath.push(path);
}

// Cryptographic Keys:
var colSerEncKey = manifest.collectingServer.encryption_key;
var colSerDecKey = config.decryptionKey;
var colSerVerKey = manifest.collectingServer.verification_key;
var colSerSigKey = config.signingKey;

// Create the list (map) of eligible voters. Usage if (eligibleVoters[v]) ...
var eligibleVoters = {};
var listOfEligibleVoters = [];
for( var i=0; i<manifest.votersList.length; ++i ) {
    var voter_id = manifest.votersList[i].email;
    eligibleVoters[voter_id] = true;
    listOfEligibleVoters.push(voter_id);
}
var voterIdentifiers = java.newArray("java.lang.String", listOfEligibleVoters); 

// Create the instance of CollectingServerWrapper:
console.log('Creating an instance of CollectingServerWrapper');
var csWrapper = java.newInstanceSync("de.uni.trier.infsec.eVotingSystem.wrappers.CollectingServerWrapper", 
                                      colSerEncKey, colSerDecKey, colSerVerKey, colSerSigKey, 
                                      manifest.electionID, voterIdentifiers);
console.log(' ...CollectingServerWrapper created');

// Export the wrapper:
module.exports = csWrapper;
module.exports.eligibleVoters = eligibleVoters;
