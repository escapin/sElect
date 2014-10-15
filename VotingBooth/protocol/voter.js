var java = require("java");
var config = require("../config");

// Add (java) class paths, as specified in the config:
for (var i=0; i<config.class_paths.length; ++i) {
    var path = config.class_paths[i];
    java.classpath.push(path);
}

// Cryptographic Keys
var colSerVerKey = config.manifest.collectingServer.verification_key;
var finSerEncKey = config.manifest.finalServer.encryption_key;

console.log('Creating an instance of VoterWrapper');
var voterWrapper = java.newInstanceSync("de.uni.trier.infsec.eVotingSystem.wrappers.VoterWrapper", 
                                         colSerVerKey, finSerEncKey);
console.log(' ...VoterWrapper created');

function createBallot( choice_nr, callback) {
    console.log('CreateBallot for choice_nr = ', choice_nr);
    //java.callMethod(voterWrapper, "createBallot", choice_nr, callback);
    voterWrapper.createBallot(choice_nr, callback);
}


exports.createBallot = createBallot;

