package de.uni.trier.infsec.eVotingSystem.apps;

import java.io.FileNotFoundException;
import java.io.IOException;

import de.uni.trier.infsec.eVotingSystem.bean.VoterID;
import de.uni.trier.infsec.eVotingSystem.core.Params;
import de.uni.trier.infsec.eVotingSystem.core.Voter;
import de.uni.trier.infsec.eVotingSystem.parser.ElectionManifest;
import de.uni.trier.infsec.eVotingSystem.parser.Keys;
import de.uni.trier.infsec.eVotingSystem.parser.KeysParser;
import de.uni.trier.infsec.functionalities.digsig.Signer;
import de.uni.trier.infsec.functionalities.pkenc.Decryptor;
import de.uni.trier.infsec.lib.network.NetworkClient;
import static de.uni.trier.infsec.utils.Utilities.byteArrayToHexString;
import static de.uni.trier.infsec.eVotingSystem.apps.AppUtils.readCharsFromFile;
import static de.uni.trier.infsec.eVotingSystem.core.Utils.errln;
import static de.uni.trier.infsec.eVotingSystem.core.Utils.outl;

public class VotingCmdLine 
{
	private static ElectionManifest elManifest = null;
	
	
	public static void main(String[] args) 
	{	
		int uniqueID = -1;
		//byte[] candidate = null;
		int candidateNumber=0;

		// Parse arguments:

		if (args.length != 2 ) {
			outl("Wrong number of Arguments!");
			outl("Expected: VotingCmdLine <user_id [int]> <candidate_number [int]>");
			outl("Example: VotingCmdLine 07 03");
			System.exit(-1);
		} 
		else {
			try {				
				uniqueID = Integer.parseInt(args[0]);
				candidateNumber=Integer.parseInt(args[1]);
				//candidate = args[1].getBytes();
			} catch (Exception e) {
				outl("Something is wrong with arguments!");
				e.printStackTrace();
				System.exit(-1);
			}
		}

		elManifest=AppUtils.retrieveElectionManifest();

		// retrieve the Voter private keys
		String filename=AppParams.PRIVATE_KEY_path + "voter" + (uniqueID<10? "0":"") + uniqueID + "_PR.json";
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
		
		// Create the voter:
		outl("Creating a voter object.");
		Voter voter;
		try {
			voter = new Voter(uniqueID, elManifest, decr, sign);
		
			// Create a ballot;
			//out("Creating a ballot with candidate " + new String(candidate));
			outl("Creating a ballot with candidate number " + candidateNumber);
			byte[] ballot = voter.createBallot(candidateNumber);
				
			// Send the ballot:
			outl("Sending the ballot to the server");
			byte[] serverResponse = NetworkClient.sendRequest(ballot, AppParams.colServURI.hostname, AppParams.colServURI.port);
				
			//TODO: implement the VoterApp from here to the end of the file
				
			// Validate the server's response:
			Voter.ResponseTag responseTag = voter.validateResponse(serverResponse);
			outl("Response of the server: " + responseTag);
				
			if (responseTag == Voter.ResponseTag.VOTE_COLLECTED) {
				// Output the verification data:
				Voter.Receipt receipt = voter.getReceipt();
				outl("RECEIPT:");
				outl("    nonce = " + byteArrayToHexString(receipt.nonce));
				outl("    inner ballot = " + byteArrayToHexString(receipt.innerBallot));
				if (receipt.serverSignature != null)
					outl("    server's signature = " + byteArrayToHexString(receipt.serverSignature));
					
				// Store the receipt:
				String receipt_fname = AppParams.RECEIPT_file + uniqueID + ".msg"; 
				AppUtils.storeAsFile(receipt.asMessage(), receipt_fname);
		}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}	
}
