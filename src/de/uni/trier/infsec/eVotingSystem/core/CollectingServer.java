package de.uni.trier.infsec.eVotingSystem.core;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Hashtable;

import de.uni.trier.infsec.eVotingSystem.bean.VoterID;
import de.uni.trier.infsec.eVotingSystem.parser.ElectionManifest;
import de.uni.trier.infsec.functionalities.digsig.Signer;
// import de.uni.trier.infsec.functionalities.digsig.Verifier;
import de.uni.trier.infsec.functionalities.nonce.NonceGen;
import de.uni.trier.infsec.functionalities.pkenc.Decryptor;
// import de.uni.trier.infsec.functionalities.pkenc.Encryptor;
import de.uni.trier.infsec.lib.network.NetworkError;
import de.uni.trier.infsec.utils.MessageTools;
import de.uni.trier.infsec.utils.Utilities;
// import de.uni.trier.infsec.utils.Utilities;
import static de.uni.trier.infsec.utils.MessageTools.first;
import static de.uni.trier.infsec.utils.MessageTools.second;
// import static de.uni.trier.infsec.utils.MessageTools.byteArrayToInt;
import static de.uni.trier.infsec.utils.MessageTools.intToByteArray;
import static de.uni.trier.infsec.utils.MessageTools.concatenate;

public class CollectingServer 
{


	// PUBLIC CLASSES

	/**
	 * Exception thrown when the request we received does not conform
	 * to an expected format (we get, for instance, a trash message). 
	 */
	public static class MalformedMessage extends Exception {
		private final String description;
		public MalformedMessage(String str) {
			description = str;
		}
		public String toString() {
			return "Malformed message: " + description;
		}
	}
	
	/**
	 * Response of the server to a request. If otp flag is set, then the 
	 * response contains, besides a response message to be sent back to the 
	 * client, an otp to be delivered to the client via e-mail.
	 */
	public static class Response {
		public byte[] responseMsg;
		public boolean otp_response;
		public byte[] otp; // set only for an otp response (if otp_response==true)
		private Response(byte[] responseMsg, boolean otp_response, byte[] otp) {
			this.otp_response = otp_response;
			this.responseMsg = responseMsg;
			this.otp = otp;
		}
		static Response standardResponse(byte[] responseMsg) {
			return new Response(responseMsg, false, null);
		}
		static Response otpResponse(byte[] responseMsg, byte[] otp) {
			return new Response(responseMsg, true, otp);
		}
	}
	
	// PRIVATE CLASSES
	
	// A class encapsulating a voter id (represented as bytes) and providing 'equals' and 'hashCode'.
	private static class VID {
		byte[] id;
		
		public VID(byte[] id) { this.id = id; }
		public VID(String id) { this.id = Utilities.stringAsBytes(id); }
		
		@Override public boolean equals(Object that) {
		    if (that instanceof VID) 
		    	return Arrays.equals(this.id, ((VID)that).id);
		    else 
		    	return false;
		  }
		
		@Override public int hashCode() {
			return Arrays.hashCode(id);
		}
	}
	
	// Data stored by the server associated with a voter: whether a voter voted or not and her opt 
	private static class VoterInfo {
		public boolean voted;
		public byte[] otp;
		public byte[] innerBallot = null;
		public VoterInfo(boolean voted, byte[] otp) {
			this.voted = voted;
			this.otp = otp;
		}
	}


	// CRYPTOGRAPHIC FUNCTIONALITIES

	private final Decryptor decryptor;
	private final Signer signer;
	private final NonceGen noncegen;
	
	
	// STATE
	private final ElectionManifest elManifest;
	private boolean inVotingPhase; // indicates if the system is still in the voting phase
	private final byte[][] ballots; 
	private int numberOfCastBallots = 0;
	private int numberOfVoters;
	private Hashtable<VID, VoterInfo> voterInfo;


	// CONSTRUCTORS

	public CollectingServer(ElectionManifest elManifest, Decryptor decryptor, Signer signer) {
		this.signer = signer;
		this.decryptor = decryptor;
		this.noncegen = new NonceGen();
		this.elManifest = elManifest;
		this.inVotingPhase = true;
		this.numberOfVoters = elManifest.votersList.length;
		this.ballots = new byte[numberOfVoters][]; // (inner ballots which have been cast)
		// initially no voter has cast their ballot:
		for(int i=0; i<numberOfVoters; ++i)
			ballots[i] = null;
		this.voterInfo = new Hashtable<VID, VoterInfo>();
		// initialize the voters' map (create an entry for every voter in the manifest)
		for( VoterID vid : elManifest.votersList ) {
			byte[] otp = noncegen.newNonce();
			voterInfo.put(new VID(vid.email), new VoterInfo(false,otp));
		}
	}

	// PUBLIC METHODS
	
	/**
	 * Check the request type and call the appropriate methods to process it.
	 * Returns null if there is no 
	 * @param request
	 * @return
	 */
	public Response processRequest(byte[] encryptedRequest) throws MalformedMessage {
		// Processing independent of the type of request (cast ballot/opt)
		byte[] request = decryptor.decrypt(encryptedRequest); 
		if (request == null)
			throw new MalformedMessage("Wrong decryption");
		
				
		byte[] elID = first(request);
		byte[] voterID_tag_rest = second(request);
		byte[] voterID = first(voterID_tag_rest);
		byte[] tag_rest = second(voterID_tag_rest);
		byte[] tag = first(tag_rest);
		byte[] rest = second(tag_rest);
		if (elID==null || voterID==null || tag==null || rest==null)
			throw new MalformedMessage("Malformed message");

		// Check the election id
		if( !MessageTools.equal(elID, elManifest.electionID) )
			return Response.standardResponse(errorMessage(elID, voterID, Params.INVALID_ELECTION_ID));		

		// Check if the voter is eligible
		VID vid = new VID(voterID);
		if (!voterInfo.containsKey(vid))
			return Response.standardResponse(errorMessage(elID, voterID, Params.INVALID_VOTER_ID));

		// Proceed further depending on the type of request
		if (MessageTools.equal(tag, Params.OTP_REQUEST)) { // otp request
			return Response.otpResponse(otpAcknMessage(elID, voterID), voterInfo.get(vid).otp);
		}
		else if (MessageTools.equal(tag, Params.CAST_BALLOT)) { // cast ballot request
			return Response.standardResponse(collectBallot(elID, voterID, rest));
		}
		else
			throw new MalformedMessage("Wrong tag");
	}

	
	/**
	 * Process a new ballot and return a response. A response can be either a standard
	 * response (if the ballot has been accepted) or an error response. The method
	 * may also produce no response (if it is not able / not willing to produce it),
	 * but instead throw an exception. This is the case, if the message is malformed or 
	 * is not authorized by an eligible voter. 
	 *
	 * NOTE: 
	 * 	for the sake of the verification process, we implement a simple way to store 
	 *  inner_ballot(s), based on an array whose length is NumberOfVoters, with the assumption
	 *	that a voterID is a number between 0 and NumberOfVoters-1.
	 *
	 */
	private byte[] collectBallot(byte[] elID, byte[] voterID, byte[] otp_innerBallot) throws MalformedMessage {
		// Decrypt and deconstruct the input ballot.
		// If this step fails, an exception is thrown (there is no response to be send back).
		byte[] otp = first(otp_innerBallot);
		byte[] innerBallot = second(otp_innerBallot);	
		
		// Check the OTP
		VID vid = new VID(voterID);
		if( !MessageTools.equal(otp, voterInfo.get(vid).otp) )
			return errorMessage(elID, voterID, Params.WRONG_OTP);
		
		
		// Check the time
		//TODO: maybe we should deal with the time only in the App
		//tt: yest it should be done in the app, so that
		//TODO: it is also checked for the otp request
		if(System.currentTimeMillis()<elManifest.startTime.getTime())
			return errorMessage(elID, voterID, Params.ELECTION_NOT_STARTED);
		if(System.currentTimeMillis()>elManifest.endTime.getTime() )
			return errorMessage(elID, voterID, Params.ELECTION_OVER);

		// Check if the voter has already voted for a different inner ballot
		VoterInfo vinf = voterInfo.get(vid);
		if( vinf.voted && !MessageTools.equal(innerBallot, vinf.innerBallot) ) {
			return errorMessage(elID, voterID, Params.ALREADY_VOTED);
		}
		
		// Collect the ballot if the voter votes for the first time (not if the voter re-votes)
		if( ! vinf.voted ) {
			vinf.voted = true; // check the voter
			vinf.innerBallot = innerBallot; // remember her inner ballot
			// add the inner ballot to the list of inner ballots
			ballots[numberOfCastBallots++] = innerBallot;
		}
		// create receipt for the voter
		byte[] elID_innerBallot = concatenate(elManifest.electionID, innerBallot);
		byte[] accepted_elID_innerBallot = concatenate(Params.ACCEPTED, elID_innerBallot);
		byte[] receipt = signer.sign(accepted_elID_innerBallot);

		byte[] accepted_serverSign=concatenate(Params.ACCEPTED, receipt);
		return signResponse(concatenate(elManifest.electionID, concatenate(voterID, accepted_serverSign)));
	}

	public int getNumberOfBallots() {
		return numberOfCastBallots;
	}

	/**
	 * Return the result (content of the input tally), to be publicly posted.
	 */
	public byte[] getResult() 
	{
		// if the result is given out, voting should not be possible any longer
		inVotingPhase = false;

		// sort the ballots
		/*
		byte[][] bb = new byte[numberOfCastBallots][];
		for (int id=0,ind=0; id<numberOfVoters; ++id) {
			if (ballots[id]!=null)
				bb[ind++] = ballots[id];
		}
		*/
		byte[][] bb = new byte[numberOfCastBallots][];
		for (int i=0; i<numberOfCastBallots; ++i) {
			bb[i] = ballots[i];
		}
		
		Arrays.sort(bb, new java.util.Comparator<byte[]>() {
			public int compare(byte[] a1, byte[] a2) {
				return Utils.compare(a1, a2);
			}
		});

		// concatenate all the (inner) ballots
		byte[] ballotsAsAMessage = Utils.concatenateMessageArray(bb, numberOfCastBallots);

		// concatenate identifiers of the voters who voted (they are already "sorted")
		byte[][] vv = new byte[numberOfCastBallots][];
		for (int id=0,ind=0; id<numberOfVoters; ++id) {
			if (ballots[id]!=null)
				vv[ind++] = intToByteArray(id);
		}
		byte[] votersAsAMessage = Utils.concatenateMessageArray(vv);

		// put together the election ID, inner ballots, and list of voters
		byte[] result = concatenate(elManifest.electionID, concatenate(ballotsAsAMessage, votersAsAMessage));
		
		// sign the result
		byte[] signature = signer.sign(result);
		byte[] resultWithSignature = concatenate(result, signature);

		return resultWithSignature;
	}


	// PRIVATE METHODS

	byte[] errorMessage(byte[] elID, byte[] voterID, byte[] problem) {
		return signResponse(concatenate(elID, concatenate(voterID, concatenate(Params.REJECTED, problem))));
	}
	
	byte[] otpAcknMessage(byte[] elID, byte[] voterID) {
		byte[] emptyMsg = {};
		return signResponse(concatenate(elID, concatenate(voterID, concatenate(Params.OTP_ACCEPTED, emptyMsg))));
	}
	
	
	/**
	 * Add voterID, sign, then encrypt with the voter's public key  
	 * @throws NetworkError
	 * @throws RegisterEnc.PKIError 
	 */
	private byte[] signResponse(byte[] payload)
	{
		// add server signature
		byte[] signature = signer.sign(payload);
		byte[] signed_payload = concatenate(payload, signature);
		return signed_payload;
	}
	
	// METHODS FOR TESTING //
	/* FIXME: Should we delete these two methods and change the Test according to it?
	 * 		These two methods could be somehow misleading for someone using this
	 *		class. For instance, I confused the method getBallots() with getResult()
	 */
	
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
		for (int id=0; id<numberOfVoters; ++id) {
			if (ballots[id]!=null)
				l[ind++] = id;
		}
		assert(ind == numberOfCastBallots-1);
		return l;
	}
}