package de.uni.trier.infsec.functionalities.pkisig;

import static de.uni.trier.infsec.utils.MessageTools.concatenate;
import static de.uni.trier.infsec.utils.MessageTools.copyOf;
import static de.uni.trier.infsec.utils.MessageTools.first;
import static de.uni.trier.infsec.utils.MessageTools.second;
import de.uni.trier.infsec.lib.crypto.CryptoLib;
import de.uni.trier.infsec.lib.crypto.KeyPair;

/**
 * An object encapsulating a signing/verification key pair and allowing a user to
 * create signatures.
 */
public class Signer {
	byte[] verifKey;
	byte[] signKey;

	public Signer() {
		KeyPair keypair = CryptoLib.generateSignatureKeyPair();
		this.signKey = copyOf(keypair.privateKey);
		this.verifKey = copyOf(keypair.publicKey);
	}

	Signer(byte[] verifKey, byte[] signKey ) {
		this.verifKey = verifKey;
		this.signKey = signKey;
	}

	public byte[] sign(byte[] message) {
		byte[] signature = CryptoLib.sign(copyOf(message), copyOf(signKey));
		return copyOf(signature);
	}

	public Verifier getVerifier() {
		return new Verifier(verifKey);
	}


	// Not in the ideal functionality: 

	public byte[] toBytes() {
		return concatenate(signKey, verifKey);
	}

	public static Signer fromBytes(byte[] bytes) {
		byte[] sign_key = first(bytes);
		byte[] verif_key = second(bytes);
		return new Signer(verif_key, sign_key);
	}
}