package de.uni.trier.infsec.functionalities.pkienc;

import de.uni.trier.infsec.functionalities.pki.PKI;
import de.uni.trier.infsec.lib.network.NetworkError;


public class RegisterEnc {

	public static void registerEncryptor(Encryptor encryptor, int id, byte[] pki_domain) throws PKIError, NetworkError {
		try {
			PKI.register(id, pki_domain, encryptor.getPublicKey());
		} catch (PKI.Error e) {
			throw new PKIError();
		}
	}

	public static Encryptor getEncryptor(int id, byte[] pki_domain) throws PKIError, NetworkError {
		try {
			byte[] key = PKI.getKey(id, pki_domain);
			return new Encryptor(key);
		} catch (PKI.Error e) {
			throw new PKIError();
		}
	}

	@SuppressWarnings("serial")
	public static class PKIError extends Exception { }

	public static final byte[] DEFAULT_PKI_DOMAIN  = new byte[] {0x03, 0x01};
}
