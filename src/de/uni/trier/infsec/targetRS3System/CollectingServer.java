package de.uni.trier.infsec.targetRS3System;

public class CollectingServer {
	
	/**
	 * Process a new ballot and return a response. Response in null, if the
	 * ballot is rejected.
	 */
	public byte[] collectBallot(byte[] ballot) {
		// TODO: implement onCollectBallot
		return null;
	}
	
	/**
	 * Return the signer partial result (content of the input tally), to be 
	 * posted on the bulletin board. Returns null if the result is not ready.
	 */
	public byte[] publishResult() {
		// TODO: implement onPublishResult
		return null;
	}
}
