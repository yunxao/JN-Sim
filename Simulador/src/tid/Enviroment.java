package tid;

import tid.utils.Config;
import drcl.comp.Component;

public class Enviroment {
	
	/**
	 * Duration of the simulation
	 */
	public static long time;
	/**
	 * Maximum of nodes per link.
	 * @see tid.inet.InetUtil#createTopology(Component, drcl.inet.Link, com.renesys.raceway.DML.Configuration)
	 */
	public static int nodesPerLink = 2;
	/**
	 * @see tid.Enviroment#time
	 * @return
	 */
	public static long time() {return time;}
	private static Component network;

	/**
	 * Directory where the logs and traces are saved 
	 */
	public static String tracedir = "";

	public static boolean debugFlag = true;

	public static boolean errorFlag = true;

	public static boolean MEMORY_OPTIMIZATION = false;
	public static void setNetwork(Component network) { Enviroment.network = network; }
	public static Component getNetwork () {return Enviroment.network;}
	public static void resetNetwork(Component network){
		network.reset();
		network.removeAll();
		network.reboot();
		network.disconnectAll();
	}
	
	public static void prepareNetwork(Component network){
		// DEBUG flag
		network.setDebugEnabled(debugFlag,true);
		network.setErrorNoticeEnabled(errorFlag, true);
	}
	 
}
