package de.uni.trier.infsec.eVotingSystem.apps;

import java.io.File;

public class AppParams 
{
	public static final String PATH_STORAGE = System.getProperty("java.io.tmpdir") + File.separator + "eVotingSystem" + File.separator;
	
	public static final String VOTERAPPNAME = "TrustVote - RS3 Demo";
	public static final String VERIFYAPPNAME = "VerifYourVote - RS3 Demo";
	
	public static final String ELECTIONMSG = "Who is your favourite candidate?";
	public static final byte[] ELECTIONID = "ElectionTest Nr 153".getBytes();
	public static final String[] CANDIDATESARRAY = {	
								"Candidate 00",
								"Candidate 01",
								"Candidate 02",
								"Candidate 03",
								"Candidate 04",
								"Candidate 05",
								"Candidate 06",
								"Candidate 07",
								"Candidate 08",
								"Candidate 09",
								};
	
	// to set when the election is over
	public static final int ALLOWEDVOTERS=3;
	
	public static final String RECEIPT_file = PATH_STORAGE + "receipt_"; // + voterID + ".msg";
	public static final String COLL_SERVER_RESULT_file =  PATH_STORAGE + "SignedPartialResult.msg";
	public static final String FIN_SERVER_RESULT_file = PATH_STORAGE + "SignedFinalResult.msg";

	public static String SERVER1_NAME = "localhost";
	public static int SERVER1_PORT= 7075;
	public static String SERVER2_NAME = "localhost";
	public static int SERVER2_PORT= 7076;
}
