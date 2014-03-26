package de.uni.trier.infsec.eVotingSystem.apps;

import static de.uni.trier.infsec.utils.MessageTools.concatenate;
import static de.uni.trier.infsec.eVotingSystem.core.Utils.outl;
import de.uni.trier.infsec.eVotingSystem.core.Params;
import de.uni.trier.infsec.eVotingSystem.core.Utils;
import de.uni.trier.infsec.eVotingSystem.core.Voter;
import de.uni.trier.infsec.eVotingSystem.core.Utils.MessageSplitIter;
import de.uni.trier.infsec.functionalities.pki.PKI;
import de.uni.trier.infsec.functionalities.pkisig.RegisterSig;
import de.uni.trier.infsec.functionalities.pkisig.Verifier;
import de.uni.trier.infsec.utils.MessageTools;
import de.uni.trier.infsec.utils.Utilities;

public class VerifierCmdLine {
	
	private static Verifier server1ver = null;
	private static Verifier server2ver = null;

	
	public static void main(String[] args) throws Exception
	{	
		// fetch the verifiers of the servers
		PKI.useRemoteMode();
		server1ver = RegisterSig.getVerifier(Params.SERVER1ID, Params.SIG_DOMAIN);
		server2ver = RegisterSig.getVerifier(Params.SERVER2ID, Params.SIG_DOMAIN);
		
		
		int voterID = -1;

		// Parse arguments:
		if (args.length != 3 ) {
			outl("Wrong number of Arguments!");
			outl("Expected: VerifierCmdLine <receipt_fname> <partial_result_fname> <final_result_fname>");
			System.exit(-1);
		} 
		String receiptFileName = args[0];
		String partialResultFileName = args[1];
		String finalResultFileName = args[2];
		
		// Read the receipt:
		byte[] receiptMsg = null;
		try {
			receiptMsg = AppUtils.readFromFile(receiptFileName);
		}
		catch (Exception e) {
			outl("Cannot read the receipt file.");
			e.printStackTrace();
			System.exit(-1);
		}
		Voter.Receipt receipt = Voter.Receipt.fromMessage(receiptMsg);

		// Read the partial result:
		byte[] signedPartialResult = null;
		try {
			signedPartialResult = AppUtils.readFromFile(partialResultFileName);
		}
		catch (Exception e) {
			outl("Cannot read the file with the partial result.");
			e.printStackTrace();
			System.exit(-1);
		}
		
		// Read the final result:
		byte[] signedFinalResult = null;
		try {
			signedFinalResult = AppUtils.readFromFile(finalResultFileName);
		}
		catch (Exception e) {
			outl("Cannot read the file with the final result.");
			e.printStackTrace();
			System.exit(-1);
		}

		
		// TODO: verify the signature in the receipt
		if (!signatureInReceiptOK(receipt)) {
			outl("\nPROBLEM: Server's signature in your recipt is not correct!");
			outl("You may not be able to prove that you are cheated on, if you are.");
		}
		
		boolean ok = true;
		
		// Print out (part of) the receipt:
		outl("\nRECEIPT:");
		outl("    election ID  = " + new String(receipt.electionID) );
		outl("    candidate number   = " + receipt.voterChoice );
		outl("    nonce        = " + Utilities.byteArrayToHexString(receipt.nonce));
		
		// Check whether the partial results contains the inner ballot from the receipt:
		if (!partialResultOK(receipt, signedPartialResult))
			ok = false;
		
		// Check whether the final result contains your nonce and print out the vote:
		if (!finalResultOK(receipt, signedFinalResult))
			ok = false;
		
		if (ok) 
			outl("\nEverything seems ok.");
	}

	private static boolean signatureInReceiptOK(Voter.Receipt receipt) throws Exception {
		byte[] expected_signed_msg = concatenate(Params.ACCEPTED, concatenate(receipt.electionID, receipt.innerBallot));
		return server1ver.verify(receipt.serverSignature, expected_signed_msg);
	}
	
	private static boolean partialResultOK(Voter.Receipt receipt, byte[] signedPartialResult) {
		byte[] result = MessageTools.first(signedPartialResult);
		byte[] signature = MessageTools.second(signedPartialResult);
		
		// check the signature
		if (!server1ver.verify(signature, result)) {
			outl("PROBLEM: Invalid signature on the partial result.");
			return false;
		}
		
		// check the election id
		byte[] elid = MessageTools.first(result);
		if (!MessageTools.equal(elid, receipt.electionID)) {
			outl("PROBLEM: The election ID in the receipt does not match the one in the partial result");
			return false;
		}
		
		// check if the result contain the inner ballot from the receipt
		byte[] ballotsAsMessage = MessageTools.first(MessageTools.second(result));
		if (!Utils.contains(ballotsAsMessage, receipt.innerBallot)) {
			outl("\nPROBLEM: The partial result does not contain your inner ballot!");
			outl(Utilities.byteArrayToHexString(receipt.innerBallot));
			return false;
		}
		
		return true;
	}
	
	private static boolean finalResultOK(Voter.Receipt receipt, byte[] signedFinalResult) {
		byte[] result = MessageTools.first(signedFinalResult);
		byte[] signature = MessageTools.second(signedFinalResult);
		
		// check the signature
		if (!server2ver.verify(signature, result)) {
			outl("PROBLEM: Invalid signature on the final result.");
			return false;
		}
		
		// check the election id
		byte[] elid = MessageTools.first(result);
		if (!MessageTools.equal(elid, receipt.electionID)) {
			outl("PROBLEM: The election ID in the receipt does not match the one in the final result");
			return false;
		}
		
		byte[] entriesAsMessage = MessageTools.second(result);
		
		// look up for our nonce:
		int candidateNumber=0;
		byte[] vote = null;
		for( MessageSplitIter iter = new MessageSplitIter(entriesAsMessage); vote==null && iter.notEmpty(); iter.next() ) { 
			if (MessageTools.equal(MessageTools.first(iter.current()), receipt.nonce)) // nonce found
				vote = MessageTools.second(iter.current());
		}
		candidateNumber=MessageTools.byteArrayToInt(vote);
		if (vote == null) {
			outl("\nPROBLEM: The final result does not contain your nonce!");
			outl(Utilities.byteArrayToHexString(receipt.nonce));
			return false;
		}
		else if (candidateNumber!=receipt.voterChoice) {
			outl("\nPROBLEM: In the final result, the vote next to your nonce is not your vote!");
			outl("Found candidate number: " + candidateNumber);
			return false;
		}
		else {
			outl("\nYour nonce is in the result along with next the number of the candidate you voted:");
			outl("" + candidateNumber);
		}
		
		return true;
	}
	


}
