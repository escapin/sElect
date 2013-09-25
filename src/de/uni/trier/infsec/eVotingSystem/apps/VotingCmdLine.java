package de.uni.trier.infsec.eVotingSystem.apps;

import java.io.FileNotFoundException;
import java.io.IOException;

import de.uni.trier.infsec.eVotingSystem.coreSystem.Voter;
import de.uni.trier.infsec.functionalities.pki.PKI;
import de.uni.trier.infsec.functionalities.pkienc.Decryptor;
import de.uni.trier.infsec.functionalities.pkisig.Signer;
import de.uni.trier.infsec.lib.network.NetworkClient;
import de.uni.trier.infsec.utils.MessageTools;

public class VotingCmdLine 
{
	public static void main(String[] args) 
	{	
		int voterID = -1;
		byte[] candidate = null;

		// Parse arguments:

		if (args.length != 2 ) {
			out("Wrong number of Arguments!");
			out("Expected: VotingCmdLine <user_id [int]> <candidate [String]>");
			out("Example: VotingCmdLine 07 Alice");
			System.exit(-1);
		} 
		else {
			try {				
				voterID = Integer.parseInt(args[0]);
				candidate = args[1].getBytes();
				// String str_cand = new String(candidate);
				//out(candidate.toString());
				// out(str_cand);
				
			} catch (Exception e) {
				out("Something is wrong with arguments!");
				e.printStackTrace();
				System.exit(-1);
			}
		}

		try {
			// De-serialize keys and create cryptographic functionalities:
			byte[] serialized=null;
			try {
				serialized = AppUtils.readFromFile(AppParams.PATH_STORAGE + "voter" + voterID + ".info");
			} 
			catch (FileNotFoundException e){
				System.out.println("Voter " + voterID + " not registered!");
				System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(0);
			}
			byte[] idMsg =  MessageTools.first(serialized);
			int idFromMsg = MessageTools.byteArrayToInt(idMsg);
			assert( idFromMsg == voterID );
			byte[] decr_sig = MessageTools.second(serialized);
			byte[] decryptorMsg = MessageTools.first(decr_sig);
			byte[] signerMsg = MessageTools.first(decr_sig);		
			Decryptor decryptor = Decryptor.fromBytes(decryptorMsg);
			Signer signer = Signer.fromBytes(signerMsg);

			// Initialize the interface to the public key infrastructure:
			PKI.useRemoteMode();

			// Create the voter:
			out("Creating a voter object.");
			Voter voter = new Voter(voterID, AppParams.electionID, decryptor, signer);
			
			// Create a ballot;
			out("Creating a ballot with candidate " + new String(candidate));
			byte[] ballot = voter.createBallot(candidate);
			
			// Send the ballot:
			out("Sending the ballot to the server");
			byte[] response = NetworkClient.sendRequest(ballot, AppParams.SERVER_NAME, AppParams.SERVER_PORT);
			
			// Validate the server's response:
			Voter.ResponseTag responseTag = voter.validateResponse(response);
			
			out("Response of the server: " + responseTag);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}	
	
	
	private static void out(String s) {
		System.out.println(s);
	}
}
