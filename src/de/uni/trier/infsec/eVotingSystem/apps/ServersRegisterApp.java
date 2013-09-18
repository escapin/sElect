package de.uni.trier.infsec.eVotingSystem.apps;

import static de.uni.trier.infsec.utils.MessageTools.concatenate;

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

public class ServersRegisterApp {


	public static void main(String[] args) {		
		System.setProperty("remotemode", Boolean.toString(true));
		ServersRegisterApp.registerAndSave(Params.SERVER1ID);
		ServersRegisterApp.registerAndSave(Params.SERVER2ID);
	}

	private static void registerAndSave(int serverID){
		PKI.useRemoteMode();
		Decryptor server_decr = new Decryptor();
		Signer server_signer = new Signer();
		try {
			RegisterEnc.registerEncryptor(server_decr.getEncryptor(), serverID, Params.ENC_DOMAIN);
			RegisterSig.registerVerifier(server_signer.getVerifier(), serverID, Params.SIG_DOMAIN);
		} catch (RegisterEnc.PKIError | RegisterSig.PKIError e) {
			e.printStackTrace();
			System.exit(0);
		} catch (NetworkError e) {
			e.printStackTrace();
			System.exit(0);
		}
		byte[] id = MessageTools.intToByteArray(serverID);
		byte[] decryptor = server_decr.toBytes();
		byte[] signer = server_signer.toBytes();
		byte[] serialized = concatenate(id, concatenate(decryptor, signer));
		String pathServer=Params.PATH_STORAGE + "server" + serverID + ".info";
		try {
			Utils.storeAsFile(serialized, pathServer);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
