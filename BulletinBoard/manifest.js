var fs = require('fs');
var config = require('./config.json')

var manifest = null;

console.log('read manifest from', config.MANIFEST_FILE);
var manifest_file = config.MANIFEST_FILE;
console.log('Read manifest from:', manifest_file);
if (fs.existsSync(manifest_file)) {
    manifest = require(manifest_file);
}

if (!manifest) { // there is no manifest
    console.log('ERROR: Cannot find an election manifest file.');
    console.log('Server not started.');
    process.exit(1);
}

module.exports = manifest;
