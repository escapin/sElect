package de.uni.trier.infsec.eVotingSystem.apps;

import static de.uni.trier.infsec.utils.MessageTools.concatenate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import de.uni.trier.infsec.eVotingSystem.coreSystem.Params;
import de.uni.trier.infsec.functionalities.pki.PKI;
import de.uni.trier.infsec.functionalities.pkienc.Decryptor;
import de.uni.trier.infsec.functionalities.pkienc.RegisterEnc;
import de.uni.trier.infsec.functionalities.pkisig.RegisterSig;
import de.uni.trier.infsec.functionalities.pkisig.Signer;
import de.uni.trier.infsec.lib.network.NetworkError;
import de.uni.trier.infsec.utils.MessageTools;

public class AppUtils 
{
	public static void storeAsFile(byte[] data, String sFile) throws IOException {
		File f = new File(sFile);
		File fdir = new File(sFile.substring(0, sFile.lastIndexOf(File.separator)));
		if (f.exists()) f.delete();
		fdir.mkdirs();
		f.createNewFile();

		FileOutputStream file = new FileOutputStream(f);
		file.write(data);
		file.flush();
		file.close();
	}
	
	public static byte[] readFromFile(String path) throws IOException {
		FileInputStream f = new FileInputStream(path);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while (f.available() > 0){			
			bos.write(f.read());
		}
		f.close();
		byte[] data = bos.toByteArray();
		return data;
	}
	
	public static void registerAndSave(int id, String filename) throws IOException, RegisterEnc.PKIError, RegisterSig.PKIError, NetworkError {
		PKI.useRemoteMode();
		
		Decryptor server_decr = new Decryptor();
		Signer server_signer = new Signer();
		RegisterEnc.registerEncryptor(server_decr.getEncryptor(), id, Params.ENC_DOMAIN);
		RegisterSig.registerVerifier(server_signer.getVerifier(), id, Params.SIG_DOMAIN);

		byte[] idmsg = MessageTools.intToByteArray(id);
		byte[] decryptor = server_decr.toBytes();
		byte[] signer = server_signer.toBytes();
		byte[] serialized = concatenate(idmsg, concatenate(decryptor, signer));
		// String pathServer = AppParams.PATH_STORAGE + "server" + id + ".info";
		AppUtils.storeAsFile(serialized, filename);
	}
	
}
