package de.uni.trier.infsec.eVotingSystem.apps;

import static de.uni.trier.infsec.eVotingSystem.apps.AppUtils.readCharsFromFile;
import static de.uni.trier.infsec.eVotingSystem.core.Utils.errln;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import de.uni.trier.infsec.eVotingSystem.core.CollectingServer;
import de.uni.trier.infsec.eVotingSystem.core.FinalServer;
import de.uni.trier.infsec.eVotingSystem.core.Params;
import de.uni.trier.infsec.eVotingSystem.parser.ElectionManifest;
import de.uni.trier.infsec.eVotingSystem.parser.Keys;
import de.uni.trier.infsec.eVotingSystem.parser.KeysParser;
import de.uni.trier.infsec.functionalities.digsig.Signer;
import de.uni.trier.infsec.functionalities.digsig.Verifier;
import de.uni.trier.infsec.functionalities.pkenc.Decryptor;
import de.uni.trier.infsec.lib.network.NetworkError;
import de.uni.trier.infsec.lib.network.NetworkServer;
import de.uni.trier.infsec.utils.MessageTools;

public class FinalServerApp {
	private static FinalServer server = null;
	private static Verifier serversVerifier = null;
	private static ElectionManifest elManifest = null;
	
	public static void main(String[] args)  {	
		System.out.println("Creating the server...");
		setupServer();
		System.out.println("Waiting for the partial results...");
		run();
	}

	private static void setupServer() {
		AppUtils.deleteFile(AppParams.COLL_SERVER_RESULT_msg);
		
		elManifest=AppUtils.retrieveElectionManifest();
		
		// retrieve the CollectingServer private keys
		String filename=AppParams.PRIVATE_KEY_path + "FinalServer_PR.json";
		String keyJSON = null;
		try {
			keyJSON = readCharsFromFile(filename);
		} catch (IOException e) {
			errln("Unable to access: " + filename);
		}
		//		catch (FileNotFoundException e){
		//		System.out.println("Server not registered yet!");
		//		System.exit(0);
		//	} 
		//	catch (IOException e) {
		//		e.printStackTrace();
		//		System.exit(0);
		//	}
		Keys k = KeysParser.parseJSONString(keyJSON);
		if(k.encrKey==null || k.decrKey==null || k.signKey==null || k.verifKey==null)
				errln("Invalid Collecting Server's keys.");
					
		Decryptor decr = new Decryptor(k.encrKey, k.decrKey);
		Signer sign = new Signer(k.verifKey, k.signKey);
		serversVerifier = sign.getVerifier();		
		server = new FinalServer(elManifest, decr, sign);
//		try {
//			server = new FinalServer(AppParams.ELECTIONID, decryptor, signer);
//		}
//		catch (Exception e) {
//			System.out.println("Cannot create the server object");
//			System.exit(-1);
//		}
	}

	private static void run()  {
		try {
			NetworkServer.listenForRequests(AppParams.finServPort);
		}
		catch(NetworkError e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		while( true ) {
			try {
				byte[] request = NetworkServer.read(AppParams.finServPort);
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
				System.err.println("an error ocurred:");
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
			Helper.FinalEntry[] fes = Helper.finalResultAsText(result, serversVerifier, elManifest.getElectionID());
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
