var fs = require('fs');

var configFileName = 'config.json';

var config = null;

console.log('Read configuration file');
if (fs.existsSync(configFileName)) {
    config_raw = fs.readFileSync(configFileName, {encoding:'utf8'});
    config = JSON.parse(config_raw);
}

if (!config) { // there is no manifest
    console.log('ERROR: Cannot find the configuration file.');
    console.log('Server not started.');
    process.exit(1);
}

module.exports = config;
