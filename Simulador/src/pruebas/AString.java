package pruebas;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;


public class AString {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	public static void main(String[] args) throws NumberFormatException, IOException {
		// TODO Auto-generated method stub
		
		while (true)
		{
			InputStreamReader isr = new InputStreamReader(System.in);
			BufferedReader br = new BufferedReader (isr);
			
			Long long1;
			long1 = new Long(br.readLine());
			
			long largo = long1.longValue();;
			byte addr[] = new byte[4];

			for (int i = 0;i<4;i++)
			{
				long temp = 1;
				for (int j = i; j<3; j++)
					temp *= 256;
				addr[i] =(byte) ( largo / temp);
				// si es mayor de un valor restarle algo
				if (addr[i]>127)
					addr[i] -= 256;
				largo = largo % temp;
			}
			InetAddress s = InetAddress.getByAddress(addr);
			System.out.println(s);
		}
		

			

	}

}
// 4145343750
// 192.168.0.1