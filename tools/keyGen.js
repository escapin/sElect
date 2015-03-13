var crypto = require('cryptofunc');

var ek = crypto.pke_keygen();

// console.log(enckeys);

var sk = crypto.sig_keygen();

// console.log(sigkeys);

var keys = { encryptionKey: ek.encryptionKey, 
             decryptionKey: ek.decryptionKey,
             verificationKey: sk.verificationKey,
             signingKey: sk.signingKey
             }

console.log(keys);

