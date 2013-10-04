package de.uni.trier.infsec.eVotingSystem.apps;

import java.io.FileNotFoundException;
import java.io.IOException;

import de.uni.trier.infsec.eVotingSystem.coreSystem.CollectingServer;
import de.uni.trier.infsec.eVotingSystem.coreSystem.Params;
import de.uni.trier.infsec.functionalities.pki.PKI;
import de.uni.trier.infsec.functionalities.pkienc.Decryptor;
import de.uni.trier.infsec.functionalities.pkisig.Signer;
import de.uni.trier.infsec.lib.network.NetworkClient;
import de.uni.trier.infsec.lib.network.NetworkError;
import de.uni.trier.infsec.lib.network.NetworkServer;
import de.uni.trier.infsec.utils.MessageTools;

public class CollectingServerApp {

	private static CollectingServer server = null;
	
	public static void main(String[] args)  {	
		PKI.useRemoteMode();
		System.out.println("Creating the server...");
		setupServer();
		System.out.println("Running...");
		run();
	}

	private static void setupServer() {
		AppUtils.deleteFile(AppParams.FIN_SERVER_RESULT_file);
		
		byte[] serialized=null;
		try {
			serialized = AppUtils.readFromFile(AppParams.PATH_STORAGE + "server" + Params.SERVER1ID + ".info");
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
		assert( idFromMsg == Params.SERVER1ID );
		byte[] decr_sig = MessageTools.second(serialized);
		byte[] decryptorMsg = MessageTools.first(decr_sig);
		byte[] signerMsg = MessageTools.second(decr_sig);		
		Decryptor decryptor = Decryptor.fromBytes(decryptorMsg);
		Signer signer = Signer.fromBytes(signerMsg);
				
		server = new CollectingServer(AppParams.ELECTIONID, decryptor, signer);
	}

	private static void run()  {
		try {
			NetworkServer.listenForRequests(AppParams.SERVER1_PORT);
		}
		catch(NetworkError e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		boolean posted = false;
		
		while( true ) { // run forever
			try {
				byte[] request = NetworkServer.nextRequest(AppParams.SERVER1_PORT);
				if (request != null) {
					System.out.println("reqeuest coming");
					byte[] response = server.collectBallot(request);
					NetworkServer.response(response);
					System.out.println("responce sent");
				} else {				
					Thread.sleep(500);
				}
			}
			catch (Exception e) {
				System.out.println("an error ocurred:");
				e.printStackTrace();
			}
			
			if (itsOver() && !posted) {
				System.out.println("Posting result...");
				postResult();
				System.out.println("done.");
				posted = true;
			}
		}
	}
	
	private static void postResult() {
		// get the official (signed) partial result:
		byte[] result = server.getResult();
					
		// write result to a file:
		String result_fname = AppParams.COLL_SERVER_RESULT_file;
		try {
			AppUtils.storeAsFile(result, result_fname);
		}
		catch (Exception e) {
			System.out.println("Problems with writing the result to a file!");
		}
	
		// send result to the final server:
		try {
			NetworkClient.send(result, AppParams.SERVER2_NAME, AppParams.SERVER2_PORT);
		}
		catch (NetworkError e) {
			System.out.println("Problems with sending the result to the final server!");
		}
		
		// TODO: send the official result along with formatted result to the bulletin board
	}
	
	
	/**
	 * Determines whether the voting phase is over
	 */
	// TODO: we should come up with better way to determine when the voting phase is over.
	// Perhaps the system should be triggered by a human operator. Or maybe just a fixed time?
	private static boolean itsOver() {
		return server.getNumberOfBallots() >= AppParams.ALLOWEDVOTERS;
	}
}
