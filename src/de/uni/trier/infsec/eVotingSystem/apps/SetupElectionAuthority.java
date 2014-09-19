package de.uni.trier.infsec.eVotingSystem.apps;

import static de.uni.trier.infsec.eVotingSystem.apps.AppUtils.setupKeys;
import de.uni.trier.infsec.eVotingSystem.parser.PrivateKeys;
import de.uni.trier.infsec.eVotingSystem.parser.PublicKeys;
import de.uni.trier.infsec.functionalities.digsig.Signer;

public class SetupElectionAuthority
{
	public static void main(String[] args) {	
		String name = "ElectionAuthority";

		Signer sign = new Signer();
		
		String filename;
		PrivateKeys prKeys = new PrivateKeys(null,sign.getSignatureKey());
		filename =  AppParams.PATH_STORAGE + name + "_PR.json";
		setupKeys(prKeys, filename);
		
		PublicKeys puKeys = new PublicKeys(null, sign.getVerificationKey());
		filename =  AppParams.PATH_STORAGE + name + "_PU.json";
		
		System.out.println(name + "'s public keys:");
		System.out.println(setupKeys(puKeys, filename));
		System.out.println("The public keys have been saved in: \n" + filename);
		
	}
}
