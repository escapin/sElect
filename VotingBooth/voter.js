var java = require("java");
var config = require("./config");
var manifest = require('./manifest');

// Add (java) class paths, as specified in the config:
for (var i=0; i<config.class_paths.length; ++i) {
    var path = config.class_paths[i];
    java.classpath.push(path);
}

// Cryptographic Keys
var colSerVerKey = manifest.collectingServer.verification_key;
var colSerEncKey = manifest.collectingServer.encryption_key;
var finSerEncKey = manifest.finalServer.encryption_key;

console.log('Creating an instance of VoterWrapper');
var voterWrapper = java.newInstanceSync("de.uni.trier.infsec.eVotingSystem.wrappers.VoterWrapper", 
                                         manifest.hash,
                                         colSerEncKey, colSerVerKey, finSerEncKey);
console.log(' ...VoterWrapper created');

module.exports = voterWrapper;

