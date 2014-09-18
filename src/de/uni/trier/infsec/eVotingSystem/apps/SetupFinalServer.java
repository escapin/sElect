package de.uni.trier.infsec.eVotingSystem.apps;

import static de.uni.trier.infsec.eVotingSystem.apps.AppUtils.setupServer;

/**
 * Registers the collecting server (using its id from the Params).
 */
public class SetupFinalServer {
	
	public static void main(String[] args) {	
		setupServer("FinalServer");
	}
}
