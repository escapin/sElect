package de.uni.trier.infsec.eVotingSystem.parser;

import static de.uni.trier.infsec.utils.Utilities.byteArrayToHexString;
import static de.uni.trier.infsec.utils.Utilities.hexStringToByteArray;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.uni.trier.infsec.eVotingSystem.apps.AppParams;
import de.uni.trier.infsec.eVotingSystem.bean.CollectingServerID;
import de.uni.trier.infsec.eVotingSystem.bean.FinalServerID;
import de.uni.trier.infsec.eVotingSystem.bean.ServerID;
import de.uni.trier.infsec.eVotingSystem.bean.URI;
import de.uni.trier.infsec.eVotingSystem.bean.VoterID;

public class ElectionManifestParser 
{
	public static String generateJSON(ElectionManifest elBoard)
	{
		JSONObject jObj = generateJSONObject(elBoard);
		return jObj.toString(1);	
	}
	
	public static ElectionManifest parseJSONString(String stringJSON) throws JSONException {
		JSONObject manifest=new JSONObject(stringJSON);
		return parseManifest(manifest);
	}
	
	
	private static final String sElectionID="electionID",
								sTitle="title",
								sDescription="description",
								sHeadline="headline",
								sChoicesList="choichesList",
								sVoterList="votersList",
								sUniqueID="uniqueID",
								sEncryptionKey="encryption_key",
								sVerificationKey="verification_key",
								sURI="URI",
								sHostname="hostname",
								sPort="port",
								sCollectingServer="collectingServer",
								sFinalServer="finalServer",
								sBulletinBoards="bulletinBoards",
								sStartTime="startTime",
								sEndTime="endTime";
	
	private static JSONObject generateJSONObject(ElectionManifest elBoard)
	{
		JSONObject jMain=new JSONObject();
		jMain.put(sElectionID, byteArrayToHexString(elBoard.electionID));
		jMain.put(sTitle, elBoard.title);
		jMain.put(sDescription, elBoard.description);
		jMain.put(sHeadline, elBoard.headline);
		
		JSONArray choicesList = new JSONArray();
		for(String choice: elBoard.choicesList)
			choicesList.put(choice);
		jMain.put(sChoicesList, choicesList);
		
		JSONArray votersList = new JSONArray();
		JSONObject jVoter;
		for(VoterID vID: elBoard.votersList){
			jVoter = new JSONObject();
			jVoter.put(sUniqueID, vID.uniqueID);
			jVoter.put(sEncryptionKey, byteArrayToHexString(vID.encryption_key));
			jVoter.put(sVerificationKey, byteArrayToHexString(vID.verification_key));
			votersList.put(jVoter);
		}
		jMain.put(sVoterList, votersList);
		
		
		ServerID[] servers=new ServerID[]{	elBoard.collectingServer,
											elBoard.finalServer};
		
		
		JSONObject jServer, jURI;
		for(ServerID server: servers){
			jServer=new JSONObject();
			jURI = new JSONObject();
			jURI.put(sHostname, server.uri.hostname);
			jURI.put(sPort, server.uri.port);
			jServer.put(sURI, jURI);
			jServer.put(sEncryptionKey, byteArrayToHexString(server.encryption_key));
			jServer.put(sVerificationKey, byteArrayToHexString(server.verification_key));		
			if(server instanceof CollectingServerID)
				jMain.put(sCollectingServer, jServer);
			else if(server instanceof FinalServerID)
				jMain.put(sFinalServer, jServer);
		}
		
		JSONArray bulletinBoardsList=new JSONArray();
		for(URI uri : elBoard.bulletinBoardsList){
			jURI = new JSONObject();
			jURI.put(sHostname, uri.hostname);
			jURI.put(sPort, uri.port);
			bulletinBoardsList.put(jURI);
		}
		jMain.put(sBulletinBoards, bulletinBoardsList);
		
		
		
		SimpleDateFormat formatter = new SimpleDateFormat(AppParams.DATE_FORMAT);
		
		String start = elBoard.startTime==null? AppParams.DATE_FORMAT.replace("'", ""): formatter.format(elBoard.startTime);
		String end = elBoard.endTime==null? AppParams.DATE_FORMAT.replace("'", "") : formatter.format(elBoard.endTime);
		
		jMain.put(sStartTime, start);
		jMain.put(sEndTime, end);
		
		return jMain;
	}
	
	
	private static ElectionManifest parseManifest(JSONObject manifest) throws JSONException
	{
		byte[] 	electionID = hexStringToByteArray(manifest.getString(sElectionID));
		
		ElectionManifest newElBoard=new ElectionManifest(electionID);
		
		newElBoard.headline = manifest.getString(sHeadline);
		
		
		JSONArray aChoicesList = manifest.getJSONArray(sChoicesList);
		newElBoard.choicesList=new String[aChoicesList.length()];
		for(int i=0;i<aChoicesList.length();i++)
			newElBoard.choicesList[i]=aChoicesList.getString(i);
		
		String		start=manifest.getString(sStartTime); 
		String		end=manifest.getString(sEndTime);	
		
		SimpleDateFormat formatter = new SimpleDateFormat(AppParams.DATE_FORMAT);
		try {
			newElBoard.startTime = formatter.parse(start);
			newElBoard.endTime = formatter.parse(end);
		} catch (ParseException e) {
			
		}
		
		// VOTERS
		JSONArray aVotersList = manifest.getJSONArray(sVoterList);
		newElBoard.votersList = new VoterID[aVotersList.length()];
		for(int i=0;i<newElBoard.votersList.length;i++){
			newElBoard.votersList[i]=createVoterID(aVotersList.getJSONObject(i));
		}
		// SERVERS
		JSONObject aColServer = manifest.getJSONObject(sCollectingServer);
		newElBoard.collectingServer = createCollectingServerID(aColServer);
		
		JSONObject aFinServer = manifest.getJSONObject(sFinalServer);
		newElBoard.finalServer = createFinalServerID(aFinServer);

		// BULLETIN BOARDS
		JSONArray aBulletinBoards = manifest.getJSONArray(sBulletinBoards);
		newElBoard.bulletinBoardsList = new URI[aBulletinBoards.length()];   
		for(int i=0;i<newElBoard.bulletinBoardsList.length;i++){
			newElBoard.bulletinBoardsList[i] = createURI(aBulletinBoards.getJSONObject(i));
		}
			
		
		String 	title=manifest.getString(sTitle),
				description=manifest.getString(sDescription);
		
		newElBoard.title=title;
		newElBoard.description=description;
		
		return newElBoard;
	}
	
	private static VoterID createVoterID(JSONObject jVoter)
	{
		byte[] encryption_key = hexStringToByteArray(jVoter.getString(sEncryptionKey));
		byte[] verification_key = hexStringToByteArray(jVoter.getString(sVerificationKey));
		return new VoterID(jVoter.getInt(sUniqueID), encryption_key, verification_key);
		
	}
	private static CollectingServerID createCollectingServerID(JSONObject jServer)
	{
		URI uri=createURI(
					jServer.getJSONObject(sURI));
		byte[] encryption_key = hexStringToByteArray(jServer.getString(sEncryptionKey));
		byte[] verification_key = hexStringToByteArray(jServer.getString(sVerificationKey));
		return new CollectingServerID(uri, encryption_key, verification_key);
	}
	private static FinalServerID createFinalServerID(JSONObject jServer)
	{
		URI uri=createURI(
					jServer.getJSONObject(sURI));
		byte[] encryption_key = hexStringToByteArray(jServer.getString(sEncryptionKey));
		byte[] verification_key = hexStringToByteArray(jServer.getString(sVerificationKey));
		return new FinalServerID(uri, encryption_key, verification_key);
	}
	private static URI createURI(JSONObject jURI)
	{
		return new URI(jURI.getString(sHostname), jURI.getInt(sPort));
	}
}
