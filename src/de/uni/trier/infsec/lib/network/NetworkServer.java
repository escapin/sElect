package de.uni.trier.infsec.lib.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;


public class NetworkServer {
	
	// Now we have one Thread listening for connections and one Thread for each connection (reading the message).
	// The Messages get cached within a queue and every call to nextRequest returns the next element.
	
	private static Socket current = null; // Socket of the current message. Needed to answer the last request which has been processed
	private static Hashtable<Integer, Thread> listenThreads = new Hashtable<>();
	private static Hashtable<Integer, Hashtable<Socket, byte[]>> queues = new Hashtable<>();
	
	/**
	 * Returns the next requests (a message).
	 */
	public static byte[] nextRequest(int port) throws NetworkError {
		if (!queues.containsKey(port)) return null;
		Hashtable<Socket, byte[]> queue = queues.get(port);
		if (queue.isEmpty()) return null;
		
		// get the next message from the queue (null if there is none)
		if (queue.keys().hasMoreElements()) {
			current = queue.keys().nextElement();
			byte[] msg = queue.get(current);
			queue.remove(current); // Removed from queue straight after it has been processed. Avoids illegal messages to stay in queue forever!
			return msg;
		}
		return null;
	}
	
	public static void listenForRequests(int port) throws NetworkError {
		if (listenThreads.containsKey(port)) throw new NetworkError();
		
		Thread t = new Thread(new ListenThread(port));
		t.start();
		listenThreads.put(port, t);
	}
	
	/**
	 * Sends a response (a message) to the last request (that is the client who
	 * sent the last request obtained by calling method 'nextRequest').
	 */
	public static void response(byte[] message) throws NetworkError {
		if (message == null) return;
		if (current == null) return;
		
		OutputStream os;
		try {
			os = current.getOutputStream();
			os.write(message);
			os.flush();
			os.close();
			
			current.close();
			current= null;
		} catch (IOException e) {
			e.printStackTrace();
			throw new NetworkError();
		}
	}
	
	/**
	 * Returns the next request (a message) and responds right away with the emtpy response.
	 */
	public static byte[] read(int port) throws NetworkError {
		if (!listenThreads.containsKey(port)) listenForRequests(port);
		
		byte[] message = nextRequest(port);
		response(null);
		return message;
	}
	
	
	///// Implementation //////
	
	private static void readMessageInThread(Socket socket, int port) {
		queues.put(port, new Hashtable<Socket, byte[]>());
		
		ReaderThread rt = new ReaderThread(socket, port);
		Thread t = new Thread(rt);
		t.start();
	}
	
	static class ReaderThread implements Runnable {
		Socket mysock = null;
		int port;
		public ReaderThread(Socket s, int port) {
			mysock = s;
			this.port = port;
		}
		
		private static final int SOCKET_TIMEOUT = 5000;
		@Override
		public void run() {
			try {
				mysock.setSoTimeout(SOCKET_TIMEOUT);
				InputStream is = mysock.getInputStream();
				ByteArrayOutputStream buffer = new ByteArrayOutputStream(); // we can handle Messages up to 1K
				
				// This loop is only needed for the case, that the message is larger than the array. It lets us read parts of the msg and buffer it.
				byte[] bufferArr = new byte[512];
				do {
					int receivedBytesCount = is.read(bufferArr); // read() blocks and waits until there are bytes received. It returns the number of bytes received
					buffer.write(bufferArr, 0, receivedBytesCount); // We write the bytes (only the as many as received) to a stream (array of dynamic length)
				} while (is.available() > 0); // > 0 means there are currently bytes to read in the stream (which have been written to the other sides OutputStream)
//				while (is.available() > 0) buffer.write(bufferArr, 0, is.read(bufferArr));
				
				// -> Doesn't this loop uses the processor? 
				// The Loop does only last as long as there are bytes to read in the stream. As soon as everything has been read, we stop reading the stream.
				// So there is no problem with this, because we start, read all bytes and stop reading after everything has been received.
				// -> How does it work?
				// Sorry, I did un-shorten the loop a bit, so it should be easier to understand. We first check, if there are bytes available to read.
				// If so, we read them into a buffer array (512 bytes). If the array is full, read will return 512 and available() will decrease 512.
				// Then we write the bytes we read to a stream, which gives us an array of the correct length after we finished - so we do not need to
				// handle a dynamic growing array. After all bytes have been read the loop quits and we return the array.
				// -> How do we know that the message ends?
				// Well, we expect the other side to send the whole message at once. Though the TCP protocol might fragment our message while transport, 
				// available will give us the number of bytes we can read from the stream.
				// So you´re right, we might have problems in cases, where the network buffer of the system if overfull and the data can´t be delivered fast
				// enough, but I think this should work fine in general.
				// If you like, we can introduce a leading message-length and wait for the whole message to be received correctly, 
				// so we could enforce some kind of integrity...
				
				Hashtable<Socket, byte[]> currQueue = queues.get(port);
				currQueue.put(mysock, buffer.toByteArray()); // Put the received message to the queue. Socket is kept open!
				queues.put(port, currQueue);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};
	
	static class ListenThread implements Runnable {
		private ServerSocket server = null;
		
		private int thePort;
		
		public ListenThread(int port) {
			this.thePort = port;
		}
				
		@Override
		public void run() {
			// If not already done: Init server
			if (server == null) {
				try {
					server = new ServerSocket(thePort);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
			
			try {
				while (true) {					
					Socket next = server.accept(); // This is a blocking call, which means the Threat is suspended until a new connection is received (no CPU load)
					readMessageInThread(next, thePort);
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}			
		}
	};
	
}
