package de.uni.trier.infsec.tests;

import java.io.IOException;
import java.nio.charset.Charset;

import junit.framework.TestCase;

import org.junit.Test;

import static de.uni.trier.infsec.eVotingSystem.apps.AppUtils.readCharsFromFile;
import static de.uni.trier.infsec.eVotingSystem.core.Utils.errln;
import de.uni.trier.infsec.eVotingSystem.apps.AppParams;
import de.uni.trier.infsec.eVotingSystem.apps.AppUtils;
import de.uni.trier.infsec.eVotingSystem.apps.Helper;
import de.uni.trier.infsec.eVotingSystem.core.CollectingServer;
import de.uni.trier.infsec.eVotingSystem.core.FinalServer;
import de.uni.trier.infsec.eVotingSystem.core.Voter;
import de.uni.trier.infsec.eVotingSystem.parser.ElectionManifest;
import de.uni.trier.infsec.eVotingSystem.parser.Keys;
import de.uni.trier.infsec.eVotingSystem.parser.KeysParser;
import de.uni.trier.infsec.functionalities.digsig.Signer;
import de.uni.trier.infsec.functionalities.digsig.Verifier;
import de.uni.trier.infsec.functionalities.pkenc.Decryptor;
import de.uni.trier.infsec.functionalities.pkenc.Encryptor;
import de.uni.trier.infsec.utils.MessageTools;
import de.uni.trier.infsec.utils.Utilities;



public class TestTargetSystem extends TestCase  
{
	private static Decryptor decryptorCS;
	private static Signer signerCS;
	private static ElectionManifest manifest; 
	private static CollectingServer colServer;
	private static FinalServer finServer;
	private static Verifier finServVerif;
	// private byte[] electionID = {100};
	// private byte[] electionID2 = {101};

	@Test
	public void testKeysConsistency() throws Exception {
		Voter voter = createVoter("a@b.c");
		Encryptor encryptorCS = decryptorCS.getEncryptor();
		assertTrue( MessageTools.equal(encryptorCS.getPublicKey(), voter.getCSPublicKey()));
	}

	@Test
	public void testStringBytesConversion() throws Exception {
		String a = "abc@d.e";
		byte[] b = a.getBytes(Charset.forName("UTF-8"));
		String c = new String(b, Charset.forName("UTF-8"));
		assertEquals(a,c);
	}

	@Test
	public void testClientServerStandardExhange() throws Exception
	{
		// (the servers are already created by the setup)
		// Create a voter
		Voter voter = createVoter("a@b.c");


		// OTP EXCHANGE

		// Obtain an otp request from the voter
		byte[] otpReq = voter.createOTPRequest();

		// Deliver it to the collecting server
		CollectingServer.Response otpResponse = colServer.processRequest(otpReq);
		// Let's check in on the server side. It should be an otp response:
		assertNotNull(otpResponse);
		assertTrue(otpResponse.otp_response);
		assertNotNull(otpResponse.responseMsg);
		byte[] otp = otpResponse.otp;
		assertNotNull(otp);
		System.out.println(otpResponse.email);
		assertEquals("a@b.c", otpResponse.email);

		// Deliver the response message (not the otp) back to the voter
		Voter.ResponseTag respTag = voter.validateResponse(otpResponse.responseMsg);
		assertTrue( respTag == Voter.ResponseTag.OTP_REQUEST_ACCEPTED );


		// BALLOT CASTING EXCHANGE

		// Make the voter create a ballot
		byte[] ballot = voter.createBallot(2, otp);

		// Deliver it to the collecting server
		CollectingServer.Response ballotResponse = colServer.processRequest(ballot);
		// Check in on the server side. It should be an otp response:
		assertNotNull(ballotResponse);
		assertFalse(ballotResponse.otp_response);
		assertNotNull(ballotResponse.responseMsg);
		assertNull(ballotResponse.otp);

		// Deliver the response message back to the voter
		respTag = voter.validateResponse(ballotResponse.responseMsg);
		// For the following to hold true, one needs to make sure that the start and end time in the manifest
		// are set correctly (and the ballot is not rejected because it is too early or too late).
		assertTrue( respTag == Voter.ResponseTag.VOTE_COLLECTED );

		// Let's try to re-vote (using the same inner ballot)
		byte[] ballot1 = voter.reCreateBallot(otp);
		ballotResponse = colServer.processRequest(ballot1);
		respTag = voter.validateResponse(ballotResponse.responseMsg);
		// System.out.println(respTag);
		assertTrue( respTag == Voter.ResponseTag.VOTE_COLLECTED );

		// Let's try to vote for a different candidate using the same otp:
		Voter voter2 = createVoter("a@b.c");
		byte[] ballot2 = voter2.createBallot(0, otp);
		ballotResponse = colServer.processRequest(ballot2);
		respTag = voter2.validateResponse(ballotResponse.responseMsg);
		// System.out.println(respTag);
		assertTrue( respTag == Voter.ResponseTag.ALREADY_VOTED );


		// CHECK THE RESULT

		assertTrue( colServer.getNumberOfBallots() == 1 );
		byte[] auxResult = colServer.getResult();
		// and give it to the final server
		byte[] finalResult = finServer.processTally(auxResult);

		Helper.FinalEntry[] fes = Helper.finalResultAsText(finalResult, finServVerif, manifest.electionID);
		assertTrue( fes.length == 1 );
		assertTrue( fes[0].vote.equals("2") );
	}

	@Test
	public void testClientServerProblems() throws Exception {
		Voter.ResponseTag respTag;
		CollectingServer.Response response;
		byte[] ballot;

		Voter voter = createVoter("a@b.c");
		Voter voter2 = createVoter("d@e.f");
		Voter voter3 = createVoter("completely@unknown.voter");

		// Sending trash:
		byte[] trash = {1,2,3,4,5};
		try {
			colServer.processRequest(trash);
			fail("Malformed message accepted");
		} catch (CollectingServer.MalformedMessage ex) {}

		// Obtain otp (nothing wrong to be expected)
		byte[] otpReq = voter.createOTPRequest();
		CollectingServer.Response otpResponse = colServer.processRequest(otpReq);
		// byte[] otp = otpResponse.otp;

		// Casting a ballot with wrong otp
		ballot = voter.createBallot(2, trash);
		response = colServer.processRequest(ballot);
		respTag = voter.validateResponse(response.responseMsg);
		assertTrue(respTag == Voter.ResponseTag.WRONG_OTP);

		// Casting a ballot with the otp of somebody else
		ballot = voter2.createBallot(2, trash);
		response = colServer.processRequest(ballot);
		respTag = voter2.validateResponse(response.responseMsg);
		assertTrue(respTag == Voter.ResponseTag.WRONG_OTP);

		// A non-eligible voter tries to obtain an otp:
		otpReq = voter3.createOTPRequest();
		otpResponse = colServer.processRequest(otpReq);
		respTag = voter3.validateResponse(otpResponse.responseMsg);
		assertTrue(respTag == Voter.ResponseTag.INVALID_VOTER_ID);		
	}


	/*
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
	 */

	/*
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
	 */

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		manifest = AppUtils.retrieveElectionManifest();	
		colServer = createCollectingServer();
		finServer = createFinalServer();
		finServVerif = new Verifier(manifest.finalServer.verification_key);
	}
	//	dbFile = new File(PKIServerCore.DEFAULT_DATABASE + "-journal");
	//	if (dbFile.exists())
	//		dbFile.delete();

	///////////////////////////////////////////////////////////////////////////////////////////////

	private CollectingServer createCollectingServer() throws Exception 
	{
		// read the private key
		String filename = AppParams.PRIVATE_KEY_path + "CollectingServer_PR.json";
		String keyJSON = null;
		try {
			keyJSON = readCharsFromFile(filename);
		} catch (IOException e) {
			errln("Unable to access: " + filename);
		}
		Keys k = KeysParser.parseJSONString(keyJSON);
		if(k.encrKey==null || k.decrKey==null || k.signKey==null || k.verifKey==null)
			errln("Invalid Collecting Server's keys.");

		// create the functionalities and the server 
		decryptorCS = new Decryptor(k.encrKey, k.decrKey);
		signerCS = new Signer(k.verifKey, k.signKey);
		return new CollectingServer(manifest, decryptorCS, signerCS);		
	}

	private FinalServer createFinalServer() throws Exception {
		// read the private key
		String filename = AppParams.PRIVATE_KEY_path + "FinalServer_PR.json";
		String keyJSON = null;
		try {
			keyJSON = readCharsFromFile(filename);
		} catch (IOException e) {
			errln("Unable to access: " + filename);
		}
		Keys k = KeysParser.parseJSONString(keyJSON);
		if(k.encrKey==null || k.decrKey==null || k.signKey==null || k.verifKey==null)
			errln("Invalid Final Server's keys.");

		// create the functionalities and the server 
		Decryptor decryptor = new Decryptor(k.encrKey, k.decrKey);
		Signer signer = new Signer(k.verifKey, k.signKey);
		return new FinalServer(manifest, decryptor, signer);		
	}

	private Voter createVoter(String email) throws Exception {
		byte[] id = Utilities.stringAsBytes(email);
		return new Voter(id, manifest);		
	}
}