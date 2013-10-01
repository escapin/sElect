package de.uni.trier.infsec.eVotingSystem.apps;

import java.io.File;

public class AppParams 
{
	public static final String PATH_STORAGE = System.getProperty("java.io.tmpdir") + File.separator + "eVotingSystem" + File.separator;
	
	public static final String APPNAME = "TrustVote - RS3 Demo";
	
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
	public static final int ALLOWEDVOTERS=3;

	public static String SERVER1_NAME = "localhost";
	public static int SERVER1_PORT= 7075;
	public static String SERVER2_NAME = "localhost";
	public static int SERVER2_PORT= 7076;
}
