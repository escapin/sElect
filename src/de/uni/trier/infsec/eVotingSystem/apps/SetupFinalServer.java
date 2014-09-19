package de.uni.trier.infsec.eVotingSystem.apps;

import static de.uni.trier.infsec.eVotingSystem.apps.AppUtils.setupKeys;
import de.uni.trier.infsec.eVotingSystem.parser.PrivateKeys;
import de.uni.trier.infsec.eVotingSystem.parser.PublicKeys;
import de.uni.trier.infsec.functionalities.digsig.Signer;
import de.uni.trier.infsec.functionalities.pkenc.Decryptor;

/**
 * Registers the collecting server (using its id from the Params).
 */
public class SetupFinalServer {
	
	public static void main(String[] args) {	
		String name="FinalServer";
		Decryptor decr = new Decryptor();
		Signer sign = new Signer();
		
		String filename;
		PrivateKeys prKeys = new PrivateKeys(decr.getDecryptionKey(),sign.getSignatureKey());
		filename =  AppParams.PATH_STORAGE + name + "_PR.json";
		setupKeys(prKeys, filename);
		
		PublicKeys puKeys = new PublicKeys(decr.getEncryptionKey(), sign.getVerificationKey());
		filename =  AppParams.PATH_STORAGE + name + "_PU.json";
		
		System.out.println(name + "'s public keys:");
		System.out.println(setupKeys(puKeys, filename));
		System.out.println("The public keys have been saved in: \n" + filename);
	}
}
