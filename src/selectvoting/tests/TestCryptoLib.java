package selectvoting.tests;

import junit.framework.TestCase;

import org.junit.Test;

import selectvoting.lib.crypto.CryptoLib;
import selectvoting.utils.Utilities;
import static selectvoting.utils.MessageTools.equal;


public class TestCryptoLib extends TestCase {

	private static byte[] key = Utilities.hexStringToByteArray("dbd08d5190d1c0207450176b386b6f28");
	private static byte[] encrypted_message = Utilities.hexStringToByteArray("1693e24f26f49c096e7cf5e73800d5ded1aac5bbfc3ee9dc586a1244a2ce5a");
	private static byte[] encrypted_plaintext = Utilities.hexStringToByteArray("3f2267");


	private void log(byte[] b) {
		System.out.println(Utilities.byteArrayToHexString(b));
	}

	@Test
	public void testSymEnc() {
		byte[] plaintext = {0x3f,0x22};

		byte[] c = CryptoLib.symkey_encrypt(key, plaintext);	
		byte[] m = CryptoLib.symkey_decrypt(key, c);
		log(m);
		assertTrue( equal(plaintext,m) );

		byte[] m1 = CryptoLib.symkey_decrypt(key, encrypted_message);
		assertTrue( equal(m1, encrypted_plaintext));
		log(m1);
	}
}
