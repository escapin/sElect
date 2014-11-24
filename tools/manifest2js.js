var fs = require('fs');
var selectUtils = require('selectUtils');

if (process.argv.length !==  3) {
    console.log('ERROR: Call the script with one argument, a file name with an election manifest:');
    console.log('node manifest2js <manifest-file-name.json>');
    process.exit(1);
}

var manifestFileName = process.argv[2];

var ms = fs.readFileSync(manifestFileName, 'utf8');
var norm = selectUtils.normalizeManifest(ms);
var norm = norm.replace(/"/g, '\\"')
console.log('var electionManifestRaw = "%s";', norm);
