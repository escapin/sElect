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
	private static String[] voterIdentifiers = {"voter1", "voter2"};
	private byte[] electionID = {100};

	private static byte[] colServerVerifKey =  Utilities.hexStringToByteArray("305C300D06092A864886F70D0101010500034B003048024100863DD199FAAF19FDAA696E8ADECB5D1324B49E6AE904646875AEC215A48A69FB34996431C1938CA1A6796FD3FA65759E4A44A1D1313ABFFF9DEEF72404ACA0810203010001");
	private static byte[] colServerSignKey = Utilities.hexStringToByteArray("30820154020100300D06092A864886F70D01010105000482013E3082013A020100024100863DD199FAAF19FDAA696E8ADECB5D1324B49E6AE904646875AEC215A48A69FB34996431C1938CA1A6796FD3FA65759E4A44A1D1313ABFFF9DEEF72404ACA08102030100010240363F7D1870899A433C3E67018F8F3709A967A42D2805325E54504EF6580BE74F998BA32A3F126FC9795C86929CF6126E66A623B23E11BAA906EB558AEA6585D1022100D382F7ADB8D8891AB25922EADB6423F71F89A3994D7D00A5572191F8C0EB23CF022100A27A2C1EDD7F2208DB3ECEC16AB44DEFC4E1CA35DCAA7258227E16CFF059FAAF02202BF9DCF937A77DCA192EC33DC563AABEA4C5FF47CE7EA0F5BF89F149A102C2AD022048AF2AE9ABE0E1D2E071DA808041A4D3EC59ADE22693418FD7EE5C3A2DA5B315022100B869F084B862822A28D533C71FFA7799569C1CE8BBBA0A7F534AE4365393C6CA");

	private static byte[] finServEncrKey = Utilities.hexStringToByteArray("30819F300D06092A864886F70D010101050003818D003081890281810098C905F5200DCCD9060CFB8C8075E938216AC213A1F11690ACA5857D941D0AD40F83BC7DE1C983F8714343D4BFA490529AC35522A85B34BE4D57A9F2969A156F8A8A2B0896809D1F26D471BCFF1532ED3A3D21E8CC6C9622706FBAAC03FEB0D2CAE121AF4A97581656B0489B6925A6AF68A879228A4275D590460EDA4BB8F42D0203010001");
	private static byte[] finServDecrKey = Utilities.hexStringToByteArray("30820277020100300D06092A864886F70D0101010500048202613082025D0201000281810098C905F5200DCCD9060CFB8C8075E938216AC213A1F11690ACA5857D941D0AD40F83BC7DE1C983F8714343D4BFA490529AC35522A85B34BE4D57A9F2969A156F8A8A2B0896809D1F26D471BCFF1532ED3A3D21E8CC6C9622706FBAAC03FEB0D2CAE121AF4A97581656B0489B6925A6AF68A879228A4275D590460EDA4BB8F42D02030100010281801AE2FD5E66B6A6FEE616B0C9C7ED780E3DAB38DE1598849D0F14CDCA0C9F93C13FBDB21500FFE26E7D18163EC13EE77AF1EB3FF72A636A83B6BE9F94A636156155A7A9A543BC067B987BCEB02F6B8366F0CCDC064DE6AB8EE0BD85DE6E54A8B69319A5CF62B49F11C85376CE15C09248E4CB6BC348BD15AF8ED1128235A44F61024100F12CE485DAB353E6E27EE7C90E7ED1B35F07EFDAAC5682F41DAFDBA5D7C117A361B90688D132EE005F7BB45AE9166F55FB281EB685CB5C36B6B065D45EE3CE19024100A22D3BEC9B0DFC675EBD654887E3DA145C5AFAA00D1BB3EBB3F12857A77A3218A9ADCD371C51C066D2382C1184C5DA8E0884FAE450A3435641081A05CF90B1350241009FEBD5C8D081731439824F2E29F77C1405E2DC705330B67B2B284E6CC5095C24518B8042BEFD978615CA90886BE11D889517406E657FB8B0EB29430CB4B3381902403D7C66DEC2BE9FB6553DFB3B6F81DC79A1B6409513C33008A9F541755222CB017CBB4F3598C009F131BC6840D014EF52B03A32A1034D92C70DEAD36AE692160D024100EC89BC4437462781DDCBB184AE2A6CD2783362C004BE40C90F588B2FA7DD6434FF6E90EDF350195092EDCC28AF9ADEC08258405E91AA7CFBA319B6E045E1517C");
	private static byte[] finServVerifKey = Utilities.hexStringToByteArray("305C300D06092A864886F70D0101010500034B00304802410085F2AFD26CF40CFFBEB2DF88E08E8D774FE8525E28BFCC1103B4335512FD27DA02E2060A2F8266F3638CD8F3A25DA9725053054C008AEAE9C39A9ADAB6EE42050203010001");
	private static byte[] finServSignKey = Utilities.hexStringToByteArray("30820154020100300D06092A864886F70D01010105000482013E3082013A02010002410085F2AFD26CF40CFFBEB2DF88E08E8D774FE8525E28BFCC1103B4335512FD27DA02E2060A2F8266F3638CD8F3A25DA9725053054C008AEAE9C39A9ADAB6EE420502030100010240646DC637BE2AE94822C1D869B6FC0AC3272D67FC630F12C0BB0733E9985828B4EA03CA071B8B06A27F7782CF8F00FAF0A3C551CEDF089B422C919096374B0C21022100DB51FB355F2E516AA8739BF41650C636480990EBC84EE125A63ED8CC2E36400D0221009C598FDA02A3D631C02AF4ADCDE95087BE966AF33A98EB4CB197AFDD67B513D902203CACF295B26E6B01D9F699F7AE50CC7569FA9388579353008A7CAA97DA5511E502205FBADAC052837C3FA3F8E92D8DF9402C8D3E4E27B506327417FCD75A4A086E29022100C258801F21F906FECB23C1D28BA412D3C879CF0BC1B641236BE1E53118DFEA9D");

	private static Verifier colServVerif = new Verifier(colServerVerifKey);
	private static Signer colServSigner = new Signer(colServerVerifKey, colServerSignKey);
	private static Encryptor finServEnc = new Encryptor(finServEncrKey);

	private static CollectingServer colServer;
	private static FinalServer finServer;
	

	@Test
	public void testStringBytesConversion() throws Exception {
		String a = "abc@d.e";
		byte[] b = a.getBytes(Charset.forName("UTF-8"));
		String c = new String(b, Charset.forName("UTF-8"));
		assertEquals(a,c);
	}
	
	@Test
	public  void testKeys() throws Exception {
		byte[] message = {1,2,3};
		byte[] signature = colServSigner.sign(message);
		boolean ok = colServVerif.verify(signature, message);
		assertTrue(ok);
	}

	@Test
	public void testClientServerStandardExhange() throws Exception
	{
		// Make the voter create an inner ballot
		byte[] innerBallot = Voter.createBallot(2, finServEnc);

		// Deliver it to the collecting server
		byte[] receipt = colServer.collectBallot("voter1", innerBallot);
		assertNotNull(receipt);

		// Re-vote with a different ballot
		byte[] receipt0 = colServer.collectBallot("voter1", innerBallot);
		assertNotNull(receipt);
		assertTrue( MessageTools.equal(receipt, receipt0) );

		
		// Try to re-vote with a different ballot
		byte[] anotherBallot = Voter.createBallot(2, finServEnc);
		byte[] anotherReceipt = colServer.collectBallot("voter1", anotherBallot);
		assertNull(anotherReceipt);
				
		// Deliver the response message back to the voter
		boolean receiptOK =  Voter.validateReceipt(receipt, electionID, innerBallot, colServVerif);
		assertTrue( receiptOK );
		
		// Create another ballot
		byte[] innerBallot2 = Voter.createBallot(5, finServEnc);

		// Deliver it to the collecting server
		byte[] receipt2 = colServer.collectBallot("voter2", innerBallot2);
		assertNotNull(receipt2);

		// Deliver the response message back to the voter
		boolean receipt2OK =  Voter.validateReceipt(receipt2, electionID, innerBallot2, colServVerif);
		assertTrue( receipt2OK );
		
		// Check a wrong receipt
		boolean receipt3OK =  Voter.validateReceipt(receipt, electionID, innerBallot2, colServVerif);
		assertFalse( receipt3OK );
		

		/*
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
		*/
	}

	/*
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
	*/


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
		colServer = createCollectingServer();
		// finServer = createFinalServer();
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
		
		return new CollectingServer(colServSigner, electionID, voterIdentifiers);
	}

	/*
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
	*/

}