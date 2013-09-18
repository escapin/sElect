package de.uni.trier.infsec.eVotingSystem.coreSystem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import de.uni.trier.infsec.utils.MessageTools;


public class Utils 
{
	public static byte[] concatenateMessageArray(byte[][] messages) {
		return concatenateMessageArray(messages, messages.length);
	}
	
	public static byte[] concatenateMessageArray(byte[][] messages, int len) {
		byte[] msg = new byte[0];
		for (int i=len-1; i>=0; --i) { // from the last to the first
			msg = MessageTools.concatenate(messages[i], msg);
		}
		return msg;
	}	
	
	public static class MessageSplitIter {
		byte[] rest;
		
		public MessageSplitIter(byte[] message) {
			rest = message;
		}
		public boolean notEmpty() {
			return rest.length>0;
		}
		public byte[] current() {
			return MessageTools.first(rest);
		}
		public void next() {
			if (notEmpty()) 
				rest = MessageTools.second(rest);
		}
	}
	
	/**
	 * Checks whether 'messgeList' treated as an encoded array of messages
	 * contains 'message'. 
	 */
	public static boolean contains(byte[] messageList, byte[] message ) {
		for( MessageSplitIter iter = new MessageSplitIter(messageList); iter.notEmpty(); iter.next() )
			if (MessageTools.equal(iter.current(), message))
				return true;
		return false;
	}
	
	public static int compare(byte[] a1, byte[] a2) {
        int n1 = a1.length;
        int n2 = a2.length;
        int min = Math.min(n1, n2);
        for (int i = 0; i < min; i++){
            byte b1 = a1[i];
            byte b2 = a2[i];
            if (b1 != b2)
            	return b1 - b2;
        }
        return n1 - n2;
    }
	
//	 public static byte[][] copyOf(byte[][] original, int newLength) {
//		 byte[][] copy = new byte[newLength][];
//		 System.arraycopy(original, 0, copy, 0,
//                 Math.min(original.length, newLength));
//		 return copy;
//	 }
	
	
	public static void storeAsFile(byte[] data, String sFile) throws IOException {
		File f = new File(sFile);
		File fdir = new File(sFile.substring(0, sFile.lastIndexOf(File.separator)));
		if (f.exists()) f.delete();
		fdir.mkdirs();
		f.createNewFile();

		FileOutputStream file = new FileOutputStream(f);
		file.write(data);
		file.flush();
		file.close();
	}
	
	public static byte[] readFromFile(String path) throws IOException {
		FileInputStream f = new FileInputStream(path);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while (f.available() > 0){			
			bos.write(f.read());
		}
		f.close();
		byte[] data = bos.toByteArray();
		return data;
	}
}
