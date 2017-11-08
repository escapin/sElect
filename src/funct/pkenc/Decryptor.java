package funct.pkenc;

import static utils.MessageTools.copyOf;

import lib.crypto.CryptoLib;
import lib.crypto.KeyPair;

/**
 * Real functionality for public-key encryption: Decryptor
 */
public final class Decryptor {
	
	private byte[] encryptionKey = null;
	private byte[] decryptionKey = null; 

	public Decryptor() {
		KeyPair keypair = CryptoLib.pke_generateKeyPair();
		encryptionKey = keypair.publicKey;  
		decryptionKey = keypair.privateKey; 
	}

    public byte[] decrypt(byte[] message) {
		byte[] plaintext = CryptoLib.pke_decrypt(copyOf(message), copyOf(decryptionKey));
		return copyOf(plaintext);
	}
    
    public Encryptor getEncryptor() {
        return new Encryptor(encryptionKey);
    }

	
	// methods not present in the ideal functionality:
    public Decryptor(byte[] encryptionKey, byte[] decryptionKey) {
    	this.encryptionKey = encryptionKey;
    	this.decryptionKey = decryptionKey;
    }

	public byte[] getEncryptionKey() {
		return encryptionKey;
	}
	
	public byte[] getDecryptionKey() {
		return decryptionKey;
	}
		
}
