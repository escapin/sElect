package de.uni.trier.infsec.eVotingSystem.bean;

import de.uni.trier.infsec.utils.Utilities;

public class FinalServerID extends ServerID
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