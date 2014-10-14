
var PORT = 3400;
var MANIFEST_FILE = process.env.HOME + '/.eVotingSystem/Public/Manifest/ElectionManifest.json';

// Read the manifest

var fs = require('fs');

var manifset = null;
if (fs.existsSync(MANIFEST_FILE)) {
    manifest = JSON.parse(fs.readFileSync(MANIFEST_FILE));
}
else { // Initialization failed
    manifest = null;
}

// EXPORTS

exports.port = PORT;
exports.manifest = manifest;
exports.colServURI = "http://localhost:3300/";  // FIXME Should be taken from the manifest
