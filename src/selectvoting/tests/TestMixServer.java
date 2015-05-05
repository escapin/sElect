package selectvoting.tests;


import junit.framework.TestCase;

import org.junit.Test;

import de.unitrier.infsec.functionalities.digsig.Signer;
import de.unitrier.infsec.functionalities.digsig.Verifier;
import de.unitrier.infsec.functionalities.nonce.NonceGen;
import de.unitrier.infsec.functionalities.pkenc.Decryptor;
import de.unitrier.infsec.utils.MessageTools;
import selectvoting.system.core.MixServer;
import selectvoting.system.core.Tag;
import selectvoting.system.core.Utils;
import selectvoting.system.core.Utils.MessageSplitIter;


public class TestMixServer extends TestCase  
{
	private static final int NUMBER_MIXSERV=5;
	
	private static MixServer[] mixServ;
	private static Signer authServSign;
	
	//private static String[] voterIdentifiers = {"voter1", "voter2", "voter3"};
	//private static int numberOfVoters=voterIdentifiers.length;
	private static int numberOfVoters=100;
	private static byte[] electionID = {0101};

	private static final NonceGen noncegen = new NonceGen(); // nonce generation functionality
	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		//System.out.println("setUp started");
		authServSign = new Signer();
		Verifier authServVerif = authServSign.getVerifier();
		createMixServers(authServVerif, NUMBER_MIXSERV);
		//System.out.println("setUp executed");
	}
	
	private static void createMixServers(Verifier authServVerif, int num){
		mixServ=new MixServer[num];
		
		Verifier precServVerif=authServVerif;
		for(int i=0;i<num;++i){
			Decryptor decr=new Decryptor();
			Signer sign=new Signer();
			mixServ[i]=new MixServer(decr, sign, precServVerif, electionID);
			precServVerif=sign.getVerifier();
		}
	}
	
	@Test
	public void testMixServers() throws Exception
	{
		byte[][] innermostBallots = new byte[numberOfVoters][];
		for(int i=0; i<numberOfVoters;++i)
			innermostBallots[i]=createBallot(i);
		
		byte[][] encrBallots=new byte[numberOfVoters][];
		for(int i=0; i<numberOfVoters;++i)
			encrBallots[i]=encryptBallot(mixServ, innermostBallots[i]);
		
		// scramble a bit
		byte[] tmp=encrBallots[0];
		encrBallots[0]=encrBallots[numberOfVoters-1];
		encrBallots[numberOfVoters-1]=tmp;
		// the input ballots to a mix server are always sorted 
		Utils.sort(encrBallots, 0, encrBallots.length);
		
		byte[] ballotsAsAMessage=Utils.concatenateMessageArray(encrBallots, encrBallots.length);
		// add election id, tag and sign
		byte[] elID_ballots = MessageTools.concatenate(electionID, ballotsAsAMessage);
		byte[] input = MessageTools.concatenate(Tag.BALLOTS, elID_ballots);
		byte[] signatureOnInput = authServSign.sign(input);
		byte[] signedInput = MessageTools.concatenate(input, signatureOnInput);
		
		/*
		 * MIXING PHASE
 		 */
		byte[] data = signedInput;
		for(int i=0;i<mixServ.length;++i)
			data=mixServ[i].processBallots(data);
		
		
		// verify the signature of the last mixserver
		byte[] tagged_payload = MessageTools.first(data);
		byte[] signature = MessageTools.second(data);
		assertTrue(mixServ[mixServ.length-1].getVerifier().verify(signature, tagged_payload));
		
		// the tag is the BALLOT tag
		byte[] tag = MessageTools.first(tagged_payload);
		assertTrue(MessageTools.equal(tag, Tag.BALLOTS));
		byte[] payload = MessageTools.second(tagged_payload);

		
		// the election id is the proper one
		byte[] el_id = MessageTools.first(payload);
		assertTrue(MessageTools.equal(el_id, electionID));		
		
		// retrieve
		byte[] innermostballotsAsAMessage = MessageTools.second(payload);
		
		byte[][] entries = new byte[numberOfVoters][];
		int numberOfEntries=0;
		
		// Loop over the entries
		for( MessageSplitIter iter = new MessageSplitIter(innermostballotsAsAMessage); iter.notEmpty(); iter.next() ) {
			// the entries must not be greater than innermostBallot
			assertFalse(numberOfEntries>numberOfVoters);			
			entries[numberOfEntries] = iter.current();
			++numberOfEntries;
		}
		assertTrue(numberOfEntries==innermostBallots.length && bijection(entries, innermostBallots));
		

	}

	// [nonce, vote]
	private static byte[] createBallot(int votersChoice){
		byte[] nonce = noncegen.nextNonce();
		byte[] vote = MessageTools.intToByteArray(votersChoice);
		byte[] ballot = MessageTools.concatenate(nonce, vote);
		return ballot;
	}
	// Encr_i[ elID, Encr_{i=1}[elID, Encr_{i+2}[...] ] ]
	private static byte[] encryptBallot(MixServer[] mixServ, byte[] ballot){
		byte[] encrBallot = ballot;
		for(int i=mixServ.length-1; i>=0; --i){ // from the last to the first
			encrBallot=MessageTools.concatenate(electionID, encrBallot);
			encrBallot=mixServ[i].getEncryptor().encrypt(encrBallot);			
		}
		return encrBallot;
	}

	private static boolean bijection(byte[][] arr1, byte[][] arr2)
	{
		if(arr1.length!=arr2.length) return false;
		byte[][] a1=MessageTools.copyOf(arr1);
		byte[][] a2=MessageTools.copyOf(arr2);
		Utils.sort(a1, 0, a1.length);
		Utils.sort(a2, 0, a2.length);
		for(int i=0;i<a1.length;i++)
			if(!MessageTools.equal(a1[i],a2[i])) 
				return false;
		return true;
	}

}