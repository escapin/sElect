package de.uni.trier.infsec.eVotingSystem.parser;

import static de.uni.trier.infsec.utils.Utilities.arrayEqual;

public class Keys
{
	public byte[] encrKey;
	public byte[] decrKey;
	public byte[] signKey;
	public byte[] verifKey;
	
	public boolean equals(Object o)
	{
		if(o instanceof Keys)
		{
			Keys othKeys=(Keys) o;
			return 	arrayEqual(this.encrKey, othKeys.encrKey) &&
					arrayEqual(this.decrKey, othKeys.decrKey) &&
					arrayEqual(this.signKey, othKeys.signKey) &&
					arrayEqual(this.verifKey, othKeys.verifKey);
		}
		return false;
	}
}