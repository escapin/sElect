package de.uni.trier.infsec.targetRS3System;

import de.uni.trier.infsec.functionalities.nonce.NonceGen;
import de.uni.trier.infsec.functionalities.pkienc.Decryptor;
import de.uni.trier.infsec.functionalities.pkienc.Encryptor;
import de.uni.trier.infsec.functionalities.pkienc.RegisterEnc;
import de.uni.trier.infsec.functionalities.pkisig.RegisterSig;
import de.uni.trier.infsec.functionalities.pkisig.Signer;
import de.uni.trier.infsec.functionalities.pkisig.Verifier;
import de.uni.trier.infsec.lib.network.NetworkError;
import static de.uni.trier.infsec.utils.MessageTools.concatenate;
import static de.uni.trier.infsec.utils.MessageTools.intToByteArray;

/**
 * Core voter's client class. It provides cryptographic operations (formating a ballot) 
 * of a voter.
 */
public class Voter {

	private final int id;               // PKI identifier of a voter (we assume that a voter is already registered)
	private final byte vote;            // the voter's choice
	private final Decryptor decryptor;  // decrytpror of the voter (containing her private decryption key)
	private final Signer signer;        // signer of a voter (containing her private signing key)
	private final Encryptor server1enc; // encryptor of the first server (ballots collecting server)
	private final Verifier server1ver;  // verifier of the first server
	private final Encryptor server2enc; // encryptor of the second server (the final server)
	private final NonceGen noncegen;    // nonce generation functionality

	private byte[] nonce = null;        // the most recent nonce (used to create the ballot)
	private byte[] inner_ballot = null; // the most recent inner ballot 

	public Voter(int id, byte vote, Decryptor decryptor, Signer signer) throws NetworkError, RegisterEnc.PKIError, RegisterSig.PKIError {
		this.id = id;
		this.vote = vote;
		this.decryptor = decryptor;
		this.signer = signer;
		this.server1enc = RegisterEnc.getEncryptor(Params.SERVER1ID, Params.ENC_DOMAIN);
		this.server1ver = RegisterSig.getVerifier(Params.SERVER1ID, Params.SIG_DOMAIN);
		this.server2enc = RegisterEnc.getEncryptor(Params.SERVER2ID, Params.ENC_DOMAIN);
		this.noncegen = new NonceGen();
	}

	// TODO: signed messages should carry election ID

	/**
	 * Creates a ballot containing the given vote.
	 * 
	 * The ballot is of the form:
	 * 
	 *     id , Sig_Voter[ Env_S1(inner_ballot) ]
	 *     
	 * where 
	 * 
	 *     inner_ballot = Enc_S2(vote,n),
	 * 
	 * Sig_Voter[msg] denotes the signature 
	 * of the voter on the message msg along with this message, n is a freshly 
	 * generated nonce, and Enc_Si(msg) denotes the message msg encrypted with 
	 * the public key of the server Si.  
	 */
	public byte[] createBallot() {
		byte[] voteMsg = new byte[] {vote};
		byte[] idMsg = intToByteArray(id);

		nonce = noncegen.nextNonce(); // note that we store the noce for further use
		byte[] vote_with_nonce = concatenate(voteMsg,nonce);
		inner_ballot = server2enc.encrypt(vote_with_nonce); // note that the inner_ballot is stored as well
		byte[] encrypted_inner_ballot = server1enc.encrypt(inner_ballot);
		byte[] signature_on_encrypted_inner_ballot = signer.sign(encrypted_inner_ballot);
		byte[] encrypted_inner_ballot_with_signature = concatenate(encrypted_inner_ballot, signature_on_encrypted_inner_ballot); 
		byte[] outer_ballot = concatenate(idMsg, encrypted_inner_ballot_with_signature);
		return outer_ballot;
	}

	/**
	 * Checks whether the response is correct (using the nonce previously
	 * generated and used by method createBallot).
	 *
	 * The expected response is of the form
	 *
	 *    Enc_Voter( Sig_S1(inner_ballot) )
	 *
	 * that is an encrypted signature of the collecting server on inner_ballot.
	 */
	public boolean responseIsCorrect(byte[] response) {
		byte[] signature = decryptor.decrypt(response);
		return server1ver.verify(signature, inner_ballot);
	}
}
