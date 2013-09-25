package de.uni.trier.infsec.eVotingSystem.apps;

import java.io.FileNotFoundException;
import java.io.IOException;

import de.uni.trier.infsec.eVotingSystem.coreSystem.CollectingServer;
import de.uni.trier.infsec.eVotingSystem.coreSystem.Params;
import de.uni.trier.infsec.functionalities.pki.PKI;
import de.uni.trier.infsec.functionalities.pkienc.Decryptor;
import de.uni.trier.infsec.functionalities.pkisig.Signer;
import de.uni.trier.infsec.lib.network.NetworkError;
import de.uni.trier.infsec.lib.network.NetworkServer;
import de.uni.trier.infsec.utils.MessageTools;

public class CollectingServerApp {

	public static void main(String[] args)  {	
		PKI.useRemoteMode();
		System.out.println("Creating the server...");
		CollectingServer server = setupServer();
		System.out.println("Running...");
		run(server);
	}

	private static CollectingServer setupServer() {
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
				
		return new CollectingServer(AppParams.electionID, decryptor, signer);
	}

	private static void run(CollectingServer server)  {
		try {
			NetworkServer.listenForRequests(AppParams.SERVER_PORT);
		}
		catch(NetworkError e) {
			System.out.println("Server not registered yet!");
			System.exit(0);
		}
		
		while(true){
			try {
				byte[] request = NetworkServer.nextRequest(AppParams.SERVER_PORT);
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
		}
	}

	
}
