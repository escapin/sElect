package selectvoting.system.core;

import infsec.utils.MessageTools;

import java.util.Arrays;
import java.util.Hashtable;

public class Utils 
{
	public static byte[] concatenateMessageArrayWithDuplicateElimination(byte[][] messages) {
		return concatenateMessageArray(messages, messages.length);
	}
	
	// we assume messages[][] is sorted
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

	
	/**
	 * Compares its two array arguments for lexicographic order.
	 * Each byte is interpreted as an signed byte. 
	 * 
	 * @param a1 the first array to be compared.
	 * @param a2 the second array to be compared.
	 * @return a negative integer, zero, or a positive integer as the first argument is 
	 * 			less than, equal to, or greater than the second
	 */
	public static int compareSigned(byte[] a1, byte[] a2) {
		int n1 = a1.length;
		int n2 = a2.length;
		int min = Math.min(n1, n2);
		for (int i = 0; i < min; i++){
			int b1 = a1[i];
			int b2 = a2[i];
			if (b1 != b2)
				return b1 - b2;
		}
		return n1 - n2;
	}
	public static void sortSigned(byte[][] byteArrays, int fromIndex, int toIndex)
	{	
		Arrays.sort(byteArrays, fromIndex, toIndex, new java.util.Comparator<byte[]>() {
			public int compare(byte[] a1, byte[] a2) {
				return Utils.compareSigned(a1, a2);
			}
		});
	}
	
	/**
	 * Compares the two arrays for lexicographic order.
	 * Each byte is interpreted as an unsigned byte. 
	 * 
	 * @param a1 the first array to be compared.
	 * @param a2 the second array to be compared.
	 * @return a negative integer, zero, or a positive integer as the first argument is 
	 * 			less than, equal to, or greater than the second
	 */
	public static int compareUnsigned(byte[] a1, byte[] a2) {
		int n1 = a1.length;
		int n2 = a2.length;
		int min = Math.min(n1, n2);
		for (int i = 0; i < min; i++){
			int b1 = (a1[i] & 0xff); // last 8 bits interpreted as natural numbers
			int b2 = (a2[i] & 0xff);
			if (b1 != b2)
				return b1 - b2;
		}
		return n1 - n2;
	}
	public static void sortUnsigned(byte[][] byteArrays, int fromIndex, int toIndex)
	{	
		Arrays.sort(byteArrays, fromIndex, toIndex, new java.util.Comparator<byte[]>() {
			public int compare(byte[] a1, byte[] a2) {
				return Utils.compareUnsigned(a1, a2);
			}
		});
	}
	
	public static int compare(byte[] a1, byte[] a2) {
		return compareUnsigned(a1, a2);
	}
	
	public static void sort(byte[][] byteArrays, int fromIndex, int toIndex){
		sortUnsigned(byteArrays, fromIndex, toIndex);
	}
	
	public static class ObjectsMap
	{
		private Hashtable<Object, Object> map = new Hashtable<Object, Object>();
		
		public void put(Object key, Object value) throws NullPointerException
		{
			map.put(key, value);
		}
		
		public Object get(Object key)
		{
			return map.get(key);
		}
		
		public boolean containsKey(Object key){
			return map.containsKey(key);
		}
	}
}
