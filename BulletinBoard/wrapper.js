var java = require("java");
var config = require("./config");
var manifest = require("./manifest")

// Add (java) class paths, as specified in the config:
for (var i=0; i<config.class_paths.length; ++i) {
    var path = config.class_paths[i];
    java.classpath.push(path);
}

// Cryptographic Keys:
var finSerVerKey = manifest.finalServer.verification_key;

// Create the instance of CollectingServerWrapper:
console.log('Creating an instance of BBWrapper');
var wrapper = java.newInstanceSync("de.uni.trier.infsec.eVotingSystem.wrappers.BBWrapper", 
                                   finSerVerKey, manifest.electionID );
console.log(' ...BBServerWrapper created');

// Export the wrapper:
module.exports = wrapper;
