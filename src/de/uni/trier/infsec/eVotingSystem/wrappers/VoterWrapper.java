package de.uni.trier.infsec.eVotingSystem.wrappers;

import de.uni.trier.infsec.eVotingSystem.core.Voter;
import de.uni.trier.infsec.functionalities.digsig.Verifier;
import de.uni.trier.infsec.functionalities.pkenc.Encryptor;
import de.uni.trier.infsec.utils.Utilities;

public class VoterWrapper {
	
	public static class Ballots {
		public final String ballot;
		public final String innerBallot;
		public Ballots(String ballot, String innerBallot) {
			this.ballot = ballot;
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
	
	public Ballots createBallot(int votersChoice) {	
		System.out.println("I'm here!");
		Voter.Ballots ballots = Voter.createBallot(votersChoice, colServEnc, finServEnc);
		System.out.println("And now here!");
		return new Ballots(string(ballots.ballot), string(ballots.innerBallot));
	}
	
	public boolean validateReceipt(String receipt, String electionID, String ballot) {
		return Voter.validateReceipt( message(receipt), message(electionID), message(ballot), colServVerif);
	}
}
