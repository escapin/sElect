package de.uni.trier.infsec.eVotingSystem.apps;

import java.io.FileNotFoundException;
import java.io.IOException;

import de.uni.trier.infsec.eVotingSystem.coreSystem.Params;
import de.uni.trier.infsec.eVotingSystem.coreSystem.Voter;
import de.uni.trier.infsec.functionalities.pki.PKI;
import de.uni.trier.infsec.functionalities.pkienc.Decryptor;
import de.uni.trier.infsec.functionalities.pkisig.RegisterSig;
import de.uni.trier.infsec.functionalities.pkisig.Signer;
import de.uni.trier.infsec.functionalities.pkisig.Verifier;
import de.uni.trier.infsec.lib.network.NetworkClient;
import de.uni.trier.infsec.utils.MessageTools;
import static de.uni.trier.infsec.utils.Utilities.byteArrayToHexString;

public class VotingCmdLine 
{
	public static void main(String[] args) 
	{	
		int voterID = -1;
		//byte[] candidate = null;
		int candidateNumber=0;

		// Parse arguments:

		if (args.length != 2 ) {
			out("Wrong number of Arguments!");
			out("Expected: VotingCmdLine <user_id [int]> <candidate_number [int]>");
			out("Example: VotingCmdLine 07 03");
			System.exit(-1);
		} 
		else {
			try {				
				voterID = Integer.parseInt(args[0]);
				candidateNumber=Integer.parseInt(args[1]);
				//candidate = args[1].getBytes();
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
				String filename = AppParams.PATH_STORAGE + "voter" + voterID + ".info";
				out("private keys filename = " + filename);
				serialized = AppUtils.readFromFile(filename);
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
			if ( idFromMsg != voterID ) {
				out("Something wrong with identifiers");
				System.exit(-1);
			}
			byte[] decr_sig = MessageTools.second(serialized);
			byte[] decryptorMsg = MessageTools.first(decr_sig);
			byte[] signerMsg = MessageTools.second(decr_sig);		
			Decryptor decryptor = Decryptor.fromBytes(decryptorMsg);
			Signer signer = Signer.fromBytes(signerMsg);

			// Initialize the interface to the public key infrastructure:
			PKI.useRemoteMode();

			// Verify that the verifier stored in the file is the same as the one in the PKI:
			Verifier myVerif = RegisterSig.getVerifier(voterID, Params.SIG_DOMAIN);
			if ( !MessageTools.equal(myVerif.getVerifKey(), signer.getVerifier().getVerifKey()) ) {
				out("Something wrong with the keys");
				System.exit(-1);
			}
			
			// Create the voter:
			out("Creating a voter object.");
			Voter voter = new Voter(voterID, AppParams.electionID, decryptor, signer);
			
			//TODO: implement the VoterApp from here to the end of the file
			
			// Create a ballot;
			//out("Creating a ballot with candidate " + new String(candidate));
			out("Creating a ballot with candidate number " + candidateNumber);
			byte[] ballot = voter.createBallot(candidateNumber);
			
			// Send the ballot:
			out("Sending the ballot to the server");
			byte[] response = NetworkClient.sendRequest(ballot, AppParams.SERVER1_NAME, AppParams.SERVER1_PORT);
			
			// Validate the server's response:
			Voter.ResponseTag responseTag = voter.validateResponse(response);
			out("Response of the server: " + responseTag);
			
			if (responseTag == Voter.ResponseTag.VOTE_COLLECTED) {
				// Output the verification data:
				Voter.Receipt receipt = voter.getReceipt();
				out("RECEIPT:");
				out("    nonce = " + byteArrayToHexString(receipt.nonce));
				out("    inner ballot = " + byteArrayToHexString(receipt.innerBallot));
				if (receipt.serverSignature != null)
					out("    server's signature = " + byteArrayToHexString(receipt.serverSignature));
				
				// Store the receipt:
				String receipt_fname = "./receipt_" + voterID + ".msg"; 
				AppUtils.storeAsFile(receipt.asMessage(), receipt_fname);
			}
			
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
