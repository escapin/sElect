var java = require("java");
var config = require("./config");
var manifest = require("./manifest")

// Add (java) class paths, as specified in the config:
for (var i=0; i<config.class_paths.length; ++i) {
    var path = config.class_paths[i];
    java.classpath.push(path);
}

// Read the private key of the collecting server
var colServPR = require(config.PRIVATE_KEY_FILE);
var signingKey = colServPR.signatureKey;

// Cryptographic Keys:
var colSerVerKey = manifest.collectingServer.verification_key;
var colSerSigKey = signingKey;

// Create the instance of CollectingServerWrapper:
var voterIdentifiers = java.newArray("java.lang.String", ["voter1", "voter2", "voter3"]); // FIXME
console.log('Creating an instance of CollectingServerWrapper');
var csWrapper = java.newInstanceSync("de.uni.trier.infsec.eVotingSystem.wrappers.CollectingServerWrapper", 
                                      colSerVerKey, colSerSigKey, manifest.electionID, voterIdentifiers);
console.log(' ...CollectingServerWrapper created');

// Call to collectBallot:
/*
function collectBallot(voter_id, ballot, callback) {
    console.log('CollectBallot for ', voter_id, ballot);
    csWrapper.collectBallot(voter_id, ballot, callback);
    console.log(' ...let us see what hapens'); 
}
*/


// EXPORTS

module.exports = csWrapper;
// exports.collectBallot = collectBallot;

