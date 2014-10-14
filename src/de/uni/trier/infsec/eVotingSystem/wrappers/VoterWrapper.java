package de.uni.trier.infsec.eVotingSystem.wrappers;

import de.uni.trier.infsec.eVotingSystem.core.Voter;
import de.uni.trier.infsec.functionalities.digsig.Verifier;
import de.uni.trier.infsec.functionalities.pkenc.Encryptor;
import de.uni.trier.infsec.utils.Utilities;

public class VoterWrapper {
	private Voter voter;
	
	public VoterWrapper(String email, String electionID, String colServEncKey, String colServVerifKey, String finServEncKey ) {
		Encryptor colServEnc = new Encryptor(Utilities.hexStringToByteArray(colServEncKey));
		Verifier colServVerif = new Verifier(Utilities.hexStringToByteArray(colServVerifKey));
		Encryptor finServEnc = new Encryptor(Utilities.hexStringToByteArray(finServEncKey));
		voter = new Voter(	Utilities.stringAsBytes(email), 
							Utilities.hexStringToByteArray(electionID),
							colServEnc, colServVerif, finServEnc );
	}
	
	public String createBallot(int votersChoice, String otp) {
		System.out.println("-- FROM JAVA --");
		System.out.println(votersChoice);
		System.out.println(otp);
		System.out.println("-- END FROM JAVA --");
		
		byte[] ballot = voter.createBallot(votersChoice, Utilities.hexStringToByteArray(otp));
		return Utilities.byteArrayToHexString(ballot);
	}
	
}
