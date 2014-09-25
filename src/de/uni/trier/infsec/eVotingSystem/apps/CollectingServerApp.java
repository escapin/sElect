package de.uni.trier.infsec.eVotingSystem.apps;

import java.io.IOException;

import de.uni.trier.infsec.eVotingSystem.core.CollectingServer;
import de.uni.trier.infsec.eVotingSystem.core.CollectingServer.MalformedMessage;
import de.uni.trier.infsec.eVotingSystem.parser.ElectionManifest;
import de.uni.trier.infsec.eVotingSystem.parser.Keys;
import de.uni.trier.infsec.eVotingSystem.parser.KeysParser;
import de.uni.trier.infsec.functionalities.digsig.Signer;
import de.uni.trier.infsec.functionalities.pkenc.Decryptor;
import de.uni.trier.infsec.lib.network.NetworkClient;
import de.uni.trier.infsec.lib.network.NetworkError;
import de.uni.trier.infsec.lib.network.NetworkServer;
import static de.uni.trier.infsec.eVotingSystem.apps.AppUtils.deleteFile;
import static de.uni.trier.infsec.eVotingSystem.apps.AppUtils.readCharsFromFile;
import static de.uni.trier.infsec.eVotingSystem.core.Utils.errln;

public class CollectingServerApp {

	private static CollectingServer server = null;
	private static ElectionManifest elManifest = null;
	
	public static void main(String[] args)  {	
		System.out.println("Creating the server...");
		
		setupServer();
		System.out.println("Running...");
		run();
	}

	private static void setupServer() {
		
		deleteFile(AppParams.FIN_SERVER_RESULT_msg);
		
		//		try {} catch (FileNotFoundException e){
		//		System.out.println("Server not registered yet!");
		//		System.exit(-1);
		//	} catch (IOException e) {
		//		e.printStackTrace();
		//		System.exit(-1);
		//	}
		
		elManifest=AppUtils.retrieveElectionManifest();
		
		// retrieve the CollectingServer private keys
		String filename=AppParams.PRIVATE_KEY_path + "CollectingServer_PR.json";
		String keyJSON = null;
		try {
			keyJSON = readCharsFromFile(filename);
		} catch (IOException e) {
			errln("Unable to access: " + filename);
		}
		Keys k = KeysParser.parseJSONString(keyJSON);
		if(k.encrKey==null || k.decrKey==null || k.signKey==null || k.verifKey==null)
			errln("Invalid Collecting Server's keys.");
		
		Decryptor decr = new Decryptor(k.encrKey, k.decrKey);
		Signer sign = new Signer(k.verifKey, k.signKey);
		
		server = new CollectingServer(elManifest, decr, sign);
	}

	private static void run()  {
		try {
			NetworkServer.listenForRequests(AppParams.colServPort);
		}
		catch(NetworkError e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		boolean posted = false;
		boolean electionStatus = false;
		while( true ) { // run forever
			if(System.currentTimeMillis()>elManifest.getEndTime() && !electionStatus){
				electionStatus=posted=true;
				System.out.println("\tElection Already Closed!");
			}
			else if(System.currentTimeMillis()>elManifest.getStartTime() && !electionStatus){
				electionStatus=true;
				System.out.println("\tElection Opened!");
				long millis=elManifest.getEndTime()-elManifest.getStartTime();
				long second = (millis / 1000) % 60;
				long minute = (millis / (1000 * 60)) % 60;
				long hour = (millis / (1000 * 60 * 60)) % 24;
				System.out.println("\tDuration:  " + String.format("%02dh:%02dm:%02ds", hour, minute, second)); 
			}
				
				
			try {
				byte[] request = NetworkServer.nextRequest(AppParams.colServPort);
				if (request != null) {
					System.out.println("reqeuest coming");
					byte[] response=null;
					try {
						response = server.collectBallot(request);
					} catch (MalformedMessage e) {
						System.out.println("Ballot malformed!");
					}
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
				System.out.println("\tElection Closed!");
				posted = true;
			}
		}
	}
	
	private static void postResult() {
		// get the official (signed) partial result:
		byte[] result = server.getResult();
					
		// write result to a file:
		String result_fname = AppParams.COLL_SERVER_RESULT_msg;
		try {
			AppUtils.storeAsFile(result, result_fname);
		}
		catch (Exception e) {
			System.out.println("Problems with writing the result to a file!");
		}
	
		// send result to the final server:
		try {
			NetworkClient.send(result, elManifest.getCollectingServer().uri.hostname, elManifest.getCollectingServer().uri.port);
		}
		catch (NetworkError e) {
			System.err.println("Problems with sending the result to the final server!");
		}
		
		// TODO: send the official result along with formatted result to the bulletin board
	}
	
	
	/**
	 * Determines whether the voting phase is over
	 */
	private static boolean itsOver() {
		//return server.getNumberOfBallots() >= AppParams.ALLOWEDVOTERS;
		return System.currentTimeMillis()>elManifest.getEndTime();
	}
}
