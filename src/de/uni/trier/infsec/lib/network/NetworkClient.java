package de.uni.trier.infsec.lib.network;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class NetworkClient {
	
	private static final int SOCKET_TIMEOUT = 5000;

	/**
	 * Sends message to the given server/port (without waiting for any response)
	 */
	public static void send(byte[] message, String server, int port) throws NetworkError {
		Socket s = null;
		try {
			s = new Socket(server, port);
			OutputStream os = s.getOutputStream();			
			os.write(message);
			os.flush();
		} catch (Exception e) {
			e.printStackTrace();
			throw new NetworkError();
		} finally {
			try { s.close(); } catch (Exception e) {}
		}
	}

	/**
	 * Sends message to the given server/port and gets the response
	 * (also a message).
	 */
	public static byte[] sendRequest(byte[] message, String server, int port) throws NetworkError {
		Socket s = null;
		try {
			// First thing to do is, open a connection to the Server
			s = new Socket(server, port);
			s.setSoTimeout(SOCKET_TIMEOUT);
			// Now, after connection is established, we send the message to the server. 
			OutputStream os = s.getOutputStream();
			os.write(message); // Here first we send the message to the 
			os.flush(); // This call enforces the message to be sent NOW

			// Now, when the message has been sent, we read the socket for a response.
			InputStream is = s.getInputStream();
			ByteArrayOutputStream buffer = new ByteArrayOutputStream(); // This buffer lets us write buffered parts of the message and get an array of the complete msg in the end

			byte[] bufferArr = new byte[512];
			do {
				int receivedBytesCount = is.read(bufferArr); // read() blocks and waits until there are bytes received. It returns the number of bytes received
				buffer.write(bufferArr, 0, receivedBytesCount); // We write the bytes (only the as many as received) to a stream (array of dynamic length)
			} while (is.available() > 0); // > 0 means there are currently bytes to read in the stream (which have been written to the other sides OutputStream)
			
			return buffer.toByteArray(); // This will return the complete message that has been received
		} catch (Exception e) {
			System.out.println(port);
			e.printStackTrace();
			throw new NetworkError();
		} finally {
			try { s.close(); } catch (Exception e) {}
		}
	}

}
