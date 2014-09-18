package de.uni.trier.infsec.eVotingSystem.parser;

import static de.uni.trier.infsec.utils.Utilities.byteArrayToHexString;
import static de.uni.trier.infsec.utils.Utilities.hexStringToByteArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.uni.trier.infsec.eVotingSystem.parser.ElectionManifest.CollectingServerID;
import de.uni.trier.infsec.eVotingSystem.parser.ElectionManifest.ElectionBoardError;
import de.uni.trier.infsec.eVotingSystem.parser.ElectionManifest.FinalServerID;
import de.uni.trier.infsec.eVotingSystem.parser.ElectionManifest.NotInElectionArranged;
import de.uni.trier.infsec.eVotingSystem.parser.ElectionManifest.ServerID;
import de.uni.trier.infsec.eVotingSystem.parser.ElectionManifest.URI;
import de.uni.trier.infsec.eVotingSystem.parser.ElectionManifest.VoterID;

public class ElectionManifestParser 
{
	public static String generateJSON(ElectionManifest elBoard) throws NotInElectionArranged
	{
		JSONObject jObj = generateJSONObject(elBoard);
		return jObj.toString(1);	
	}
	
	public static ElectionManifest parseJSONString(String stringJSON) throws JSONException, ElectionBoardError{
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
		jMain.put(sElectionID, elBoard.getElectionID());
		jMain.put(sTitle, elBoard.getTitle());
		jMain.put(sDescription, elBoard.getDescription());
		jMain.put(sHeadline, elBoard.getHeadline());
		
		JSONArray choicesList = new JSONArray();
		for(String choice: elBoard.getChoicesList())
			choicesList.put(choice);
		jMain.put(sChoicesList, choicesList);
		
		JSONArray votersList = new JSONArray();
		JSONObject jVoter;
		for(VoterID vID: elBoard.getVotersList()){
			jVoter = new JSONObject();
			jVoter.put(sUniqueID, vID.uniqueID);
			jVoter.put(sEncryptionKey, byteArrayToHexString(vID.encryption_key));
			jVoter.put(sVerificationKey, byteArrayToHexString(vID.verification_key));
			votersList.put(jVoter);
		}
		jMain.put(sVoterList, votersList);
		
		
		ServerID[] servers=new ServerID[]{	elBoard.getCollectingServer(),
											elBoard.getFinalServer()};
		
		
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
		for(URI uri : elBoard.getBulletinBoardsList()){
			jURI = new JSONObject();
			jURI.put(sHostname, uri.hostname);
			jURI.put(sPort, uri.port);
			bulletinBoardsList.put(jURI);
		}
		jMain.put(sBulletinBoards, bulletinBoardsList);
		
		jMain.put(sStartTime, elBoard.getStartTime());
		jMain.put(sEndTime, elBoard.getEndTime());
		
		return jMain;
	}
	
	
	private static ElectionManifest parseManifest(JSONObject manifest) throws JSONException, ElectionBoardError
	{
		String 	electionID = manifest.getString(sElectionID),
				headline = manifest.getString(sHeadline);
		
		JSONArray aChoicesList = manifest.getJSONArray(sChoicesList);
		String[] choicesList=new String[aChoicesList.length()];
		for(int i=0;i<aChoicesList.length();i++)
			choicesList[i]=aChoicesList.getString(i);
		
		long	startTime=manifest.getLong(sStartTime), 
				endTime=manifest.getLong(sEndTime);	
		
	
		// VOTERS
		JSONArray aVotersList = manifest.getJSONArray(sVoterList);
		VoterID[] votersList = new VoterID[aVotersList.length()];
		for(int i=0;i<votersList.length;i++){
			votersList[i]=createVoterID(aVotersList.getJSONObject(i));
		}
		// SERVERS
		JSONObject aColServer = manifest.getJSONObject(sCollectingServer);
		CollectingServerID colServerID = createCollectingServerID(aColServer);
		
		JSONObject aFinServer = manifest.getJSONObject(sFinalServer);
		FinalServerID finServerID = createFinalServerID(aFinServer);

		// BULLETIN BOARDS
		JSONArray aBulletinBoards = manifest.getJSONArray(sBulletinBoards);
		URI[] bulletinBoards = new URI[aBulletinBoards.length()];   
		for(int i=0;i<bulletinBoards.length;i++){
			bulletinBoards[i] = createURI(aBulletinBoards.getJSONObject(i));
		}
			
		ElectionManifest newElBoard=new ElectionManifest(electionID, 
				startTime, endTime, 
				headline, choicesList, 
				votersList, colServerID, 
				finServerID, bulletinBoards);
		String 	title=manifest.getString(sTitle),
				description=manifest.getString(sDescription);
		newElBoard.setTitle(title);
		newElBoard.setDescription(description);
		
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
