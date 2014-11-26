package de.unitrier.infsec.functionalities.digsig;

import de.unitrier.infsec.lib.crypto.CryptoLib;
import de.unitrier.infsec.lib.crypto.KeyPair;
import static de.unitrier.infsec.utils.MessageTools.copyOf;

/**
 * An object encapsulating a signing/verification key pair and allowing a user to
 * create signatures.
 */
public class Signer {
	byte[] verificationKey;
	byte[] signatureKey;

	public Signer() {
		KeyPair keypair = CryptoLib.generateSignatureKeyPair();
		this.signatureKey = copyOf(keypair.privateKey);
		this.verificationKey = copyOf(keypair.publicKey);
	}

	public byte[] sign(byte[] message) {
		byte[] signature = CryptoLib.sign(copyOf(message), copyOf(signatureKey));
		return copyOf(signature);
	}

	public Verifier getVerifier() {
		return new Verifier(verificationKey);
	}
	
	
	// methods not present in the ideal functionality:
	public Signer(byte[] verificationKey, byte[] signatureKey ) {
		this.verificationKey = verificationKey;
		this.signatureKey = signatureKey;
	}
	
	public byte[] getVerificationKey() {
		return verificationKey;
	}
	
	public byte[] getSignatureKey() {
		return signatureKey;
	}

	
}