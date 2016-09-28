const path = require('path');
var fs = require('fs');
var ejs = require('ejs');

var manifest = JSON.parse(fs.readFileSync(path.join(__dirname, "../../ElectionManifest.json")));
var config = JSON.parse(fs.readFileSync(path.join(__dirname, "../../config.json")));

var compiled = ejs.compile(fs.readFileSync(path.join(__dirname, 'votingBooth.ejs'), 'utf8'));
var html = compiled({ seperateAuthentication: manifest.seperateAuthentication, randomness : manifest.userChosenRandomness, showOtp : config.showOtp });
fs.writeFileSync(path.join(__dirname, "../votingBooth.html"), html);

compiled = ejs.compile(fs.readFileSync(path.join(__dirname, 'selectBooth.ejs'), 'utf8'));
html = compiled({ seperateAuthentication: manifest.seperateAuthentication, randomness : manifest.userChosenRandomness, showOtp : config.showOtp });
fs.writeFileSync(path.join(__dirname, "../js/selectBooth.js"), html);
