package selectvoting.functionalities.digsig;

import static selectvoting.utils.MessageTools.copyOf;
import selectvoting.lib.crypto.CryptoLib;

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