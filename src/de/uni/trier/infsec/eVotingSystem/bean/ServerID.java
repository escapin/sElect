package de.uni.trier.infsec.eVotingSystem.bean;

import de.uni.trier.infsec.utils.Utilities;

/**
 * Record to store servers' attributes.
 * @author scapin
 *
 */
public class ServerID
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