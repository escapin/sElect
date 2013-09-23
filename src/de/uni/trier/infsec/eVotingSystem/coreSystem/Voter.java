package de.uni.trier.infsec.eVotingSystem.coreSystem;

import java.util.Arrays;

import de.uni.trier.infsec.functionalities.nonce.NonceGen;
import de.uni.trier.infsec.functionalities.pkienc.Decryptor;
import de.uni.trier.infsec.functionalities.pkienc.Encryptor;
import de.uni.trier.infsec.functionalities.pkienc.RegisterEnc;
import de.uni.trier.infsec.functionalities.pkisig.RegisterSig;
import de.uni.trier.infsec.functionalities.pkisig.Signer;
import de.uni.trier.infsec.functionalities.pkisig.Verifier;
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
		public final byte[] nonce;
		public final byte[] innerBallot;
		public byte[] serverSignature;

		private Receipt(byte[] electionID, byte[] nonce, byte[] inner_ballot, byte[] serverSignature) {
			this.electionID=electionID;
			this.nonce=nonce;
			this.innerBallot=inner_ballot;	
			this.serverSignature=serverSignature;
		}
		private Receipt getCopy() {
			return new Receipt(electionID, MessageTools.copyOf(nonce), copyOf(innerBallot), copyOf(serverSignature));
		}
	}

	public static enum ResponseTag {
		VOTE_COLLECTED, INVALID_ELECTION_ID,
		INVALID_VOTER_ID, ELECTION_OVER, ALREADY_VOTED,
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

	private final int voterID;          // PKI identifier of a voter (we assume that a voter is already registered)
	private final byte[] electionID;	// election identifier
	private final Decryptor decryptor;	// decrytpror of the voter (containing her private decryption key)
	private final Signer signer;        // signer of a voter (containing her private signing key)
	private final Encryptor server1enc; // encryptor of the first server (ballots collecting server)
	private final Verifier server1ver;  // verifier of the first server
	private final Encryptor server2enc; // encryptor of the second server (the final server)
	private final NonceGen noncegen;    // nonce generation functionality	
	private Receipt receipt;            // receipt containing information for response validation and verification procedure

	/// CONTRUCTOR(S) ///

	public Voter(int voterID, byte[] electionID, Decryptor decryptor, Signer signer) throws NetworkError, RegisterEnc.PKIError, RegisterSig.PKIError {
		this.voterID = voterID;
		this.electionID=electionID;
		this.decryptor = decryptor;
		this.signer = signer;
		this.server1enc = RegisterEnc.getEncryptor(Params.SERVER1ID, Params.ENC_DOMAIN);
		this.server1ver = RegisterSig.getVerifier(Params.SERVER1ID, Params.SIG_DOMAIN);
		this.server2enc = RegisterEnc.getEncryptor(Params.SERVER2ID, Params.ENC_DOMAIN);
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
	 *     ENC_S1(voterID , SIG_V[ electionID, inner_ballot]
	 *     
	 * where 
	 * 
	 *     inner_ballot = ENC_S2(vote, nonce)
	 * 
	 * Sig_Voter[msg] denotes the signature 
	 * of the voter on the message msg along with this message, n is a freshly 
	 * generated nonce, and Enc_Si(msg) denotes the message msg encrypted with 
	 * the public key of the server Si.  
	 */
	public byte[] createBallot(byte[] vote) {
		if (receipt != null) // a ballot has already been created
			return null;
		byte[] nonce = noncegen.newNonce();
		byte[] nonce_vote = concatenate(nonce, vote);
		byte[] inner_ballot = server2enc.encrypt(nonce_vote);
		receipt=new Receipt(electionID, nonce, inner_ballot, null); // no server signature
		return encapsulate(receipt.innerBallot); // add the election id, sign, end encrypt
	}


	/**
	 *  Uses previously generated inner ballot to prepare a new ballot (for re-voting) 
	 */
	public byte[] reCreateBallot() throws ReceiptError {
		if (receipt == null) // cannot recreate the ballot because it has not been created yet
			return null;
		return encapsulate(receipt.innerBallot);
	}


	/**
	 * Checks whether the response is correct (using the nonce previously
	 * generated and used by method createBallot).
	 *
	 * The expected response is of the form
	 *
	 *    ENC_V( SIG_S1[ voterID, electionID, ACCEPTED, Sig_S1(ACCEPTED, electionID, inner_ballot) ] )
	 *    or
	 *    ENC_V( SIG_S1[ voterID, electionID, REJECTED,  rejectedReason ] )
	 *
	 * that is an encrypted signature of the collecting server on inner_ballot.
	 */
	public ResponseTag validateResponse(byte[] response) throws Error{
		if(receipt==null)
			throw new ReceiptError("Validating a responce without having the correspondent receipt");

		// decrypt
		byte[] serverResp_signature = decryptor.decrypt(response);
		// verify signature
		byte[] serverResp = first(serverResp_signature);
		byte[] signature = second(serverResp_signature);
		if(!server1ver.verify(signature, serverResp))
			throw new MalformedMessage("The reply does not come from the server"); 

		byte[] voterIDMsg = first(serverResp);
		if(byteArrayToInt(voterIDMsg)!=voterID)
			throw new MalformedMessage("The server's reply is not for us"); 

		byte[] elID_tag_payload = second(serverResp);

		byte[] elID = first(elID_tag_payload);
		if (!MessageTools.equal(elID, electionID))
			throw new MalformedMessage("The server's reply is not for the current election"); 

		byte[] tag_payload = second(elID_tag_payload);
		// analyze the response tag
		byte[] tag = first(tag_payload);
		if(Arrays.equals(tag, Params.REJECTED)){
			byte[] reason = second(tag_payload);
			if (Utilities.arrayEqual(reason, Params.INVALID_ELECTION_ID))
				return ResponseTag.INVALID_ELECTION_ID;
			// else if (Utilities.arrayEqual(reason, Params.INVALID_VOTER_ID))
			//	return ResponseTag.INVALID_VOTER_ID;
			else if (Utilities.arrayEqual(reason, Params.ELECTION_OVER))
				return ResponseTag.ELECTION_OVER;
			else if (Utilities.arrayEqual(reason, Params.ALREADY_VOTED))
				return ResponseTag.ALREADY_VOTED;
			else return ResponseTag.UNKNOWN_ERROR;

		}
		else if(Arrays.equals(tag, Params.ACCEPTED)){
			byte[] server_signature = second(tag_payload);
			byte[] expected_signed_msg = concatenate(Params.ACCEPTED, concatenate(electionID, receipt.innerBallot));
			if (!server1ver.verify(server_signature, expected_signed_msg))
				throw new MalformedMessage("Wrong server's signature"); 

			receipt.serverSignature=server_signature;
			return ResponseTag.VOTE_COLLECTED;
		}
		else
			throw new MalformedMessage("Unknown tag");
	}


	/**
	 * Returns (a copy of) the receipt. A receipt contains data necessary for the verification procedure.
	 */
	public Receipt getReceipt() {
		return receipt.getCopy();
	}

	public int getVoterId() {
		return voterID;
	}

	
	/// PRIVATE METHODS ///

	// ads election id, signs, end encrypts (for the collecting server), producing:
	//     Enc_server1( voterID,  Sig_Voter[elID, innerBallot] )
	private byte[] encapsulate(byte[] innerBallot) {
		byte[] voterIDMsg = intToByteArray(voterID);
		byte[] elID_innerBallot = concatenate(electionID, innerBallot);
		byte[] signature_on_elID_innerBallot = signer.sign(elID_innerBallot);
		byte[] elID_innerBallot_with_signature = concatenate(elID_innerBallot, signature_on_elID_innerBallot); 
		byte[] payload = concatenate(voterIDMsg, elID_innerBallot_with_signature);
		return server1enc.encrypt(payload);
	}

	/// FOR TESTING ONLY ///
	
	public byte[] forceCreateBallot(byte[] vote) {
		receipt = null;
		return createBallot(vote);
	}

}
