package funct.pkenc;

import static utils.MessageTools.copyOf;

import lib.crypto.CryptoLib;

/**
 * Real functionality for public-key encryption: Encryptor
 */
public final class Encryptor {

	private byte[] encryptionKey = null;
	
	// Note that this constructor is not public in the ideal functionality. 
	public Encryptor(byte[] encryptionKey) { 
		this.encryptionKey = encryptionKey;
	}
		
	public byte[] encrypt(byte[] message) {
		return CryptoLib.pke_encrypt(copyOf(message), copyOf(encryptionKey));
	}
	
	public byte[] getPublicKey() {
		return copyOf(encryptionKey);
	}
	
	
	
}
