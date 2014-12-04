package selectvoting.system.core;

import de.unitrier.infsec.utils.MessageTools;
import java.util.Hashtable;
import java.util.Arrays;

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
	
	public static void sort(byte[][] byteArrays, int fromIndex, int toIndex)
	{
		/* to VERIFY: comment the body of this method out */
		
		Arrays.sort(byteArrays, fromIndex, toIndex, new java.util.Comparator<byte[]>() {
			public int compare(byte[] a1, byte[] a2) {
				return Utils.compare(a1, a2);
			}
		});
	}
	
	public static class ObjectsMap
	{
		//private MessagePairList map = new MessagePairList();
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
		
		private static class MessagePairList {
			private static class MessagePair
			{
				Object key;
				Object value;
				MessagePair next;
				public MessagePair(Object key, Object value) {
					this.key = key;
					this.value = value;
					this.next = null;
				}
			}
			public MessagePair head, last;
			public void put(Object key, Object value) throws NullPointerException{
				if(key==null || value==null)
					throw new NullPointerException();
				MessagePair newEntry = new MessagePair(key, value);
				if(head==null)
					head=last=newEntry;
				else{
					last.next=newEntry;
					last=newEntry;
				}
			}

			public Object get(Object key) {
				for(MessagePair entry=head; entry!=null; entry=entry.next) {
					if( entry.key.equals(key) )
						return entry.value;
				}
				return null;
			}

			public boolean containsKey(Object key) {
				return get(key) != null;
			}
		}
	}
}
