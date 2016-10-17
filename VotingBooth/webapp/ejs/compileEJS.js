const path = require('path');
var fs = require('fs');
var ejs = require('ejs');

var config = JSON.parse(fs.readFileSync(path.join(__dirname, "../../config.json")));
var useRadioButtons = config.maxChoicesPerVoter === 1;

var compiled = ejs.compile(fs.readFileSync(path.join(__dirname, 'votingBooth.ejs'), 'utf8'));
var html = compiled({ seperateAuthentication: config.seperateAuthentication, randomness : config.userChosenRandomness, showOtp : config.showOtp, radioButtons: useRadioButtons});
fs.writeFileSync(path.join(__dirname, "../votingBooth.html"), html);

compiled = ejs.compile(fs.readFileSync(path.join(__dirname, 'selectBooth.ejs'), 'utf8'));
var js = compiled({ seperateAuthentication: config.seperateAuthentication, randomness : config.userChosenRandomness, showOtp : config.showOtp });
fs.writeFileSync(path.join(__dirname, "../js/selectBooth.js"), js);
