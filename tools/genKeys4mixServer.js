var fs = require('fs');
var crypto = require('cryptofunc');

var manifest_path = process.argv[2];
var config_path = process.argv[3];
var URI = process.argv[4]; // optional

var manifest = JSON.parse(fs.readFileSync(manifest_path, 'utf8'));
var config = JSON.parse(fs.readFileSync(config_path, 'utf8'));
//var manifest = require(manifest_path);
//var config = require(config_path);

function save_jsonObj(file, jsonObj) {
	fs.writeFile(file, JSON.stringify(jsonObj, null, 4), function (err) {
        if (err)
            console.log('Problems with saving the json object in ', file);
        else
            console.log('json object saved in', file);
    });
}

//lookup in the manifest the corresponding mix server
var index=-1;
if(URI !== undefined){
	for(var i=0; i<manifest.mixServers.length; ++i)
		if(URI === manifest.mixServers[i].URI){
			index = i;
			break;
		}
} else if (config.encryption_key !== undefined){ 
	for(var i=0; i<manifest.mixServers.length; ++i){
		if(config.encryption_key === manifest.mixServers[i].encryption_key){
			index = i;
			break;
		}
	}
} else if (config.signing_key !== undefined){
	for(var i=0; i<manifest.mixServers.length; ++i)
		if(config.signing_key === manifest.mixServers[i].signing_key){
			index = i;
			break;
		}
}
if (index < 0 || index>=manifest.mixServers.length){
	console.log('ERROR: impossible to look the correct mix server up.');
	console.log('Please specify at least the URI in the mix server\'s list in the election manifest and give it as third argument.');
	process.exit(1);
}

// generate new keys
var ek = crypto.pke_keygen();
var sk = crypto.sig_keygen();
var keys = 	{ 	encryption_key: ek.encryptionKey,
				decryption_key: ek.decryptionKey,
				verification_key: sk.verificationKey,
				signing_key: sk.signingKey
             	}
console.log(keys);
console.log();

// update config
config.decryption_key=keys.decryption_key;
config.signing_key=keys.signing_key;

config.encryption_key=keys.encryption_key;
config.verification_key=keys.verification_key;

// update manifest
manifest.mixServers[index].encryption_key=keys.encryption_key;
manifest.mixServers[index].verification_key=keys.verification_key;



// write the json obj in the files
save_jsonObj(manifest_path, manifest);
save_jsonObj(config_path, config);

