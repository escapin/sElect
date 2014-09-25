package de.uni.trier.infsec.eVotingSystem.apps;

import java.io.File;

import de.uni.trier.infsec.eVotingSystem.bean.URI;

public class AppParams 
{
	public static final String PATH_STORAGE = System.getProperty("user.home") + File.separator + ".eVotingSystem" + File.separator;
	
	public static final String PUBLIC = PATH_STORAGE + "Public" + File.separator;
	public static final String PUBLIC_KEY_path = PUBLIC + "PublicKeys" + File.separator;
	public static final String EL_MANIFEST_path = PUBLIC + "Manifest" + File.separator;
	
	public static final String PRIVATE = PATH_STORAGE + "Private" + File.separator;
	public static final String PRIVATE_KEY_path = PRIVATE + "PrivateKeys" + File.separator;
	public static final String RECEIPT_file = PRIVATE + "Receipts" + "receipt_"; // + voterID + ".msg";
	
	//public static final String PKI_DATABASE = System.getProperty("java.io.tmpdir") + File.separator + "PKI_server.db";
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
	public static final String[] PATH_LOCALFILES = 	{	// PKI_DATABASE, 
														COLL_SERVER_RESULT_msg,
														FIN_SERVER_RESULT_msg,
														FINAL_RESULT_file
													};
	/******************************************/
	/********** ELECTION PARAMETERS ***********/
	/******************************************/
	
	public static final String VOTERAPPNAME = "sElect";
	public static final String VERIFYAPPNAME = "VerifYourVote";
	
	
	public static final long STARTTIME = System.currentTimeMillis() + (60000); // election starts in 1 minute
	public static final long DURATION = (60000)*3; // 3 minutes
	
	public static final String EL_TITLE = "Favourite RS3 project";
	public static final String EL_DESCRIPTION = "Election for the best RS3 project";
	public static final String HEADLINE = "Please make your choice.";
	public static final String[] CHOICESLIST = {	
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
	
	public static URI colServURI = new URI("localhost", 7075);
	public static URI finServURI = new URI("localhost", 7076);
	public static URI[] bulletinBoardList = 	{ new URI("localhost", 7077)
												};

}
