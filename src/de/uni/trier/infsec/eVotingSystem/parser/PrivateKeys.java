package de.uni.trier.infsec.eVotingSystem.parser;

import static de.uni.trier.infsec.utils.Utilities.arrayEqual;

public class PrivateKeys extends KeyPair
{
	public PrivateKeys(byte[] decryptionKey, byte[] signatureKey)
	{
		pkencKey=decryptionKey;
		digsigKey=signatureKey;
	}
	
	public boolean equals(Object o)
	{
		if(o instanceof PrivateKeys)
		{
			PrivateKeys othKeys=(PrivateKeys) o;
			return arrayEqual(this.pkencKey, othKeys.pkencKey) &&
					arrayEqual(this.digsigKey, othKeys.digsigKey);
		}
		return false;
	}
}