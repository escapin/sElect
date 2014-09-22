/**
 * 
 */
package de.uni.trier.infsec.tests;

import junit.framework.TestCase;

import org.json.JSONObject;
import org.json.XML;
import org.junit.Before;
import org.junit.Test;

import de.uni.trier.infsec.eVotingSystem.parser.Keys;
import de.uni.trier.infsec.eVotingSystem.parser.KeysParser;
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
		Keys k = new Keys();
		k.decrKey = decr.getDecryptionKey();
		k.encrKey = decr.getEncryptionKey();
		k.signKey = sign.getSignatureKey();
		k.verifKey = sign.getVerificationKey();
		
		
		String stringJSON = KeysParser.generateJSON(k);
		// PRINT the JSON
		System.out.println(stringJSON);
		
		Keys new_keys = KeysParser.parseJSONString(stringJSON);
		
		assertTrue(k.equals(new_keys));
		
		System.out.println();
		// PUBLIC KEYS
		Keys puKeys = new Keys();
		puKeys.encrKey = decr.getEncryptionKey();
		puKeys.verifKey = sign.getVerificationKey();
		
		stringJSON = KeysParser.generateJSON(puKeys);
		System.out.println(stringJSON);
		
		Keys new_puKeys = KeysParser.parseJSONString(stringJSON);
		assertTrue(puKeys.equals(new_puKeys));

	}

}
