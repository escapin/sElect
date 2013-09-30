package de.uni.trier.infsec.eVotingSystem.apps;

import de.uni.trier.infsec.eVotingSystem.coreSystem.Utils.MessageSplitIter;
import de.uni.trier.infsec.functionalities.pkisig.Verifier;
import de.uni.trier.infsec.utils.MessageTools;
import de.uni.trier.infsec.utils.Utilities;

public class Helper {

	public static class HelperError extends Exception {
		private String info;
		public HelperError(String info) {
			this.info = info;
		}
		public String toString() {
			return info;
		}
	}
	
	private static void error(String info) throws HelperError {
		throw new HelperError(info);
	}
	
	public static class FinalEntry {
		public int vote;
		public String nonce;
		public FinalEntry(int vote, String nonce) {
			this.vote = vote;
			this.nonce = nonce;
		}
	}
	
	public static FinalEntry[] finalResultAsText(byte[] signedFinalResult, Verifier serverVerif, byte[] electionID) throws HelperError {
		byte[] result = MessageTools.first(signedFinalResult);
		byte[] signature = MessageTools.second(signedFinalResult);
		// check the signature
		if (!serverVerif.verify(signature, result)) 
			error("Invalid signature");
		
		// check the election id
		byte[] elid = MessageTools.first(result);
		if (!MessageTools.equal(elid, electionID))
			error("The election ID in the receipt does not match the one in the final result");

		byte[] entriesAsMessage = MessageTools.second(result);

		// count the number of entries
		int numberOfEntries = 0;
		for( MessageSplitIter iter = new MessageSplitIter(entriesAsMessage); iter.notEmpty(); iter.next() )
				++numberOfEntries;
		
		// construct a list with result
		FinalEntry[] fes = new FinalEntry[numberOfEntries];
		int count = 0;
		for( MessageSplitIter iter = new MessageSplitIter(entriesAsMessage); iter.notEmpty(); iter.next() ) {
			byte[] nonce_vote = iter.current();
			byte[] nonce = MessageTools.first(nonce_vote);
			byte[] vote  = MessageTools.second(nonce_vote);
			fes[count++] = new FinalEntry(MessageTools.byteArrayToInt(vote), Utilities.byteArrayToHexString(nonce));
		}
		
		return fes;
	}
	
}
