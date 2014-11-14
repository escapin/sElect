var forge = require('node-forge');
var crypto = require('cryptofunc');

var hexToBytes = forge.util.hexToBytes;
var bytesToHex = forge.util.bytesToHex;

describe( 'Conatenation and deconcatenation', function()
{
    it( 'are consistent', function()
    {
        var a = 'ffaa77';
        var b = 'cc9911227733';
        var p = crypto.deconcatenate(crypto.concatenate(a, b));
        expect (p.first) .toBe(a);
        expect (p.second).toBe(b);
    });
});


describe( 'Symmetric encryption', function()
{
    it( 'encrypts and then decrypts correctly', function()
    {
        var m = bytesToHex(forge.random.getBytesSync(1000)); // a random 1000-byte message
        var k = crypto.symkeygen();
        var c = crypto.symenc(k, m);
        var d = crypto.symdec(k, c);
        expect(d).toBe(m);
    });
});


describe( 'PKE Encryption', function()
{
    it( 'encrypts and then decrypts correctly', function()
    {
        var m = bytesToHex(forge.random.getBytesSync(80)); // a random 80-byte message
        var k = crypto.rsa_keygen();
        var c = crypto.rsa_encrypt(k.encryptionKey, m);
        var d = crypto.rsa_decrypt(k.decryptionKey, c);
        expect(d).toBe(m);
    });

    it( 'works correctly for some fixtures', function()
    {
        // var encryption_key = "30819F300D06092A864886F70D010101050003818D0030818902818100B21E1FA56085DFEF9DA015A731CA2243FFF2A6354CD6C3AC5210C9D047702908A876F4E822A35A097BF0D8E0397A1B9C3F7BB4A055239E3F67500A707A3B5659FBCA35A1CEFFC251D72BE04F313A4B11451845E01F3A30B18546A521B268772051BC2ADC22EBDA6B9ECE530460A6DFE8818B1F53363E5C91BB7BA450C21AFCE90203010001";
        var decryption_key = "30820278020100300D06092A864886F70D0101010500048202623082025E02010002818100B21E1FA56085DFEF9DA015A731CA2243FFF2A6354CD6C3AC5210C9D047702908A876F4E822A35A097BF0D8E0397A1B9C3F7BB4A055239E3F67500A707A3B5659FBCA35A1CEFFC251D72BE04F313A4B11451845E01F3A30B18546A521B268772051BC2ADC22EBDA6B9ECE530460A6DFE8818B1F53363E5C91BB7BA450C21AFCE902030100010281807401E2A297671A1EBA0ED58B7B8627231AC433346BC344D62AECFC444702E9F6D5A204885C66FFF14563EC1CBDD2A5C0F227E3D0B922E5A26DEB57A1423AFB55B128D0A4289E27D0510CDCCAF268EC471B2FDC8F8A2C270B82BB0FD115A5DF1AFECE4680A64F6F62E64BA515F03E9C5FF891F0832DC2F6103DE02D1915C1DCF1024100E53FC931375907C421471E9A02518543AC4A521E56346586C8D4E7BC3C22F55E6F485781AE23F8A6C904D936147D3EE78FC0674D275D833ED5C1E3E9BA323CEB024100C6E6EB5184781CF25E5273FAFFE9C39EACD7B1986F0356DD3CA8226B1D6AF9A1A77A0E22CB3DBC60C920FEF75C6071643C07BE59B2D09BCB292F05A79E99287B024100BF8255B483A42054BBE809AC669B6B54692D7D0452C75AB90A34B192123AB1F7BDC71533042290A9E3EBE4F8C48D0C6BAD2EF21D05F19C9E753B9005C4C20B19024100B7D0B46C5376059A5F5CE7DE711F022FE42039FA5BADC45B1531750D74D465FAE521C16A9A55658034A00FC15E57AAB32D5F22A516C1FF1893E8E6DAEF912F7D024100BA4216E24F08F731F0DAF2566CB538954148CAEB9DA3F9667A0A421F7D5739B39FD8E0CA8FD41FA1F28559783AAFB15CC542BBC29ACD955D4F02A1F30C90A007";

        var m = '3f33';
        var c = "70E6A5B721F707203ED4409B9956E95E8203F2C9AD73DB2284934E44174A7521C58B345E5516733AB86938EA51D33B72D291D4D9D516F17E55B6490A50F04CE8112805FBE754509BCD54E25B5224C8D0BA45F64FAFDE996CDAC35FBF9095C0CD426276648FB93FE71260021BF8B5D8927907D95300DDD6FC96969FE8D9305644";

        var d = crypto.rsa_decrypt(decryption_key, c);
        expect(d).toBe(m);
    });

    it( 'works consistently with the native node-forge encryption/decryption', function()
    {
        var keypair = forge.rsa.generateKeyPair({bits: 1024, e: 0x10001});
        var encryptionKey = crypto.publicKeyToHex(keypair.publicKey);
        var decryptionKey = crypto.privateKeyToHex(keypair.privateKey);

        var m = '77bb88ff';
        var b = hexToBytes(m);

        var c1 = keypair.publicKey.encrypt(b); // encrypt directly in forge
        var d1 = keypair.privateKey.decrypt(c1); // decrypt directly in forge
        expect (bytesToHex(d1)).toBe(m);

        var c2 = crypto.rsa_encrypt(encryptionKey, m); // encrypt in cryptofunc
        var d2 = crypto.rsa_decrypt(decryptionKey, c2); // decrypt in cryptofunc
        expect (d2).toBe(m);

        var d3 = crypto.rsa_decrypt(decryptionKey, bytesToHex(c1));
        expect (d3).toBe(m);
    });
});


describe( 'Hybrid encryption', function()
{
    it( 'encrypts and then decrypts correctly messages', function()
    {
        var m = bytesToHex(forge.random.getBytesSync(1000)); // a random message
        var k = crypto.pke_keygen();
        var c = crypto.pke_encrypt(k.encryptionKey, m);
        var d = crypto.pke_decrypt(k.decryptionKey, c);
        var res = (d===m)
        expect(res).toBe(true);
    });
});

/*
describe( '', function()
{
    it( '', function()
    {

    });
});
*/

