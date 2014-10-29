package de.uni.trier.infsec.eVotingSystem.core;

import java.util.Arrays;
import java.util.Hashtable;

import de.uni.trier.infsec.functionalities.digsig.Signer;
import de.uni.trier.infsec.functionalities.pkenc.Decryptor;
import de.uni.trier.infsec.utils.MessageTools;
import static de.uni.trier.infsec.utils.MessageTools.intToByteArray;
import static de.uni.trier.infsec.utils.MessageTools.concatenate;

public class CollectingServer 
{
	public static byte[] TAG_ACCEPTED = {0x00};
	public static byte[] TAG_RESULT   = {0x01};
	
	public static class Error extends Exception {
		public final String info;
		public Error(String info) { this.info = info; }
	} 
	
	// CRYPTOGRAPHIC FUNCTIONALITIES

	private final Signer signer;
	private final Decryptor decryptor;

	// STATE

	private final byte[] electionID;
	private final byte[][] ballots; 
	private int numberOfCastBallots = 0;
	private int numberOfVoters;
	private Hashtable<String, byte[]> voterInfo;


	// CONSTRUCTORS

	public CollectingServer(Decryptor decryptor, Signer signer, byte[] electionID, String[] voterIdentifiers) {
		this.signer = signer;
		this.decryptor = decryptor;
		this.electionID = electionID;
		// this.noncegen = new NonceGen();
		this.numberOfVoters = voterIdentifiers.length;
		this.ballots = new byte[numberOfVoters][]; // (inner ballots which have been cast)
		// initially no voter has cast their ballot:
		for(int i=0; i<numberOfVoters; ++i)	ballots[i] = null;
		// initialize the map with information kept for each voter 
		voterInfo = new Hashtable<String, byte[]>();
		for( String vid : voterIdentifiers ) {
			byte[] empty = {};
			voterInfo.put(vid, empty); // empty means no (inner) ballot collected
		}
	}

	// PUBLIC METHODS

	/**
	 *  Collects a ballot. If everything goes fine, a receipt is returned. 
	 *  Otherwise, that is if (a) the voter id is not valid (which should be 
	 *  ruled out on the application level) or if the voter has already voted with 
	 *  different inner ballot, the method returns null.
	 */
	public byte[] collectBallot(String voterID, byte[] ballot) throws Error {

		if (!voterInfo.containsKey(voterID)) // not eligible voter
			throw new Error("Wrong voter id");

		byte[] innerBallot = decryptor.decrypt(ballot);
		if (innerBallot==null) // decryption has failed
			throw new Error("Malformed ballot (decryption failed)");

		// Check if the voter has already voted with different inner ballot: 
		byte[] storedInnerBallot = voterInfo.get(voterID);
		if( storedInnerBallot.length!=0 && !MessageTools.equal(innerBallot, storedInnerBallot) )
			throw new Error("Voter already voted");

		// Collect the ballot if the voter votes for the first time (not if the voter re-votes)
		if( storedInnerBallot.length == 0 ) {
			voterInfo.put(voterID, innerBallot); // store the inner ballot under the voter's id
			ballots[numberOfCastBallots++] = innerBallot; // add the inner ballot to the list of inner ballots
		}

		// Create a receipt for the voter
		byte[] elID_innerBallot = concatenate(electionID, innerBallot);
		byte[] accepted_elID_innerBallot = concatenate(TAG_ACCEPTED, elID_innerBallot);
		byte[] receipt = signer.sign(accepted_elID_innerBallot);

		return receipt;
	}

	/**
	 * Return the result.
	 */
	public byte[] getResult() 
	{
		// sort the ballots
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
		byte[] result = concatenate(electionID, concatenate(ballotsAsAMessage, votersAsAMessage));
		byte[] tag_result = concatenate(TAG_RESULT, result);

		// sign the result
		byte[] signature = signer.sign(tag_result);
		byte[] resultWithSignature = concatenate(tag_result, signature);

		return resultWithSignature;
	}
}