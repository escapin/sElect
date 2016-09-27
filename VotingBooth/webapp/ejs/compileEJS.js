var fs = require('fs');
var ejs = require('ejs');

var manifest = JSON.parse(fs.readFileSync("../../ElectionManifest.json"));
var config = JSON.parse(fs.readFileSync("../../config.json"));

var compiled = ejs.compile(fs.readFileSync('votingBooth.ejs', 'utf8'));
var html = compiled({ seperateAuthentication: manifest.seperateAuthentication, randomness : manifest.userChosenRandomness, showOtp : config.showOtp });
fs.writeFileSync("../votingBooth.html", html);

compiled = ejs.compile(fs.readFileSync('selectBooth.ejs', 'utf8'));
html = compiled({ seperateAuthentication: manifest.seperateAuthentication, randomness : manifest.userChosenRandomness, showOtp : config.showOtp });
fs.writeFileSync("../js/selectBooth.js", html);