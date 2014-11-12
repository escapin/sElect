package selectvoting.system.wrappers;

import selectvoting.functionalities.digsig.Signer;
import selectvoting.functionalities.digsig.Verifier;
import selectvoting.functionalities.pkenc.Decryptor;
import selectvoting.system.core.FinalServer;
import selectvoting.utils.Utilities;

public class FinalServerWrapper {
	private static String string(byte[] message) { return Utilities.byteArrayToHexString(message); }
	private static byte[] message(String str)    { return Utilities.hexStringToByteArray(str); }
	private FinalServer fs;
	
	public FinalServerWrapper(String encKey, String decKey, String verifKey, String signKey, String csVerifKey, String elId, int numberOfVoters) {
		Decryptor decryptor = new Decryptor(message(encKey), message(decKey));
		Signer signer = new Signer(message(verifKey), message(signKey));
		Verifier colServVerif = new Verifier(message(csVerifKey));
		fs = new FinalServer(decryptor, signer, colServVerif, message(elId), numberOfVoters);
	}
	
	public class Result {
		public final boolean ok;
		public final String data;
		public Result(boolean ok, String data) { this.ok = ok;  this.data = data;}
	}
	
	public Result processTally(String data) {
		try {
			byte[] result = fs.processTally(message(data));
			return new Result(true, string(result));
		} catch(FinalServer.MalformedData ex) {
			return new Result(false, ex.description);
		}
	}
}