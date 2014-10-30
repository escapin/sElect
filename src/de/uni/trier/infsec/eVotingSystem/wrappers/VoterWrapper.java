package de.uni.trier.infsec.eVotingSystem.wrappers;

import de.uni.trier.infsec.eVotingSystem.core.Voter;
import de.uni.trier.infsec.functionalities.digsig.Verifier;
import de.uni.trier.infsec.functionalities.pkenc.Encryptor;
import de.uni.trier.infsec.utils.Utilities;

public class VoterWrapper {

	public static class BallotInfo {
		public final String ballot;
		public final String nonce;
		public final String innerBallot;
		public BallotInfo(String ballot, String nonce, String innerBallot) {
			this.ballot = ballot;
			this.nonce = nonce;
			this.innerBallot = innerBallot;
		}
	}

	private static String string(byte[] message) { return Utilities.byteArrayToHexString(message); }
	private static byte[] message(String str)    { return Utilities.hexStringToByteArray(str); } 

	private final Voter voter;

	public VoterWrapper(String electionID, String colServEncKey, String colServVerifKey, String finServEncKey ) {
		Verifier colServVerif = new Verifier(message(colServVerifKey));
		Encryptor colServEnc = new Encryptor(message(colServEncKey));
		Encryptor finServEnc = new Encryptor(message(finServEncKey));
		voter = new Voter(message(electionID), colServEnc, colServVerif, finServEnc);
	}

	public BallotInfo createBallot(int votersChoice) {	
		Voter.BallotInfo bi = voter.createBallot(votersChoice);
		return new BallotInfo(string(bi.ballot), string(bi.nonce), string(bi.innerBallot));
	}

	public boolean validateReceipt(String receipt, String innerBallot) {
		return voter.validateReceipt( message(receipt), message(innerBallot));
	}
}
