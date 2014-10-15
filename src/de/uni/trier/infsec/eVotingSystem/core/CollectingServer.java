package de.uni.trier.infsec.eVotingSystem.core;

import java.util.Arrays;
import java.util.Hashtable;

import de.uni.trier.infsec.functionalities.digsig.Signer;
import de.uni.trier.infsec.utils.MessageTools;
import de.uni.trier.infsec.utils.Utilities;
import static de.uni.trier.infsec.utils.MessageTools.intToByteArray;
import static de.uni.trier.infsec.utils.MessageTools.concatenate;

public class CollectingServer 
{
	// PRIVATE CLASSES

	/*
	// A class encapsulating a voter id (represented as bytes) and providing 'equals' and 'hashCode'.
	private static class VID {
		byte[] id;

		public VID(byte[] id) { this.id = id; }
		// public VID(String id) { this.id = Utilities.stringAsBytes(id); }

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
	*/

	// CRYPTOGRAPHIC FUNCTIONALITIES

	private final Signer signer;

	// STATE

	private final byte[] electionID;
	private final byte[][] ballots; 
	private int numberOfCastBallots = 0;
	private int numberOfVoters;
	private Hashtable<String, byte[]> voterInfo;


	// CONSTRUCTORS

	public CollectingServer(Signer signer, byte[] electionID, String[] voterIdentifiers) {
		this.signer = signer;
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
	public byte[] collectBallot(String voterID, byte[] innerBallot) {			

		// Make sure that the voter is eligible
		if (!voterInfo.containsKey(voterID))
			return null;

		// Check if the voter has already voted with different inner ballot: 
		byte[] storedInnerBallot = voterInfo.get(voterID);
		if( storedInnerBallot.length!=0 && !MessageTools.equal(innerBallot, storedInnerBallot) )
			return null;

		// Collect the ballot if the voter votes for the first time (not if the voter re-votes)
		if( storedInnerBallot.length != 0 ) {
			voterInfo.put(voterID, innerBallot); // store the inner ballot under the voter's id
			ballots[numberOfCastBallots++] = innerBallot; // add the inner ballot to the list of inner ballots
		}

		// Create a receipt for the voter
		byte[] elID_innerBallot = concatenate(electionID, innerBallot);
		byte[] accepted_elID_innerBallot = concatenate(Params.ACCEPTED, elID_innerBallot);
		System.out.println("Signature produced on: ");
		System.out.println(Utilities.byteArrayToHexString(accepted_elID_innerBallot));
		byte[] receipt = signer.sign(accepted_elID_innerBallot);
		System.out.println(signer);
		System.out.println("Produced signature: ");
		System.out.println(Utilities.byteArrayToHexString(receipt));

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

		// sign the result
		byte[] signature = signer.sign(result);
		byte[] resultWithSignature = concatenate(result, signature);

		return resultWithSignature;
	}


	// PRIVATE METHODS


	// METHODS FOR TESTING //
	/* FIXME: Should we delete these two methods and change the Test according to it?
	 * 		These two methods could be somehow misleading for someone using this
	 *		class. For instance, I confused the method getBallots() with getResult()
	 */

	/**
	 * For testing. Returns array of cast ballots.
	 */
	/*
	public byte[][] getBallots(){
		byte[][] b = new byte[numberOfCastBallots][];
		int ind=0;
		for (int i=0; i<numberOfCastBallots; ++i)
			if(ballots[i]!=null)
				b[ind++] = ballots[i];
		return b;
	}
	*/

	/**
	 * For testing.
	 */
	/*
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
	*/
}