package functionalities.nonce;

import infsec.lib.crypto.CryptoLib;

public class NonceGen {
	public NonceGen() {
	}

	public byte[] nextNonce() {
		return CryptoLib.nextNonce();
	}
}