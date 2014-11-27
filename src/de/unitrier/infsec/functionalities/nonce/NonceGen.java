package de.unitrier.infsec.functionalities.nonce;

import de.unitrier.infsec.lib.crypto.CryptoLib;

public class NonceGen {
	public NonceGen() {
	}

	public byte[] nextNonce() {
		return CryptoLib.nextNonce();
	}
}