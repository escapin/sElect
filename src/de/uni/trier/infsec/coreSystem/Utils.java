package de.uni.trier.infsec.coreSystem;

import de.uni.trier.infsec.utils.MessageTools;

public class Utils 
{
	public static byte[] concatenateMessageArray(byte[][] messages) 
	{
		return concatenateMessageArray(messages, messages.length);
	}
	
	public static byte[] concatenateMessageArray(byte[][] messages, int len) 
	{
		byte[] msg = new byte[0];
		for (int i=len-1; i>=0; --i) { // from the last to the first
			msg = MessageTools.concatenate(messages[i], msg);
		}
		return msg;
	}	
	
	public static class MessageSplitIter 
	{
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
	 * contains 'mesage'. 
	 */
	public static boolean contains(byte[] messageList, byte[] message ) 
	{
		for( MessageSplitIter iter = new MessageSplitIter(messageList); iter.notEmpty(); iter.next() )
			if (MessageTools.equal(iter.current(), message))
				return true;
		return false;
	}
}
