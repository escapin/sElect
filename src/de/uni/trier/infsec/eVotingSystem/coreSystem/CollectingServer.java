package de.uni.trier.infsec.eVotingSystem.coreSystem;

import java.util.Arrays;

import de.uni.trier.infsec.functionalities.pkienc.Decryptor;
import de.uni.trier.infsec.functionalities.pkienc.Encryptor;
import de.uni.trier.infsec.functionalities.pkienc.RegisterEnc;
import de.uni.trier.infsec.functionalities.pkisig.RegisterSig;
import de.uni.trier.infsec.functionalities.pkisig.Signer;
import de.uni.trier.infsec.functionalities.pkisig.Verifier;
import de.uni.trier.infsec.lib.network.NetworkError;
import de.uni.trier.infsec.utils.Utilities;
import static de.uni.trier.infsec.utils.MessageTools.*;
//import static de.uni.trier.infsec.utils.MessageTools.first;
//import static de.uni.trier.infsec.utils.MessageTools.second;
//import static de.uni.trier.infsec.utils.MessageTools.byteArrayToInt;
//import static de.uni.trier.infsec.utils.MessageTools.concatenate;

public class CollectingServer 
{
	// CRYPTOGRAPHIC FUNCTIONALITIES
	
	private final Decryptor decryptor;
	private final Signer signer;
	
	// STATE (PRIVATE)
	
	private boolean inVotingPahse = true; // indicates if the system is still in the voting phase
	private final int electionID;
	private final byte[][] ballots = new byte[Params.NumberOfVoters][]; // (inner ballots which have been cast) 
	private int numberOfCastBallots = 0;
	
	
	// CLASSES
	
	/**
	 * Error thrown if a collected message is ill-formed.
	 */
	public static class Error extends Exception 
	{
		private static final long serialVersionUID = 2280511187763698373L;
		private String description;
		public Error(String description) {
			this.description = description;
		}
		public String toString() {
			return "Collecting Server Error: " + description;
		}
	}

	// CONSTRUCTORS
	
	public CollectingServer(int electionID, Decryptor decryptor, Signer signer) {
		this.signer = signer;
		this.decryptor = decryptor;
		this.electionID=electionID;
		// initially no voter has cast their ballot:
		for(int i=0; i<Params.NumberOfVoters; ++i)
			ballots[i]=null;
	}

	// PUBLIC METHODS
	
	/**
	 * Process a new ballot and return a response. Response in null, if the
	 * ballot is rejected.
	 *
	 * NOTE: 
	 * 	for the sake of the verification process, we implement a simple way to store 
	 *  inner_ballot(s), based on an array whose length is NumberOfVoters, with the assumption
	 *	that a voterID is a number between 0 and NumberOfVoters-1.
	 *
	 * @throws  
	 */
	public byte[] collectBallot(byte[] ballot) throws MalformedMessage, NetworkError, RegisterSig.PKIError, RegisterEnc.PKIError{
		
		VoterBallot vb = decryptValidateRequest(ballot);
		
		
		byte[] rejected_reason={};
		boolean rejected=false;
		if( rejected=vb.electionID!=electionID )
			rejected_reason=concatenate(Params.REJECTED, Params.INVALID_ELECTION_ID);
		else if( rejected=(vb.voterID<=0 || vb.voterID>Params.NumberOfVoters) )
			rejected_reason=concatenate(Params.REJECTED, Params.INVALID_VOTER_ID);
		else if( rejected=!inVotingPahse )
			rejected_reason=concatenate(Params.REJECTED, Params.ELECTION_OVER);
		else if( rejected=(ballots[vb.voterID]!=null && !Utilities.arrayEqual(vb.inner_ballot, ballots[vb.voterID])) )	// check whether the vote has already voted
			rejected_reason=concatenate(Params.REJECTED, Params.ALREADY_VOTED);
		
		byte[] elIDmsg=intToByteArray(electionID);
		if(rejected)
			/* SHAPE OF THE RESPONSE
			 * 	 [electionID, REJECTED, rejectedReason]
			 */
			return buildSecureChannel(vb.voterID, concatenate(elIDmsg, rejected_reason));
		
		if(ballots[vb.voterID]==null){
			numberOfCastBallots++;
			// STORE the inner_ballot		
			ballots[vb.voterID]=vb.inner_ballot;
		}
		// build the server signature as receipt for the voter
		byte[] elID_inner_ballot=concatenate(elIDmsg,vb.inner_ballot);
		byte[] accepted_elID_inner_ballot=concatenate(Params.ACCEPTED,elID_inner_ballot);
		byte[] serverSign=signer.sign(accepted_elID_inner_ballot);
		// TODO: perhaps the server should store the voters signatures on her ballot
		
		byte[] accepted_serverSign=concatenate(Params.ACCEPTED,serverSign);
		/*
		 * SHAPE OF THE RESPONSE
		 * 	 [electionID, ACCEPTED, serverSignature(ACCEPTED, electionID, innerBallot)]
		 */
		return buildSecureChannel(vb.voterID, concatenate(elIDmsg, accepted_serverSign));				
	}

	
	private class VoterBallot {
		int voterID;
		int electionID;
		byte[] inner_ballot;
		
		VoterBallot(int voterID, int electionID, byte[] inner_ballot) {
			this.voterID=voterID;
			this.electionID=electionID;
			this.inner_ballot=inner_ballot;
		}
	}
	
	/**
	 * Decrypt the message, verify that it's a response of the server to our request
	 * (otherwise an exception is thrown). 
	 * 
	 * @param ballot the voter ballot
	 * @return a VoterBallot object
	 * @throws MalformedMessage if something went wrong during the validation process
	 */
	private VoterBallot decryptValidateRequest(byte[] ballot) throws MalformedMessage, NetworkError, RegisterSig.PKIError{ 
		byte[] id_payload_sign = decryptor.decrypt(ballot);
		
		byte[] voterIDmsg = first(id_payload_sign);
		if(voterIDmsg.length!=4) // since clientID is supposed to be a integer, its length must be 4 bytes
			throw new MalformedMessage();
		int voterID = byteArrayToInt(voterIDmsg);
		
		byte[] payload_sign= second(id_payload_sign);
		
		// fetch the voter's verifier (if it fails, let if fail now);
		// these steps can throw a NetworkError or one of the PKIErrors
		Verifier voter_verifier =  RegisterSig.getVerifier(voterID, Params.SIG_DOMAIN);
		byte[] payload=first(payload_sign);
		byte[] signVoter=second(payload_sign);
		if (!voter_verifier.verify(signVoter, payload))
			throw new MalformedMessage();
		byte[] elIDmsg=first(payload);
		if(voterIDmsg.length!=4) // since electionID is supposed to be a integer, its length must be 4 bytes
			throw new MalformedMessage();
		int elID = byteArrayToInt(elIDmsg);
		byte[] inner_ballot=second(payload);
		
		return new VoterBallot(voterID, elID, inner_ballot); 
	}
	
	/**
	 * Add voterID, sign then encrypt to send everything to the voter.  
	 * @throws NetworkError 
	 * @throws RegisterEnc.PKIError 
	 */
	private byte[] buildSecureChannel(int voterID, byte[] payload) throws NetworkError, RegisterEnc.PKIError{
		// 1. add voterID
		byte[] id_payload = concatenate(intToByteArray(voterID), payload);
		// 2. server signature
		byte[] signServer = signer.sign(id_payload);
		byte[] id_payload_signature = concatenate(id_payload, signServer);
		// 3. encryption with the voter public key
		Encryptor voter_encryptor = RegisterEnc.getEncryptor(voterID, Params.ENC_DOMAIN);
		byte[] response=voter_encryptor.encrypt(id_payload_signature);
		
		return response;
	}
	
	/**
	 * Return the result (content of the input tally), to be publicly posted. 
	 */
	public byte[] getResult() 
	{
		// if the result is given out, voting should not be possible any longer
		inVotingPahse = false;

		// sort the ballots
		Arrays.sort(ballots, new java.util.Comparator<byte[]>() {
		    public int compare(byte[] a1, byte[] a2) {
		    	// null greater than every other vote
		    	if(a1==null && a2==null)
		    		return 0;
		    	else if(a1==null)
		    		return +1;
		    	else if(a2==null)
		    		return -1;
		    	return Utils.compare(a1, a2);
		    }
		});
		// concatenate all the (inner) ballots
		byte[] ballotsAsAMessage = Utils.concatenateMessageArray(ballots, numberOfCastBallots);
		//byte[] ballotsAsAMessage = Utils.concatenateMessageArray(Utilities.copyOf(ballots, numberOfCastBallots), numberOfCastBallots);

		// concatenate all the voters who voted
		// the voters are already sorted ascending
		byte[][] vv = new byte[numberOfCastBallots][];
		int ind = 0;
		for (int id=0; id<Params.NumberOfVoters; ++id) {
			if (ballots[id]!=null)
				vv[ind++] = intToByteArray(id);
		}
		byte[] votersAsAMessage = Utils.concatenateMessageArray(vv);

		// put them (ballots and the voters) together
		byte[] publicData = concatenate(ballotsAsAMessage, votersAsAMessage);

		// sign the public data
		byte[] signature = signer.sign(publicData);
		byte[] publicDataWithSignature = concatenate(publicData, signature);

		return publicDataWithSignature;
	}

	/**
	 * Exception thrown when the request we received does not conform
	 * to an expected format (we get, for instance, a trash message). 
	 */
	@SuppressWarnings("serial")
	public static class MalformedMessage extends Exception {}
	
	
	
	// METHODS FOR TESTING //
	
	
	/**
	 * For testing. Returns array of cast ballots.
	 */
	public byte[][] getBallots(){
		byte[][] b = new byte[numberOfCastBallots][];
		int ind=0;
		for (int i=0; i<numberOfCastBallots; ++i)
			if(ballots[i]!=null)
				b[ind++] = ballots[i];
		return b;
	}
	
	/**
	 * For testing.
	 */
	public int[] getListOfVotersWhoVoted()
	{
		int[] l = new int[numberOfCastBallots];
		int ind = 0;
		for (int id=0; id<Params.NumberOfVoters; ++id) {
			if (ballots[id]!=null)
				l[ind++] = id;
		}
		assert(ind == numberOfCastBallots-1);
		return l;
	}

}
