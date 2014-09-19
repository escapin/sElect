package de.uni.trier.infsec.eVotingSystem.apps;

import static de.uni.trier.infsec.eVotingSystem.apps.AppUtils.storeAsFile;
import static de.uni.trier.infsec.utils.MessageTools.concatenate;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import de.uni.trier.infsec.eVotingSystem.core.Params;
import de.uni.trier.infsec.eVotingSystem.parser.KeyPair;
import de.uni.trier.infsec.eVotingSystem.parser.KeysParser;
import de.uni.trier.infsec.eVotingSystem.parser.PrivateKeys;
import de.uni.trier.infsec.eVotingSystem.parser.PublicKeys;
//import de.uni.trier.infsec.functionalities.digsig.RegisterSig;
import de.uni.trier.infsec.functionalities.digsig.Signer;
//import de.uni.trier.infsec.functionalities.pki.PKI;
import de.uni.trier.infsec.functionalities.pkenc.Decryptor;
//import de.uni.trier.infsec.functionalities.pkienc.RegisterEnc;
import de.uni.trier.infsec.lib.network.NetworkError;
import de.uni.trier.infsec.utils.MessageTools;

public class AppUtils 
{
	// FIXME: storeAsFile does not work if the file name does not have '/'
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

	public static void storeAsFile(String data, String sFile) throws IOException {
		File f = new File(sFile);
		File fdir = new File(sFile.substring(0, sFile.lastIndexOf(File.separator)));
		if (f.exists()) f.delete();
		fdir.mkdirs();
		f.createNewFile();

		FileWriter fw = new FileWriter(f.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(data);
		bw.flush();
		bw.close();
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

	public static String setupKeys(KeyPair k, String filename)
	{
		String prKeysJSON = KeysParser.generateJSON(k);
		try {
			storeAsFile(prKeysJSON, filename);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prKeysJSON;
	}
/*	public static void setupServer(String filename) throws IOException, RegisterEnc.PKIError, RegisterSig.PKIError, NetworkError {
		PKI.useRemoteMode();

		Decryptor decr = new Decryptor();
		Signer sign = new Signer();
		

		
		byte[] decryptor = decr.toBytes();
		byte[] signer = sign.toBytes();
		byte[] serialized = concatenate(idmsg, concatenate(decryptor, signer));
		AppUtils.storeAsFile(serialized, filename);
	}

	public static void deleteFile(String filename) {
		File f = new File(filename);
		if (f.exists()) f.delete();
		
	}*/
}
