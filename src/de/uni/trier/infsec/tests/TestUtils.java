package de.uni.trier.infsec.tests;

import junit.framework.TestCase;
import org.junit.Test;

import de.uni.trier.infsec.coreSystem.Utils;
import de.uni.trier.infsec.utils.MessageTools;


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
}
