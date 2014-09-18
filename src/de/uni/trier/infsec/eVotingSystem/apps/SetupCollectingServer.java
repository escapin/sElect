package de.uni.trier.infsec.eVotingSystem.apps;

import de.uni.trier.infsec.eVotingSystem.core.Params;
import de.uni.trier.infsec.functionalities.digsig.Signer;
import de.uni.trier.infsec.functionalities.pkenc.Decryptor;


/**
 * Registers the collecting server (using its id from the Params).
 */
public class SetupCollectingServer {
	
	public static void main(String[] args) {	
		Decryptor decr = new Decryptor();
		Signer sign = new Signer();
		
		String filename = AppParams.PATH_STORAGE + "CollectingServer";		
		String file_pu = filename + "_PU.json";
		String file_pr = filename + "_PR.json";
		
		
					
	
		
	}
	
	
	
}
