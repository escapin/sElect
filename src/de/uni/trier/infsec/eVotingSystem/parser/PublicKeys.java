package de.uni.trier.infsec.eVotingSystem.parser;

import static de.uni.trier.infsec.utils.Utilities.arrayEqual;

public class PublicKeys extends KeyPair{
	
	public PublicKeys(byte[] encryptionKey, byte[] verificationKey)
	{
		this.pkencKey=encryptionKey;
		this.digsigKey=verificationKey;
	}
	
	public boolean equals(Object o)
	{
		if(o instanceof PublicKeys)
		{
			PublicKeys puKeys=(PublicKeys) o;
			
			return arrayEqual(this.pkencKey, puKeys.pkencKey) &&
					arrayEqual(this.digsigKey, puKeys.digsigKey);
		}
		return false;
	}
}