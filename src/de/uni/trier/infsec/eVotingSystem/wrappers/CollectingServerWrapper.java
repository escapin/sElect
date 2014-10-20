package de.uni.trier.infsec.eVotingSystem.wrappers;

import de.uni.trier.infsec.eVotingSystem.core.CollectingServer;
import de.uni.trier.infsec.functionalities.digsig.Signer;
import de.uni.trier.infsec.functionalities.nonce.NonceGen;
import de.uni.trier.infsec.functionalities.pkenc.Decryptor;
import de.uni.trier.infsec.utils.Utilities;

public class CollectingServerWrapper 
{
	public class Result {
		public final boolean ok;
		public final String data;
		public Result(boolean ok, String data) { this.ok = ok;  this.data = data;}
	}

	private static String string(byte[] message) { return Utilities.byteArrayToHexString(message); }
	private static byte[] message(String str)    { return Utilities.hexStringToByteArray(str); }
	private NonceGen nonceGen = new NonceGen();

	private CollectingServer cs;

	public CollectingServerWrapper(String encKey, String decKey, String verifKey, String signKey, 
			                       String electionID, String[] voterIdentifiers) {
		Signer signer = new Signer(message(verifKey), message(signKey));
		Decryptor decryptor = new Decryptor(message(encKey), message(decKey));
		cs = new CollectingServer(decryptor, signer, message(electionID), voterIdentifiers);
	}

	public Result collectBallot(String voterID, String ballot) {
		try {
			byte[] receipt = cs.collectBallot(voterID, message(ballot));
			return receipt!=null ? new Result(true, string(receipt)) : new Result(false, "Unknown Error");
		}
		catch (CollectingServer.Error err) {
			return new Result(false, err.info);
		}
	}

	public String getResult() {
		return string(cs.getResult()); 
	}
	
	public String getFreshOTP() {
		return string(nonceGen.newNonce());
	}
}
