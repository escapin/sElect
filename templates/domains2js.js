// Convert json files to java script objects

var fs = require('fs');
var selectUtils = require('selectUtils');

var fileName = "trustedDomains.json"
var varname = "trustedDomainsRaw";

var ms = JSON.parse(fs.readFileSync(fileName, 'utf8'));
var authenticator = JSON.stringify({temp: ms.Authenticator}).replace('{"temp":', "").slice(0, -1);
var authchannel = JSON.stringify({temp: ms.authChannel}).replace('{"temp":', "").slice(0, -1);

var norm = selectUtils.normalizeManifest(authenticator);
norm = norm.replace(/\\/g, '\\\\') // Escape backslashes ( \ -> \\ )
norm = norm.replace(/"/g, '\\"') // Escape Quotes ( " -> \" )
norm = "var "+varname+" = "+'"'+norm+'"';
fs.writeFileSync("../Authenticator/webapp/trustedDomain.js", norm);

var norm = selectUtils.normalizeManifest(authchannel);
norm = norm.replace(/\\/g, '\\\\') // Escape backslashes ( \ -> \\ )
norm = norm.replace(/"/g, '\\"') // Escape Quotes ( " -> \" )
norm = "var "+varname+" = "+'"'+norm+'"';
fs.writeFileSync("../CollectingServer/webapp/trustedDomain.js", norm);