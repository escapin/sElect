var fs = require('fs');
var crypto = require('cryptofunc');

var manifest_path = process.argv[2];
var config_path = process.argv[3];
var manifest = require(manifest_path);
var config = require(config_path);

function save_jsonObj(file, jsonObj) {
	fs.writeFile(file, JSON.stringify(jsonObj, null, 4), function (err) {
        if (err)
            console.log('Problems with saving the json object in ', file);
        else
            console.log('json object saved in', file);
    });
}

var ek = crypto.pke_keygen();
var sk = crypto.sig_keygen();
var keys = 	{ 	encryption_key: ek.encryptionKey,
             	decryption_key: ek.decryptionKey,
             	verification_key: sk.verificationKey,
             	signing_key: sk.signingKey
             	}
console.log(keys);
console.log();
var index=config.chainIndex;
if(index<0 || index>=manifest.mixServers.length){
	console.log("index of the mix server out of bound");
	process.exit(1);
}
// update the public keys
manifest.collectingServer.encryption_key=keys.encryption_key;
manifest.collectingServer.verification_key=keys.verification_key;

// update the private keys
config.decryption_key=keys.decryption_key;
config.signing_key=keys.signing_key;

// write the json obj in the files
save_jsonObj(manifest_path, manifest);
save_jsonObj(config_path, config);

