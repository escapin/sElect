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
import static de.uni.trier.infsec.utils.MessageTools.*;
//import static de.uni.trier.infsec.utils.MessageTools.intToByteArray;
//import static de.uni.trier.infsec.utils.MessageTools.byteArrayToInt;
//import static de.uni.trier.infsec.utils.MessageTools.concatenate;
//import static de.uni.trier.infsec.utils.MessageTools.first;
//import static de.uni.trier.infsec.utils.MessageTools.second;
/**
 * Core voter's client class. It provides cryptographic operations (formating a ballot) 
 * of a voter.
 */
public class Voter {

	private final int voterID;           // PKI identifier of a voter (we assume that a voter is already registered)
	private final int electionID;		// PKI identifier of the election
	private final Decryptor decryptor;	// decrytpror of the voter (containing her private decryption key)
	private final Signer signer;        // signer of a voter (containing her private signing key)
	private final Encryptor server1enc; // encryptor of the first server (ballots collecting server)
	private final Verifier server1ver;  // verifier of the first server
	private final Encryptor server2enc; // encryptor of the second server (the final server)
	private final NonceGen noncegen;    // nonce generation functionality
	

	private byte[] nonce = null;            // the most recent nonce (used to create the ballot)
	private byte[] vote_with_nonce = null;  // the most recent concatenation of the vote and the nonce
	private byte[] inner_ballot = null;     // the most recent inner ballot
	private byte[] server_signature = null; // server's signature on the response

	public static class Receipt {
		public byte[] nonce;
		public byte[] inner_ballot;
		public byte[] server_signature;
		public Receipt(byte[] nonce, byte[] inner_ballot, byte[] server_signature){
			this.nonce=nonce;
			this.inner_ballot=inner_ballot;
			this.server_signature=server_signature;
		}
	}
	
	public Voter(int voterID, int electionID, Decryptor decryptor, Signer signer) throws NetworkError, RegisterEnc.PKIError, RegisterSig.PKIError {
		this.voterID = voterID;
		this.electionID=electionID;
		this.decryptor = decryptor;
		this.signer = signer;
		this.server1enc = RegisterEnc.getEncryptor(Params.SERVER1ID, Params.ENC_DOMAIN);
		this.server1ver = RegisterSig.getVerifier(Params.SERVER1ID, Params.SIG_DOMAIN);
		this.server2enc = RegisterEnc.getEncryptor(Params.SERVER2ID, Params.ENC_DOMAIN);
		this.noncegen = new NonceGen();
	}

	/**
	 * Creates a ballot containing the given vote.
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
		byte[] voteMsg = copyOf(vote);
			//FIXME: according to Ralf suggestion in POSTCloudStorageSystem, we should change the nonce functionality
			// 			from 'nextNonce' to 'newNonce' also in CryptoJavaGeneric
			nonce = noncegen.newNonce(); // note that we store the nonce for further use
			vote_with_nonce = concatenate(voteMsg, nonce); // as above
			inner_ballot = server2enc.encrypt(vote_with_nonce);  // as above
		
			return createOuterBallot(inner_ballot);
	}
	
	/**
	 *  Uses previously generated inner ballot to prepare a new ballot (for re-voting)
	 */
	public byte[] reCreateBallot(Receipt receipt) {
		return createOuterBallot(receipt.inner_ballot);
	}
	
	private byte[] createOuterBallot(byte[] inner_ballot){
		byte[] voterIDMsg = intToByteArray(voterID);
		byte[] elIDMsg = intToByteArray(electionID);
		
		byte[] elID_inner_ballot = concatenate(elIDMsg, inner_ballot);
		byte[] signature_inner_ballot = signer.sign(elID_inner_ballot);
		byte[] inner_ballot_with_signature = concatenate(elID_inner_ballot, signature_inner_ballot); 
		byte[] outer_ballot = concatenate(voterIDMsg, inner_ballot_with_signature);
		
		return server1enc.encrypt(outer_ballot);
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
	public byte[] validateResponse(byte[] response) throws PollError{
		// 1. decrypt
		byte[] serverResp_signature = decryptor.decrypt(response);
		// 2. verify signature
		byte[] serverResp=first(serverResp_signature);
		byte[] signature=second(serverResp_signature);
		if(!server1ver.verify(signature, serverResp))
			throw new MalformedMessage("The reply does not come from the server"); 
		
		byte[] voterIDMsg =first(serverResp);
		if(byteArrayToInt(voterIDMsg)!=voterID)
			throw new IncorrectReply("The server's reply is not for us"); 
			//FIXME: or is it better new MalformedMessage ???
		
		byte[] elID_tag_payload=second(serverResp);
		
		byte[] elIDmsg = first(elID_tag_payload);
		if(byteArrayToInt(elIDmsg)!=electionID)
			throw new IncorrectReply("The server's reply is not for the current election"); 
			//FIXME: or is it better new MalformedMessage ???
		
		byte[] tag_payload=second(elID_tag_payload);
		// analyze the response tag
		byte[] tag=first(tag_payload);
		if(Arrays.equals(tag, Params.REJECTED)){
			byte[] rejectedReason = second(tag_payload);
			return rejectedReason; 
		}
		else if(Arrays.equals(tag, Params.ACCEPTED))
			server_signature=second(tag_payload);
		else
			throw new MalformedMessage("The server's reply is malformed");
		
		return Params.VOTE_COLLECTED;
	}

	

	
	public Receipt getReceipt() {
		//TODO: build the receipt only when the vote is correctly collected
		return new Receipt(nonce, inner_ballot, server_signature);
	}
	
	
	public int getId() {
		return voterID;
	}
	
	public byte[] getInnerBallot() {
		return inner_ballot;
	}
	
	public byte[] getNonce() {
		return nonce;
	}
	
	public int getElectionID() {
		return electionID;
	}
	
	public byte[] getVoteWithNonce() {
		return vote_with_nonce;
	}
	
	
	public class PollError extends Exception {
		public PollError(String msg){
			super(msg);
		}
	}
	
	/**
	 * Exception thrown when the response is invalid and demonstrates that the server
	 * has misbehaved (the server has be ill-implemented or malicious).
	 */
	public class IncorrectReply extends PollError {
		public IncorrectReply(String msg){
			super(msg);
		}
	}

	/**
	 * Exception thrown when the response of the server does not conform
	 * to an expected format (we get, for instance, a trash message or a response
	 * to a different request). 
	 */
	public class MalformedMessage extends PollError {
		public MalformedMessage(String msg){
			super(msg);
		}
	}

}
