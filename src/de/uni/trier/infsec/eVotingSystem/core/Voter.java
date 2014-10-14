package de.uni.trier.infsec.eVotingSystem.core;

import de.uni.trier.infsec.functionalities.digsig.Verifier;
import de.uni.trier.infsec.functionalities.nonce.NonceGen;
import de.uni.trier.infsec.functionalities.pkenc.Encryptor;
import de.uni.trier.infsec.utils.MessageTools;
import static de.uni.trier.infsec.utils.MessageTools.intToByteArray;
import static de.uni.trier.infsec.utils.MessageTools.byteArrayToInt;
import static de.uni.trier.infsec.utils.MessageTools.concatenate;
import static de.uni.trier.infsec.utils.MessageTools.first;
import static de.uni.trier.infsec.utils.MessageTools.second;
import static de.uni.trier.infsec.utils.MessageTools.copyOf;

/**
 * Core voter's client class. It provides cryptographic operations (formating a ballot) 
 * of a voter.
 */
public class Voter 
{
	/// CLASSES ///

	public static class Receipt {
		public final byte[] electionID;
		public final int voterChoice;
		public final byte[] nonce;
		public final byte[] innerBallot;
		public byte[] serverSignature;
		
		private static final byte[] emptySig = new byte[] {};

		private Receipt(byte[] electionID, int voterChoice, byte[] nonce, byte[] inner_ballot, byte[] serverSignature) {
			this.electionID = electionID;
			this.voterChoice = voterChoice;
			this.nonce = nonce;
			this.innerBallot = inner_ballot;	
			this.serverSignature = serverSignature;
		}
		
		public byte[] asMessage() {
			byte [] signature = serverSignature==null ? emptySig : serverSignature;
			return	concatenate( electionID,
					concatenate( intToByteArray(voterChoice),
					concatenate( nonce,
					concatenate( innerBallot,
							     signature ))));
		}
		
		public static Receipt fromMessage(byte[] message) {
			byte[] electionID = first(message);
			message = second(message);
			int candidateNumber = byteArrayToInt(first(message));
			message = second(message);
			byte[] nonce = first(message);
			message = second(message);
			byte[] innerBallot = first(message);
			byte[] signature = second(message);
			
			return new Receipt(electionID, candidateNumber, nonce, innerBallot, signature);
		}
		
		private Receipt getCopy() {
			return new Receipt(MessageTools.copyOf(electionID), voterChoice, MessageTools.copyOf(nonce), copyOf(innerBallot), copyOf(serverSignature));
		}
		
	}


	public class Error extends Exception {
		public Error(String msg){
			super(msg);
		}
	}

	/**
	 * Exception thrown when the response of the server does not conform
	 * to an expected format (we get, for instance, a trash message or a response
	 * to a different request). 
	 */
	public class MalformedMessage extends Error {
		public MalformedMessage(String msg){
			super(msg);
		}
	}

	/**
	 * Exception thrown when either the receipt has still to be created 
	 * or it is malformed.	
	 */
	public class ReceiptError extends Error{
		public ReceiptError(String msg){
			super(msg);
		}
	}
	
	/// STATE ///

	private final byte[] voterID;				// e-mail address (encoded as a bitstring) that identifies the voter
	private final byte[] electionID;			// election identifier
	private final Encryptor server1enc;			// encryptor of the first server (ballots collecting server)
	private final Verifier server1ver;			// verifier of the first server
	private final Encryptor server2enc;			// encryptor of the second server (the final server)
	private final NonceGen noncegen;			// nonce generation functionality
	private Receipt receipt;					// receipt containing information for response validation and verification procedure

	/// CONTRUCTOR(S) ///

	public Voter(byte[] voterID, byte[] electionID, Encryptor colServEnc, Verifier colServVerif, Encryptor finServEnc) {
		this.voterID = voterID;
		this.electionID = electionID;
		this.server1enc = colServEnc;
		this.server1ver = colServVerif;
		this.server2enc = finServEnc;
		this.noncegen = new NonceGen();
		this.receipt = null;
	}

	/// METHODS ///

	
	/**
	 * Creates and returns a ballot containing the given vote. As a side effect, it 
	 * also creates a receipt (field 'receipt')
	 * 
	 * The ballot is of the form:
	 * 
	 *     ENC_S1(electionId, voterID, CAST_BALLOT, otp, inner_ballot)
	 *     
	 * where 
	 * 
	 *     inner_ballot = ENC_S2(vote, nonce),
	 * 
	 * n is a freshly generated nonce, and Enc_Si(msg) denotes the message msg 
	 * encrypted with the public key of the server Si.  
	 */
	public byte[] createBallot(int votersChoice, byte[] otp) {
		if (receipt != null) // a ballot has already been created
			return null;
		byte[] nonce = noncegen.newNonce();
		byte[] vote = intToByteArray(votersChoice);
		byte[] innerBallot = server2enc.encrypt(concatenate(nonce, vote));
		receipt = new Receipt(electionID, votersChoice, nonce, innerBallot, null); // no server signature yet
		return encapsulateInnerBallot(otp, innerBallot);
	}


	/**
	 *  Uses previously generated inner ballot to prepare a new ballot (for re-voting) 
	 */
	public byte[] reCreateBallot(byte[] otp) {
		if (receipt == null) // cannot recreate the ballot because it has not been created yet
			return null;
		return encapsulateInnerBallot(otp, receipt.innerBallot);
	}


	// Checks if the signed receipt. If yes, the method saves the signature and return true 
	public boolean validateReceipt(byte[] receiptSignature) {		
		// verify the signature on the receipt
		byte[] expectedSignature = concatenate(Params.ACCEPTED, concatenate(receipt.electionID, receipt.innerBallot));
		if (!server1ver.verify(receiptSignature, expectedSignature))
			return false; 
		// store the signed receipt
		receipt.serverSignature = receiptSignature;
		return true;
	}
	

	/**
	 * Returns (a copy of) the receipt. A receipt contains data necessary for the verification procedure.
	 */
	public Receipt getReceipt() {
		return receipt.getCopy();
	}

	public byte[] getVoterId() {
		return voterID;
	}

	
	/// PRIVATE METHODS ///
	
	// Given innerBallot, returns  
	//    ENC_S1(electionId, voterID, BALLOT, otp, inner_ballot)
	private byte[] encapsulateInnerBallot(byte[] otp, byte[] innerBallot) {
		byte[] unencryptedBallot =  concatenate( electionID, 
									concatenate( voterID, 
									concatenate( Params.CAST_BALLOT,
									concatenate( otp, 
											     receipt.innerBallot ))));
		byte[] ballot = server1enc.encrypt(unencryptedBallot);
		
		return ballot;		
	}
	

	/// FOR TESTING ONLY ///
	/*
	public byte[] forceCreateBallot(int candidateNumber) {
		receipt = null;
		return createBallot(candidateNumber);
	}
	*/
	
	public byte[] getCSPublicKey() {
		return this.server1enc.getPublicKey();
	}
}
