package de.uni.trier.infsec.tests;

import java.io.File;

import junit.framework.TestCase;
import org.junit.Test;

import de.uni.trier.infsec.coreSystem.CollectingServer;
import de.uni.trier.infsec.coreSystem.FinalServer;
import de.uni.trier.infsec.coreSystem.Params;
import de.uni.trier.infsec.coreSystem.Utils;
import de.uni.trier.infsec.coreSystem.Voter;
import de.uni.trier.infsec.functionalities.pki.PKI;
import de.uni.trier.infsec.functionalities.pki.PKIServerCore;
import de.uni.trier.infsec.functionalities.pkienc.Decryptor;
import de.uni.trier.infsec.functionalities.pkienc.RegisterEnc;
import de.uni.trier.infsec.functionalities.pkisig.RegisterSig;
import de.uni.trier.infsec.functionalities.pkisig.Signer;
import de.uni.trier.infsec.functionalities.pkisig.Verifier;
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
		boolean ok = voter.validateResponse(response);
		assertTrue(ok);

		// make the voter create another ballot
		voter.createBallot();
		// now it should not accept the old response
		ok = voter.validateResponse(response);
		assertFalse(ok);
		// and neither this garbage
		ok = voter.validateResponse(ballot);
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
	public void testVotingProcess() throws Exception 
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
			boolean ok = voters[i].validateResponse(response);
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
		
		// get the result (content of the bulletin board) of the collecting server
		byte[] signedTally = colServer.getResult();
		
		// and split it
		byte[] publicData = MessageTools.first(signedTally);
		byte[] signatureOnPublicData = MessageTools.second(signedTally);

		// check the signature on it
		Verifier colServerVer = RegisterSig.getVerifier(Params.SERVER1ID, Params.SIG_DOMAIN);
		boolean signature_ok =  colServerVer.verify(signatureOnPublicData, publicData);
		assertTrue("Incorrect signature on the public data of the collecting server",  signature_ok);
		
		// extract ballots and voter list
		byte[] ballotsAsAMessage = MessageTools.first(publicData);
		byte[] votersAsAMessage = MessageTools.second(publicData);
		
		// lets take some voter
		Voter selected_voter = voters[3];
		byte[] voter_id = MessageTools.intToByteArray(selected_voter.getId());
		
		// and check if she is listed in the result
		assertTrue("Voter not listed in the result", 
				   Utils.contains(votersAsAMessage, voter_id));
		
		// check whether her inner ballot is listed in the result
		assertTrue("Inner ballot not in the result",
				   Utils.contains(ballotsAsAMessage, selected_voter.getInnerBallot()));
		
		// deliver the data to the second server
		byte[] signedResult = finServer.processInputTally(signedTally);
		
		// check the signature on the result
		byte[] result = MessageTools.first(signedResult);
		byte[] signatureOnResult = MessageTools.second(signedResult);
		Verifier finServerVer = RegisterSig.getVerifier(Params.SERVER2ID, Params.SIG_DOMAIN);
		signature_ok = finServerVer.verify(signatureOnResult, result);
		assertTrue("Incorrect signature on the result of the final server",  signature_ok);
		
		// check if the selected voter can find her nonce and vote
		byte[] voteWithNonce = selected_voter.getVoteWithNonce();
		assertTrue("Voter's nonce not in the final result",
				   Utils.contains(result, voteWithNonce));
		
		// make sure that another (fresh) entry is not in the result
		selected_voter.createBallot(); // now the receipt data has changed
		byte[] anotherVoteWithNonce = selected_voter.getVoteWithNonce();
		assertFalse("Unexpected entry in the result",
				    Utils.contains(result, anotherVoteWithNonce));
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
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
		Decryptor decryptor = new  Decryptor();
		Signer signer = new Signer();
		// register the collecting server
		RegisterEnc.registerEncryptor(decryptor.getEncryptor(), Params.SERVER1ID, Params.ENC_DOMAIN);
		RegisterSig.registerVerifier(signer.getVerifier(), Params.SERVER1ID, Params.SIG_DOMAIN);
		// create the collecting server
		return new CollectingServer(signer, decryptor);		
	}

	private FinalServer createFinalServer() throws Exception {
		// create the final server' functionalities
		Decryptor decryptor = new  Decryptor();
		Signer signer = new Signer();
		// register the final server
		RegisterEnc.registerEncryptor(decryptor.getEncryptor(), Params.SERVER2ID, Params.ENC_DOMAIN);
		RegisterSig.registerVerifier(signer.getVerifier(), Params.SERVER2ID, Params.SIG_DOMAIN);
		return new FinalServer(signer, decryptor);
	}
	
	private Voter createVoter(int id, byte vote) throws Exception {
		// create voter's functionalities
		Decryptor decryptor = new  Decryptor();
		Signer signer = new Signer();
		// register
		RegisterEnc.registerEncryptor(decryptor.getEncryptor(), id, Params.ENC_DOMAIN);
		RegisterSig.registerVerifier(signer.getVerifier(), id, Params.SIG_DOMAIN);
		// create the voter
		return new Voter(id, vote, decryptor, signer);		
	}
}
