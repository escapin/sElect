package de.uni.trier.infsec.eVotingSystem.core;

import java.util.Arrays;

import de.uni.trier.infsec.eVotingSystem.bean.CollectingServerID;
import de.uni.trier.infsec.eVotingSystem.bean.FinalServerID;
import de.uni.trier.infsec.eVotingSystem.parser.ElectionManifest;
// import de.uni.trier.infsec.functionalities.digsig.Signer;
import de.uni.trier.infsec.functionalities.digsig.Verifier;
import de.uni.trier.infsec.functionalities.nonce.NonceGen;
// import de.uni.trier.infsec.functionalities.pkenc.Decryptor;
import de.uni.trier.infsec.functionalities.pkenc.Encryptor;
import de.uni.trier.infsec.lib.network.NetworkError;
import de.uni.trier.infsec.utils.MessageTools;
import de.uni.trier.infsec.utils.Utilities;
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

	public static enum ResponseTag {
		VOTE_COLLECTED, OTP_REQUEST_ACCEPTED, 
		INVALID_ELECTION_ID, INVALID_VOTER_ID, 
		ELECTION_OVER, ELECTION_NOT_STARTED, 
		ALREADY_VOTED, WRONG_OTP,
		UNKNOWN_ERROR; 
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

	private final ElectionManifest elManifest;	// election identifier

	private final byte[] voterID;				// e-mail address (encoded as a bitstring) that identifies the voter
	
	// private final int voterID;			    // PKI identifier of a voter (we assume that a voter is already registered)
	// private final Decryptor decryptor;		// decrytpror of the voter (containing her private decryption key)
	// private final Signer signer;				// signer of a voter (containing her private signing key)

	private final Encryptor server1enc;			// encryptor of the first server (ballots collecting server)
	private final Verifier server1ver;			// verifier of the first server
	private final Encryptor server2enc;			// encryptor of the second server (the final server)
	private final NonceGen noncegen;			// nonce generation functionality
	private Receipt receipt;					// receipt containing information for response validation and verification procedure

	/// CONTRUCTOR(S) ///

	public Voter(byte[] voterID, ElectionManifest elManifest) throws NetworkError {
		// TODO: Manifest should not be given here; the application should deal with it.
		this.voterID = voterID;
		this.elManifest=elManifest;
		CollectingServerID colSer = elManifest.collectingServer;
		FinalServerID finSer = elManifest.finalServer;
		this.server1enc = new Encryptor(colSer.encryption_key);
		this.server1ver = new Verifier(colSer.verification_key);
		this.server2enc = new Encryptor(finSer.encryption_key);
		this.noncegen = new NonceGen();
		this.receipt = null;
	}

	/// METHODS ///

	/**
	 * Creates and returns an OTP request for the given voter id. The request is if the form
	 * 
	 *     ENC_S1(electionId, voterID, OTP_REQUEST, empty-message )
	 * 
	 */
	public byte[] createOTPRequest() {
		byte[] emptyMsg = {};
		byte[] payload = concatenate(elManifest.electionID, concatenate(voterID, concatenate(Params.OTP_REQUEST, emptyMsg)));
		byte[] encryptedPayload = server1enc.encrypt(payload);
		return encryptedPayload;
	}
	
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
		receipt = new Receipt(elManifest.electionID, votersChoice, nonce, innerBallot, null); // no server signature yet
		return encapsulateInnerBallot(otp, innerBallot);
	}


	/**
	 *  Uses previously generated inner ballot to prepare a new ballot (for re-voting) 
	 */
	public byte[] reCreateBallot(byte[] otp) throws ReceiptError {
		if (receipt == null) // cannot recreate the ballot because it has not been created yet
			return null;
		return encapsulateInnerBallot(otp, receipt.innerBallot);
	}

	
	/**
	 * Checks whether the response is correct (using the nonce previously
	 * generated and used by method createBallot).
	 *
	 * The expected response is of the form
	 *
	 *    SIG_S1[ electionID, voterID, ACCEPTED, Sig_S1(ACCEPTED, electionID, inner_ballot) ]
	 *    or
	 *    SIG_S1[ electionID, voterID, REJECTED, rejectedReason ]
	 *    or
	 *    SIG_S1[ electionID, voterID, OTP_ACCEPTED, empty]
	 *
	 * that is an encrypted signature of the collecting server on inner_ballot.
	 * 
	 * The method throws an error if the response is not well formed.
	 * Otherwise, the method 
	 */
	public ResponseTag validateResponse(byte[] signedResponse) throws Error {
		
		// verify signature
		byte[] response = first(signedResponse);
		byte[] signature  = second(signedResponse);
		if(!server1ver.verify(signature, response))
			throw new MalformedMessage("Invalid signature in the server's response"); 
		// checks the election ID:
		byte[] elID = first(response);
		if (!MessageTools.equal(elID, elManifest.electionID))
			throw new MalformedMessage("Wrond election ID in the server's response");
		// check the voter ID
		byte[] voterID_tag_rest = second(response);	
		byte[] voterID = first(voterID_tag_rest);
		if (!MessageTools.equal(voterID, this.voterID))
			throw new MalformedMessage("Wrong voter ID in the server's response"); 
		// check the response tag
		byte[] tag_rest = second(voterID_tag_rest);
		byte[] tag = first(tag_rest);
		
		// Now, we proceed depending on the tag:
	
		// TODO: this look really bad:
		if (Arrays.equals(tag, Params.REJECTED)){  // rejected
			byte[] reason = second(tag_rest);
			if (Utilities.arrayEqual(reason, Params.INVALID_ELECTION_ID))
				return ResponseTag.INVALID_ELECTION_ID;
			else if (Utilities.arrayEqual(reason, Params.INVALID_VOTER_ID))
				return ResponseTag.INVALID_VOTER_ID;
			else if (Utilities.arrayEqual(reason, Params.WRONG_OTP))
				return ResponseTag.WRONG_OTP;
			else if (Utilities.arrayEqual(reason, Params.ELECTION_OVER))
				return ResponseTag.ELECTION_OVER;
			else if (Utilities.arrayEqual(reason, Params.ELECTION_NOT_STARTED))
				return ResponseTag.ELECTION_NOT_STARTED;
			else if (Utilities.arrayEqual(reason, Params.ALREADY_VOTED))
				return ResponseTag.ALREADY_VOTED;
			else return ResponseTag.UNKNOWN_ERROR;

		}
		else if (Arrays.equals(tag, Params.ACCEPTED)){ // standard (cast ballot) request accepted -- a receipt expected
			if(receipt==null)
				throw new ReceiptError("Validating a responce without having any receipt");
			
			// verify the signature on the receipt
			byte[] server_signature = second(tag_rest);
			byte[] expected_signed_msg = concatenate(Params.ACCEPTED, concatenate(receipt.electionID, receipt.innerBallot));
			if (!server1ver.verify(server_signature, expected_signed_msg))
				throw new MalformedMessage("Wrong server's signature"); 
			// TODO: apparently, the server does not need to send us back a signed message (including the message);
			// is would be enough if it sent the signature only. 
			
			// store the signed receipt
			receipt.serverSignature = server_signature;
			return ResponseTag.VOTE_COLLECTED;
		}
		else if (Arrays.equals(tag, Params.OTP_ACCEPTED)){ // OTP request accepted
			return ResponseTag.OTP_REQUEST_ACCEPTED;
		}
		else
			throw new MalformedMessage("Unknown tag");
		
		
		// FIXME: only for the standard thing
		
		
		
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
		byte[] unencryptedBallot =  concatenate( elManifest.electionID, 
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
