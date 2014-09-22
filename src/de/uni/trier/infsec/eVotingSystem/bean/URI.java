package de.uni.trier.infsec.eVotingSystem.bean;

/**
 * Record to store Uniform Resource Locator(s)
 * 
 * @author scapin
 */
public class URI
{
	// URL
	public String hostname;
	public int port; 
	//according to the URI definition, maybe TODO: URN (Uniform resource name) 
	
	public URI(String hostname, int port){
		this.hostname=hostname;
		this.port=port;
	}
	
	public boolean equals(Object o)
	{
		if(o instanceof URI){
			URI u=(URI) o;
			return this.hostname.equals(u.hostname) && 
					this.port==u.port;
		}
		return false;
	}
}