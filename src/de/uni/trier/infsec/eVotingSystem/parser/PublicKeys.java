package de.uni.trier.infsec.eVotingSystem.parser;

import static de.uni.trier.infsec.utils.Utilities.arrayEqual;

public class PublicKeys {
	
	public byte[] encryptionKey;
	public byte[] verificationKey;
	
	public PublicKeys(byte[] encryptionKey, byte[] verificationKey)
	{
		this.encryptionKey=encryptionKey;
		this.verificationKey=verificationKey;
	}
	
	public boolean equals(Object o)
	{
		if(o instanceof PublicKeys)
		{
			PublicKeys puKeys=(PublicKeys) o;
			return arrayEqual(this.encryptionKey, puKeys.encryptionKey) &&
					arrayEqual(this.verificationKey, puKeys.verificationKey);
		}
		return false;
	}
}