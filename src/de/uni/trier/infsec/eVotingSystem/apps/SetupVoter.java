package de.uni.trier.infsec.eVotingSystem.apps;

import static de.uni.trier.infsec.eVotingSystem.apps.AppUtils.setupPrivateKeys;
import static de.uni.trier.infsec.eVotingSystem.apps.AppUtils.setupPublicKeys;
import de.uni.trier.infsec.eVotingSystem.core.Params;
import de.uni.trier.infsec.eVotingSystem.parser.Keys;
import de.uni.trier.infsec.functionalities.digsig.Signer;
import de.uni.trier.infsec.functionalities.pkenc.Decryptor;

public class SetupVoter {

	public static void main(String[] args) {
		int voterID=0;
		if (args.length != 1) {
			System.out.println("Wrong number of Arguments!\nExpected: VoterRegisterApp <voter_id [int]>\nExample: VoterRegisterApp 01");
			System.exit(0);
		} 
		else {
			try {				
				voterID = Integer.parseInt(args[0]);
			} catch (Exception e) {
				System.out.println("Something is wrong with arguments!\nExpected: VoterRegisterApp <voter_id [int]>\nExample: VoterRegisterApp 01");
				e.printStackTrace();
				System.exit(0);
			}
			if(voterID<0 || voterID>=Params.NumberOfVoters) {
				System.out.println("Voter identifier out of range!\nExpected: \n\t 0 <= voter_id < " + Params.NumberOfVoters);
				System.exit(0);
			}
			String name="voter" + (voterID<10? "0":"") + voterID;
			
			Decryptor decr = new Decryptor();
			Signer sign = new Signer();
			
			Keys k = new Keys();
			k.encrKey=decr.getEncryptionKey();
			k.decrKey=decr.getDecryptionKey();
			k.signKey=sign.getSignatureKey();
			k.verifKey=sign.getVerificationKey();
			
			String filename =  AppParams.PRIVATE_KEY_dir + name + "_PR.json";
			setupPrivateKeys(k, filename);
			
			filename =  AppParams.PUBLIC_KEY_dir + name + "_PU.json";
			String publicKeys=setupPublicKeys(k, filename);
			
			System.out.println(name + "'s public keys:");
			System.out.println(publicKeys);
			System.out.println("The public keys have been saved in: \n" + filename);
		}
	}	
	/*
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
		String pathVoter = AppParams.PATH_STORAGE + "voter" + voterID + ".info";
		try {
			AppUtils.storeAsFile(serialized, pathVoter);
			System.out.println("Voter " + voterID + " registered!");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
    */
}
