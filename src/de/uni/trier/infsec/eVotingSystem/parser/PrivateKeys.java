package de.uni.trier.infsec.eVotingSystem.parser;

import static de.uni.trier.infsec.utils.Utilities.arrayEqual;

public class PrivateKeys
{
	public byte[] decryptionKey;
	public byte[] signatureKey;
	
	public PrivateKeys(byte[] decryptionKey, byte[] signatureKey)
	{
		this.decryptionKey=decryptionKey;
		this.signatureKey=signatureKey;
	}
	
	public boolean equals(Object o)
	{
		if(o instanceof PrivateKeys)
		{
			PrivateKeys prKeys=(PrivateKeys) o;
			return arrayEqual(this.decryptionKey, prKeys.decryptionKey) &&
					arrayEqual(this.signatureKey, prKeys.signatureKey);
		}
		return false;
	}
}