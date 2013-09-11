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
import de.uni.trier.infsec.targetRS3System.FinalServer;
import de.uni.trier.infsec.targetRS3System.Params;
import de.uni.trier.infsec.targetRS3System.Voter;
import de.uni.trier.infsec.utils.MessageTools;


public class TestTargetSystem extends TestCase  
{
	private static CollectingServer colServer;
	private static FinalServer finServer;
	
	@Test
	public void testClientServerExhange() throws Exception
	{
		Voter voter = createVoter(10, (byte)13);
		
		// create the ballot
		byte[] ballot = voter.createBallot();
		// deliver it to the collecting server
		byte[] response = colServer.collectBallot(ballot);
		// check whether the responce is correct
		boolean ok = voter.responseIsCorrect(response);
		assertTrue(ok);

		// make the voter create another ballot
		voter.createBallot();
		// now it should not accept the old response
		ok = voter.responseIsCorrect(response);
		assertFalse(ok);
		// and neither this garbage
		ok = voter.responseIsCorrect(ballot);
		assertFalse(ok);
		
		// try vote again
		try {
			colServer.collectBallot(ballot);
			fail("Revoging -- exception expected");
		} catch (CollectingServer.Error e) {}
		
		// try vote with some trash
		try {
			colServer.collectBallot(response);
			fail("Voting with trash -- exception expected");
		} catch (CollectingServer.Error e) {}
		
	}
	
	@Test
	public void testBallotCollecting() throws Exception 
	{
		// create 5 voters with ids 0..4
		Voter[] voters = new Voter[5];
		for (int i=0; i<5; ++i) {
			Voter v = createVoter(i, (byte)13);
			voters[i] = v;
		}
		
		// make them vote
		for (int i=0; i<5; ++i) {
			byte[] ballot = voters[i].createBallot();
			byte[] response = colServer.collectBallot(ballot);
			boolean ok = voters[i].responseIsCorrect(response);
			assertTrue(ok);			
		}
		
		// get the list of voters who voted and check it
		int vv[] = colServer.getListOfVotersWhoVoted();
		for (int id=0; id<5; ++id) {
			boolean found = false;
			for(int i=0; i<vv.length; ++i) {
				if (vv[i]==6) // this voter should not be here
					fail("Voter in the list of voting voters");
				if (vv[i]==id) { // and this one should
					found = true;
					break;
				}
			}
			assertTrue("Voter not in the list of voting voters", found);
		}		
		
		// get the list of recorded ballots and check it
		byte[][] ballots = colServer.getBallots();
		assertTrue( ballots.length == vv.length );
		for (int id=0; id<5; ++id) {
			boolean found = false;
			for(int i=0; i<ballots.length; ++i) {
				if (MessageTools.equal(ballots[i], voters[id].getInnerBallot())) {
					found = true;
					break;
				}
			}
			assertTrue("Voter not in the list of voting voters", found);
		}		
		
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		System.out.println("Set Up!");
		if (colServer == null ) {
			PKI.useLocalMode();
			File f = new File(PKIServerCore.DEFAULT_DATABASE);
			f.delete();
			colServer = createCollectingServer();
			finServer = createFinalServer();
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////

	private CollectingServer createCollectingServer() throws Exception {
		// create the collecting server's functionalities
		Decryptor server_decryptor = new  Decryptor();
		Signer server_signer = new Signer();
		// register the collecting server
		RegisterEnc.registerEncryptor(server_decryptor.getEncryptor(), Params.SERVER1ID, Params.ENC_DOMAIN);
		RegisterSig.registerVerifier(server_signer.getVerifier(), Params.SERVER1ID, Params.SIG_DOMAIN);
		// create the collecting server
		return new CollectingServer(server_signer, server_decryptor);		
	}

	private FinalServer createFinalServer() throws Exception {
		// create the final server' functionalities
		Decryptor server2_decryptor = new  Decryptor();
		Signer server2_signer = new Signer();
		// register the final server
		RegisterEnc.registerEncryptor(server2_decryptor.getEncryptor(), Params.SERVER2ID, Params.ENC_DOMAIN);
		RegisterSig.registerVerifier(server2_signer.getVerifier(), Params.SERVER2ID, Params.SIG_DOMAIN);
		return null; // TODO: create and return the server
	}
	
	private Voter createVoter(int id, byte vote) throws Exception {
		// create voter's functionalities
		Decryptor voter_decryptor = new  Decryptor();
		Signer voter_signer = new Signer();
		// register
		RegisterEnc.registerEncryptor(voter_decryptor.getEncryptor(), id, Params.ENC_DOMAIN);
		RegisterSig.registerVerifier(voter_signer.getVerifier(), id, Params.SIG_DOMAIN);
		// create the votert
		return new Voter(id, vote, voter_decryptor, voter_signer);		
	}
}
