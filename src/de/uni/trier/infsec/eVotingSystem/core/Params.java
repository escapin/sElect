package de.uni.trier.infsec.eVotingSystem.core;

public class Params {
	
	public static byte[] ACCEPTED 		= {0x00};
	public static byte[] REJECTED 		= {0x01};
	public static byte[] OTP_ACCEPTED 	= {0x02};
	
	//public static byte[] VOTE_COLLECTED 		= {0x20};		// the vote has been correctly collected (tag 'ACCEPTED')
	public static byte[] INVALID_ELECTION_ID 	= {0x21};		// Invalid election identifier
	public static byte[] INVALID_VOTER_ID 		= {0x22};		// Invalid voter identifier	
	public static byte[] ELECTION_OVER 			= {0x23}; 		// Voting phase is over
	public static byte[] ELECTION_NOT_STARTED	= {0x24};
	public static byte[] ALREADY_VOTED 			= {0x25};		// Already voted with a different ballot
	public static byte[] WRONG_OTP				= {0x26}; 
	
	public static byte[] OTP_REQUEST 	= {0x40}; // request type OTP  
	public static byte[] CAST_BALLOT 	= {0x41}; // request type ballot
}
