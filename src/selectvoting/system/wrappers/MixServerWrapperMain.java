package selectvoting.system.wrappers;

import infsec.utils.Utilities;

import java.nio.file.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

import functionalities.digsig.Signer;
import functionalities.digsig.Verifier;
import functionalities.pkenc.Decryptor;
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
		// 8 args: 6 to create MixServerWrapper + 2 path file
		if(args.length!=8){
			System.out.println("[MixServerWrapper] Wrong Number of Arguments");
			System.exit(10);
		}
		byte[] encKey = message(args[0]);
		byte[] decKey = message(args[1]);
		byte[] verifKey = message(args[2]);
		byte[] signKey = message(args[3]);
		byte[] precServVerifKey = message(args[4]);
		byte[] elId = message(args[5]);
		
		String inputFile_path = args[6];
		String outputFile_path = args[7];
		
		Decryptor decryptor = new Decryptor(encKey, decKey);
		Signer signer = new Signer(verifKey, signKey);
		Verifier precServVerif = new Verifier(precServVerifKey);
		mixServ = new MixServer(decryptor, signer, precServVerif, elId);
		String sInput = null;
		try {
			sInput = dataFromFile(inputFile_path);
		} catch (IOException e) {
			System.out.println("[MixServerWrapper] \t ***IOException*** \t reading the file: " + inputFile_path);
			System.out.print("\t\t");
			e.printStackTrace();
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
			dataToFile(string(result), outputFile_path);
		} catch (IOException e) {
			System.out.println("[MixServerWrapper] ***IOException*** \t writing the file: " + outputFile_path);
			System.out.print("\t\t");
			e.printStackTrace();
			System.exit(12);
		}
		System.out.println("[MixServerWrapper] Results stored in: \t\t" + outputFile_path);
	}
	
	private static String dataFromFile(String path) throws IOException {
		Path file = Paths.get(path);
		BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8);
		StringBuilder sb = new StringBuilder();
		for(int c=reader.read(); c>=0; c=reader.read()){
			if (c >= 65535) throw new IOException("Not a character");
			sb.append((char)c);
		}
		reader.close();
		return sb.toString();
	}
	
	private static void dataToFile(String data, String path) throws IOException {
		Path file = Paths.get(path);
		BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8, 
				StandardOpenOption.CREATE,				// create a file if it doesn't exist
				StandardOpenOption.TRUNCATE_EXISTING,	// or initially truncating an existing regular-file to a size of 0
				StandardOpenOption.WRITE); 				// open the file for writing
//		for(int i=0;i<data.length(); ++i)
//			writer.write(data.charAt(i));
		writer.write(data, 0, data.length());
		writer.flush();
		writer.close();
	}
}


/* Methods to read and write bytes */
//	private static byte[] dataFromFile(String path) throws IOException {
//	Path file = Paths.get(path);
//	byte[] data = Files.readAllBytes(file);
//	return data;
//}	
	
//	private static void dataToFile(byte[] data , String path) throws IOException {
//		if(data==null)
//			return;
//		Path file = Paths.get(path);
//		/* default options:  CREATE, TRUNCATE_EXISTING, and WRITE
//		 * it opens the file for writing, creating the file if it doesn't exist, 
//		 * or initially truncating an existing regular-file to a size of 0
//		 */
//		Files.write(file, data);
//	}