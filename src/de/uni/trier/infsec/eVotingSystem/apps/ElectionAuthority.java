package de.uni.trier.infsec.eVotingSystem.apps;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;

import de.uni.trier.infsec.eVotingSystem.bean.CollectingServerID;
import de.uni.trier.infsec.eVotingSystem.bean.FinalServerID;
import de.uni.trier.infsec.eVotingSystem.bean.URI;
import de.uni.trier.infsec.eVotingSystem.bean.VoterID;
import de.uni.trier.infsec.eVotingSystem.parser.ElectionManifest;
import de.uni.trier.infsec.eVotingSystem.parser.ElectionManifestParser;
import de.uni.trier.infsec.eVotingSystem.parser.Keys;
import de.uni.trier.infsec.eVotingSystem.parser.KeysParser;
import de.uni.trier.infsec.functionalities.digsig.Signer;
import de.uni.trier.infsec.functionalities.nonce.NonceGen;
import static de.uni.trier.infsec.eVotingSystem.apps.AppUtils.readCharsFromFile;
import static de.uni.trier.infsec.eVotingSystem.apps.AppUtils.storeAsFile;
import static de.uni.trier.infsec.eVotingSystem.core.Utils.errln;

public class ElectionAuthority {
	public static void main(String[] args){
		URI unknownURI = new URI("???", -1);

		// retrieve the public keys of Collecting Server
		String filename = AppParams.PUBLIC_KEY_path + "CollectingServer_PU.json";
		String stringJSON=null;
		try {
			stringJSON = readCharsFromFile(filename);
		} catch (IOException e) {
			errln("Unable to access: " + filename);
		}
		Keys k=KeysParser.parseJSONString(stringJSON);
		CollectingServerID colServID = new CollectingServerID(unknownURI, k.encrKey, k.verifKey);
		
		// retrieve the public keys of Final Server
		filename = AppParams.PUBLIC_KEY_path + "FinalServer_PU.json";
		try {
			stringJSON = readCharsFromFile(filename);
		} catch (IOException e) {
			errln("Unable to access: " + filename);
		}
		k=KeysParser.parseJSONString(stringJSON);
		FinalServerID finServID = new FinalServerID(unknownURI, k.encrKey, k.verifKey);
		
		// retrieve the public keys of voters
		String pattern="voter*";
		Path dir=Paths.get(AppParams.PUBLIC_KEY_path);
		MatchFinder finder = new MatchFinder(pattern);
		try {
			Files.walkFileTree(dir, finder);
		} catch (IOException e) {
			errln("Unable to access: " + filename);
		}
		LinkedList<Path> fileMatched = finder.getMatches();
		VoterID[] voterList = new VoterID[fileMatched.size()];
		String digits, fName; int uniqueID;
		for(int i=0;i<voterList.length;i++){
			fName=fileMatched.get(i).toString();
			digits=fName.replaceAll("[^0-9]", "");
			uniqueID=Integer.parseInt(digits);
			filename = AppParams.PUBLIC_KEY_path + fileMatched.get(i).toString();
			try {
				stringJSON =  readCharsFromFile(filename);
			} catch (IOException e) {
				errln("Unable to access: " + filename);
			}
			k = KeysParser.parseJSONString(stringJSON);
			voterList[i]=new VoterID(uniqueID, k.encrKey, k.verifKey);
		}
		
		// get an electionID
		byte[] electionID = new NonceGen().newNonce();
		
		// creates an election manifest
		ElectionManifest elManifest = new ElectionManifest(electionID,
			AppParams.STARTTIME, AppParams.STARTTIME+AppParams.DURATION,
			AppParams.HEADLINE, AppParams.CHOICESLIST, voterList, colServID,
			finServID, AppParams.bulletinBoardList);
		elManifest.setTitle(AppParams.EL_TITLE);
		elManifest.setDescription(AppParams.EL_DESCRIPTION);
		
		String sManifestJSON = ElectionManifestParser.generateJSON(elManifest);
		
		// retrieve the signature key of the Election Authority
		filename = AppParams.PRIVATE_KEY_path + "ElectionAuthority_PR.json";
		try {
			stringJSON = readCharsFromFile(filename);
		} catch (IOException e) {
			errln("Unable to access: " + filename);
		}
		k=KeysParser.parseJSONString(stringJSON);
		
		Signer sign = new Signer(k.verifKey, k.signKey);
		byte[] manifestSignature=sign.sign(sManifestJSON.getBytes());
		
		// generate the JSON file
		filename=AppParams.EL_MANIFEST_path + "ElectionManifest.json";
		try {
			storeAsFile(sManifestJSON, filename);
		} catch (IOException e) {
			errln("Unable to access: " + filename);
		}
		
		// generate the Signature file
		filename=AppParams.EL_MANIFEST_path + "ElectionManifest.sig";
		try {
			storeAsFile(manifestSignature, filename);
		} catch (IOException e) {
			errln("Unable to access: " + filename);
		}
		
		
	}
	
	
	public static class MatchFinder extends SimpleFileVisitor<Path> {

     private final PathMatcher matcher;
     private int numMatches = 0;
     private LinkedList<Path> fMatched = new LinkedList<Path>();

     MatchFinder(String pattern) {
         matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
     }

     // Compares the glob pattern against
     // the file or directory name.
     void find(Path file) {
         Path name = file.getFileName();
         if (name != null && matcher.matches(name)) {
             numMatches++;
             fMatched.add(name);
         }
     }

     int getNumMatches(){
    	 return numMatches;
     }
     LinkedList<Path> getMatches(){
    	 return fMatched;
     }

     // Invoke the pattern matching
     // method on each file.
     @Override
     public FileVisitResult visitFile(Path file,
             BasicFileAttributes attrs) {
         find(file);
         return FileVisitResult.CONTINUE;
     }

     // Invoke the pattern matching
     // method on each directory.
     @Override
     public FileVisitResult preVisitDirectory(Path dir,
             BasicFileAttributes attrs) {
         find(dir);
         return FileVisitResult.CONTINUE;
     }

     @Override
     public FileVisitResult visitFileFailed(Path file,
             IOException exc) {
         System.err.println(exc);
         return FileVisitResult.CONTINUE;
     }
 }
}
