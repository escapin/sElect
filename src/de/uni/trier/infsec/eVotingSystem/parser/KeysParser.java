package de.uni.trier.infsec.eVotingSystem.parser;

import static de.uni.trier.infsec.utils.Utilities.byteArrayToHexString;
import static de.uni.trier.infsec.utils.Utilities.hexStringToByteArray;
import org.json.JSONException;
import org.json.JSONObject;


public class KeysParser 
{
	public static String generateJSON(Keys k) 
	{
		JSONObject jObj = generateJSONObject(k);
		return jObj.toString(1);	
	}
	
	public static Keys parseJSONString(String stringJSON) throws JSONException 
	{
		JSONObject jMain=new JSONObject(stringJSON);
		return parseKeyJSON(jMain);
	}
	private static final String 	sEncrKey="encryptionKey",
									sDecrKey="decryptionKey",
									sSignKey="signatureKey",
									sVerifKey="verificationKey";									
	
	private static JSONObject generateJSONObject(Keys k)
	{
		JSONObject jMain=new JSONObject();
		if(k.encrKey!=null)
			jMain.put(sEncrKey, byteArrayToHexString(k.encrKey));
		if(k.decrKey!=null)
			jMain.put(sDecrKey, byteArrayToHexString(k.decrKey));
		if(k.signKey!=null)		
			jMain.put(sSignKey, byteArrayToHexString(k.signKey));
		if(k.verifKey!=null)
			jMain.put(sVerifKey, byteArrayToHexString(k.verifKey));
		return jMain;
	}
	
	
	private static Keys parseKeyJSON(JSONObject jMain) throws JSONException
	{
		Keys k = new Keys();
		
		try{
			k.encrKey = hexStringToByteArray(jMain.getString(sEncrKey));
		} catch (JSONException e){}
		try{
			k.decrKey = hexStringToByteArray(jMain.getString(sDecrKey));
		} catch (JSONException e){}
		try{
			k.signKey = hexStringToByteArray(jMain.getString(sSignKey));
		} catch (JSONException e){}
		try{
			k.verifKey = hexStringToByteArray(jMain.getString(sVerifKey));
		} catch (JSONException e){}
		
		return k;
	}
}