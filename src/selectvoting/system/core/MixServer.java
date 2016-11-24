package selectvoting.system.core;

import selectvoting.system.core.Utils.MessageSplitIter;
import functionalities.pkenc.Decryptor;
import functionalities.pkenc.Encryptor;
import functionalities.digsig.Signer;
import functionalities.digsig.Verifier;
import infsec.utils.MessageTools;

public class MixServer 
{	
	// Cryptographic functionalities
	private final Signer signer;
	private final Decryptor decryptor;
	private final Verifier precServVerif;
	private final byte[] electionID;
	// private final int numberOfVoters;



	// PUBLIC CLASSES
	/**
	 * Error thrown if the input data is ill-formed.
	 */

	public static class MalformedData extends Exception {
		public int errCode;
		public String description;
		public MalformedData(int errCode, String description) {
			this.errCode = errCode;
			this.description = description;
		}
		public String toString() {
			return "Mix Server Error: " + description;
		}
	}

	public static class ServerMisbehavior extends Exception {
		public int errCode;
		public String description;
		public ServerMisbehavior(int errCode, String description) {
			this.errCode = errCode;
			this.description = description;
		}
		public String toString() {
			return "Previous Server Misbehavior: " + description;
		}
	}

	// CONSTRUCTORS

	public MixServer(Decryptor decryptor, Signer signer, Verifier precServVerif, byte[] electionID) {
		this.signer = signer;
		this.decryptor = decryptor;
		this.electionID = electionID;
		this.precServVerif = precServVerif;
	}



	// this is the randomly chosen message array
	//@ public ghost instance byte[][] msg;
	// this is the value of entr_arr after the call to Utils.sort
	//@ public ghost instance byte[][] a;
	// this is the value of entr_arr after the call to ConservativeExtenson.getRandomMessages
	//@ public ghost instance byte[][] b;
	/**
	 * Here are some model methods which are used as lemmas.
	 */
	/**
	 * Antisimmetry of compare <= 0
	 */
	/*@	  
	  requires Utils.compare(a,b) <= 0; 	  
	  requires Utils.compare(b,a) <= 0;
	  ensures  (\dl_array2seq(a) == \dl_array2seq(b));
	  ensures \result;
	  public static model two_state boolean antiSym(byte[] a, byte[] b){
	     return (\dl_array2seq(a) == \dl_array2seq(b));
	  }
	 @*/
	/**
	 * Reflexivity of compare <= 0
	 */
	/*@	  
	  ensures Utils.compare(a,a) <= 0;
	  ensures \result;
	  public static model two_state boolean refl(byte[] a){
	     return Utils.compare(a,a) <= 0;
	  } 
	 @*/
	/**
	 * Transitivity of compare <= 0
	 */
	/*@	  
	  requires Utils.compare(a,b) <= 0; 	  
	  requires Utils.compare(b,c) <= 0;
	  ensures  Utils.compare(a,c) <= 0;
	  ensures \result;
	  public static model two_state boolean trans(byte[] a, byte[] b, byte[] c){
	     return Utils.compare(a,c) <= 0;
	  }
	 @*/

	/*@	  
	  requires Utils.compare(a,b) > 0;	  
	  ensures  Utils.compare(b,a) <= 0;
	  ensures \result;
	  public static model two_state boolean total(byte[] a, byte[] b){
	     return Utils.compare(b,a) <= 0;
	  }
	 @*/
	/**
	 * Split equality in two gte.
	 */
	/*@	   	  
	  requires \dl_array2seq(a) == \dl_array2seq(b);
	  ensures  Utils.compare(a,b) <= 0;
	  ensures  Utils.compare(b,a) <= 0;
	  ensures \result;
	  public static model two_state boolean antiSym2(byte[] a, byte[] b){
	     return Utils.compare(a,b) <= 0 && Utils.compare(b,a) <= 0;
	  } 
	 @*/
	/**
	 * If a is sorted then a[j] is smaller or equal than all elements with an index >= j.
	 */
	/*@
	  requires 0 <= j  && j < a.length;	  
	  requires (\forall int i; 0 <= i && i < a.length-1; Utils.compare(a[i],a[i+1]) <= 0);
	  ensures (\forall int i; j <= i && i < a.length; Utils.compare(a[j],a[i]) <= 0);
	  ensures \result;
	  public static model two_state boolean minEl(byte[][] a,int j){
	     return (\forall int i; j <= i && i < a.length; Utils.compare(a[j],a[i]) <= 0);
	  } 
	 @*/	
	/**
	 * If a and b are permutations of each other and both are sorted, then a is a copy of b.
	 */
	/*@
	  requires \dl_seqPerm(\dl_array2seq2d(a), \dl_array2seq2d(b));
	  requires (\forall int i; 0 <= i && i < a.length-1; Utils.compare(a[i],a[i+1]) <= 0); 	  
	  requires (\forall int i; 0 <= i && i < b.length-1; Utils.compare(b[i],b[i+1]) <= 0);
	  ensures  (\dl_array2seq2d(a) == \dl_array2seq2d(b));
	  ensures \result;
	  public static model two_state boolean sortedPermIsEqual(byte[][] a, byte[][] b) {
	    return (\dl_array2seq2d(a) == \dl_array2seq2d(b));
	  } 
	 @*/


	// PUBLIC METHODS

	/**
	 * Process data that supposed to be the signed output of the preceding mix server. 
	 * Returns the signed result of the mixing. 
	 * 
	 * I/O format:
	 * 			SIGN_prec[tag, elID, ballotsAsAMessage]
	 * where, each ballot:
	 * 			ENC_curr[elID, innerBallot] 
	 * 
	 */
	/*
	 * Assuming that the messages stored in the CE are a permutation of msg we prove
	 * That the values in entr_arr after sort() are equal to the values of entr_arr after 
	 * ConservativeExtension.retrieveSortetMessages().
	 */
	/*@
	  public normal_behaviour
	  requires ConservativeExtension.messages != null;
	  requires (\forall int i; 0 <= i && i < ConservativeExtension.messages.length; ConservativeExtension.messages[i] != null);
	  requires msg != null;
	  requires msg != ConservativeExtension.messages;
	  requires (\forall int i; 0 <= i && i < msg.length; msg[i] != null);	  
	  requires \dl_seqPerm(\dl_array2seq2d(msg), \dl_array2seq2d(ConservativeExtension.messages));
	  ensures true;
	 @*/	
	public byte[] processBallots(byte[] data) throws MalformedData, ServerMisbehavior {




		byte[] ballotsAsAMessage = checkAndGetBallots(data);

		byte[][] entr_arr = extractBallots(ballotsAsAMessage);


		/**
		 * Assumption: the messages in the array variable 'entr_arr' above 
		 * are a permutation of the messages in the array variable 'msg' in Setup.java
		 * 
		 * Assumption necessary because of the issues in verifying that the 
		 * encryption scheme works:
		 * The property 
		 * 	'The message decrypted is equals to the message previously 
		 * 	 encrypted' 
		 * is too demanding and time consuming for KeY.
		 */


		entr_arr = sort(entr_arr); 

		/** 
		 * We show that this statement does not change any locations of already created objects on the heap.
		 * Also we show that the values of entr_arr before the statement and after the statement fulfill the requirements
		 * of the  sortedPermIsEqual model method. The contract of sortedPermIsEqual then guarantees that entr_arr before
		 * the redundant statement and entr_arr after the redundant statement are copies of each other. The contract of 
		 * sortedPermIsEqual is proved separately. 
		 */
		/*@
		 public normal_behaviour
		 requires ConservativeExtension.messages != null;
		 requires (\forall int i; 0 <= i && i < ConservativeExtension.messages.length; ConservativeExtension.messages[i] != null);		 	  	 
		 ensures sortedPermIsEqual(entr_arr, \old(entr_arr));		 
		 ensures entr_arr != null;
		 ensures (\forall int i; 0 <= i && i < entr_arr.length; entr_arr[i] != null);
		 ensures ConservativeExtension.messages != null;
	     ensures (\forall int i; 0 <= i && i < ConservativeExtension.messages.length; ConservativeExtension.messages[i] != null);
		 assignable \nothing;
		 @*/
		{
			entr_arr = ConservativeExtension.retrieveSortedMessages();
		}


		byte[] signedResult = postProcess(entr_arr);

		return signedResult;
	}

	/**
	 * Returns a sorted copy of entr_arr.
	 */
	/*@
	  public normal_behaviour	
	  requires ConservativeExtension.messages != null;
	  requires (\forall int i; 0 <= i && i < ConservativeExtension.messages.length; ConservativeExtension.messages[i] != null);  	  
	  ensures \dl_seqPerm(\dl_array2seq2d(\result), \dl_array2seq2d(entr_arr));
	  ensures (\forall int i; 0 <= i && i < \result.length-1; Utils.compare(\result[i],\result[i+1]) <= 0);	
	  ensures \fresh(\result);
	  ensures ConservativeExtension.messages != null;
	  ensures (\forall int i; 0 <= i && i < ConservativeExtension.messages.length; ConservativeExtension.messages[i] != null);	     
	  assignable \nothing;	    
	@*/	
	private byte[][] sort(byte[][] entr_arr) {

		byte[][] result = Utils.copyOf(entr_arr);

		if (result != null) {
			Utils.sort(result, 0, result.length);
			return result;
		} else {
			return new byte[][] {};
		}
	}
	
	public byte[][] decryptBallots(byte[][] msg){
		byte[][] res= new byte[msg.length][];
		for (int i = 0; i < msg.length; i++) {
			try{
				res[i] = decryptor.decrypt(msg[i]);
			}catch(Throwable t){}
		}
		return res;
	}
	
	public void checkBallots(byte[][] msg) throws ServerMisbehavior{
		for (int i = 0; i < msg.length-1; i++) {
			
			byte[] first = null;
			byte[] second = null;
			
			try{
				first = msg[i];
				second = msg[i+1];
			}catch(Throwable t){}
			
			
			if(! (Utils.compare(first, second) <= 0)){
				throw new ServerMisbehavior(-2, "Ballots not sorted");
			}
			if((Utils.compare(first, second) == 0)){
				throw new ServerMisbehavior(-3, "Duplicate ballots");
			}
		}
	}
	
	public byte[][] checkandRemoveElectionId(byte[][] msg){
		byte[][] res = new byte[msg.length][];
		for (int i = 0; i < msg.length; i++) {
			byte[] elId = MessageTools.first(msg[i]);			
			if(MessageTools.equal(elId, electionID)){
				try{
					res[i] = MessageTools.second(msg[i]);
				}catch(Throwable t){}
			}
		}
		return res;
	}

	public byte[][] split(byte [] ballots){
		byte[][] messages = new byte[0][];
		byte[] bal  = ballots;
		while(bal.length >= 0){
			byte[] first = MessageTools.first(bal);
			bal = MessageTools.second(bal);
			messages = addEntry(messages, first);
		}
		return messages;
	}

	public byte[][] addEntry(byte[][] messages, byte[] m){

		byte[][] res = new byte[messages.length+1][];
		try{
			for(int i = 0 ; i <messages.length; i++){
				res[i] = messages[i];
			}
			res[messages.length] = MessageTools.copyOf(m);
		}catch(Throwable t){}
		return res;

	}
	
	
	public byte[][] extractBallots(byte[] msg) throws ServerMisbehavior{
		byte[][] res = split(msg);
		checkBallots(res);
		res = decryptBallots(res);
		res = checkandRemoveElectionId(res);
		return res;
	}


	/**
	 * We assume this method returns a permutation of the array 'msg'.
	 */
	/*@
	  public normal_behaviour
	  requires ConservativeExtension.messages != null;
	  requires (\forall int i; 0 <= i && i < ConservativeExtension.messages.length; ConservativeExtension.messages[i] != null);
	  ensures \result != null;
	  ensures \dl_seqPerm( \dl_array2seq2d(\result), \dl_array2seq2d(msg));
	  ensures ConservativeExtension.messages != null;
	  ensures (\forall int i; 0 <= i && i < ConservativeExtension.messages.length; ConservativeExtension.messages[i] != null);	   
	  assignable \strictly_nothing; 
	 @*/
//	private byte[][] extractBallots3(byte[] ballotsAsAMessage) throws ServerMisbehavior{
//		//ArrayList<byte[]> entries = new ArrayList<byte[]>();
//		EntryList entries = new EntryList();
//
//		// Loop over the input entries 
//		byte[] last = null;
//		//int numberOfEntries = 0;
//		for( MessageSplitIter iter = new MessageSplitIter(ballotsAsAMessage); iter.notEmpty(); iter.next() ) {
//			byte[] current = iter.current();
//			if (last!=null && Utils.compare(last, current)>0)
//				throw new ServerMisbehavior(-2, "Ballots not sorted");
//			if (last!=null && Utils.compare(last, current)==0)
//				throw new ServerMisbehavior(-3, "Duplicate ballots"); 
//			last = current;
//			byte[] decryptedBallot = decryptor.decrypt(current); // decrypt the current ballot
//			if (decryptedBallot == null){
//				//System.out.println("[MixServer.java] Decryption failed for ballot #" + numberOfEntries);
//				continue;
//			}
//			byte[] elID = MessageTools.first(decryptedBallot);
//			if (elID!=null || MessageTools.equal(elID, electionID)) // otherwise ballot is invalid and we ignore it
//				entries.add(MessageTools.second(decryptedBallot));
//			else {
//				try {
//					//System.out.println("[MixServer.java] Ballot #" + numberOfEntries + " invalid");
//				} catch (Throwable t) {}
//			}
//			//++numberOfEntries;
//		}
//
//		byte[][] entr_arr = new byte[entries.size()][];
//		entries.toArray(entr_arr);
//		return entr_arr;
//	}
	/**
	 * Since this method is called after the redundant statement, we don't care what it does.
	 *
	 */
	/*@
	  public normal_behaviour
	  ensures true;
	  assignable \strictly_nothing;
	 @*/
	private byte[] postProcess(byte[][] entr_arr) {

		if(entr_arr == null){
			return null;
		}

		// format entries as one message
		//byte[] entriesAsAMessage = Utils.concatenateMessageArray(entr_arr, entr_arr.length);
		byte[] entriesAsAMessage = Utils.concatenateMessageArray(entr_arr);


		// add election id, tag and sign
		byte[] elID_entriesAsAMessage = MessageTools.concatenate(electionID, entriesAsAMessage);
		byte[] result = MessageTools.concatenate(Tag.BALLOTS, elID_entriesAsAMessage);
		byte[] signatureOnResult = signer.sign(result);
		byte[] signedResult = MessageTools.concatenate(result, signatureOnResult);
		return signedResult;
	}

	/**
	 * We assume it doesn't change any fields.
	 */
	/*@
	  public normal_behaviour
	  requires ConservativeExtension.messages != null;
	  requires (\forall int i; 0 <= i && i < ConservativeExtension.messages.length; ConservativeExtension.messages[i] != null);	  
	  ensures ConservativeExtension.messages != null;
	  ensures (\forall int i; 0 <= i && i < ConservativeExtension.messages.length; ConservativeExtension.messages[i] != null);	   
	  assignable \strictly_nothing; 
	 @*/
	private byte[] checkAndGetBallots(byte[] data) throws MalformedData {
		// verify the signature of previous server
		byte[] tagged_payload = MessageTools.first(data);
		byte[] signature = MessageTools.second(data);
		if (!precServVerif.verify(signature, tagged_payload))
			throw new MalformedData(1, "Wrong signature");

		// check the tag
		byte[] tag = MessageTools.first(tagged_payload);
		if (!MessageTools.equal(tag, Tag.BALLOTS))
			throw new MalformedData(2, "Wrong tag");		
		byte[] payload = MessageTools.second(tagged_payload);

		// check the election id 
		byte[] el_id = MessageTools.first(payload);
		if (!MessageTools.equal(el_id, electionID))
			throw new MalformedData(3, "Wrong election ID");

		// retrieve and process ballots (store decrypted entries in 'entries')
		byte[] ballotsAsAMessage = MessageTools.second(payload);
		return ballotsAsAMessage;
	}


	// methods for testing
	public Encryptor getEncryptor(){
		return decryptor.getEncryptor();
	}	
	public Verifier getVerifier(){
		return signer.getVerifier();
	}
}
