package selectvoting.system.wrappers;

import de.unitrier.infsec.functionalities.digsig.Signer;
import de.unitrier.infsec.functionalities.digsig.Verifier;
import de.unitrier.infsec.functionalities.pkenc.Decryptor;
import de.unitrier.infsec.utils.Utilities;
import selectvoting.system.core.MixServer;

public class MixServerWrapperMain {
	private static String string(byte[] message) { return Utilities.byteArrayToHexString(message); }
	private static byte[] message(String str)    { return Utilities.hexStringToByteArray(str); }
	private static MixServer mixServ;
	
	
	public static void main(String[] args) throws Exception{
		// 9 args: 7 to create MixServerWrapper + 2 path file
		if(args.length!=9)
			throw new Exception("Wrong Number of Arguments");
		byte[] encKey = message(args[0]);
		byte[] decKey = message(args[1]);
		byte[] verifKey = message(args[2]);
		byte[] signKey = message(args[3]);
		byte[] precServVerifKey = message(args[4]);
		byte[] elId = message(args[5]);
		int numberOfVoters = Integer.parseInt(args[6]);
		
		String inputFile_path = args[7];
		String outputFile_path = args[8];
		
		Decryptor decryptor = new Decryptor(encKey, decKey);
		Signer signer = new Signer(verifKey, signKey);
		Verifier colServVerif = new Verifier(precServVerifKey);
		mixServ = new MixServer(decryptor, signer, colServVerif, elId, numberOfVoters);
		byte[] input = dataFromFile(inputFile_path);
		try {
			byte[] result = mixServ.processBallots(input);
			storeAsFile(result, outputFile_path);			
		} catch(MixServer.MalformedData ex) {
			// FIXME: write to stdout
			//return new Result(false, ex.description);
		} catch(MixServer.ServerMisbehavior ex) {
			// FIXME: write to stdout
			//return new Result(false, ex.description);
		}
	}	
	
//	public MixServerWrapper(String encKey, String decKey, String verifKey, String signKey, String precServVerifKey, String elId, int numberOfVoters) {
//		Decryptor decryptor = new Decryptor(message(encKey), message(decKey));
//		Signer signer = new Signer(message(verifKey), message(signKey));
//		Verifier colServVerif = new Verifier(message(precServVerifKey));
//		mixServ = new MixServer(decryptor, signer, colServVerif, message(elId), numberOfVoters);
//	}
//	
//	public class Result {
//		public final boolean ok;
//		public final String data;
//		public Result(boolean ok, String data) { this.ok = ok;  this.data = data;}
//	}
//	
//	public Result processBallots(String data) {
//		try {
//			byte[] result = mixServ.processBallots(message(data));
//			return new Result(true, string(result));
//		} catch(MixServer.MalformedData ex) {
//			return new Result(false, ex.description);
//		} catch(MixServer.ServerMisbehavior ex) {
//			return new Result(false, ex.description);
//		}
//	}
	
	private static byte[] dataFromFile(String path){
		return null;
	}
	
	private static void storeAsFile(byte[] data , String path){
	}
	
	
}