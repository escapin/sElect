package de.uni.trier.infsec.eVotingSystem.apps;

import java.io.File;

public class AppParams 
{
	public static final String PATH_STORAGE = System.getProperty("java.io.tmpdir") + File.separator + "eVotingSystem" + File.separator;
	public static final byte[] electionID = "Election Nr 123".getBytes();

	public static String SERVER1_NAME = "localhost";
	public static int SERVER1_PORT= 7075;
	public static String SERVER2_NAME = "localhost";
	public static int SERVER2_PORT= 7076;
}
