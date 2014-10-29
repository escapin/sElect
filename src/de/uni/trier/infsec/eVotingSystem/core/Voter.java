package de.uni.trier.infsec.eVotingSystem.core;

import de.uni.trier.infsec.functionalities.digsig.Verifier;
import de.uni.trier.infsec.functionalities.nonce.NonceGen;
import de.uni.trier.infsec.functionalities.pkenc.Encryptor;
import static de.uni.trier.infsec.utils.MessageTools.intToByteArray;
import static de.uni.trier.infsec.utils.MessageTools.concatenate;

/**
 * Core voter's client class. It provides cryptographic operations (formating a ballot) 
 * of a voter.
 */
public class Voter 
{
	public static class BallotInfo {
		public final byte[] ballot;
		public final byte[] nonce;
		public final byte[] innerBallot;
		public BallotInfo(byte[] ballot, byte[] nonce, byte[] innerBallot) {
			this.ballot = ballot;
			this.nonce = nonce;
			this.innerBallot = innerBallot;
		}
	}
	
	private static final NonceGen noncegen = new NonceGen(); // nonce generation functionality

	/**
	 * Creates and returns a ballot containing the given vote. 
	 * The ballot is of the form:
	 * 
	 *     Enc_S1(ENC_S2(vote, recID)),
	 * 
	 * where recID is a freshly generated nonce, and Enc_Si(msg) denotes the message msg 
	 * encrypted with the public key of the server Si.  
	 */
	public static BallotInfo createBallot(int votersChoice, Encryptor colServEnc, Encryptor finServEnc) {
		byte[] nonce = noncegen.newNonce();
		byte[] vote = intToByteArray(votersChoice);
		byte[] innerBallot = finServEnc.encrypt(concatenate(nonce, vote));
		byte[] ballot = colServEnc.encrypt(innerBallot);
		return new BallotInfo(ballot, nonce, innerBallot);
	}

	/**
	 * Checks if 'receiptSignature' is a signature on the receipt. If yes, the method saves the signature and return true.  
	 */ 
	public static boolean validateReceipt(byte[] receipt, byte[] electionID, byte[] innerBallot, Verifier colServVerif) {		
		// verify the signature on the receipt
		byte[] expectedMessage = concatenate(CollectingServer.TAG_ACCEPTED, concatenate(electionID, innerBallot));
		return colServVerif.verify(receipt, expectedMessage);
	}
}
