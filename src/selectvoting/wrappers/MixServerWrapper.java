package selectvoting.wrappers;

import funct.digsig.Signer;
import funct.digsig.Verifier;
import funct.pkenc.Decryptor;
import selectvoting.core.MixServer;
import utils.Utilities;

public class MixServerWrapper {
	private static String string(byte[] message) { return Utilities.byteArrayToHexString(message); }
	private static byte[] message(String str)    { return Utilities.hexStringToByteArray(str); }
	private MixServer mixServ;
	
	public MixServerWrapper(String encKey, String decKey, String verifKey, String signKey, String precServVerifKey, String elId) {
		Decryptor decryptor = new Decryptor(message(encKey), message(decKey));
		Signer signer = new Signer(message(verifKey), message(signKey));
		Verifier colServVerif = new Verifier(message(precServVerifKey));
		mixServ = new MixServer(decryptor, signer, colServVerif, message(elId));
	}
	
	public class Result {
		public final boolean ok;
		public final String data;
		public Result(boolean ok, String data) { this.ok = ok;  this.data = data;}
	}
	
	public Result processBallots(String data) {
		try {
			byte[] result = mixServ.processBallots(message(data));
			return new Result(true, string(result));
		} catch(MixServer.MalformedData ex) {
			return new Result(false, ex.description);
		} catch(MixServer.ServerMisbehavior ex) {
			return new Result(false, ex.description);
		}
	}
}