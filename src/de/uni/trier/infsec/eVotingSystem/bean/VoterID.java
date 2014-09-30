package de.uni.trier.infsec.eVotingSystem.bean;

/**
 * Record to store voters' attributes.
 * @author scapin
 *
 */
public class VoterID
{
	public final String email; 
	// FIXME: perhaps we should have a 'uniqueID' of type String instead of int.

	
	public VoterID(String email){
		this.email=email;
	}
	
	public boolean equals(Object o)
	{
		if(o instanceof VoterID){
			VoterID vID=(VoterID) o;
			return	this.email.equals(vID.email);
		}
		return false;
	}
	
}