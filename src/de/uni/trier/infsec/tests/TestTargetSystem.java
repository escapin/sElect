package de.uni.trier.infsec.tests;

import java.io.File;

import junit.framework.TestCase;
import org.junit.Test;

import de.uni.trier.infsec.functionalities.pki.PKI;
import de.uni.trier.infsec.functionalities.pki.PKIServerCore;
import de.uni.trier.infsec.functionalities.pkienc.Decryptor;
import de.uni.trier.infsec.functionalities.pkienc.RegisterEnc;
import de.uni.trier.infsec.functionalities.pkisig.RegisterSig;
import de.uni.trier.infsec.functionalities.pkisig.Signer;
import de.uni.trier.infsec.targetRS3System.CollectingServer;
import de.uni.trier.infsec.targetRS3System.Params;
import de.uni.trier.infsec.targetRS3System.Voter;


public class TestTargetSystem extends TestCase  
{
	@Test
	public void testClientServerExhange() throws Exception
	{
		initializePKI();

		// create the collecting server's functionalities
		Decryptor server_decryptor = new  Decryptor();
		Signer server_signer = new Signer();
		// register the collecting server
		RegisterEnc.registerEncryptor(server_decryptor.getEncryptor(), Params.SERVER1ID, Params.ENC_DOMAIN);
		RegisterSig.registerVerifier(server_signer.getVerifier(), Params.SERVER1ID, Params.SIG_DOMAIN);
		// create the collecting server
		CollectingServer colServer = new CollectingServer(server_signer, server_decryptor);

		// create the final server' functionalities
		Decryptor server2_decryptor = new  Decryptor();
		Signer server2_signer = new Signer();
		// register the final server
		RegisterEnc.registerEncryptor(server2_decryptor.getEncryptor(), Params.SERVER2ID, Params.ENC_DOMAIN);
		RegisterSig.registerVerifier(server2_signer.getVerifier(), Params.SERVER2ID, Params.SIG_DOMAIN);

		// create voter's functionalities
		Decryptor voter_decryptor = new  Decryptor();
		Signer voter_signer = new Signer();
		// register
		int voter_id = 1;
		RegisterEnc.registerEncryptor(voter_decryptor.getEncryptor(), voter_id, Params.ENC_DOMAIN);
		RegisterSig.registerVerifier(voter_signer.getVerifier(), voter_id, Params.SIG_DOMAIN);
		// create the votert
		Voter voter = new Voter(voter_id, (byte)13, voter_decryptor, voter_signer);

		// create the ballot
		byte[] ballot = voter.createBallot();
		// deliver it to the collecting server
		byte[] response = colServer.collectBallot(ballot);
		// check whether the responce is correct
		boolean ok = voter.responseIsCorrect(response);
		assertTrue(ok);

		// have the voter create another ballot
		voter.createBallot();
		// now it should not accept the old response
		ok = voter.responseIsCorrect(response);
		assertFalse(ok);
		// and neither this garbage
		ok = voter.responseIsCorrect(ballot);
		assertFalse(ok);
	}


	private void initializePKI() throws Exception 
	{
		PKI.useLocalMode();
		File f = new File(PKIServerCore.DEFAULT_DATABASE);
		f.delete();
	}

}
