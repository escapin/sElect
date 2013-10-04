package de.uni.trier.infsec.eVotingSystem.coreSystem;

import java.util.Arrays;

import de.uni.trier.infsec.eVotingSystem.apps.AppParams;
import de.uni.trier.infsec.functionalities.pkienc.Decryptor;
import de.uni.trier.infsec.functionalities.pkienc.Encryptor;
import de.uni.trier.infsec.functionalities.pkienc.RegisterEnc;
import de.uni.trier.infsec.functionalities.pkisig.RegisterSig;
import de.uni.trier.infsec.functionalities.pkisig.Signer;
import de.uni.trier.infsec.functionalities.pkisig.Verifier;
import de.uni.trier.infsec.lib.network.NetworkError;
import de.uni.trier.infsec.utils.MessageTools;
import de.uni.trier.infsec.utils.Utilities;
import static de.uni.trier.infsec.utils.MessageTools.first;
import static de.uni.trier.infsec.utils.MessageTools.second;
import static de.uni.trier.infsec.utils.MessageTools.byteArrayToInt;
import static de.uni.trier.infsec.utils.MessageTools.intToByteArray;
import static de.uni.trier.infsec.utils.MessageTools.concatenate;

public class CollectingServer 
{
	// CRYPTOGRAPHIC FUNCTIONALITIES

	private final Decryptor decryptor;
	private final Signer signer;


	// STATE

	private boolean inVotingPhase; // indicates if the system is still in the voting phase
	private final byte[] electionID;
	private final byte[][] ballots = new byte[Params.NumberOfVoters][]; // (inner ballots which have been cast) 
	private int numberOfCastBallots = 0;


	// CLASSES

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


	// CONSTRUCTORS

	public CollectingServer(byte[] electionID, Decryptor decryptor, Signer signer) {
		this.signer = signer;
		this.decryptor = decryptor;
		this.electionID=electionID;
		inVotingPhase=true;
		// initially no voter has cast their ballot:
		for(int i=0; i<Params.NumberOfVoters; ++i)
			ballots[i]=null;
	}

	// PUBLIC METHODS

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
	public byte[] collectBallot(byte[] ballot) throws MalformedMessage, NetworkError, RegisterSig.PKIError, RegisterEnc.PKIError {

		// Decrypt, check the signature, and deconstruct the input ballot.
		// If this step fails, an exception is thrown (there is no response to be send back):
		//
		byte[] id_payload_sign = decryptor.decrypt(ballot);
		byte[] voterIDmsg = first(id_payload_sign);
		if(voterIDmsg.length!=4) // since clientID is supposed to be a integer, its length must be 4 bytes
			throw new MalformedMessage("Client ID expected");
		int voterID = byteArrayToInt(voterIDmsg);
		System.out.printf(" [ voterId = %d ]\n", voterID);
		if( voterID<0 || voterID>=Params.NumberOfVoters ) // only accept requests from eligible voters
			throw new MalformedMessage("Not eligible voter");
		byte[] payload_sign = second(id_payload_sign);
		byte[] payload = first(payload_sign);
		byte[] signVoter = second(payload_sign);
		Verifier voter_verifier = RegisterSig.getVerifier(voterID, Params.SIG_DOMAIN); // (may throw NetworkError or PKIerror) 
		if (!voter_verifier.verify(signVoter, payload))  // only accept signed requests (by an eligible voter)
			throw new MalformedMessage("Invalid signature");
		byte[] elID = first(payload);
		byte[] innerBallot=second(payload);

		// Check if the ballot is to be rejected (with an error response).
		//
		byte[] problem = null;  // null means that everything is ok 
		if( !MessageTools.equal(elID, electionID) )
			problem =  Params.INVALID_ELECTION_ID;
		else if( !inVotingPhase )
			problem = Params.ELECTION_OVER;
		else if( ballots[voterID]!=null && !Utilities.arrayEqual(innerBallot, ballots[voterID]) )	// check whether the vote has already voted
			problem = Params.ALREADY_VOTED;

		if (problem != null) { // there is a problem; create an error response of the form [electionID, REJECTED, rejectedReason]
			return encapsulateResponse(voterID, concatenate(elID, concatenate(Params.REJECTED, problem)));
			// note that we reply with the election identifier as provided in the voter's request
		}

		// No errors -- record the ballot and return a standard response of the form
		// [electionID, ACCEPTED, serverSignature(ACCEPTED, electionID, innerBallot)]
		//
		if(ballots[voterID] == null) { // store the inner ballot (only if the voter has sent it for the first time)
			numberOfCastBallots++;
			ballots[voterID] = innerBallot; 
		}
		// create receipt for the voter
		byte[] elID_innerBallot = concatenate(electionID, innerBallot);
		byte[] accepted_elID_innerBallot = concatenate(Params.ACCEPTED, elID_innerBallot);
		byte[] receipt = signer.sign(accepted_elID_innerBallot);
		// TODO: perhaps the server should store voters' signatures

		byte[] accepted_serverSign=concatenate(Params.ACCEPTED, receipt);
		return encapsulateResponse(voterID, concatenate(electionID, accepted_serverSign));
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
		byte[][] bb = new byte[numberOfCastBallots][];
		for (int id=0,ind=0; id<Params.NumberOfVoters; ++id) {
			if (ballots[id]!=null)
				bb[ind++] = ballots[id];
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
		for (int id=0,ind=0; id<Params.NumberOfVoters; ++id) {
			if (ballots[id]!=null)
				vv[ind++] = intToByteArray(id);
		}
		byte[] votersAsAMessage = Utils.concatenateMessageArray(vv);

		// put together the election ID, inner ballots, and list of voters
		byte[] result = concatenate(electionID, concatenate(ballotsAsAMessage, votersAsAMessage));
		
		// sign the result
		byte[] signature = signer.sign(result);
		byte[] resultWithSignature = concatenate(result, signature);

		return resultWithSignature;
	}


	// PRIVATE METHODS

	/**
	 * Add voterID, sign, then encrypt with the voter's public key  
	 * @throws NetworkError 
	 * @throws RegisterEnc.PKIError 
	 */
	private byte[] encapsulateResponse(int voterID, byte[] payload) throws NetworkError, RegisterEnc.PKIError {
		// add voterID
		byte[] id_payload = concatenate(intToByteArray(voterID), payload);
		// add server signature
		byte[] signServer = signer.sign(id_payload);
		byte[] id_payload_signature = concatenate(id_payload, signServer);
		// encryption with the voter public key
		Encryptor voter_encryptor = RegisterEnc.getEncryptor(voterID, Params.ENC_DOMAIN);
		byte[] response=voter_encryptor.encrypt(id_payload_signature);

		return response;
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
		for (int id=0; id<Params.NumberOfVoters; ++id) {
			if (ballots[id]!=null)
				l[ind++] = id;
		}
		assert(ind == numberOfCastBallots-1);
		return l;
	}

}
