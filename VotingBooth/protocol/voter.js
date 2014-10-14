var java = require("java");
var config = require("../config");

for (var i=0; i<config.class_paths.length; ++i) {
    var path = config.class_paths[i];
    java.classpath.push(path);
}

// Create the voting client instance
// TODO: for now there is only one instance (a proof of concept for node-java communication)
//       Instances should be bound to the session.


var colSerEncKey = config.manifest.collectingServer.encryption_key;
var colSerVerKey = config.manifest.collectingServer.verification_key;
var finSerEncKey = config.manifest.finalServer.encryption_key;

var voter = java.newInstanceSync("de.uni.trier.infsec.eVotingSystem.wrappers.VoterWrapper", 
                                 "email", manifest.electionID,
                                 colSerEncKey, colSerVerKey, finSerEncKey);

console.log('Voter instance created');

exports.voter = voter;

