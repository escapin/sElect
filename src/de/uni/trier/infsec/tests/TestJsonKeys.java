/**
 * 
 */
package de.uni.trier.infsec.tests;

import junit.framework.TestCase;

import org.json.JSONObject;
import org.json.XML;
import org.junit.Before;
import org.junit.Test;

import de.uni.trier.infsec.eVotingSystem.parser.KeyPair;
import de.uni.trier.infsec.eVotingSystem.parser.KeysParser;
import de.uni.trier.infsec.eVotingSystem.parser.PrivateKeys;
import de.uni.trier.infsec.eVotingSystem.parser.PublicKeys;
import de.uni.trier.infsec.functionalities.digsig.Signer;
import de.uni.trier.infsec.functionalities.pkenc.Decryptor;

/**
 * @author scapin
 *
 */
public class TestJsonKeys extends TestCase
{
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception{}

	@Test
	public void test() throws Exception
	{
		Decryptor decr = new Decryptor();
		Signer sign = new Signer();
		
		// PRIVATE KEYS
		PrivateKeys prKeys = new PrivateKeys(decr.getDecryptionKey(),sign.getSignatureKey());
		
		String stringJSON = KeysParser.generateJSON(prKeys);
		// PRINT the JSON
		System.out.println(stringJSON);
		PrivateKeys new_prKeys = (PrivateKeys) KeysParser.parseJSONString(stringJSON);
		
		assertTrue(prKeys.equals(new_prKeys));
		
		System.out.println();
		// PUBLIC KEYS
		PublicKeys puKeys = new PublicKeys(decr.getEncryptionKey(), sign.getVerificationKey());
		
		stringJSON = KeysParser.generateJSON(puKeys);
		System.out.println(stringJSON);
		PublicKeys new_puKeys = (PublicKeys) KeysParser.parseJSONString(stringJSON);
		
		assertTrue(puKeys.equals(new_puKeys));
		
		prKeys = new PrivateKeys(null, sign.getSignatureKey());
		//prKeys = new PrivateKeys(decr.getDecryptionKey(), null);
		stringJSON = KeysParser.generateJSON(prKeys);
		System.out.println(stringJSON);
		new_prKeys = (PrivateKeys) KeysParser.parseJSONString(stringJSON);
		assertTrue(prKeys.equals(new_prKeys));

		puKeys = new PublicKeys(null, sign.getVerificationKey());
		//puKeys = new PublicKeys(decr.getEncryptionKey(), null);
		stringJSON = KeysParser.generateJSON(puKeys);
		System.out.println(stringJSON);
		new_puKeys = (PublicKeys) KeysParser.parseJSONString(stringJSON);
		assertTrue(puKeys.equals(new_puKeys));
	}

}
