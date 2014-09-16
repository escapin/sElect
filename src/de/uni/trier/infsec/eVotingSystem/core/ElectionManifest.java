package de.uni.trier.infsec.eVotingSystem.core;

import static de.uni.trier.infsec.eVotingSystem.core.UtilsCore.arrayEqual;
import de.uni.trier.infsec.utils.Utilities;	// arrayEqual between arrays of byte

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
	
	/**
	 * Record to store voters' attributes.
	 * @author scapin
	 *
	 */
	public static class VoterID
	{
		public final int uniqueID; 
		// FIXME: perhaps we should have a 'uniqueID' of type String instead of int.
		
		// perhaps TODO: add the OAuth/OpenID (public) credentials
		public final byte[] encryption_key;
		public final byte[] verification_key;
		
		public VoterID(int uniqueID, byte[] encryption_key, byte[] verifier_key){
			this.uniqueID=uniqueID;
			this.encryption_key=encryption_key;
			this.verification_key=verifier_key;
		}
		
		public boolean equals(Object o)
		{
			if(o instanceof VoterID){
				VoterID vID=(VoterID) o;
				return	this.uniqueID==vID.uniqueID &&
						Utilities.arrayEqual(encryption_key, vID.encryption_key) &&
						Utilities.arrayEqual(verification_key, vID.verification_key);
			}
			return false;
		}
		
	}
	
	/**
	 * Record to store Uniform Resource Locator(s)
	 * 
	 * @author scapin
	 */
	public static class URI
	{
		// URL
		public String hostname;
		public int port; 
		//according to the URI definition, maybe TODO: URN (Uniform resource name) 
		
		public URI(String hostname, int port){
			this.hostname=hostname;
			this.port=port;
		}
		
		public boolean equals(Object o)
		{
			if(o instanceof URI){
				URI u=(URI) o;
				return this.hostname.equals(u.hostname) && 
						this.port==u.port;
			}
			return false;
		}
	}
	
	/**
	 * Record to store servers' attributes.
	 * @author scapin
	 *
	 */
	public static class ServerID
	{		
		public final URI uri;
		
		//perhaps TODO: add the OAuth/OpenID (public) credentials
		public final byte[] encryption_key;
		public final byte[] verification_key;
		
		public ServerID(URI uri, 
				byte[] encryption_key, byte[] verification_key)
		{
			this.uri=uri;
			this.encryption_key=encryption_key;
			this.verification_key=verification_key;
		}
		
		public boolean equals(Object o)
		{
			if(o instanceof ServerID){
				ServerID sID=(ServerID) o;
				return	this.uri.equals(sID.uri) &&
						Utilities.arrayEqual(this.encryption_key, sID.encryption_key) &&
						Utilities.arrayEqual(this.verification_key, sID.verification_key);
			}
			return false;
		}
	}
	
	public static class CollectingServerID extends ServerID
	{
		public CollectingServerID(URI uri, 
				byte[] encyption_key, byte[] verification_key) 
		{
			super(uri, encyption_key, verification_key);
		}
		public boolean equals(Object o)
		{
			if(o instanceof CollectingServerID){
				CollectingServerID sID=(CollectingServerID) o;
				return	this.uri.equals(sID.uri) &&
						Utilities.arrayEqual(this.encryption_key, sID.encryption_key) &&
						Utilities.arrayEqual(this.verification_key, sID.verification_key);
			}
			return false;			
		}
	}
	public static class FinalServerID extends ServerID
	{
		public FinalServerID(URI uri, 
				byte[] encryption_key, byte[] verification_key)
		{
			super(uri, encryption_key, verification_key);
			
		}
		public boolean equals(Object o)
		{
			if(o instanceof FinalServerID){
				FinalServerID sID=(FinalServerID) o;
				return	this.uri.equals(sID.uri) &&
						Utilities.arrayEqual(this.encryption_key, sID.encryption_key) &&
						Utilities.arrayEqual(this.verification_key, sID.verification_key);
			}
			return false;			
		}
	}
	
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

