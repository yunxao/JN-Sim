package tid.utils;

import infonet.javasim.bgp4.comm.IOBGPMessage;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
/**
 * This class has a auxiliar functions
 * @author Francisco Huertas
 *
 */
public class Utils {
	/**
	 * Convert a Long address to String
	 * @param addr
	 * @return
	 * @throws NumberFormatException
	 * @throws UnknownHostException
	 */
	public static String addrLongToString(long addr) throws NumberFormatException, UnknownHostException{
		byte addr_byte[] = new byte[4];

		for (int i = 0;i<4;i++)
		{
			long temp = 1;
			for (int j = i; j<3; j++)
				temp *= 256;
			addr_byte[i] =(byte) ( addr / temp);
			// si es mayor de un valor restarle algo
			if (addr_byte[i]>127)
				addr_byte[i] -= 256;
			addr = addr % temp;
		}
		InetAddress s = InetAddress.getByAddress(addr_byte);
		return s.getHostAddress();
	}
	/**
	 * Convert a address from Inet4Address format to Long id
	 * @param address address in Inet4Address
	 * @return Long with value of address
	 */
	public static Long inetAddressToLong(InetAddress address){
		if (address instanceof Inet4Address){
			return IOBGPMessage.getU32(address.getAddress(), 0);
		}
		return null;
	}
	public static Long stringAddressToLong(String address) throws UnknownHostException{
		return inetAddressToLong(InetAddress.getByName(address));
	}
	/**
	 * Return the element MyT[MyI]. <br>
	 * (Thanks Ville Petteri Pöyhönen)
	 * @param MyT Array 
	 * @param MyI Position in the array
	 * @return MyT[MyI]
	 */
	public static long getElement(long[] MyT, int MyI) {
		long ret_val;

		ret_val=(long) MyT[MyI];
		return ret_val;
	}
	/**
	 * Return the element MyT[MyI]. <br>
	 * @param MyT Array 
	 * @param MyI Position in the array
	 * @return MyT[MyI]
	 */
	public static Object getElement(Object[] MyT, int MyI) {
		return MyT[MyI];
	}
	public static void nothing(Object a){
		System.out.println(a.getClass().getCanonicalName());
	}
	
	public static long resta (long a, long b){
		return a-b;
	}

}
