package selectvoting.system.wrappers;

import de.unitrier.infsec.functionalities.digsig.Signer;
import de.unitrier.infsec.functionalities.pkenc.Decryptor;
import de.unitrier.infsec.utils.Utilities;
import selectvoting.system.core.CollectingServer;

public class CollectingServerWrapper 
{

	private static String string(byte[] message) { return Utilities.byteArrayToHexString(message); }
	private static byte[] message(String str)    { return Utilities.hexStringToByteArray(str); }

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
}
