package de.uni.trier.infsec.eVotingSystem.wrappers;

import de.uni.trier.infsec.eVotingSystem.core.CollectingServer;
import de.uni.trier.infsec.functionalities.digsig.Signer;
import de.uni.trier.infsec.functionalities.nonce.NonceGen;
import de.uni.trier.infsec.utils.Utilities;

public class CollectingServerWrapper 
{
	private static String string(byte[] message) { return Utilities.byteArrayToHexString(message); }
	private static byte[] message(String str)    { return Utilities.hexStringToByteArray(str); }
	private NonceGen nonceGen = new NonceGen();

	private CollectingServer cs;

	public CollectingServerWrapper(String verifKey, String signKey, String electionID, String[] voterIdentifiers) {
		Signer signer = new Signer(message(verifKey), message(signKey));
		cs = new CollectingServer(signer, message(electionID), voterIdentifiers);
	}

	public String collectBallot(String voterID, String ballot) {
		byte[] receipt = cs.collectBallot(voterID, message(ballot));
		return receipt!=null ? string(receipt) : "";
	}

	public String getResult() {
		return string(cs.getResult()); 
	}
	
	public String getFreshOTP() {
		return string(nonceGen.newNonce());
	}
}
