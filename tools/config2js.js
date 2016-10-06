// Convert json files to java script objects

var fs = require('fs');
var selectUtils = require('selectUtils');

if (process.argv.length !==  4) {
    console.log('ERROR: Call the script with two arguments, the json file you want to convert and' + 
    			' the target variable name: ');
    console.log('node config2js <jsonFile.json> <targetVarName>');
    process.exit(1);
}

var manifestFileName = process.argv[2];
var varname = process.argv[3];

var ms = fs.readFileSync(manifestFileName, 'utf8');
var norm = selectUtils.normalizeManifest(ms);
norm = norm.replace(/\\/g, '\\\\') // Escape backslashes ( \ -> \\ )
norm = norm.replace(/"/g, '\\"') // Escape Quotes ( " -> \" )
console.log('var %s = "%s";', varname,norm);