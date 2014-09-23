package de.uni.trier.infsec.eVotingSystem.apps;

import static de.uni.trier.infsec.eVotingSystem.apps.AppUtils.setupPrivateKeys;
import static de.uni.trier.infsec.eVotingSystem.apps.AppUtils.setupPublicKeys;

import java.io.IOException;

import de.uni.trier.infsec.eVotingSystem.parser.Keys;
import de.uni.trier.infsec.functionalities.digsig.Signer;
import de.uni.trier.infsec.functionalities.pkenc.Decryptor;

/**
 * Registers the collecting server (using its id from the Params).
 */
public class SetupCollectingServer 
{
	public static void main(String[] args) {	
		String name="CollectingServer";
		
		Decryptor decr = new Decryptor();
		Signer sign = new Signer();
		
		Keys k = new Keys();
		k.encrKey=decr.getEncryptionKey();
		k.decrKey=decr.getDecryptionKey();
		k.signKey=sign.getSignatureKey();
		k.verifKey=sign.getVerificationKey();
		
		
		String filename =  AppParams.PRIVATE_KEY_path + name + "_PR.json";
		try {
			setupPrivateKeys(k, filename);
		} catch (IOException e) {
			System.err.println("Unable to access: " + filename);
			System.exit(-1);
		}
		
		filename =  AppParams.PUBLIC_KEY_path + name + "_PU.json";
		String publicKeys=null;
		try {
			publicKeys = setupPublicKeys(k, filename);
		} catch (IOException e) {
			System.err.println("Unable to access: " + filename);
			System.exit(-1);
		}
		
		System.out.println(name + "'s public keys:");
		System.out.println(publicKeys);
		System.out.println("The public keys have been saved in: \n" + filename);
	}
}