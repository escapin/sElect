package de.uni.trier.infsec.eVotingSystem.parser;

import java.util.Date;

import de.uni.trier.infsec.eVotingSystem.bean.CollectingServerID;
import de.uni.trier.infsec.eVotingSystem.bean.FinalServerID;
import de.uni.trier.infsec.eVotingSystem.bean.URI;
import de.uni.trier.infsec.eVotingSystem.bean.VoterID;
import de.uni.trier.infsec.utils.Utilities;

public class ElectionManifest
{
	@SuppressWarnings("serial")
	public static class ElectionBoardError extends Exception{}
	@SuppressWarnings("serial")
	public static class ElectionAlreadyArranged extends ElectionBoardError{}
	@SuppressWarnings("serial")
	public static class CapacityOverflowError extends ElectionBoardError{}
	
	/*
	 * ATTRIBUTES WHICH DEFINE AN ELECTION BOARD:
	 */
	public final byte[] electionID;
	public String headline;			// the question to submit
	public String[] choicesList;		// e.g. list of candidates
	
	public String title;	// of the election
	public String description;	// of the election
	
	public 	Date startTime, endTime;	
	
	
	public VoterID[] votersList;		
	public CollectingServerID collectingServer;
	public FinalServerID finalServer;
	public URI[] bulletinBoardsList;

	
	/**
	 * The Election Board containing the information used by the
	 * the administrative agency for the conduct of elections.
	 * 
	 * @param electionID unique identifier of the particular election
	 * @param startTime the time point since the election starts 
	 * @param endTime the time point since the election ends
	 * @param headline the question to submit to the voters
	 * @param choicesList array of candidate/choices which can be selected by the voters	 
	 *  
	 */
	public ElectionManifest(byte[] electionID)
	{
//		if(startTime>=endTime)
//			throw new IllegalArgumentException("Wrong Election Period");
		
		this.electionID=electionID;
//		this.headline=headline;
//		
//		this.startTime=startTime;
//		this.endTime=endTime;
//		
//		this.choicesList=choicesList;
//		
//		this.votersList=votersList;
//		this.collectingServer=collectingServer;
//		this.finalServer=finalServer;
//		this.bulletinBoardsList=bulletinBoardList;
	}
	
	/**
	 * Indicate whether some other ElectionBoard has all its
	 * getters "equals to" this one.
	 * 
	 * @param obj the reference ElectionBoard with which to compare.
	 * @return true if this ElectionBoard is the same as the obj argument; false otherwise.
	 * 
	 * @author scapin
	 */
	public boolean equals(Object obj)
	{
		if(obj instanceof ElectionManifest){
			ElectionManifest otherElBoard=(ElectionManifest) obj;
			return 	Utilities.arrayEqual(this.electionID, otherElBoard.electionID);
//					this.getHeadline().equals(otherElBoard.getHeadline()) &&
//					this.getStartTime().equals(otherElBoard.getStartTime()) &&
//					this.getEndTime().equals(otherElBoard.getEndTime()) && 
//					arrayEqual(this.getChoicesList(), otherElBoard.getChoicesList()) &&
//					arrayEqual(this.getVotersList(), otherElBoard.getVotersList()) &&
//					this.getCollectingServer().equals(otherElBoard.getCollectingServer()) &&
//					this.getFinalServer().equals(otherElBoard.getFinalServer()) &&
//					arrayEqual(this.getBulletinBoardsList(), otherElBoard.getBulletinBoardsList());
			/* 
			 * Note that, since they are not declared 'final', 
			 * the fields 'title' and 'description' are not used to assert equality.
			 */
		}
		return false;
		
	}
}

