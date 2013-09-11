package de.uni.trier.infsec.targetRS3System;

import static de.uni.trier.infsec.utils.MessageTools.first;
import static de.uni.trier.infsec.utils.MessageTools.second;
import static de.uni.trier.infsec.utils.MessageTools.byteArrayToInt;
import de.uni.trier.infsec.functionalities.pkienc.Decryptor;
import de.uni.trier.infsec.functionalities.pkienc.Encryptor;
import de.uni.trier.infsec.functionalities.pkienc.RegisterEnc;
import de.uni.trier.infsec.functionalities.pkisig.RegisterSig;
import de.uni.trier.infsec.functionalities.pkisig.Signer;
import de.uni.trier.infsec.functionalities.pkisig.Verifier;
import de.uni.trier.infsec.lib.network.NetworkError;

public class CollectingServer {

	// CONSTANTS
	
	public static final int NumberOfVoters = 50;
	
	// CRYPTOGRAPHIC FUNCTIONALITIES
	
	private final Decryptor decryptor;
	private final Signer signer;
	
	// STATE (PRIVATE)
	
	private final boolean[] voterVoted = new boolean[NumberOfVoters]; // which ballots are already cast
	private boolean inVotingPahse = true; // indicates if the system is still in the voting phase
	private final byte[][] ballots = new byte[NumberOfVoters][]; // (inner ballots which have been cast) 
	private int numberOfCastBallots = 0; 

	// CLASSES
	
	/**
	 * Error thrown if a collected message is ill-formed.
	 */
	public static class Error extends Exception 
	{
		private static final long serialVersionUID = 2280511187763698373L;
		private String description;
		public Error(String description) {
			this.description = description;
		}
		public String toString() {
			return "Collecting Server Error: " + description;
		}
	}

	// CONSTRUCTORS
	
	public CollectingServer(Signer signer, Decryptor decryptor) 
	{
		this.signer = signer;
		this.decryptor = decryptor;
		// initially no voter has cast their ballot:
		for(int i=0; i<NumberOfVoters; ++i)
			voterVoted[i] = false;
	}

	// PUBLIC METHODS
	
	/**
	 * Process a new ballot and return a response. Response in null, if the
	 * ballot is rejected.
	 */
	public byte[] collectBallot(byte[] ballot) throws Error, NetworkError, RegisterSig.PKIError, RegisterEnc.PKIError 
	{
		if (!inVotingPahse)
			throw new Error("Voting phase is over");
		
		// check the voter id
		byte[] idMsg = first(ballot);
		int voter_id = byteArrayToInt(idMsg);
		if( voter_id<0 || voter_id>=NumberOfVoters )
			throw new Error("Invalid voter identifier");
		
		// check if the voter has already voted
		if( voterVoted[voter_id] )
			throw new Error("Ballot already cast");

		// fetch the voter's verifier and encryptor (if it fails, let if fail now);
		// these steps can throw a NetworkError or one of the PKIErrors
		Verifier voter_verifier =  RegisterSig.getVerifier(voter_id, Params.SIG_DOMAIN);
		Encryptor voter_encryptor = RegisterEnc.getEncryptor(voter_id, Params.ENC_DOMAIN);

		// take the ballot into the parts
		byte[] encrypted_inner_ballot_with_signature = second(ballot);
		byte[] encrypted_inner_ballot = first(encrypted_inner_ballot_with_signature);
		byte[] signature_on_encrypted_inner_ballot = second(encrypted_inner_ballot_with_signature);

		// verify the voter's signature on her encrypted inner ballot
		if( ! voter_verifier.verify(signature_on_encrypted_inner_ballot, encrypted_inner_ballot) )
			throw new Error("Wrong signature");

		// decrypt the inner ballot
		byte[] inner_ballot = decryptor.decrypt(encrypted_inner_ballot);

		// accept and store the ballot
		voterVoted[voter_id] = true;
		assert(numberOfCastBallots<NumberOfVoters);
		ballots[numberOfCastBallots] = inner_ballot; // shouldn't we store the copy of this message?
		numberOfCastBallots++;
		
		// format and return the response (the inner ballot signed by the server and encrypted for the voter)
		byte[] response = voter_encryptor.encrypt(signer.sign(inner_ballot));
		return response;
	}

	/**
	 * Return the result (content of the input tally), to be publicly posted. 
	 */
	public byte[] getResult() 
	{
		// if the result is taken, voting should not be possible any longer
		inVotingPahse = false;
		// TODO: implement getResult
		return null;
	}
	
	/**
	 * For testing. Returns array of cast ballots.
	 */
	public byte[][] getBallots()
	{
		byte[][] b = new byte[numberOfCastBallots][];
		for (int i=0; i<numberOfCastBallots; ++i)
			b[i] = ballots[i];
		return b;
	}
	
	/**
	 * For testing.
	 */
	public int[] getListOfVotersWhoVoted()
	{
		int[] l = new int[numberOfCastBallots];
		int ind = 0;
		for (int id=0; id<NumberOfVoters; ++id) {
			if (voterVoted[id])
				l[ind++] = id;
		}
		assert(ind == numberOfCastBallots-1);
		return l;
	}

}
