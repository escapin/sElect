package de.unitrier.infsec.lib.crypto;

import java.security.KeyException;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import de.unitrier.infsec.utils.MessageTools;

// TODO: check how exceptions/errors are handled

/**
 * Real implementation of same interface as environment.crypto.CryptoLib
 */
public class CryptoLib {

	private static final int pkKeySize 		= 1024; // 1024 Bits keysize for public key crypto
	private static final int signKeySize 	= 512; // 512 Bits keysize for Signature -- in order to encrypt signatures, we need a larger PK for encryption!
	private static final int nonce_length 	= 16; // 16 Bytes = 128 Bit nonce length

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	/**
	 * Key generation for symmetric encryption.
	 */
	public static byte[] symkey_generateKey() {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES", "BC");
			kgen.init(256);
			SecretKey seckey = kgen.generateKey();
			return seckey.getEncoded();

		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			// e.printStackTrace();
		}
		return null;
	}

	/**
	 *  Authenticated encryption.
	 * 
	 *  GCM mode of encryption with AES and random 96-bit IV. 
	 */
	public static byte[] symkey_encrypt(byte[] key, byte[] plaintext) {
		// wrap the key
		SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
		// generate a random iv
		byte[] iv_bytes = new byte[12]; // 96-bit IV gives best performance for GCM 
		SecureRandom rnd = new SecureRandom();
		rnd.nextBytes(iv_bytes);
		IvParameterSpec iv = new IvParameterSpec(iv_bytes);
		try {
			// encrypt and add the iv
			Cipher c = Cipher.getInstance("AES/GCM/NoPadding", "BC");
			c.init(Cipher.ENCRYPT_MODE, keySpec, iv);
			byte[] encrypted =  c.doFinal(plaintext);
			return MessageTools.raw_concatenate(iv_bytes, encrypted);
		} catch (Exception e) { // (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException |  e)			
			//e.printStackTrace();
		}
		return null;
	}

	/**
	 * Authenticated dectyprion. If decription fails (integrity test fails), this method returns null;
	 * 
	 * (See symkey_encrypt for chosen cryptographic implementation)
	 */
	public static byte[] symkey_decrypt(byte[] key, byte[] ciphertext) {
		// wrap the key
		SecretKeySpec keySpec;
		try {
			keySpec = new SecretKeySpec(key, "AES");
		} catch (Exception e) {
			// e.printStackTrace();
			return null;			
		}
		// recover the iv (first 12 bytes of the ciphertext)
		if (ciphertext.length < 12) return null;
		IvParameterSpec iv = new IvParameterSpec(ciphertext, 0, 12); // parameters: bytes, offset, length
		try {
			// decrypt
			Cipher c = Cipher.getInstance("AES/GCM/NoPadding", "BC");
			c.init(Cipher.DECRYPT_MODE, keySpec, iv);
			return c.doFinal(ciphertext, 12, ciphertext.length-12);  // param: bytes, offset, length
		} catch (javax.crypto.BadPaddingException e) {
			return null;  // mac check in GCM failed --- return bottom
		} catch (Exception e) { // (NoSuchAlgorithmException | NoSuchProviderException	| NoSuchPaddingException |  e) {			
			// e.printStackTrace();
		}
		return null;
	}


	/**
	 * Public key encryption (of message with the key publicKey), using RSA with PKCS#1 padding.
	 *
	 * (Note there is a limit on the message length -- this is no hybrid encryption.)
	 */
	public static byte[] just_pke_encrypt(byte[] message, byte[] publicKey) {
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA", "BC");
			// for private keys use PKCS8EncodedKeySpec; for public keys use
			// X509EncodedKeySpec
			X509EncodedKeySpec ks = new X509EncodedKeySpec(publicKey);
			PublicKey pk = kf.generatePublic(ks);

			Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
			c.init(Cipher.ENCRYPT_MODE, pk);
			byte[] out = c.doFinal(message);
			return out;

		} catch (Exception e) {
			// e.printStackTrace();
		}
		return null;
	}

	public static byte[] just_pke_decrypt(byte[] message, byte[] privKey) {
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA", "BC");
			// for private keys use PKCS8EncodedKeySpec; for public keys use
			// X509EncodedKeySpec
			PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(privKey);
			PrivateKey pk = kf.generatePrivate(ks);
			Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
			c.init(Cipher.DECRYPT_MODE, pk);
			byte[] out = c.doFinal(message);
			return out;
		} catch (Exception e) {
			// e.printStackTrace();
		}
		return null;
	}

	// Hybrid encryption
	public static byte[] pke_encrypt(byte[] message, byte[] publicKey) {
		// generate an auxiliary random symmetric key
		byte[] aux_key = symkey_generateKey();
		// encrypt it using the public key
		byte[] encrypted_aux_key = just_pke_encrypt(aux_key, publicKey);
		// encrypt the message using the auxiliary key
		byte[] encrypted_message = symkey_encrypt(aux_key, message);
		// the result ciphertext is the concatenation of the above
		return MessageTools.concatenate(encrypted_aux_key, encrypted_message);
	}

	public static byte[] pke_decrypt(byte[] ciphertext, byte[] privKey) {
		// split the input message into parts
		byte[] encrypted_aux_key = MessageTools.first(ciphertext);
		byte[] encrypted_message =  MessageTools.second(ciphertext);
		// retrieve the auxiliary key
		byte[] aux_key = just_pke_decrypt(encrypted_aux_key, privKey);
		// decrypt and return the message
		return symkey_decrypt(aux_key, encrypted_message);
	}

	public static KeyPair pke_generateKeyPair() {
		KeyPair out = new KeyPair();
		KeyPairGenerator keyPairGen;
		try {
			keyPairGen = KeyPairGenerator.getInstance("RSA", "BC");
			keyPairGen.initialize(pkKeySize);
			java.security.KeyPair pair = keyPairGen.generateKeyPair();
			out.privateKey = pair.getPrivate().getEncoded();
			out.publicKey = pair.getPublic().getEncoded();
			return out;
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return null;
	}

	public static byte[] sign(byte[] data, byte[] signingKey) {
		Signature signer;
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA", "BC");
			// for private keys use PKCS8EncodedKeySpec;
			PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(signingKey);
			PrivateKey pk = kf.generatePrivate(ks);

			signer = Signature.getInstance("SHA256WithRSA", "BC");
			signer.initSign(pk);
			signer.update(data);
			return signer.sign();
		} catch (NoSuchAlgorithmException | NoSuchProviderException | KeyException | SignatureException | InvalidKeySpecException e) {
			//System.out.println("Signature creation failed " + e.getLocalizedMessage());
		}
		return null;
	}

	public static boolean verify(byte[] data, byte[] signature, byte[] verificationKey) {
		Signature signer;
		try {
			KeyFactory kf = KeyFactory.getInstance("RSA", "BC");
			// for private keys use PKCS8EncodedKeySpec; for public keys use
			// X509EncodedKeySpec
			X509EncodedKeySpec ks = new X509EncodedKeySpec(verificationKey);
			PublicKey pk = kf.generatePublic(ks);

			signer = Signature.getInstance("SHA256WithRSA", "BC");
			signer.initVerify(pk);
			signer.update(data);
			return signer.verify(signature);
		} catch (NoSuchAlgorithmException | NoSuchProviderException | KeyException | SignatureException | InvalidKeySpecException e) {
			//System.out.println("Signature verification failed " + e.getLocalizedMessage());
		}
		return false;
	}

	public static KeyPair generateSignatureKeyPair() {
		KeyPair out = new KeyPair();
		KeyPairGenerator keyPairGen;
		try {
			keyPairGen = KeyPairGenerator.getInstance("RSA", "BC");
			keyPairGen.initialize(signKeySize);
			java.security.KeyPair pair = keyPairGen.generateKeyPair();
			out.privateKey = pair.getPrivate().getEncoded();
			out.publicKey = pair.getPublic().getEncoded();
			return out;
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return null;
	}

	public static byte[] nextNonce() {
		SecureRandom random = new SecureRandom();
		byte bytes[] = new byte[nonce_length];
		random.nextBytes(bytes);
		return bytes;
	}

}
