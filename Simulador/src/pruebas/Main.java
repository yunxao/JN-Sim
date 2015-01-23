package pruebas;

import java.io.*;

import java.net.InetAddress;

import org.jdom.Document;

import tid.graphic.GraphicBGPEventManager;
import tid.graphic.GraphicEventManager;


//import drcl.inet.*;

public class Main {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
//		while (true)
//		{
//			InputStreamReader isr = new InputStreamReader(System.in);
//			BufferedReader br = new BufferedReader (isr);
//			String cadena = new String ();
//			cadena = br.readLine();
//			
//			InetAddress s = InetAddress.getByName(cadena);
//			
//			byte addr[] = s.getAddress();
//			long l_ip = 0;
//			for (int i = 0;i<4;i++)
//			{
//				long temp = 1;
//				long temp2 = addr[i];
//				
//				
//				if (temp2<0)
//					temp2 += 256;
//				for (int j = i; j<3; j++)
//					temp *= 256;
//				
//				l_ip += temp2 * temp;
//				
//	
//			
//			}
//			System.out.println(l_ip);
//		}
		GraphicBGPEventManager gem= new GraphicBGPEventManager("bgp-5000");
		gem.addClause(new Double(0), "Router1_1", "10.0.1.3", 4, "prueba desde el contructor", "estoy bien gracias");
		gem.addClause(new Double(1), "180.1.0.1", "180.1.0.2", 2, "Que te pires", "estamos tan agustito");
		gem.addClause(new Double(2), "10.0.4.1", "10.0.4.2", 1, "kakakakak", "estoy aqui porque he llegado");
		gem.addClause(new Double(3), "10.0.1.5", "10.0.2.2", 3, "Prubando externo", "esto es cojonudo");
		gem.addClause(new Double(5), "10.0.2.1", "10.0.2.4", 1, "obi, oba", "cada dia te quiero más");
		gem.addClause(new Double(6), "Router1_1", "10.0.1.2", 2, "pasamelo", "dame una cala");
		gem.addClause(new Double(4), "Router1_1", "10.0.1.5", 5, "que me quiero colocar", "cada dia te quiero mas");
		gem.writeDocuemnt();
		
	}
}