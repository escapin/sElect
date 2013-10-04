package de.uni.trier.infsec.eVotingSystem.apps;

import java.io.File;

public class AppParams 
{
	public static final String PATH_STORAGE = System.getProperty("java.io.tmpdir") + File.separator + "eVotingSystem" + File.separator;
	
	public static final String VOTERAPPNAME = "TVote";
	public static final String VERIFYAPPNAME = "VerifYourVote";
	
	public static final String ELECTIONMSG = "Please make your choice.";
	public static final byte[] ELECTIONID = "Favourite RS3 project".getBytes();
	public static final String[] CANDIDATESARRAY = {	
								"DeduSec",
								"IFlow",
								"ALBIA",
								"E-Voting",
								"Information Flow Control for JavaScript",
								"IFC for Mobile Components",
								"MORES",
								"MoVeSPAcI",
								"RSCP",
								"Secure Type Systems and Deduction",
								"SpAGAT",
								"Usage Control",
								};
	
	// to set when the election is over
	public static final int ALLOWEDVOTERS=3;
	
	public static final String RECEIPT_file = PATH_STORAGE + "receipt_"; // + voterID + ".msg";
	public static final String COLL_SERVER_RESULT_file =  "./SignedPartialResult.msg";
	//public static final String COLL_SERVER_RESULT_file =  PATH_STORAGE + "SignedPartialResult.msg";
	public static final String FIN_SERVER_RESULT_file = "./SignedFinalResult.msg";
	//public static final String FIN_SERVER_RESULT_file = PATH_STORAGE + "SignedFinalResult.msg";

	public static String SERVER1_NAME = "localhost";
	public static int SERVER1_PORT= 7075;
	public static String SERVER2_NAME = "localhost";
	public static int SERVER2_PORT= 7076;
}
