var java = require("java");
var config = require("./config");

// Add (java) class paths, as specified in the config:
for (var i=0; i<config.class_paths.length; ++i) {
    var path = config.class_paths[i];
    java.classpath.push(path);
}

// Cryptographic Keys:
var colSerVerKey = config.manifest.collectingServer.verification_key;
var colSerSigKey = config.signingKey;

// Create the instance of CollectingServerWrapper:
var voterIdentifiers = java.newArray("java.lang.String", ["voter1", "voter2", "voter3"]); // FIXME
console.log('Creating an instance of CollectingServerWrapper');
var csWrapper = java.newInstanceSync("de.uni.trier.infsec.eVotingSystem.wrappers.CollectingServerWrapper", 
                                      colSerVerKey, colSerSigKey, config.manifest.electionID, voterIdentifiers);
console.log(' ...CollectingServerWrapper created');

// Call to collectBallot:
function collectBallot(voter_id, ballot, callback) {
    console.log('CollectBallot for ', voter_id, ballot);
    csWrapper.collectBallot(voter_id, ballot, callback);
    console.log(' ...let us see what hapens'); 
}


// EXPORTS

exports.collectBallot = collectBallot;

