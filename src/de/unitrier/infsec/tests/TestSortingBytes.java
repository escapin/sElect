package de.unitrier.infsec.tests;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import junit.framework.TestCase;

import org.junit.Test;

import de.unitrier.infsec.functionalities.nonce.NonceGen;
import de.unitrier.infsec.functionalities.pkenc.Decryptor;
import de.unitrier.infsec.utils.MessageTools;
import de.unitrier.infsec.utils.Utilities;
import selectvoting.system.core.Utils;


public class TestSortingBytes extends TestCase  
{
	
	
	//private static String[] voterIdentifiers = {"voter1", "voter2", "voter3"};
	//private static int numberOfVoters=voterIdentifiers.length;
	private static int numberOfVoters=5;
	//private static byte[] electionID = {0101};

	private static final NonceGen noncegen = new NonceGen(); // nonce generation functionality
	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
	}
	
	// [nonce, vote]
	private static byte[] createBallot(int votersChoice){
		byte[] nonce = noncegen.nextNonce();
		byte[] vote = MessageTools.intToByteArray(votersChoice);
		byte[] ballot = MessageTools.concatenate(nonce, vote);
		return ballot;
	}
	
	private static byte[][] hashEncryptData(byte[][] data){
		Decryptor decr=new Decryptor();
		//Signer sign=new Signer();
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		byte[][] encData = new byte[data.length][];
		byte[][] hashData = new byte[data.length][];
		for(int i=0;i<data.length;++i){
			encData[i]= decr.getEncryptor().encrypt(data[i]);
			md.update(encData[i]);
			//MessageDigest tc1 = md.clone();
		    hashData[i] = md.digest();
		    md.reset();
		}
		return hashData;
	}
	
	@Test
	public void testMixServers() throws Exception
	{
		byte[][] innermostBallots = new byte[numberOfVoters][];
		for(int i=0; i<numberOfVoters;++i)
			innermostBallots[i]=createBallot(i);
		
		byte[][] hashedEncrBallots=hashEncryptData(innermostBallots);
		
		// scramble a bit
		byte[] tmp=hashedEncrBallots[0];
		hashedEncrBallots[0]=hashedEncrBallots[numberOfVoters-1];
		hashedEncrBallots[numberOfVoters-1]=tmp;
		
			System.out.println("\tUnsorted\t\t\t\t\trepr: UNSIGNED int\t\t\t\t\t\t\t\t\t\t\t\t\t\t\trepr: SIGNED int");
			System.out.println(toString(hashedEncrBallots));
		
		byte[][] toBeSortedSigned=MessageTools.copyOf(hashedEncrBallots);
		byte[][] toBeSortedUnsigned=MessageTools.copyOf(hashedEncrBallots);
		
		Utils.sortSigned(toBeSortedSigned, 0, toBeSortedSigned.length); 
		Utils.sortUnsigned(toBeSortedUnsigned, 0, toBeSortedUnsigned.length);
			
			System.out.println("\tSorted signed\t\t\t\t\trepr: UNSIGNED int\t\t\t\t\t\t\t\t\t\t\t\t\t\t\trepr: SIGNED int");
			System.out.println(toString(toBeSortedSigned));
			System.out.println("\tSorted unsigned\t\t\t\t\trepr: UNSIGNED int\t\t\t\t\t\t\t\t\t\t\t\t\t\t\trepr: SIGNED int");
			System.out.println(toString(toBeSortedUnsigned));
		
		assertTrue(bijection(hashedEncrBallots, toBeSortedSigned));
		assertTrue(bijection(hashedEncrBallots, toBeSortedUnsigned));
		assertTrue(bijection(toBeSortedSigned, toBeSortedUnsigned));
		
		Utils.sort(hashedEncrBallots, 0, hashedEncrBallots.length);
			
			System.out.println("\tSorted\t\t\t\t\t\trepr: UNSIGNED int\t\t\t\t\t\t\t\t\t\t\t\t\t\t\trepr: SIGNED int");
			System.out.println(toString(hashedEncrBallots));
		
	}

	private static String toString(byte[][] a)
	{
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<a.length;++i){
			sb.append(Utilities.byteArrayToHexString(a[i]) + "\t" + byteArraysToUnsignIntString(a[i]) + "\t\t\t" + byteArraysToSignIntString(a[i]) + "\n");
		}
		return sb.toString();
	}
	
	private static String intArrayToString(int[] nums){
		StringBuffer sb = new StringBuffer("[");
		for(int i=0;i<nums.length;++i){
			sb.append(nums[i] + "];[");
		}
		sb.append("]");
		return sb.toString();
	}
	private static int[] bytesToSignedInt(byte[] a)
	{
		int[] nums = new int[a.length];
		for(int i=0;i<a.length;++i)
			nums[i] = a[i];
		return nums;
	}
	private static String byteArraysToSignIntString(byte[] a){
		int[] nums = bytesToSignedInt(a);
		return intArrayToString(nums);
	}
	private static int[] bytesToUnsignedInt(byte[] a){
		int[] nums = new int[a.length];
		for(int i=0;i<a.length;++i)
			nums[i] = (a[i] & 0xff);
		return nums;
	}
	private static String byteArraysToUnsignIntString(byte[] a){
		int[] nums = bytesToUnsignedInt(a);
		return intArrayToString(nums);
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