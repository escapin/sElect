package de.uni.trier.infsec.eVotingSystem.wrappers;

import de.uni.trier.infsec.eVotingSystem.core.CollectingServer;
import de.uni.trier.infsec.functionalities.digsig.Signer;
import de.uni.trier.infsec.utils.Utilities;


public class CollectingServerWrapper 
{
	private static String string(byte[] message) { return Utilities.byteArrayToHexString(message); }
	private static byte[] message(String str)    { return Utilities.hexStringToByteArray(str); } 

	private CollectingServer cs;

	public CollectingServerWrapper(String verifKey, String signKey, String electionID, String[] voterIdentifiers) {
		Signer signer = new Signer(message(verifKey), message(signKey));
		cs = new CollectingServer(signer, message(electionID), voterIdentifiers);
	}
	
	public String collectBallot(String voterID, String ballot) {
		byte[] receipt = cs.collectBallot(voterID, message(ballot));
		return receipt!=null ? string(receipt) : "";
	}
}


/*
public class VoterWrapper {

	private static String string(byte[] message) { return Utilities.byteArrayToHexString(message); }
	private static byte[] message(String str)    { return Utilities.hexStringToByteArray(str); } 

	private Verifier  colServVerif;
	private Encryptor finServEnc;


	public VoterWrapper(String colServVerifKey, String finServEncKey ) {
		colServVerif = new Verifier(message(colServVerifKey));
		finServEnc = new Encryptor(message(finServEncKey));		
	}

	public String createBallot(int votersChoice) {	
		byte[] ballot = Voter.createBallot(votersChoice, finServEnc);
		return string(ballot);
	}

	public boolean validateReceipt(String receipt, String electionID, String ballot) {
		return Voter.validateReceipt( message(receipt), message(electionID), message(ballot), colServVerif);
	}
}
 */