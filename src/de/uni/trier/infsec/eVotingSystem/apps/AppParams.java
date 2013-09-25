package de.uni.trier.infsec.eVotingSystem.apps;

import java.io.File;

public class AppParams 
{
	public static final String PATH_STORAGE = System.getProperty("java.io.tmpdir") + File.separator + "eVotingSystem" + File.separator;
	public static final byte[] electionID = "Election Nr 123".getBytes();

	public static String SERVER_NAME = "localhost";
	public static int SERVER_PORT= 7075;	
}
