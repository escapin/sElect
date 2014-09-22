package de.uni.trier.infsec.eVotingSystem.parser;

import static de.uni.trier.infsec.eVotingSystem.core.Utils.arrayEqual;
import de.uni.trier.infsec.eVotingSystem.bean.CollectingServerID;
import de.uni.trier.infsec.eVotingSystem.bean.FinalServerID;
import de.uni.trier.infsec.eVotingSystem.bean.URI;
import de.uni.trier.infsec.eVotingSystem.bean.VoterID;

public class ElectionManifest
{
	@SuppressWarnings("serial")
	public static class ElectionBoardError extends Exception{}
	@SuppressWarnings("serial")
	public static class ElectionAlreadyArranged extends ElectionBoardError{}
	@SuppressWarnings("serial")
	public static class NotInElectionArranged extends ElectionBoardError{} // used by eVotingSystem.parser.ElectionManifest
	@SuppressWarnings("serial")
	public static class CapacityOverflowError extends ElectionBoardError{}
	
	/*
	 * ATTRIBUTES WHICH DEFINE AN ELECTION BOARD:
	 */
	private final String electionID;
	private final String headline;			// the question to submit
	private final String[] choicesList;		// e.g. list of candidates
	
	private String title;	// of the election
	private String description;	// of the election
	
	private final long startTime, endTime;	// milliseconds time value since January 1, 1970, 00:00:00 GMT
	//TODO: just for the Collecting Server
	
	private final VoterID[] votersList;		
	private final CollectingServerID collectingServer;
	private final FinalServerID finalServer;
	private final URI[] bulletinBoardsList;

	
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
	public ElectionManifest(String electionID,
							long startTime, long endTime,
							String headline, 
							String[] choicesList,
							VoterID[] votersList, 
							CollectingServerID collectingServer,
							FinalServerID finalServer,
							URI[] bulletinBoardList)
	{
		if(startTime>=endTime)
			throw new IllegalArgumentException("Wrong Election Period");
		
		this.electionID=electionID;
		this.headline=headline;
		
		this.startTime=startTime;
		this.endTime=endTime;
		
		this.choicesList=choicesList;
		
		this.votersList=votersList;
		this.collectingServer=collectingServer;
		this.finalServer=finalServer;
		this.bulletinBoardsList=bulletinBoardList;
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
;			ElectionManifest otherElBoard=(ElectionManifest) obj;
			return 	this.getElectionID().equals(otherElBoard.getElectionID()) &&
					this.getHeadline().equals(otherElBoard.getHeadline()) &&
					this.getStartTime()==otherElBoard.getStartTime() &&
					this.getStartTime()==otherElBoard.getStartTime() && 
					arrayEqual(this.getChoicesList(), otherElBoard.getChoicesList()) &&
					arrayEqual(this.getVotersList(), otherElBoard.getVotersList()) &&
					this.getCollectingServer().equals(otherElBoard.getCollectingServer()) &&
					this.getFinalServer().equals(otherElBoard.getFinalServer()) &&
					arrayEqual(this.getBulletinBoardsList(), otherElBoard.getBulletinBoardsList());
			/* 
			 * Note that, since they are not declared 'final', 
			 * the fields 'title' and 'description' are not used to assert equality.
			 */
		}
		return false;
		
	}

	/* 
	 * GETTER(s) and SETTER(s)
	 */
	public VoterID[] getVotersList()
	{
		return votersList;
	}
	
	public CollectingServerID getCollectingServer()
	{
		return collectingServer;
	}

	public FinalServerID getFinalServer()
	{
		return finalServer;
	}


	public URI[] getBulletinBoardsList() 
	{
		return bulletinBoardsList;
	}
		
	
	public String getTitle()
	{
		return title;
	}


	public void setTitle(String title)
	{
		this.title = title;
	}


	public String getDescription()
	{
		return description;
	}


	public void setDescription(String description)
	{
		this.description = description;
	}


	public String getHeadline()
	{
		return headline;
	}


	public String getElectionID()
	{
		return electionID;
	}


	public String[] getChoicesList()
	{
		return choicesList;
	}

	public long getStartTime()
	{
		return startTime;
	}


	public long getEndTime()
	{
		return endTime;
	}
}

