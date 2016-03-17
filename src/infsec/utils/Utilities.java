package infsec.utils;

import java.nio.charset.Charset;

public class Utilities {
	
	public static final String byteArrayToHexString(byte[] b) {
		final String hexChar = "0123456789ABCDEF";

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < b.length; i++)
		{
			sb.append(hexChar.charAt((b[i] >> 4) & 0x0f));
			sb.append(hexChar.charAt(b[i] & 0x0f));
		}
		return sb.toString();
	}
	
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	public static byte[] stringAsBytes(String str) {
		return str.getBytes(Charset.forName("UTF-8"));
	}
	
	public static String bytesAsString(byte[] bytes) {
		return new String(bytes, Charset.forName("UTF-8"));
	}
}
