package selectvoting.system.wrappers;

import infsec.utils.Utilities;
import functionalities.digsig.Signer;
import functionalities.digsig.Verifier;
import functionalities.pkenc.Decryptor;
import selectvoting.system.core.MixServer;

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