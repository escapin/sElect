package selectvoting.system.core;

import de.unitrier.infsec.functionalities.digsig.Signer;
import de.unitrier.infsec.functionalities.pkenc.Decryptor;
import de.unitrier.infsec.utils.MessageTools;
import de.unitrier.infsec.utils.Utilities;

public class CollectingServer 
{
	public static class Error extends Exception {
		public final String info;
		public Error(String info) { this.info = info; }
	} 
	
	// CRYPTOGRAPHIC FUNCTIONALITIES

	private final Signer signer;
	private final Decryptor decryptor;

	// STATE

	private final byte[] electionID;
	private final byte[][] innerBallots; 
	private int numberOfCastBallots = 0;
	private int numberOfVoters;
	private Utils.ObjectsMap voterInfo;
	private String[] voterIdentifiers;


	// CONSTRUCTORS

	public CollectingServer(Decryptor decryptor, Signer signer, byte[] electionID, String[] voterIdentifiers) {
		this.signer = signer;
		this.decryptor = decryptor;
		this.electionID = electionID;
		this.voterIdentifiers = voterIdentifiers;
		// this.noncegen = new NonceGen();
		this.numberOfVoters = voterIdentifiers.length;
		this.innerBallots = new byte[numberOfVoters][]; // (inner ballots which have been cast)
		// initially no voter has cast their ballot:
		for(int i=0; i<numberOfVoters; ++i)	innerBallots[i] = null;
		// initialize the map with information kept for each voter 
		voterInfo = new Utils.ObjectsMap();
		for(int i=0; i<numberOfVoters;i++){
			byte[] empty = {};
			voterInfo.put(voterIdentifiers[i], empty); // empty means no (inner) ballot collected
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

		byte[] elID_innerBallot = decryptor.decrypt(ballot);
		if (elID_innerBallot==null) // decryption failed
			throw new Error("Malformed ballot (decryption failed)");
		byte[] elID = MessageTools.first(elID_innerBallot);
		if (elID==null || !MessageTools.equal(electionID, elID))
			throw new Error("Malformed ballot (wrong election ID)");
		byte[] innerBallot = MessageTools.second(elID_innerBallot);
		if (innerBallot==null || innerBallot.length==0)
			throw new Error("Malformed ballot (empty inner ballot)");

		// Check if the voter has already voted with different inner ballot: 
		byte[] storedInnerBallot = (byte[]) voterInfo.get(voterID);
		if( storedInnerBallot.length!=0 && !MessageTools.equal(innerBallot, storedInnerBallot) )
			throw new Error("Voter already voted");

		// Collect the ballot if the voter votes for the first time (not if the voter re-votes)
		if( storedInnerBallot.length == 0 ) {
			voterInfo.put(voterID, innerBallot); // store the inner ballot under the voter's id
			innerBallots[numberOfCastBallots++] = innerBallot; // add the inner ballot to the list of inner ballots
		}

		// Create a receipt for the voter
		// byte[] elID_innerBallot = concatenate(electionID, innerBallot);
		byte[] accepted_elID_innerBallot = MessageTools.concatenate(Tag.ACCEPTED, elID_innerBallot);
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
			bb[i] = innerBallots[i];
		}

		Utils.sort(bb, 0, bb.length);
		

		// concatenate all the (inner) ballots
		byte[] ballotsAsAMessage = Utils.concatenateMessageArray(bb, numberOfCastBallots);

		// concatenate identifiers of the voters who voted (they are already "sorted")
		byte[][] vv = new byte[numberOfCastBallots][];
		
		int k = 0;
		for(int i=0; i<voterIdentifiers.length; ++i){
			String vid=voterIdentifiers[i];
			if (voterInfo.containsKey(vid) && ((byte[])voterInfo.get(vid)).length!=0) { // voter voted
				vv[k++] = Utilities.stringAsBytes(vid);
			}
		}	
		byte[] votersAsAMessage = Utils.concatenateMessageArrayWithDuplicateElimination(vv);

		// put together the election ID, inner ballots, and list of voters
		byte[] result = MessageTools.concatenate(electionID, MessageTools.concatenate(ballotsAsAMessage, votersAsAMessage));
		byte[] tag_result = MessageTools.concatenate(Tag.RESULT, result);

		// sign the result
		byte[] signature = signer.sign(tag_result);
		byte[] resultWithSignature = MessageTools.concatenate(tag_result, signature);

		return resultWithSignature;
	}
}