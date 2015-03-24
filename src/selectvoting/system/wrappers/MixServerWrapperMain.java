package selectvoting.system.wrappers;

import static java.nio.file.StandardOpenOption.*;

import java.nio.file.*;
import java.io.*;
import java.nio.charset.Charset;

import de.unitrier.infsec.functionalities.digsig.Signer;
import de.unitrier.infsec.functionalities.digsig.Verifier;
import de.unitrier.infsec.functionalities.pkenc.Decryptor;
import de.unitrier.infsec.utils.Utilities;
import selectvoting.system.core.MixServer;

public class MixServerWrapperMain {
	private static String string(byte[] message) { return Utilities.byteArrayToHexString(message); }
	private static byte[] message(String str)    { return Utilities.hexStringToByteArray(str); }
	private static MixServer mixServ;
	
	/*
	 * 	ExitCode	*&*		description
	 *
	 * 		1		-->		MalformedData: Wrong signature
	 *   	2		-->		MalformedData: Wrong tag
	 *   	3		-->		MalformedData: Wrong election ID
	 *
	 *     -1		-->		ServerMisbehavior: Too many entries
	 *     -2		-->		ServerMisbehavior: Ballots not sorted
	 *     -3		-->		ServerMisbehavior: Duplicate ballots
	 *
	 *		10		-->		Wrapper: Wrong Number of Arguments
	 *		11		-->		Wrapper: [IOException] reading the file
	 *		12		-->		Wrapper: [IOException] writing the file
	 */
	public static void main(String[] args){
		// 9 args: 7 to create MixServerWrapper + 2 path file
		if(args.length!=9){
			System.out.println("[MixServerWrapper] Wrong Number of Arguments");
			System.exit(10);
		}
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
		Verifier precServVerif = new Verifier(precServVerifKey);
		//System.out.print("[MixServerWrapper] Creating the MixServer...");
		mixServ = new MixServer(decryptor, signer, precServVerif, elId, numberOfVoters);
		//System.out.println("OK!");
		String sInput = null;
		try {
			sInput = dataFromFile(inputFile_path);
		} catch (IOException e) {
			System.out.println("[MixServerWrapper] \t ***IOException*** \t reading the file: " + inputFile_path);
			System.out.println("\t\t" + e.getMessage());
			System.exit(11);
		} 
		System.out.println("[MixServerWrapper] Ballots read from the file: \t" + inputFile_path);
		//System.out.println("\n" + sInput + "\n");

		System.out.print("[MixServerWrapper] Processing the ballots...");
		byte[] input = message(sInput);
		byte[] result = null;
		try {
			result = mixServ.processBallots(input);			
		} catch(MixServer.MalformedData ex) {
			System.out.println("\n[MixServerWrapper] \t ***MalformedData*** \t" + ex.description);
			System.exit(ex.errCode);
		} catch(MixServer.ServerMisbehavior ex) {
			System.out.println("\n[MixServerWrapper] \t ***ServerMisbehavior*** \t" + ex.description);
			System.exit(ex.errCode);
		}
		
		System.out.println("done!");
		//System.out.println("\n" + string(result) + "\n");
		try{
			storeAsFile(string(result), outputFile_path);
		} catch (IOException e) {
			System.out.println("[MixServerWrapper] ***IOException*** \t writing the file: " + outputFile_path);
			System.out.println("\t\t" + e.getMessage());
			System.exit(12);
		}
		System.out.println("[MixServerWrapper] Results stored in: \t\t" + outputFile_path);
	}
	
	private static String dataFromFile(String path) throws IOException {
		Path file = Paths.get(path);
		BufferedReader reader = Files.newBufferedReader(file, Charset.defaultCharset()); 
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			/* WARN: the string in the var 'line' doesn't include any 
			 * line-termination characters: line feed ('\n'), a carriage return 
			 * ('\r'), or a carriage return followed immediately by a linefeed
			 */
			sb.append(line);
		}
		reader.close();
		return sb.toString();
	}
	
	private static void storeAsFile(String data, String path) throws IOException {
		Path file = Paths.get(path);
		InputStream is = new ByteArrayInputStream(data.getBytes());
	    Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING);
	}
}


/* Methods to read and write bytes */
//	private static byte[] dataFromFile(String path) throws IOException {
//	Path file = Paths.get(path);
//	byte[] data = Files.readAllBytes(file);
//	return data;
//}	
	
//	private static void storeAsFile(byte[] data , String path) throws IOException {
//		if(data==null)
//			return;
//		Path file = Paths.get(path);
//		/* default options:  CREATE, TRUNCATE_EXISTING, and WRITE
//		 * it opens the file for writing, creating the file if it doesn't exist, 
//		 * or initially truncating an existing regular-file to a size of 0
//		 */
//		Files.write(file, data);
//	}