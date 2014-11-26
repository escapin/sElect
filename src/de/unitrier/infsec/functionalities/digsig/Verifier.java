package de.unitrier.infsec.functionalities.digsig;

import static de.unitrier.infsec.utils.MessageTools.copyOf;
import de.unitrier.infsec.lib.crypto.CryptoLib;

/**
 * An object encapsulating the verification key and allowing a user to verify
 * a signature.
 */
public class Verifier {
	
	private byte[] verificationKey;

	// Note that this constructor is not public in the ideal functionality.
	public Verifier(byte[] verificationKey) {
		this.verificationKey = verificationKey;
	}

	public boolean verify(byte[] signature, byte[] message) {
		return CryptoLib.verify(copyOf(message), copyOf(signature), copyOf(verificationKey));
	}

	public byte[] getVerificationKey() {
		return copyOf(verificationKey);
	}
}