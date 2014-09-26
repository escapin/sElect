package de.uni.trier.infsec.eVotingSystem.apps;


import static de.uni.trier.infsec.eVotingSystem.apps.AppUtils.readBytesFromFile;
import static de.uni.trier.infsec.eVotingSystem.apps.AppUtils.readCharsFromFile;
import static de.uni.trier.infsec.eVotingSystem.core.Utils.errln;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import de.uni.trier.infsec.eVotingSystem.parser.ElectionManifest;
import de.uni.trier.infsec.eVotingSystem.parser.ElectionManifestParser;
import de.uni.trier.infsec.eVotingSystem.parser.Keys;
import de.uni.trier.infsec.eVotingSystem.parser.KeysParser;
import de.uni.trier.infsec.functionalities.digsig.Verifier;
import de.uni.trier.infsec.utils.Utilities;


public class AppUtils 
{
	public static void storeAsFile(String data, String sFile) throws IOException {
		Path pFile = Paths.get(sFile);
		
		Path pDir = pFile.getParent();
		if(pDir!=null && !Files.isDirectory(pDir, LinkOption.NOFOLLOW_LINKS))
			Files.createDirectories(pDir);
		else
			Files.deleteIfExists(pFile);
		
		Path file=Files.createFile(pFile);
		BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8);
		writer.write(data);
		writer.flush();
		writer.close();
	}
	
	public static String readCharsFromFile(String sFile) throws IOException {
		Path pFile = Paths.get(sFile);
		
		BufferedReader reader = Files.newBufferedReader(pFile, StandardCharsets.UTF_8);
		StringBuffer out = new StringBuffer();
		int c;
		while((c =  reader.read()) != -1) 
			out.append((char) c);
		return out.toString();
	}
	
	public static void storeAsFile(byte[] data, String sFile) throws IOException {
		Path pFile = Paths.get(sFile);
		
		Path pDir = pFile.getParent();
		if(pDir!=null && !Files.isDirectory(pDir, LinkOption.NOFOLLOW_LINKS))
			Files.createDirectories(pDir);
		else
			Files.deleteIfExists(pFile);
		
		Path file=Files.createFile(pFile);
		OutputStream writer =  Files.newOutputStream(file);
		writer.write(data);
		writer.flush();
		writer.close();
	}

	public static byte[] readBytesFromFile(String sFile) throws IOException {
		Path pFile = Paths.get(sFile);
		
		InputStream reader= Files.newInputStream(pFile);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int b;
		while((b =  reader.read()) != -1) 
			out.write((byte) b);
		return out.toByteArray();
	}
	
	public static void deleteFile(String filename) {
		File f = new File(filename);
		if (f.exists()) f.delete();	
	}
	
	protected static void setupPrivateKeys(Keys k, String filename) throws IOException 
	{
		setupKeys(k,filename);
	}
	
	protected static String setupPublicKeys(Keys k, String filename) throws IOException
	{
		Keys pu = new Keys();
		pu.encrKey=k.encrKey;
		pu.verifKey=k.verifKey;
		return setupKeys(pu,filename);
	}
	
	private static String setupKeys(Keys k, String filename) throws IOException
	{
		String prKeysJSON = KeysParser.generateJSON(k);
		storeAsFile(prKeysJSON, filename);
		return prKeysJSON;
	}

	public static ElectionManifest retrieveElectionManifest()
	{
		String manifestJSON=null;
		String filename = AppParams.EL_MANIFEST_path + "ElectionManifest.json";
		try {
			manifestJSON = readCharsFromFile(filename);
		} catch (IOException e) {
			errln("Unable to access: " + filename);
		}
		byte[] manifestSignature=null;
		filename = AppParams.EL_MANIFEST_path + "ElectionManifest.sig";
		try {
			manifestSignature = readBytesFromFile(filename);
		} catch (IOException e) {
			errln("Unable to access: " + filename);
		}
		
		String keyJSON=null;
		filename = AppParams.PUBLIC_KEY_path + "ElectionAuthority_PU.json";
		try {
			keyJSON = readCharsFromFile(filename);
		} catch (IOException e) {
			errln("Unable to access: " + filename);
		}
		Keys k = KeysParser.parseJSONString(keyJSON);
		
		Verifier elManifestVerifier = new Verifier(k.verifKey);
		
		if(!elManifestVerifier.verify(manifestSignature, manifestJSON.getBytes())){
			errln("Invalid Manifest's Signature.");
		}
		
		ElectionManifest elManifest = ElectionManifestParser.parseJSONString(manifestJSON);
		
		if(	elManifest.bulletinBoardsList==null || elManifest.choicesList==null ||
			elManifest.collectingServer==null || elManifest.description==null ||
			elManifest.endTime==null || elManifest.finalServer==null ||
			elManifest.headline==null || elManifest.startTime==null ||  
			elManifest.title==null || elManifest.votersList==null)
			errln("Manifest incomplete.");
		
		return elManifest;
	}
}


/*	public static void setupServer(String filename) throws IOException, RegisterEnc.PKIError, RegisterSig.PKIError, NetworkError {
	PKI.useRemoteMode();

	Decryptor decr = new Decryptor();
	Signer sign = new Signer();
	

	
	byte[] decryptor = decr.toBytes();
	byte[] signer = sign.toBytes();
	byte[] serialized = concatenate(idmsg, concatenate(decryptor, signer));
	AppUtils.storeAsFile(serialized, filename);
	}*/
