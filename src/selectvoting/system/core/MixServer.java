package selectvoting.system.core;


import de.unitrier.infsec.functionalities.digsig.Signer;
import de.unitrier.infsec.functionalities.digsig.Verifier;
import de.unitrier.infsec.functionalities.pkenc.Decryptor;
import de.unitrier.infsec.functionalities.pkenc.Encryptor;
import de.unitrier.infsec.utils.MessageTools;
import selectvoting.system.core.Utils.MessageSplitIter;

public class MixServer 
{	
	// Cryptographic functionalities
	private final Signer signer;
	private final Decryptor decryptor;
	private final Verifier precServVerif;
	private final byte[] electionID;
	private final int numberOfVoters;
	
	// PUBLIC CLASSES
	/**
	 * Error thrown if the input data is ill-formed.
	 */
	@SuppressWarnings("serial")
	public static class MalformedData extends Exception {
		public String description;
		public MalformedData(String description) {
			this.description = description;
		}
		public String toString() {
			return "Final Server Error: " + description;
		}
	}
	
	// CONSTRUCTORS
	
	public MixServer(Decryptor decryptor, Signer signer, Verifier precServVerif, byte[] electionID, int numberOfVoters) {
		this.signer = signer;
		this.decryptor = decryptor;
		this.numberOfVoters = numberOfVoters;
		this.electionID = electionID;
		this.precServVerif = precServVerif;
	}
	
	// PUBLIC METHODS
	
	/**
	 * Process data that supposed to be the signed output of the preceding mix server. 
	 * Returns the signed result of the mixing. 
	 * 
	 * I/O format:
	 * 			SIGN_prec[tag, elID, ballotsAsAMessage]
	 * where, each ballot:
	 * 			ENC_curr[elID, innerBallot] 
	 *   
	 */
	public byte[] processBallots(byte[] data) throws MalformedData {
		// verify the signature of preceding server
		byte[] tagged_payload = MessageTools.first(data);
		byte[] signature = MessageTools.second(data);
		if (!precServVerif.verify(signature, tagged_payload))
			throw new MalformedData("Wrong signature");
		
		// check the tag
		byte[] tag = MessageTools.first(tagged_payload);
		if (!MessageTools.equal(tag, Tag.BALLOTS))
			throw new MalformedData("Wrong tag");		
		byte[] payload = MessageTools.second(tagged_payload);
		
		// check the election id 
		byte[] el_id = MessageTools.first(payload);
		if (!MessageTools.equal(el_id, electionID))
			throw new MalformedData("Wrong election ID");
		
		// retrieve and process ballots (store decrypted entries in 'entries')
		byte[] ballotsAsAMessage = MessageTools.second(payload);
		
		byte[][] entries = new byte[numberOfVoters][];
		int numberOfEntries = 0;

		// Loop over the input entries eliminating duplicates (under the assumption ballots are sorted)
		byte[] last = null;
		for( MessageSplitIter iter = new MessageSplitIter(ballotsAsAMessage); iter.notEmpty(); iter.next() ) {
			if (numberOfEntries > numberOfVoters) // too many entries
				throw new MalformedData("Too many entries");
			byte[] current = iter.current();
			if (last!=null && MessageTools.equal(current, last)) continue; // ignore duplicates
			last = current;
			byte[] currentDecr = decryptor.decrypt(current); // decrypt the current ballot
			if (currentDecr == null) continue; // decryption failed			
			byte[] elID = MessageTools.first(currentDecr);
			if (elID==null || !MessageTools.equal(elID, electionID)) continue; // wrong election ID
			entries[numberOfEntries] = MessageTools.second(currentDecr);
			++numberOfEntries;
		}

		// sort the entries
		Utils.sort(entries, 0, numberOfEntries);
		
		// format entries as one message
		byte[] entriesAsAMessage = Utils.concatenateMessageArray(entries, numberOfEntries);
		
		// add election id, tag and sign
		byte[] elID_entriesAsAMessage = MessageTools.concatenate(electionID, entriesAsAMessage);
		byte[] result = MessageTools.concatenate(Tag.BALLOTS, elID_entriesAsAMessage);
		byte[] signatureOnResult = signer.sign(result);
		byte[] signedResult = MessageTools.concatenate(result, signatureOnResult);
		
		return signedResult;
	}
	
	public Encryptor getEncryptor(){
		return decryptor.getEncryptor();
	}
	
	public Verifier getVerifier(){
		return signer.getVerifier();
	}
}
