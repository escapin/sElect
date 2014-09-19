package de.uni.trier.infsec.eVotingSystem.parser;

import static de.uni.trier.infsec.utils.Utilities.byteArrayToHexString;
import static de.uni.trier.infsec.utils.Utilities.hexStringToByteArray;
import org.json.JSONException;
import org.json.JSONObject;


public class KeysParser 
{
	public static String generateJSON(KeyPair k) 
	{
		JSONObject jObj = generateJSONObject(k);
		return jObj.toString(1);	
	}
	
	public static KeyPair parseJSONString(String stringJSON) throws JSONException 
	{
		JSONObject jMain=new JSONObject(stringJSON);
		return parseKeyJSON(jMain);
	}
	private static final String 	sEncryptionKey="encryptionKey",
									sDecryptionKey="decryptionKey",
									sVerificationKey="verificationKey",
									sSignatureKey="signatureKey";
	
	private static JSONObject generateJSONObject(KeyPair k)
	{
		String sPkencKey =
				(k instanceof PublicKeys)? sEncryptionKey: sDecryptionKey;
		String sDigsigKey = 
				(k instanceof PublicKeys)? sVerificationKey: sSignatureKey;
		
		JSONObject jMain=new JSONObject();
		if(k.pkencKey!=null)
			jMain.put(sPkencKey, byteArrayToHexString(k.pkencKey));
		if(k.digsigKey!=null)
			jMain.put(sDigsigKey, byteArrayToHexString(k.digsigKey));
		return jMain;
	}
	
	
	private static KeyPair parseKeyJSON(JSONObject jMain) throws JSONException
	{
		byte[]	pkencKey=null,
				digsigKey=null;
		
		try{
			pkencKey = hexStringToByteArray(jMain.getString(sEncryptionKey));
		} catch (JSONException e){}
		try{
			digsigKey = hexStringToByteArray(jMain.getString(sVerificationKey));
		} catch (JSONException e){}
		
		if(pkencKey!=null || digsigKey!=null)
			return new PublicKeys(pkencKey, digsigKey);
		else{
			try{
				pkencKey = hexStringToByteArray(jMain.getString(sDecryptionKey));
			} catch (JSONException e){}
			try{
				digsigKey = hexStringToByteArray(jMain.getString(sSignatureKey));
			} catch (JSONException e){}
			return new PrivateKeys(pkencKey, digsigKey);
		}
	}
}