package de.uni.trier.infsec.eVotingSystem.parser;

import static de.uni.trier.infsec.utils.Utilities.byteArrayToHexString;
import static de.uni.trier.infsec.utils.Utilities.hexStringToByteArray;

import org.json.JSONException;
import org.json.JSONObject;


public class PrivateKeysParser 
{
	public static String generateJSON(PrivateKeys prKeys) 
	{
		JSONObject jObj = generateJSONObject(prKeys);
		return jObj.toString(1);	
	}
	
	public static PrivateKeys parseJSONString(String stringJSON) throws JSONException 
	{
		JSONObject jMain=new JSONObject(stringJSON);
		return parseManifest(jMain);
	}
	
	private static final String sDecryptionKey="decryptionKey",
								sSignatureKey="signatureKey";
	
	private static JSONObject generateJSONObject(PrivateKeys prKeys)
	{
		JSONObject jMain=new JSONObject();
		jMain.put(sDecryptionKey, byteArrayToHexString(prKeys.decryptionKey));
		jMain.put(sSignatureKey, byteArrayToHexString(prKeys.signatureKey));
		return jMain;
	}
	
	
	private static PrivateKeys parseManifest(JSONObject jMain) throws JSONException
	{
		byte[] decryptionKey = hexStringToByteArray(jMain.getString(sDecryptionKey)); 
		byte[] signatureKey = hexStringToByteArray(jMain.getString(sSignatureKey));
		PrivateKeys prKeys = new PrivateKeys(decryptionKey, signatureKey);
		return prKeys;
	}
}