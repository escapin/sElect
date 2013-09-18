package de.uni.trier.infsec.eVotingSystem.apps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.uni.trier.infsec.eVotingSystem.coreSystem.Params;
import de.uni.trier.infsec.eVotingSystem.coreSystem.Utils;
import de.uni.trier.infsec.functionalities.pki.PKI;
import de.uni.trier.infsec.functionalities.pkienc.*;
import de.uni.trier.infsec.functionalities.pkisig.*;
import de.uni.trier.infsec.lib.network.NetworkError;
import de.uni.trier.infsec.utils.MessageTools;

public class VoterRegisterApp {

	public static void main(String[] args) {
		System.setProperty("remotemode", Boolean.toString(true));
		int voterID=0;
		if (args.length != 1) {
			System.out.println("Wrong number of Arguments!\nExpected: VoterRegisterApp <user_id [int]>\nExample: VoterRegisterApp 01");
			System.exit(0);
		} else {
			try {				
				voterID = Integer.parseInt(args[0]);
			} catch (Exception e) {
				System.out.println("Something is wrong with arguments!\nExpected: VoterRegisterApp <user_id [int]>\nExample: VoterRegisterApp 01");
				e.printStackTrace();
				System.exit(0);
			}
			VoterRegisterApp.register(voterID);
		}
	}	

	private static void register(int voterID) {
		PKI.useRemoteMode();
		Decryptor voter_decryptor = new Decryptor();
		Signer voter_signer = new Signer();
		try {
			RegisterEnc.registerEncryptor(voter_decryptor.getEncryptor(), voterID, Params.ENC_DOMAIN);
			RegisterSig.registerVerifier(voter_signer.getVerifier(), voterID, Params.SIG_DOMAIN);
		} catch (RegisterEnc.PKIError | RegisterSig.PKIError e) {
			e.printStackTrace();
			System.exit(0);
		} catch (NetworkError e) {
			e.printStackTrace();
			System.exit(0);
		}
		byte[] id = MessageTools.intToByteArray(voterID);
		
		byte[] decryptor = voter_decryptor.toBytes();
		byte[] signer = voter_signer.toBytes();

		byte[] decr_sig = MessageTools.concatenate(decryptor, signer);
		// we don't handle the symenc so far
		//SymEnc symenc = new SymEnc();
		//byte[] sym_decr_sig = MessageTools.concatenate(symenc.getKey(), decr_sig);
		//byte[] serialized = MessageTools.concatenate(id, sym_decr_sig);
		byte[] serialized = MessageTools.concatenate(id, decr_sig);
		String pathVoter=Params.PATH_STORAGE + "voter" + voterID + ".info";
		try {
			Utils.storeAsFile(serialized, pathVoter);
			System.out.println("Voter " + voterID + " registered!");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	

}
