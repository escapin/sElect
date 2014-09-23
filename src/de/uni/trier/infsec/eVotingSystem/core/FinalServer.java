package de.uni.trier.infsec.eVotingSystem.core;

import java.util.Arrays;

import de.uni.trier.infsec.eVotingSystem.core.Utils.MessageSplitIter;
import de.uni.trier.infsec.eVotingSystem.parser.ElectionManifest;
import de.uni.trier.infsec.functionalities.digsig.Signer;
import de.uni.trier.infsec.functionalities.digsig.Verifier;
import de.uni.trier.infsec.functionalities.pkenc.Decryptor;
import de.uni.trier.infsec.lib.network.NetworkError;
import de.uni.trier.infsec.utils.MessageTools;

public class FinalServer 
{	
	// Cryptographic functionalities
	private final Signer signer;
	private final Decryptor decryptor;
	private final ElectionManifest elManifest;
	private final Verifier collectingServerVerif;
	
	// CLASSES
	/**
	 * Error thrown if the input data is ill-formed.
	 */
	public static class MalformedData extends Exception 
	{
		private String description;
		public MalformedData(String description) {
			this.description = description;
		}
		public String toString() {
			return "Final Server Error: " + description;
		}
	}

	
	// CONSTRUCTORS
	
	public FinalServer(ElectionManifest elManifest, Decryptor decryptor, Signer signer) {
		this.signer = signer;
		this.decryptor = decryptor;
		this.elManifest = elManifest;
		// fetch the functionalities of the collecting server
		byte[] colServVerifier = elManifest.getCollectingServer().verification_key;
		this.collectingServerVerif = new Verifier(colServVerifier); 
	}
	
	// PUBLIC METHODS
	
	/**
	 * Process data that supposed to be the input tally prepared and signed 
	 * by the collecting server. Returns the signed result of the election 
	 * (to be publicly posted). 
	 */
	public byte[] processTally(byte[] data) throws MalformedData {
		// verify the signature of server1
		byte[] payload = MessageTools.first(data);
		byte[] signature = MessageTools.second(data);
		if (!collectingServerVerif.verify(signature, payload))
			throw new MalformedData("Wrong signature");
		
		// check that election id in the processed data
		byte[] el_id = MessageTools.first(payload);
		if (!MessageTools.equal(el_id, elManifest.getElectionID()))
			throw new MalformedData("Wrong election ID");
		
		// retrieve and process ballots (store decrypted entries in 'entries')
		byte[] ballotsAsAMessage = MessageTools.first(MessageTools.second(payload)); 
		//FIXME: why do we include the list of voters if we ignore them?		
		byte[][] entries = new byte[Params.NumberOfVoters][];
		int numberOfEntries = 0;
		for( MessageSplitIter iter = new MessageSplitIter(ballotsAsAMessage); iter.notEmpty(); iter.next() ) {
			byte[] nonce_vote = decryptor.decrypt(iter.current());
			if (nonce_vote == null) // decryption failed
				throw new MalformedData("Wrong data (decryption failed)");
			entries[numberOfEntries] = nonce_vote;
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
		byte[] result = MessageTools.concatenate(elManifest.getElectionID(), entriesAsAMessage);
		byte[] signatureOnResult = signer.sign(result);
		byte[] signedResult = MessageTools.concatenate(result, signatureOnResult);
		return signedResult;
	}
}
