package de.unitrier.infsec.tests;

import junit.framework.TestCase;

import org.junit.Test;

import de.unitrier.infsec.lib.crypto.CryptoLib;
import de.unitrier.infsec.lib.crypto.KeyPair;
import de.unitrier.infsec.utils.Utilities;
import static de.unitrier.infsec.utils.MessageTools.equal;
import static de.unitrier.infsec.utils.Utilities.hexStringToByteArray;


public class TestCryptoLib extends TestCase {

	private static byte[] key = hexStringToByteArray("dbd08d5190d1c0207450176b386b6f28");
	private static byte[] encrypted_message = hexStringToByteArray("1693e24f26f49c096e7cf5e73800d5ded1aac5bbfc3ee9dc586a1244a2ce5a");
	private static byte[] encrypted_plaintext = hexStringToByteArray("3f2267");

	private static byte[] public_key =  hexStringToByteArray("30819F300D06092A864886F70D010101050003818D0030818902818100B21E1FA56085DFEF9DA015A731CA2243FFF2A6354CD6C3AC5210C9D047702908A876F4E822A35A097BF0D8E0397A1B9C3F7BB4A055239E3F67500A707A3B5659FBCA35A1CEFFC251D72BE04F313A4B11451845E01F3A30B18546A521B268772051BC2ADC22EBDA6B9ECE530460A6DFE8818B1F53363E5C91BB7BA450C21AFCE90203010001");
	private static byte[] private_key =  hexStringToByteArray("30820278020100300D06092A864886F70D0101010500048202623082025E02010002818100B21E1FA56085DFEF9DA015A731CA2243FFF2A6354CD6C3AC5210C9D047702908A876F4E822A35A097BF0D8E0397A1B9C3F7BB4A055239E3F67500A707A3B5659FBCA35A1CEFFC251D72BE04F313A4B11451845E01F3A30B18546A521B268772051BC2ADC22EBDA6B9ECE530460A6DFE8818B1F53363E5C91BB7BA450C21AFCE902030100010281807401E2A297671A1EBA0ED58B7B8627231AC433346BC344D62AECFC444702E9F6D5A204885C66FFF14563EC1CBDD2A5C0F227E3D0B922E5A26DEB57A1423AFB55B128D0A4289E27D0510CDCCAF268EC471B2FDC8F8A2C270B82BB0FD115A5DF1AFECE4680A64F6F62E64BA515F03E9C5FF891F0832DC2F6103DE02D1915C1DCF1024100E53FC931375907C421471E9A02518543AC4A521E56346586C8D4E7BC3C22F55E6F485781AE23F8A6C904D936147D3EE78FC0674D275D833ED5C1E3E9BA323CEB024100C6E6EB5184781CF25E5273FAFFE9C39EACD7B1986F0356DD3CA8226B1D6AF9A1A77A0E22CB3DBC60C920FEF75C6071643C07BE59B2D09BCB292F05A79E99287B024100BF8255B483A42054BBE809AC669B6B54692D7D0452C75AB90A34B192123AB1F7BDC71533042290A9E3EBE4F8C48D0C6BAD2EF21D05F19C9E753B9005C4C20B19024100B7D0B46C5376059A5F5CE7DE711F022FE42039FA5BADC45B1531750D74D465FAE521C16A9A55658034A00FC15E57AAB32D5F22A516C1FF1893E8E6DAEF912F7D024100BA4216E24F08F731F0DAF2566CB538954148CAEB9DA3F9667A0A421F7D5739B39FD8E0CA8FD41FA1F28559783AAFB15CC542BBC29ACD955D4F02A1F30C90A007");
	private static byte[] rsa_encrypted = hexStringToByteArray("8a410a36675b34c778280dc892ed3c8a40626aceaa9991fe1888a2b5a77da45d84363372f496495c1f8dfa636b42fc92eb5f1e507f989e6f27cd827623dbf4c25cd1f42fff930b64082d04a8ad14402feb49761dd96644d7ebfea4f508f483eeeda6bf3da1a5691a29a51f87e41774d1dfdcac577b4417f73ce4e73b0d3ae559");
			
	private static byte[] pk_encrypted = hexStringToByteArray("0000008071cbe711e2d086f6ff589db11da84685f2d1d10105d75c5fd2683642399fdd3fb5ddce8750ffc4464b124ef94dedf238b19e24fbefee37d00fe498e464faffb687b7a473718802d4de8e8cd6da0189937ddb63b728d2e1645b6b8ead8510520ef6ccf80950427255804c370ab5294c0489c850850790c013d75e205e86dc5bb17d31558be8dcd0d32991f5201d8c1bbf61854c26a17dba89e6d00c93d4b7");
	
	private void log(byte[] b) {
		System.out.println(Utilities.byteArrayToHexString(b));
	}

	@Test
	public void testSymEnc() {
		byte[] plaintext = {0x3f,0x22};

		byte[] c = CryptoLib.symkey_encrypt(key, plaintext);	
		byte[] m = CryptoLib.symkey_decrypt(key, c);
		// log(m);
		assertTrue( equal(plaintext,m) );

		byte[] m1 = CryptoLib.symkey_decrypt(key, encrypted_message);
		assertTrue( equal(m1, encrypted_plaintext));
		// log(m1);
	}

	@Test
	public void testRawPKE() {
		byte[] plaintext = {0x3f,0x33};

		byte[] c = CryptoLib.just_pke_encrypt(plaintext, public_key);
		// log(c);
		byte[] m = CryptoLib.just_pke_decrypt(c, private_key);
		// log(m);
		assertTrue( equal(plaintext,m) );

		byte[] m1 = CryptoLib.just_pke_decrypt(rsa_encrypted, private_key);
		// log(m1);
		assertTrue( equal(plaintext,m1) );
	}

	@Test
	public void testPKE() {
		byte[] plaintext = {0x3f,0x37};

		byte[] c = CryptoLib.pke_encrypt(plaintext, public_key);
		// log(c);
		byte[] m = CryptoLib.pke_decrypt(c, private_key);
		// log(m);
		assertTrue( equal(plaintext,m) );
		
		byte[] d = CryptoLib.pke_decrypt(pk_encrypted, private_key);
		assertTrue( equal(plaintext,d) );
	}

	@Test
	public void testSignatures() {
		KeyPair k = CryptoLib.generateSignatureKeyPair();
		byte[] message = {0x3f,0x22, 0x1f};
		byte[] message1 = {0x3f,0x22, 0x1e};
		byte[] signature = CryptoLib.sign(message, k.privateKey);
		boolean verified = CryptoLib.verify(message, signature, k.publicKey);
		assertTrue( verified );
		verified = CryptoLib.verify(message1, signature, k.publicKey);
		assertFalse( verified );
		
		signature = CryptoLib.sign(message, private_key);
		// log(signature);		
		verified = CryptoLib.verify(message, signature, public_key);
		assertTrue( verified );
		verified = CryptoLib.verify(message1, signature, public_key);
		assertFalse( verified );
		
	}
	
}
