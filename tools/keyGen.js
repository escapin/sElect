var crypto = require('cryptofunc');


var keysMapsNumber=1

if(process.argv.length > 2)
	keysMapsNumber = process.argv[2];

var keysMapsArray = [];

for(var i = 0; i<keysMapsNumber; i++){

	var ek = crypto.pke_keygen();
	var sk = crypto.sig_keygen();

	keysMapsArray[i] = { encryptionKey: ek.encryptionKey,
					decryptionKey: ek.decryptionKey,
					verificationKey: sk.verificationKey,
					signingKey: sk.signingKey
             		}
	console.log(JSON.stringify(keysMapsArray[i]));
}



