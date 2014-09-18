package de.uni.trier.infsec.eVotingSystem.parser;

import static de.uni.trier.infsec.utils.Utilities.byteArrayToHexString;
import static de.uni.trier.infsec.utils.Utilities.hexStringToByteArray;

import org.json.JSONException;
import org.json.JSONObject;


public class PublicKeysParser 
{
	public static String generateJSON(PublicKeys puKeys) 
	{
		JSONObject jObj = generateJSONObject(puKeys);
		return jObj.toString(1);	
	}
	
	public static PublicKeys parseJSONString(String stringJSON) throws JSONException 
	{
		JSONObject jMain=new JSONObject(stringJSON);
		return parseManifest(jMain);
	}
	
	private static final String sEncryptionKey="encryptionKey",
								sVerificationKey="verificationKey";
	
	private static JSONObject generateJSONObject(PublicKeys puKeys)
	{
		JSONObject jMain=new JSONObject();
		jMain.put(sEncryptionKey, byteArrayToHexString(puKeys.encryptionKey));
		jMain.put(sVerificationKey, byteArrayToHexString(puKeys.verificationKey));
		return jMain;
	}
	
	
	private static PublicKeys parseManifest(JSONObject jMain) throws JSONException
	{
		byte[] decryptionKey = hexStringToByteArray(jMain.getString(sEncryptionKey)); 
		byte[] signatureKey = hexStringToByteArray(jMain.getString(sVerificationKey));
		PublicKeys puKeys = new PublicKeys(decryptionKey, signatureKey);
		return puKeys;
	}
}