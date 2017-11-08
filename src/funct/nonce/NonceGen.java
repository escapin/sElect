package funct.nonce;

import lib.crypto.CryptoLib;

public class NonceGen {
	public NonceGen() {
	}

	public byte[] nextNonce() {
		return CryptoLib.nextNonce();
	}
}