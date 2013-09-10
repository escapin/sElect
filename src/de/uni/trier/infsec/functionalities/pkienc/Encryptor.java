package de.uni.trier.infsec.functionalities.pkienc;

import static de.uni.trier.infsec.utils.MessageTools.copyOf;
import de.uni.trier.infsec.lib.crypto.CryptoLib;

/** An object encapsulating the public key of some party. 
 *  This key can be accessed directly of indirectly via method encrypt.  
 */
public class Encryptor {
	private byte[] publicKey;

	public Encryptor(byte[] publicKey) {
		this.publicKey = publicKey;
	}

	public byte[] encrypt(byte[] message) {
		return copyOf(CryptoLib.pke_encrypt(copyOf(message), copyOf(publicKey)));		
	}

	public byte[] getPublicKey() {
		return copyOf(publicKey);
	}
}