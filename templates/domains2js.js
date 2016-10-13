// Convert json files to java script objects

var fs = require('fs');
var selectUtils = require('selectUtils');

var fileName = "trustedDomains.json"
var varname = "trustedDomains";

var ms = JSON.parse(fs.readFileSync(fileName, 'utf8'));
var authenticator = ms.Authenticator;
var authchannel = ms.authChannel;

var domains = "[";
for(var i = 0; i < authenticator.length; i++){
	domains = domains + '"'+authenticator[i]+'",';
}
domains = domains.slice(0, -1) + "];";
domains = "var "+varname+" = "+domains;
fs.writeFileSync("../Authenticator/webapp/trustedDomains.js", domains);

var domains = "[";
for(var i = 0; i < authchannel.length; i++){
	domains = domains + '"'+authchannel[i]+'",';
}
domains = domains.slice(0, -1) + "];";
domains = "var "+varname+" = "+domains;
fs.writeFileSync("../CollectingServer/webapp/trustedDomains.js", domains);