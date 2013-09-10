package de.uni.trier.infsec.functionalities.pkisig;

import static de.uni.trier.infsec.utils.MessageTools.copyOf;
import de.uni.trier.infsec.lib.crypto.CryptoLib;

/**
 * An object encapsulating the verification key and allowing a user to verify
 * a signature.
 */
public class Verifier {
	private byte[] verifKey;

	public Verifier(byte[] verifKey) {
		this.verifKey = verifKey;
	}

	public boolean verify(byte[] signature, byte[] message) {
		return CryptoLib.verify(copyOf(message), copyOf(signature), copyOf(verifKey));
	}

	public byte[] getVerifKey() {
		return copyOf(verifKey);
	}
}