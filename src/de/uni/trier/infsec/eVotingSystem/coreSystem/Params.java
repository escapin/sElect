package de.uni.trier.infsec.eVotingSystem.coreSystem;

import java.io.File;


public class Params {
	public static final int    SERVER1ID = -1;
	public static final int    SERVER2ID = -2;
	
	public static final byte[] ENC_DOMAIN = {0x10};
	public static final byte[] SIG_DOMAIN = {0x11};
	public static final int NumberOfVoters = 50;
	
	public static byte[] ACCEPTED = {0x00};
	public static byte[] REJECTED = {0x01};
	
	// Storage
	public static final String PATH_STORAGE = System.getProperty("java.io.tmpdir") + File.separator + "eVotingSystem" + File.separator;
	
	public static byte[] VOTE_COLLECTED 		= {0x20};		// the vote has been correctly collected (tag 'ACCEPTED')
	public static byte[] INVALID_ELECTION_ID 	= {0x21};		// Invalid election identifier
	public static byte[] INVALID_VOTER_ID 		= {0x22};		// Invalid voter identifier	
	public static byte[] ELECTION_OVER 			= {0x23}; 		// Voting phase is over
	public static byte[] ALREADY_VOTED 			= {0x24};		// Already voted with a different ballot
}
