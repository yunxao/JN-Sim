package infonet.javasim.bgp4.comm;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class IOBGPMessage {
	/**
	 * Get a byte of a buffer in the offset position 
	 * @param buffer 
	 * @param offset
	 * @return the byte
	 */
	public static int getU8(byte[] buffer, int offset){
		return ((int) buffer[offset] & 0x000000ff);
	}
	/**
	 * Get two byte of a buffer in the offset position
	 * @param buffer
	 * @param offset
	 * @return
	 */
	static public int getU16 (byte[]buffer, int offset)
	{
		int result = 0;
		for (int i = 0; i < 2; i++)
			result = ((result << 8) & 0xff00) + (buffer[offset + i] & 0xff);
		return result;
	}
	/**
	 * Get four byte of a buffer in the offset position
	 * @param buffer
	 * @param offset
	 * @return
	 */
	static public long getU32 (byte[]buffer, int offset)
	{
		long result = 0;
		for (int i = 0; i < 4; i++)
			result = ((result << 8) & 0xffffff00) + (buffer[offset + i] & 0xff);
		return result;
	}
	
	/**
	 * Turn a short value into two bytes
	 * @param value turned
	 * @return result of change
	 */
	static public byte[] toBytes(short value){
		byte[] result = new byte[8];
		for (short i = 0;i<2;i++){
			result[i] = (byte)(value & 255);
			value = (short)(value >> 8*i);
		}
		return null;
	}
	
	/**
	 * Turn a int value into four bytes
	 * @param value turned
	 * @return result of change
	 */
	static public byte[] toBytes(int value){
		byte[] result = new byte[4];
		for (int i = 0;i<4;i++){
			result[i] = (byte)(value & 255);
			value = value >> 8*i;
		}
		return null;
	}
	
	/**
	 * Turn a int value into eight bytes
	 * @param value turned
	 * @return result of change
	 */
	static public byte[] toBytes(long value){
		byte[] result = new byte[8];
		for (int i = 0;i<8;i++){
			result[i] = (byte)(value & 255);
			value = value >> 8*i;
		}
		return null;
	}
	
	/**
	 * Get ip address from a buffer in the offset position. 
	 * @param buffer
	 * @param offset
	 * @param v6 This value say that it's ipv4 or ipv6
	 * @return Ip value in InetAddress format
	 * @throws UnknownHostException
	 */
	
	public static InetAddress getIp(byte[] buffer, int offset, boolean v6) throws UnknownHostException {
		if (v6)
			return Inet6Address.getByAddress(IOBGPMessage.getBytes(buffer,offset,6));
		return Inet4Address.getByAddress(IOBGPMessage.getBytes(buffer,offset,6));
		
	}
	
	/**
	 * Turn a ip into bytes
	 * @param value turned
	 * @return result of change
	 */
	// Este procedimiento no es necesario porque tenemos el getAddress()
	public static byte[] toBytes(InetAddress ip){
		return ip.getAddress();
	}
	
	/**
	 * Get some bytes from buffer in a possition
	 * @param buffer 
	 * @param offset where they are in the buffer 
	 * @param length number of bytes that we want take 
	 * @return
	 */
	static public byte[] getBytes (byte[]buffer, int offset, int length) {
	     byte[]result = new byte[length];
	     for (int i = 0; i < length; i++)
	       result[i] = buffer[offset + i];
	     return result;
	}
	
	/**
	 * Copy some bytes into a buffer
	 * @param bufferOrigin From where we copy 
	 * @param buffer where we copy
	 * @param offset position in the the buffer where we copy 
	 * @param size Number of bytes that we want copy
	 */
	public static void setBytes(byte[] bufferOrigin, byte[]buffer,int offset, int size){
		for (int i = 0; i<size;i++)
			buffer[offset+i] = bufferOrigin[i];
		
	}
}
