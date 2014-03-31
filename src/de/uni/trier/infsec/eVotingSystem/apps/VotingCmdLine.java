package de.uni.trier.infsec.eVotingSystem.apps;

import java.io.FileNotFoundException;
import java.io.IOException;

import de.uni.trier.infsec.eVotingSystem.core.Params;
import de.uni.trier.infsec.eVotingSystem.core.Voter;
import de.uni.trier.infsec.functionalities.pki.PKI;
import de.uni.trier.infsec.functionalities.pkienc.Decryptor;
import de.uni.trier.infsec.functionalities.pkisig.RegisterSig;
import de.uni.trier.infsec.functionalities.pkisig.Signer;
import de.uni.trier.infsec.functionalities.pkisig.Verifier;
import de.uni.trier.infsec.lib.network.NetworkClient;
import de.uni.trier.infsec.utils.MessageTools;
import static de.uni.trier.infsec.utils.Utilities.byteArrayToHexString;
import static de.uni.trier.infsec.eVotingSystem.core.Utils.outl;

public class VotingCmdLine 
{
	public static void main(String[] args) 
	{	
		int voterID = -1;
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
				voterID = Integer.parseInt(args[0]);
				candidateNumber=Integer.parseInt(args[1]);
				//candidate = args[1].getBytes();
			} catch (Exception e) {
				outl("Something is wrong with arguments!");
				e.printStackTrace();
				System.exit(-1);
			}
		}

		try {
			// De-serialize keys and create cryptographic functionalities:
			byte[] serialized=null;
			try {
				String filename = AppParams.PATH_STORAGE + "voter" + voterID + ".info";
				outl("private keys filename = " + filename);
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
				outl("Something wrong with identifiers");
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
				outl("Something wrong with the keys");
				System.exit(-1);
			}
			
			// Create the voter:
			outl("Creating a voter object.");
			Voter voter = new Voter(voterID, AppParams.ELECTIONID, decryptor, signer);
			
			
			
			// Create a ballot;
			//out("Creating a ballot with candidate " + new String(candidate));
			outl("Creating a ballot with candidate number " + candidateNumber);
			byte[] ballot = voter.createBallot(candidateNumber);
			
			// Send the ballot:
			outl("Sending the ballot to the server");
			byte[] serverResponse = NetworkClient.sendRequest(ballot, AppParams.SERVER1_NAME, AppParams.SERVER1_PORT);
			
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
				String receipt_fname = AppParams.RECEIPT_file + voterID + ".msg"; 
				AppUtils.storeAsFile(receipt.asMessage(), receipt_fname);
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}	
}
