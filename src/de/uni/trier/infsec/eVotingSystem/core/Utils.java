package de.uni.trier.infsec.eVotingSystem.core;

import de.uni.trier.infsec.utils.MessageTools;

public class Utils 
{
	public static byte[] concatenateMessageArrayWithDuplicateElimination(byte[][] messages) {
		return concatenateMessageArray(messages, messages.length);
	}

	public static byte[] concatenateMessageArrayWithDuplicateElimination(byte[][] messages, int len) {
		byte[] msg = new byte[0];
		byte[] last = null;
		for (int i=len-1; i>=0; --i) { // from the last to the first
			byte[] current = messages[i];
			if (last==null || !MessageTools.equal(current, last)) {
				msg = MessageTools.concatenate(current, msg);
			}
			last = current;
		}
		return msg;
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
}
