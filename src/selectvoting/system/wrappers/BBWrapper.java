package selectvoting.system.wrappers;

import selectvoting.functionalities.digsig.Verifier;
import selectvoting.system.core.Utils.MessageSplitIter;
import selectvoting.utils.MessageTools;
import selectvoting.utils.Utilities;

public class BBWrapper {
	
	public class Result {
		public final boolean ok;
		public final String data;
		public Result(boolean ok, String data) { this.ok = ok;  this.data = data;}
	}
	
	private static String string(byte[] message) { return Utilities.byteArrayToHexString(message); }
	private static byte[] message(String str)    { return Utilities.hexStringToByteArray(str); }

	private Verifier finServVerif;
	private byte[] electionID;
	
	
	public BBWrapper(String finServVerifKey, String electionID) {
		this.finServVerif = new Verifier(message(finServVerifKey));
		this.electionID = message(electionID);
	}

	public Result finalResultAsText(String signedFinalResult)  {
		byte[] signedFinalResultMsg = message(signedFinalResult);
		byte[] result = MessageTools.first(signedFinalResultMsg);
		byte[] signature = MessageTools.second(signedFinalResultMsg);
		
		// check the signature
		if (!finServVerif.verify(signature, result)) 
			return new Result(false, "Invalid signature");
		
		// check the election id
		byte[] elid = MessageTools.first(result);
		if (!MessageTools.equal(elid, electionID))
			return new Result(false, "The election ID in the receipt does not match the one in the final result");

		byte[] entriesAsMessage = MessageTools.second(result);

		// construct the text of the result
		String res = "";
		String sep = "";
		for( MessageSplitIter iter = new MessageSplitIter(entriesAsMessage); iter.notEmpty(); iter.next() ) {
			byte[] nonce_vote = iter.current();
			byte[] nonce = MessageTools.first(nonce_vote);
			byte[] vote  = MessageTools.second(nonce_vote);
			int candidateNr = MessageTools.byteArrayToInt(vote);
			res = res + sep + candidateNr + " " + string(nonce);
			sep = "\n";
		}
		
		return new Result(true, res);
	}
}
