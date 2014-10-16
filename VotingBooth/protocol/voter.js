var java = require("java");
var config = require("../config");
var manifest = require('../manifest');

// Add (java) class paths, as specified in the config:
for (var i=0; i<config.class_paths.length; ++i) {
    var path = config.class_paths[i];
    java.classpath.push(path);
}

// Cryptographic Keys
var colSerVerKey = manifest.collectingServer.verification_key;
var finSerEncKey = manifest.finalServer.encryption_key;

console.log('Creating an instance of VoterWrapper');
var voterWrapper = java.newInstanceSync("de.uni.trier.infsec.eVotingSystem.wrappers.VoterWrapper", 
                                         colSerVerKey, finSerEncKey);
console.log(' ...VoterWrapper created');

// Calling Java to create a ballot
function createBallot( choice_nr, callback) {
    console.log('Call Java to create a ballot for choice_nr = ', choice_nr);
    //java.callMethod(voterWrapper, "createBallot", choice_nr, callback);
    voterWrapper.createBallot(choice_nr, callback);
}

// Calling Java to validate a receipt
function validateReceipt(receipt, electionID, ballot, callback) {
    console.log('Call Java to validate a receipt');
    voterWrapper.validateReceipt(receipt, electionID, ballot, callback);
}


// EXPORTS

exports.createBallot = createBallot;
exports.validateReceipt = validateReceipt;

