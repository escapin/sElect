package de.uni.trier.infsec.coreSystem;

public class Params {
	public static final int    SERVER1ID = -1;
	public static final int    SERVER2ID = -2;
	
	public static final byte[] ENC_DOMAIN = {0x10};
	public static final byte[] SIG_DOMAIN = {0x11};
	public static final int NumberOfVoters = 50;
	
	public static byte[] ACCEPTED = "ACCEPTED".getBytes();
	public static byte[] REJECTED = "REJECTED".getBytes();
}
