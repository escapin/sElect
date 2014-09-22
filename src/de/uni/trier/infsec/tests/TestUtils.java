package de.uni.trier.infsec.tests;

import java.io.IOException;

import junit.framework.TestCase;

import org.junit.Test;

import de.uni.trier.infsec.eVotingSystem.core.Utils;
import de.uni.trier.infsec.utils.MessageTools;
import de.uni.trier.infsec.eVotingSystem.apps.AppUtils;
import de.uni.trier.infsec.utils.Utilities;


public class TestUtils extends TestCase {

	@Test
	public void testConcatenateMessages()
	{
		// create and initialize array of messages
		byte[][] messageArr = new byte[10][];
		for (int i=0; i<messageArr.length; ++i)
			messageArr[i] = new byte[] {(byte)i,(byte)i};
		
		// concatenate it
		byte[] blob = Utils.concatenateMessageArray(messageArr);
		
		// go though the blob and make sure that all the messages are there
		int ind = 0;
		for( Utils.MessageSplitIter iter = new Utils.MessageSplitIter(blob); iter.notEmpty(); iter.next() ) {
			assertTrue("Unexpected message", MessageTools.equal(iter.current(), messageArr[ind]));
			++ind;
		}
		
		// and again this way
		for (int i=0; i<messageArr.length; ++i) {
			assertTrue("Missing mesage",  Utils.contains(blob, messageArr[i]) );
		}
	}
	
	@Test
	public void testReadWriteFile() throws IOException
	{
		String data = "Hello World!\nBye\tBye Bye\n"; 
		String filename = "test.txt";
		
		AppUtils.storeAsFile(data, filename);
		String dataBack=AppUtils.readCharsFromFile(filename);
		
		AppUtils.deleteFile(filename);
		//System.out.println(data + "isEqualTo\n" + dataBack);
		assertTrue(data.equals(dataBack));
		
		
		AppUtils.storeAsFile(data.getBytes(), filename);
		byte[] byteBack=AppUtils.readBytesFromFile(filename);
		
		AppUtils.deleteFile(filename);
		//System.out.println(data.getBytes() + "\t" + byteBack);
		assertTrue(Utilities.arrayEqual(data.getBytes(), byteBack));
		
		//System.out.println(new String(byteBack));
	}
}
