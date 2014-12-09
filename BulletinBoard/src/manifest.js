var fs = require('fs');
var config = require('../config.json');
var crypto = require('cryptofunc');
var selectUtils = require('selectUtils');

var manifest = null;
var manifest_raw = null;

console.log('Read manifest from:', config.MANIFEST_FILE);
if (fs.existsSync(config.MANIFEST_FILE)) {
    manifest_raw = fs.readFileSync(config.MANIFEST_FILE, {encoding:'utf8'});
    manifest_raw = selectUtils.normalizeManifest(manifest_raw);
    manifest = JSON.parse(manifest_raw);
    manifest.hash = crypto.hash(manifest_raw).toUpperCase();
}

if (!manifest) { // there is no manifest
    console.log('ERROR: Cannot find an election manifest file.');
    console.log('Server not started.');
    process.exit(1);
}

module.exports = manifest;
