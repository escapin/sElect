package de.uni.trier.infsec.eVotingSystem.apps;

import static de.uni.trier.infsec.eVotingSystem.apps.AppUtils.readCharsFromFile;
import static de.uni.trier.infsec.eVotingSystem.apps.AppUtils.storeAsFile;
import static de.uni.trier.infsec.eVotingSystem.core.Utils.errln;

import java.io.IOException;

import de.uni.trier.infsec.eVotingSystem.parser.Keys;
import de.uni.trier.infsec.eVotingSystem.parser.KeysParser;
import de.uni.trier.infsec.functionalities.digsig.Signer;
import de.uni.trier.infsec.utils.Utilities;

public class SignTheManifest
{
	public static void main(String[] args){
		
		// retrieve the JSON file
		String filename = AppParams.EL_MANIFEST_path + "ElectionManifest.json";;
		String sManifestJSON=""; 
		try {
			sManifestJSON = readCharsFromFile(filename);
		} catch (IOException e) {
			errln("Unable to access: " + filename);
		}
		
		// retrieve the signature key of the Election Authority
		filename = AppParams.PRIVATE_KEY_path + "ElectionAuthority_PR.json";
		String stringJSON="";
		try {
			stringJSON = readCharsFromFile(filename);
		} catch (IOException e) {
			errln("Unable to access: " + filename);
		}
		Keys k=KeysParser.parseJSONString(stringJSON);
		
		Signer sign = new Signer(k.verifKey, k.signKey);
		
		byte[] manifestSignature=sign.sign(sManifestJSON.getBytes());
		System.out.println("Manifest's signature: " + 
				Utilities.byteArrayToHexString(manifestSignature));

		// generate the Signature file
		filename=AppParams.EL_MANIFEST_path + "ElectionManifest.sig";
		try {
			storeAsFile(manifestSignature, filename);
		} catch (IOException e) {
			errln("Unable to access: " + filename);
		}
	}
}
