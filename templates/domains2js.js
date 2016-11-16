// Convert json files to java script objects

var fs = require('fs');
var selectUtils = require('selectUtils');

var fileName = "trustedOrigins.json"
var varname = "trustedOrigins";

var ms = JSON.parse(fs.readFileSync(fileName, 'utf8'));
var authenticator = ms.Authenticator;
var authchannel = ms.authChannel;

var domains = "[";
for(var i = 0; i < authenticator.length; i++){
	domains = domains + '"'+authenticator[i]+'",';
}
domains = domains.slice(0, -1) + "];";
domains = "var "+varname+" = "+domains;
fs.writeFileSync("trustedOrigins_auth.js", domains);

var domains = "[";
for(var i = 0; i < authchannel.length; i++){
	domains = domains + '"'+authchannel[i]+'",';
}
domains = domains.slice(0, -1) + "];";
domains = "var "+varname+" = "+domains;
fs.writeFileSync("trustedOrigins_cs.js", domains);