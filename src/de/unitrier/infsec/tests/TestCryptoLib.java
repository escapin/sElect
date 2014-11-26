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
	private static byte[] rsa_encrypted = hexStringToByteArray("13321266d18162cac83f34e80cb7137477f14d5c7da629be30baad1baba33b7d463a601a5a5a549b22ba9e48b93501cd03cd07a47f287392f3af5f85a6d30ccb016c0245c273cb371093937adb4b89216265134b4f8659c9722159a74c47a63d33e3342a082c2479a4acbd6c2b36d71ec69b886941e309e431ad41dd45691b1b");
	private static byte[] pk_encrypted = hexStringToByteArray("000000806efc22a799ddfec610205139ffabbc5cfa9896fa689db251f5319dd376cbea4f4eded6013e2a10ae09dc2b879e8391cfeb6e59461344959cb3d0391ea2c7686a38532a05cec42304452f166993af4fe98ccf0c5129ded7bb0d672293f20cdffbd75d6b5a7f0004da53862c2ccf44e531dc64682ef7b0363d59e85a0d53161443347a2588b03e39b5c2206d04305b1e06fb1f835c68a971088a39e47344d7");

	
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
		log(signature);		
		verified = CryptoLib.verify(message, signature, public_key);
		assertTrue( verified );
		verified = CryptoLib.verify(message1, signature, public_key);
		assertFalse( verified );
		
	}
	
}
