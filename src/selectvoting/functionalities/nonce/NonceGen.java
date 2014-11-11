package selectvoting.functionalities.nonce;

import selectvoting.lib.crypto.CryptoLib;

public class NonceGen {
	public NonceGen() {
	}

	public byte[] newNonce() {
		return CryptoLib.generateNonce();
	}
}