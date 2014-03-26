package de.uni.trier.infsec.tests;

import java.io.File;

import junit.framework.TestCase;
import org.junit.Test;


import de.uni.trier.infsec.eVotingSystem.core.CollectingServer;
import de.uni.trier.infsec.eVotingSystem.core.FinalServer;
import de.uni.trier.infsec.eVotingSystem.core.Params;
import de.uni.trier.infsec.eVotingSystem.core.Utils;
import de.uni.trier.infsec.eVotingSystem.core.Voter;
import de.uni.trier.infsec.eVotingSystem.core.Utils.MessageSplitIter;
import de.uni.trier.infsec.functionalities.nonce.NonceGen;
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
	private byte[] electionID = {100};
	private byte[] electionID2 = {101};
	
	@Test
	public void testClientServerExhange() throws Exception
	{
		
		int voterID=1;
		int candidateNumber=7;
		Voter voter = createVoter(voterID, electionID);
		
		// create the ballot
		byte[] ballot = voter.createBallot(candidateNumber);
		assertNotNull(ballot);
		// deliver it to the collecting server
		byte[] response = colServer.collectBallot(ballot);
		// check whether the response is correct
		Voter.ResponseTag response_tag = voter.validateResponse(response);
		assertTrue(response_tag == Voter.ResponseTag.VOTE_COLLECTED);

		// now this should fail
		ballot=voter.createBallot(candidateNumber);
		assertTrue(ballot == null);
				
		// testing re-voting
		ballot=voter.reCreateBallot();
		assertNotNull(ballot);
		// deliver it to the collecting server
		response = colServer.collectBallot(ballot);
		// not it should accept the old response
		response_tag=voter.validateResponse(response);
		assertTrue(response_tag == Voter.ResponseTag.VOTE_COLLECTED);

		// testing voting for a different candidate
		ballot = voter.forceCreateBallot(candidateNumber+1);
		assertNotNull(ballot);
		// deliver it to the collecting server
		response = colServer.collectBallot(ballot);
		// now it should not accept this response
		response_tag = voter.validateResponse(response);
		assertTrue(response_tag == Voter.ResponseTag.ALREADY_VOTED);
		
		
		// and try validate some trash
		try{
			response_tag = voter.validateResponse(ballot);
			fail("Revoking -- exception expected");
		} catch (Voter.MalformedMessage e) {}	
	
		// try vote with some trash
		try {
			colServer.collectBallot(response);
			fail("Voting with trash -- exception expected");
		} catch (CollectingServer.MalformedMessage e) {}
		
		//create a voter with a different electionID
		Voter voter2=createVoter(voterID+2, electionID2);
		// create the ballot
		ballot = voter2.createBallot(candidateNumber);
		// deliver it to the collecting server
		response = colServer.collectBallot(ballot);
		// now the response_tag should say the electionID is incorrect
		response_tag=voter2.validateResponse(response);
		assertTrue(response_tag == Voter.ResponseTag.INVALID_ELECTION_ID);

		
		//create a voter with a wrong voterID
		Voter voter3=createVoter(Params.NumberOfVoters, electionID);
		// create the ballot
		ballot = voter3.createBallot(candidateNumber);
		// deliver it to the collecting server
		try {
			colServer.collectBallot(ballot);
			fail("Not eligible voter");
		} catch (CollectingServer.MalformedMessage e) {}


		// try to vote when the election is over
		colServer.getResult();
		// create a correct voter
		Voter voter4=createVoter(voterID+4,electionID);
		// create the ballot
		ballot=voter4.createBallot(candidateNumber);
		// create the ballot
		response = colServer.collectBallot(ballot);
		// now the response_tag should say the election is over
		response_tag=voter4.validateResponse(response);
		assertTrue(response_tag == Voter.ResponseTag.ELECTION_OVER);

	}
	
	@Test
	public void testVotingProcess() throws Exception 
	{
		// create 5 voters with ids 0..4
		Voter[] voters = new Voter[5];
		for (int i=0; i<5; ++i)
			voters[i] = createVoter(i, electionID);
		
		// make them vote
		int candidateNumber=31;
		for (int i=0; i<5; ++i) {
			byte[] ballot = voters[i].createBallot(candidateNumber+i);
			byte[] response = colServer.collectBallot(ballot);
			Voter.ResponseTag response_tag = voters[i].validateResponse(response);
			assertTrue(response_tag == Voter.ResponseTag.VOTE_COLLECTED);
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
				if (MessageTools.equal(ballots[i], voters[id].getReceipt().innerBallot)) {
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
		
		// extract election id, ballots, and voter list
		byte[] el_id = MessageTools.first(publicData);
		byte[] restOfPublicData = MessageTools.second(publicData);
		byte[] ballotsAsAMessage = MessageTools.first(restOfPublicData);
		byte[] votersAsAMessage = MessageTools.second(restOfPublicData);
		
		// check the returned election ID
		assertTrue("Wrong election id in the partial result",
				   MessageTools.equal(el_id, electionID));
		
		// lets take some voter
		Voter selected_voter = voters[3];
		byte[] voter_id = MessageTools.intToByteArray(selected_voter.getVoterId());
		
		// and check if she is listed in the result
		assertTrue("Voter not listed in the result", 
				   Utils.contains(votersAsAMessage, voter_id));
		
		// check whether her inner ballot is listed in the result
		assertTrue("Inner ballot not in the result",
				   Utils.contains(ballotsAsAMessage, selected_voter.getReceipt().innerBallot));
		
		// deliver the data to the second server
		byte[] signedResult = finServer.processTally(signedTally);
		
		// check the signature on the result
		byte[] result = MessageTools.first(signedResult);
		byte[] signatureOnResult = MessageTools.second(signedResult);
		Verifier finServerVer = RegisterSig.getVerifier(Params.SERVER2ID, Params.SIG_DOMAIN);
		signature_ok = finServerVer.verify(signatureOnResult, result);
		assertTrue("Incorrect signature on the result of the final server",  signature_ok);
		
		// check the election id in the result
		byte[] result_el_id = MessageTools.first(result);
		assertTrue("Invalid election ID in the result",
				   MessageTools.equal(result_el_id, electionID));
		
		byte[] result_entries = MessageTools.second(result);
		
		// check if the selected voter can find her nonce and vote
		byte[] voterNonce = selected_voter.getReceipt().nonce;
		
		boolean contains = false;
		for( MessageSplitIter iter = new MessageSplitIter(result_entries); !contains && iter.notEmpty(); iter.next() )
			contains=MessageTools.equal(MessageTools.first(iter.current()), voterNonce);
		assertTrue("Voter's nonce not in the final result",	contains);
		
		// make sure that another (fresh) nonce is not in the result
		NonceGen noncegen = new NonceGen(); 
		byte[] anotherFreshNonce = noncegen.newNonce();
		contains = false;
		for( MessageSplitIter iter = new MessageSplitIter(result_entries); !contains && iter.notEmpty(); iter.next() )
			contains=MessageTools.equal(MessageTools.first(iter.current()), anotherFreshNonce);
		assertFalse("Unexpected entry in the result", contains);
	}
	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		File dbFile = new File(PKIServerCore.DEFAULT_DATABASE);
		if (dbFile.exists()){
			dbFile.delete();
			PKIServerCore.initDB();
		}
		PKI.useLocalMode();
		colServer = createCollectingServer(electionID);
		finServer = createFinalServer(electionID);
	}
//	dbFile = new File(PKIServerCore.DEFAULT_DATABASE + "-journal");
//	if (dbFile.exists())
//		dbFile.delete();

	///////////////////////////////////////////////////////////////////////////////////////////////

	private CollectingServer createCollectingServer(byte[] electionID) throws Exception {
		// create the collecting server's functionalities
		Decryptor decryptor = new  Decryptor();
		Signer signer = new Signer();
		// register the collecting server
		RegisterEnc.registerEncryptor(decryptor.getEncryptor(), Params.SERVER1ID, Params.ENC_DOMAIN);
		RegisterSig.registerVerifier(signer.getVerifier(), Params.SERVER1ID, Params.SIG_DOMAIN);
		// create the collecting server
		return new CollectingServer(electionID, decryptor, signer);		
	}

	private FinalServer createFinalServer(byte[] electionID) throws Exception {
		// create the final server' functionalities
		Decryptor decryptor = new  Decryptor();
		Signer signer = new Signer();
		// register the final server
		RegisterEnc.registerEncryptor(decryptor.getEncryptor(), Params.SERVER2ID, Params.ENC_DOMAIN);
		RegisterSig.registerVerifier(signer.getVerifier(), Params.SERVER2ID, Params.SIG_DOMAIN);
		return new FinalServer(electionID, decryptor, signer);
	}
	
	private Voter createVoter(int id, byte[] electionID) throws Exception {
		// create voter's functionalities
		Decryptor decryptor = new  Decryptor();
		Signer signer = new Signer();
		// register
		RegisterEnc.registerEncryptor(decryptor.getEncryptor(), id, Params.ENC_DOMAIN);
		RegisterSig.registerVerifier(signer.getVerifier(), id, Params.SIG_DOMAIN);
		// create the voter
		return new Voter(id, electionID, decryptor, signer);		
	}
}
