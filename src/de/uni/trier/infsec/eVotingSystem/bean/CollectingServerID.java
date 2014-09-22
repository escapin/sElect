package de.uni.trier.infsec.eVotingSystem.bean;

import de.uni.trier.infsec.utils.Utilities;

public class CollectingServerID extends ServerID
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