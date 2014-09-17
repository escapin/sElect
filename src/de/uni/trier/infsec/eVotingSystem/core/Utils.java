package de.uni.trier.infsec.eVotingSystem.core;

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
	

	public static boolean arrayEqual(Object[] a1, Object[] a2)
        {
                if (a1.length != a2.length) return false;
                for (int i = 0; i < a1.length; i++) {
                        if (!a1[i].equals(a2[i])) 
                                return false;
                }
                return true;
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
	
	public static void out(String s){
		System.out.print(s);
	}
	public static void outl(String s){
		System.out.println(s);
	}
	
	
//	 public static byte[][] copyOf(byte[][] original, int newLength) {
//		 byte[][] copy = new byte[newLength][];
//		 System.arraycopy(original, 0, copy, 0,
//                 Math.min(original.length, newLength));
//		 return copy;
//	 }
	
	
}
