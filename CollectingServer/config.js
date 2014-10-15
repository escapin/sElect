
var PORT = 3300;
var MANIFEST_FILE = process.env.HOME + '/.eVotingSystem/Public/Manifest/ElectionManifest.json';
var PRIVATE_KEY_FILE = process.env.HOME + '/.eVotingSystem/Private/PrivateKeys/CollectingServer_PR.json';
var JAVA_CLASSPATHS = ["../bin", "../lib/bcprov-jdk16-146.jar"];


// Read the manifest
var fs = require('fs');
var manifset = null;
if (fs.existsSync(MANIFEST_FILE)) {
    manifest = JSON.parse(fs.readFileSync(MANIFEST_FILE));
}
else { // Initialization failed
    manifest = null;
}

// Read the private key of the collecting server

var colServPR = require(PRIVATE_KEY_FILE);

// EXPORTS

exports.port = PORT;
exports.manifest = manifest;
exports.signingKey = colServPR.signatureKey;
exports.class_paths = JAVA_CLASSPATHS;
