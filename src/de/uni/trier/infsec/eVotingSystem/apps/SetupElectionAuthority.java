package de.uni.trier.infsec.eVotingSystem.apps;


import static de.uni.trier.infsec.eVotingSystem.apps.AppUtils.setupPrivateKeys;
import static de.uni.trier.infsec.eVotingSystem.apps.AppUtils.setupPublicKeys;
import de.uni.trier.infsec.eVotingSystem.parser.Keys;
import de.uni.trier.infsec.functionalities.digsig.Signer;

public class SetupElectionAuthority
{
	public static void main(String[] args) {	
		String name = "ElectionAuthority";

		Signer sign = new Signer();
		
		String filename;
		
		Keys k = new Keys();
		k.signKey=sign.getSignatureKey();
		k.verifKey=sign.getVerificationKey();
		
		filename =  AppParams.PRIVATE_KEY_dir + name + "_PR.json";
		setupPrivateKeys(k, filename);
		
		filename =  AppParams.PUBLIC_KEY_dir + name + "_PU.json";
		String publicKeys=setupPublicKeys(k, filename);
		
		System.out.println(name + "'s public keys:");
		System.out.println(publicKeys);
		System.out.println("The public keys have been saved in: \n" + filename);
	}
}
