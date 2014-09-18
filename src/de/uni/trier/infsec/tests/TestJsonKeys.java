/**
 * 
 */
package de.uni.trier.infsec.tests;

import junit.framework.TestCase;

import org.json.JSONObject;
import org.json.XML;
import org.junit.Before;
import org.junit.Test;

import de.uni.trier.infsec.eVotingSystem.parser.PrivateKeys;
import de.uni.trier.infsec.eVotingSystem.parser.PrivateKeysParser;
import de.uni.trier.infsec.eVotingSystem.parser.PublicKeys;
import de.uni.trier.infsec.eVotingSystem.parser.PublicKeysParser;
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
		
		String stringJSON = PrivateKeysParser.generateJSON(prKeys);
		// PRINT the JSON
		System.out.println(stringJSON);
		PrivateKeys new_prKeys = PrivateKeysParser.parseJSONString(stringJSON);
		
		assertTrue(prKeys.equals(new_prKeys));
		
		System.out.println();
		// PUBLIC KEYS
		PublicKeys puKeys = new PublicKeys(decr.getEncryptionKey(), sign.getVerificationKey());
		
		stringJSON = PublicKeysParser.generateJSON(puKeys);
		System.out.println(stringJSON);
		PublicKeys new_puKeys = PublicKeysParser.parseJSONString(stringJSON);
		
		assertTrue(puKeys.equals(new_puKeys));
	}

}
