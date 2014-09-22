package de.uni.trier.infsec.eVotingSystem.bean;

import de.uni.trier.infsec.utils.Utilities;

/**
 * Record to store voters' attributes.
 * @author scapin
 *
 */
public class VoterID
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