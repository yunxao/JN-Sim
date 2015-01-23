package tid.utils;

import infonet.javasim.bgp4.comm.IOBGPMessage;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class BytesUtils {
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
		byte[] result = new byte[2];
		for (int i = 0, j = 7;i<2;i++,j--){
			result[j] = (byte)(value & 255);
			value = (short) (value >> 8);
		}
		return result;
	}
	
	/**
	 * Turn a int value into four bytes
	 * @param value turned
	 * @return result of change
	 */
	static public byte[] toBytes(int value){
		byte[] result = new byte[4];
		for (int i = 0, j = 3;i<4;i++,j--){
			
			result[j] = (byte)(value & 255);
			value = value >> 8;
		}
		return result;
	}
	
	/**
	 * Turn a int value into eight bytes
	 * @param value turned
	 * @return result of change
	 */
	static public byte[] toBytes(long value){
		byte[] result = new byte[8];
		for (int i = 0, j = 7;i<8;i++,j--){
			result[j] = (byte)(value & 255);
			value = value >> 8;
		}
		return result;
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
	public static byte[] invBytes(byte[] bytes){
		byte[] temp = new byte[bytes.length];

		for (int i = 0, j = bytes.length-1; i < bytes.length;i++,j--)
			temp[i] = bytes[j];
		return temp;
	}
	public static boolean[] byteToBin(byte[] bytes){
		return byteToBin(bytes,bytes.length*8);
	}
	public static boolean[] byteToBin(byte[] bytes,int size)
	{
		if (size > bytes.length*8)
			throw new Error("Size error");
		boolean[] binary = new boolean[size];
		for (int i = 0; i<size/8;i++)
		{
			for (int j = 0; (j<8) && (i*8+j < size);j++)
			{
				int pos = i*8+j;
				binary[pos] = !((bytes[i] & (1 << j)) == 0);
			}
		}
		
		return binary;
	}
	
	public static byte[] binToByte(boolean[] binary) {
		return binToByte(binary,binary.length);
	}
	public static byte[] binToByte(boolean[] binary,int size)
	{
		// TODO comprobar la division entera
		// 8 
		int sizeByte = binary.length /8;
		sizeByte = (binary.length % 8 == 0)?sizeByte:sizeByte+1;
		byte[] bytes = new byte[sizeByte];
		for (int i = 0; i<sizeByte;i++)
		{
			for (int j = 0; (j<8) && (i*8+j < size);j++)
			{
				int pos = i*8+j;
				if (binary[pos])
					bytes[i] = (byte)(bytes[i] | (1 << j));
			}
		}
		
		return bytes;
	}
	public static Object[] arrayCopy(Object[] source, Object[] dest,int posSource, int posDest, int size){
		if(posSource+size >= source.length)
			throw new Error("Size error in source array");
		Object[] temp = new Object[posDest+size];
		for(int i = 0; i < posDest;i++)
			temp[i] = dest[i];
		for(int i = 0; i < size;i++)
			temp[i+posDest] = source[i+posDest];
		dest = temp;
		return dest;
	}
	public static Object[] arrayCopy(Object[] source, Object[] dest,int posSource, int posDest){
		return arrayCopy (source,dest, posSource,posDest,source.length-posSource);
	}
	public static byte[] arrayCopy(byte[] source, byte[] dest,int posSource, int posDest){
		return arrayCopy (source,dest, posSource,posDest,source.length-posSource);
	}

	public static Object[] arrayCopy(Object[] source,int posSource, int size){
		return arrayCopy(source,null,posSource,0,size);
	}
	public static Object[] arrayCopy(Object[]source,int size){
		return arrayCopy(source,null,0,0,size);
	}
	
	public static Object[] arrayCopy(Object source, Object[] dest,int posDest){
		Object [] array = {source};
		return arrayCopy(array, dest, 0, posDest, 1);
		
	}
	public static byte[] arrayCopy(byte source, byte[] dest,int posDest){
		byte [] array = {source};
		return arrayCopy(array, dest, 0, posDest, 1);
		
	}
	public static byte[] arrayCopy(byte[] source, byte[] dest,int posSource, int posDest, int size){
		int arraySize = ((dest != null) && (dest.length) > size+posDest)?dest.length:size+posDest;
		if(arraySize < 0){
			System.out.println("size: "+size);
			System.out.println("posSource: "+posSource);
			System.out.println("posDest: "+posDest);
			System.out.println("Sourcelenth: "+source.length);
			
			throw new Error("Negative size. size: "+ arraySize);
		}
		if(posSource+size-1 > source.length){
			int temp = posSource + size - 1;
			throw new Error("Size error in source array. source length: "+source.length+". pos+size: "+ temp );
		}
		byte[] temp = new byte[arraySize];
		if (dest != null)
			for(int i = 0; i < dest.length;i++)
				temp[i] = dest[i];
		for(int i = 0; i < size;i++)
			temp[i+posDest] = source[i+posSource];
		return temp;
	}
	public static byte[] arrayCopy(byte[] source,int posSource, int size){
		return arrayCopy(source,null,posSource,0,size);
	}
	public static byte[] arrayCopy(byte[]source,int size){
		return arrayCopy(source,null,0,0,size);
	}
	public static String byteToString(byte[] bytes,int init, int size){
		String cad = "";
		if (size > bytes.length)
			size = bytes.length;
		for (int i = init; i < size;i++){
			if (i< 10)
				cad += i+"   |";
			else if (i< 100)
				cad += i+"  |";
			else if (i< 1000)
				cad += i+" |";
		}
		cad += "\n";
		for (int i = init; i < size;i++){

			if (bytes[i] < -99)
				cad += bytes[i]+"|";
			else if (bytes[i] < -9)
				cad += bytes[i]+" |";
			else if (bytes[i] < 0)
				cad += bytes[i]+"  |";
			else if (bytes[i] < 10)
				cad += bytes[i]+"   |";
			else if (bytes[i] < 100)
				cad += bytes[i]+"  |";
			else if (bytes[i] < 1000)
				cad += bytes[i]+" |";

				

		}
		return cad;
	}
	public static String byteToString(byte[] bytes){
		return byteToString(bytes,0,bytes.length);
	}
}
