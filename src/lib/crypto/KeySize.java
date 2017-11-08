package lib.crypto;

public class KeySize {
		protected static final int pkKeySize 	= 1024; // Bits key size for public key crypto
		protected static final int signKeySize 	= 1024; // Bits key size for Signature
		protected static final int symKeySize	= 256;	// Bits key size for sym encryption
		protected static final int nonce_length = 16; 	// 16 Bytes = 128 Bit nonce length
}
