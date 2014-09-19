package de.uni.trier.infsec.eVotingSystem.apps;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import de.uni.trier.infsec.eVotingSystem.core.FinalServer;
import de.uni.trier.infsec.eVotingSystem.core.Params;
import de.uni.trier.infsec.functionalities.digsig.Signer;
import de.uni.trier.infsec.functionalities.digsig.Verifier;
import de.uni.trier.infsec.functionalities.pki.PKI;
import de.uni.trier.infsec.functionalities.pkienc.Decryptor;
import de.uni.trier.infsec.lib.network.NetworkError;
import de.uni.trier.infsec.lib.network.NetworkServer;
import de.uni.trier.infsec.utils.MessageTools;

public class FinalServerApp {
	private static FinalServer server = null;
	private static Verifier serversVerifier = null;
	
	public static void main(String[] args)  {	
		PKI.useRemoteMode();
		System.out.println("Creating the server...");
		setupServer();
		System.out.println("Waiting for the partial results...");
		run();
	}

	private static void setupServer() {
		AppUtils.deleteFile(AppParams.COLL_SERVER_RESULT_msg);
		
		byte[] serialized=null;
		try {
			serialized = AppUtils.readBytesFromFile(AppParams.PATH_STORAGE + "server" + Params.SERVER2ID + ".info");
		} 
		catch (FileNotFoundException e){
			System.out.println("Server not registered yet!");
			System.exit(0);
		} 
		catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		
		byte[] idMsg =  MessageTools.first(serialized);
		int idFromMsg = MessageTools.byteArrayToInt(idMsg);
		assert( idFromMsg == Params.SERVER2ID );
		byte[] decr_sig = MessageTools.second(serialized);
		byte[] decryptorMsg = MessageTools.first(decr_sig);
		byte[] signerMsg = MessageTools.second(decr_sig);		
		Decryptor decryptor = Decryptor.fromBytes(decryptorMsg);
		Signer signer = Signer.fromBytes(signerMsg);
		serversVerifier = signer.getVerifier();
		
		try {
			server = new FinalServer(AppParams.ELECTIONID, decryptor, signer);
		}
		catch (Exception e) {
			System.out.println("Cannot create the server object");
			System.exit(-1);
		}
	}

	private static void run()  {
		try {
			NetworkServer.listenForRequests(AppParams.SERVER2_PORT);
		}
		catch(NetworkError e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		while( true ) {
			try {
				byte[] request = NetworkServer.read(AppParams.SERVER2_PORT);
				if (request != null) {
					System.out.println("reqeuest coming");
					byte[] result = server.processTally(request);
					System.out.println("result computed");
					postResult(result);
					break;
				} else {				
					Thread.sleep(500);
				}
			}
			catch (Exception e) {
				System.out.println("an error ocurred:");
				e.printStackTrace();
			}
		}
	}
	
	private static void postResult(byte[] result) {
		// write result to a file
		String result_fname = AppParams.FIN_SERVER_RESULT_msg;
		try {
			AppUtils.storeAsFile(result, result_fname);
		}
		catch (Exception e) {
			System.out.println("Problems with writing the result to a file!");
		}
		
		// write result to text file (as a readable text)
		try {
			Helper.FinalEntry[] fes = Helper.finalResultAsText(result, serversVerifier, AppParams.ELECTIONID);
			try {
		        BufferedWriter out = new BufferedWriter(new FileWriter(AppParams.FINAL_RESULT_file));
		        for (Helper.FinalEntry e : fes) {
		            out.write(e.vote + " \t" + e.nonce + "\n");
		            System.out.println(e.vote);
		        }
		        out.close();
		        } catch (IOException e) {}
		}
		catch (Exception e) {
			System.out.println("Problems with writing the result into a text file!");
		}
	}

}
