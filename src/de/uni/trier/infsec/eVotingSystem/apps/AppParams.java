package de.uni.trier.infsec.eVotingSystem.apps;

import java.io.File;

public class AppParams 
{
	public static final String PATH_STORAGE = System.getProperty("java.io.tmpdir") + File.separator + "eVotingSystem" + File.separator;
	public static final String RECEIPT_file = PATH_STORAGE + "receipt_"; // + voterID + ".msg";
	
	public static final String PUBLIC_KEY_dir = PATH_STORAGE + "PublicKeys" + File.separator;
	public static final String PRIVATE_KEY_dir = PATH_STORAGE + "PrivateKeys" + File.separator;
	
	public static final String PKI_DATABASE = System.getProperty("java.io.tmpdir") + File.separator + "PKI_server.db";
	// = de.uni.trier.infsec.functionalities.pki.PKIServerCore.DEFAULT_DATABASE;
	
	// RESULT PATH (by assuming that the execution of the servers is from the bin/ directory 
	public static final String COLL_SERVER_RESULT_msg =  "../BulletinBoard/public/SignedPartialResult.msg";
	//public static final String COLL_SERVER_RESULT_file =  PATH_STORAGE + "SignedPartialResult.msg";
	public static final String FIN_SERVER_RESULT_msg = "../BulletinBoard/public/SignedFinalResult.msg";
	//public static final String FIN_SERVER_RESULT_file = PATH_STORAGE + "SignedFinalResult.msg";
	public static final String FINAL_RESULT_file = "../BulletinBoard/FinalResult.txt";
	
	/*
	 * In case we store local file in memory, remember to add the path also here
	 * in such a way that we keep track of them it in unique array.
	 * (For instance, it is useful for deleting them).
	 */
	public static final String[] PATH_LOCALFILES = 	{	PKI_DATABASE, 
														COLL_SERVER_RESULT_msg,
														FIN_SERVER_RESULT_msg,
														FINAL_RESULT_file
													};
	
	public static final String VOTERAPPNAME = "sElect";
	public static final String VERIFYAPPNAME = "VerifYourVote";
	
	public static String SERVER1_NAME = "localhost";
	public static int SERVER1_PORT= 7075;
	public static String SERVER2_NAME = "localhost";
	public static int SERVER2_PORT= 7076;
	
	// to set when the election is over
	public static final int ALLOWEDVOTERS=3;
	
	public static final String ELECTIONMSG = "Please make your choice.";
	public static final byte[] ELECTIONID = "Favourite RS3 project".getBytes();
	public static final String[] CANDIDATESARRAY = {	
								"DeduSec (Verication of Security Properties)",
								"IFlow (Developing Systems with Secure IFlow)",
								"ALBIA (Logic-Based Information Flow Analysis)",
								"Analysis of E-Voting Systems",
								"IFC for JavaScript",
								"IFC for Mobile Components",
								"MORES (Data & Processes Security Requirements)",
								"MoVeSPAcI (Verication of Security Properties)",
								"RSCP (Security for Concurrent Programs)",
								"SecDed (Secure Type Systems and Deduction)",
								"SpAGAT (IFlow in Shared Document Bases)",
								"SADAN (Data-driven Usage Control)",
								};
}
