package selectvoting.system.core;

import java.util.Arrays;

import selectvoting.functionalities.digsig.Signer;
import selectvoting.functionalities.digsig.Verifier;
import selectvoting.functionalities.pkenc.Decryptor;
import selectvoting.system.core.Utils.MessageSplitIter;
import selectvoting.utils.MessageTools;

public class FinalServer 
{	
	// Cryptographic functionalities
	private final Signer signer;
	private final Decryptor decryptor;
	private final Verifier collectingServerVerif;
	private final byte[] electionID;
	private final int numberOfVoters;
	
	// CLASSES
	/**
	 * Error thrown if the input data is ill-formed.
	 */
	public static class MalformedData extends Exception 
	{
		public String description;
		public MalformedData(String description) {
			this.description = description;
		}
		public String toString() {
			return "Final Server Error: " + description;
		}
	}

	
	// CONSTRUCTORS
	
	public FinalServer(Decryptor decryptor, Signer signer, Verifier colServVerif, byte[] electionID, int numberOfVoters) {
		this.signer = signer;
		this.decryptor = decryptor;
		this.numberOfVoters = numberOfVoters;
		this.electionID = electionID;
		this.collectingServerVerif = colServVerif;
	}
	
	// PUBLIC METHODS
	
	/**
	 * Process data that supposed to be the input tally prepared and signed 
	 * by the collecting server. Returns the signed result of the election 
	 * (to be publicly posted). 
	 */
	public byte[] processTally(byte[] data) throws MalformedData {
		// verify the signature of server1
		byte[] tagged_payload = MessageTools.first(data);
		byte[] signature = MessageTools.second(data);
		if (!collectingServerVerif.verify(signature, tagged_payload))
			throw new MalformedData("Wrong signature");
		
		// check the tag
		byte[] tag = MessageTools.first(tagged_payload);
		if (!MessageTools.equal(tag, CollectingServer.TAG_RESULT))
			throw new MalformedData("Wrong tag");		
		byte[] payload = MessageTools.second(tagged_payload);
		
		// check that election id in the processed data
		byte[] el_id = MessageTools.first(payload);
		if (!MessageTools.equal(el_id, electionID))
			throw new MalformedData("Wrong election ID");
		
		// retrieve and process ballots (store decrypted entries in 'entries')
		byte[] ballotsAsAMessage = MessageTools.first(MessageTools.second(payload)); 
		byte[][] entries = new byte[numberOfVoters][];
		int numberOfEntries = 0;

		// Loop over the input entries
		byte[] last = null;
		for( MessageSplitIter iter = new MessageSplitIter(ballotsAsAMessage); iter.notEmpty(); iter.next() ) {
			if (numberOfEntries > numberOfVoters) // too many entries
				throw new MalformedData("Too many entries");
			byte[] current = iter.current();
			if (last!=null && MessageTools.equal(current, last)) continue; // ignore duplicates
			last = current;
			byte[] elID_nonce_vote = decryptor.decrypt(current); // decrypt the current entry
			if (elID_nonce_vote == null) continue; // decryption failed
			byte[] elID = MessageTools.first(elID_nonce_vote);
			if (elID==null || !MessageTools.equal(elID, electionID)) continue; // wrong election ID
			byte[] nonce_vote = MessageTools.second(elID_nonce_vote);
			if (nonce_vote==null || nonce_vote.length==0) continue; // empty message
			entries[numberOfEntries] = nonce_vote; // entry is fine; store it
			++numberOfEntries;
		}

		// sort the entries
		Arrays.sort(entries, 0, numberOfEntries, new java.util.Comparator<byte[]>() {
			public int compare(byte[] a1, byte[] a2) {
				return Utils.compare(a1, a2);
			}
		});
		
		// format entries as one message
		byte[] entriesAsAMessage = Utils.concatenateMessageArray(entries, numberOfEntries);
		
		// add election id and sign them
		byte[] result = MessageTools.concatenate(electionID, entriesAsAMessage);
		byte[] signatureOnResult = signer.sign(result);
		byte[] signedResult = MessageTools.concatenate(result, signatureOnResult);
		return signedResult;
	}
}
