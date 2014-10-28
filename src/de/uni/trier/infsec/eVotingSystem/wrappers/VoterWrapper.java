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
	
	private final Verifier  colServVerif;
	private final Encryptor colServEnc;
	private final Encryptor finServEnc;
	
		
	public VoterWrapper(String colServVerifKey, String colServEncKey, String finServEncKey ) {
		colServVerif = new Verifier(message(colServVerifKey));
		colServEnc = new Encryptor(message(colServEncKey)); 
		finServEnc = new Encryptor(message(finServEncKey));		
	}
	
	public BallotInfo createBallot(int votersChoice) {	
		System.out.println("I'm here!");
		Voter.BallotInfo bi = Voter.createBallot(votersChoice, colServEnc, finServEnc);
		System.out.println("And now here!");
		return new BallotInfo(string(bi.ballot), string(bi.nonce), string(bi.innerBallot));
	}
	
	public boolean validateReceipt(String receipt, String electionID, String ballot) {
		return Voter.validateReceipt( message(receipt), message(electionID), message(ballot), colServVerif);
	}
}
