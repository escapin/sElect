var forge = require('node-forge');
var crypto = require('cryptofunc');
var voterClient = require('voterClient');
var strHexConversion = require('strHexConversion');

var hexToBytes = forge.util.hexToBytes;
var bytesToHex = forge.util.bytesToHex;
var pair = crypto.concatenate;
var enc  = crypto.pke_encrypt;
var dec  = crypto.pke_decrypt;

/////////////////////////////////////////////////////////////////

// Fix some parameters and keys 
var TAG_ACCEPTED = "00"; // (hex encoded) tag 
var TAG_ACCEPTED1 = "02"; // (hex encoded) tag 
var electionID = '1441'
var colServerKey = crypto.sig_keygen();
var colServVerifKey = colServerKey.verificationKey;
var mixServKeys = [ crypto.pke_keygen(), crypto.pke_keygen(), crypto.pke_keygen(), crypto.pke_keygen()  ];
var mixServEncKeys = mixServKeys.map(function(k){ return k.encryptionKey; });

/////////////////////////////////////////////////////////////////

describe( 'Voter Client', function()
{
    it( 'works as expected', function()
    {
        // Create the voter
        var voter = voterClient.create(electionID, colServVerifKey, mixServEncKeys);

        // Create a ballot: [choice, userCode]
        var choice = 3;
        var userCode = "!@#$%^&*(";
        var receipt = voter.createBallot(choice, userCode);

        // Create the signature of the collecting server
        var message = pair(TAG_ACCEPTED, pair(electionID, receipt.ballot));
        var signature = crypto.sign(colServerKey.signingKey, message);

        // Complete the receipt by the signature
        receipt.signature = signature;

        // Verify the receipt
        var receiptOK = voter.validateReceipt(receipt);
        expect(receiptOK).toBe(true);

        // Re-create the ballot using the randomness in the receipt:
        var choiceMsg = crypto.int32ToHexString(choice);
        var userCodeMsg = strHexConversion.hexEncode(userCode);
        var N = mixServEncKeys.length;
        var x = pair(electionID, pair(userCodeMsg, pair(receipt.receiptID, choiceMsg)));
        for (var i=N-1; i>=0; --i) {
        	x = enc(mixServEncKeys[i], pair(electionID, x), receipt.randomCoins[i]);
            expect(x).toBe(receipt.ciphertexts[i]);
        }

        // Fake the decryption process (done by the mix servers)
        x = receipt.ballot;
        for (var i=0; i<N; ++i) {
            expect(x).toBe(receipt.ciphertexts[i]);
            x = dec(mixServKeys[i].decryptionKey, x);
            expect(x).not.toBe(null);
            var p = crypto.deconcatenate(x);
            expect(p.first).toBe(electionID);
            x = p.second;
        }
        // Check the plaintexts
        p = crypto.deconcatenate(x);
        expect(p.first).toBe(electionID);
        p = crypto.deconcatenate(p.second);
        expect(strHexConversion.hexDecode(p.first)).toBe(userCode);
        p = crypto.deconcatenate(p.second);
        expect(crypto.hexStringToInt(p.second)).toBe(choice);

        //console.log(receipt);
    });
});


