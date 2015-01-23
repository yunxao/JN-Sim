package tid.inet.protocols;

import com.renesys.raceway.DML.Configuration;
import com.renesys.raceway.DML.configException;

import drcl.inet.Node;

import org.jdom.Element;
/**
 * This interface represent the minimum that must have a class to be considered a protocol that it can be automated<br>
 * This class has the id string of the class
 * @author Francisco Huertas
 */
public interface Protocol {
	/**
	 * Constant string to bgp protocol
	 */
	public static final String BGP4 = "bgp 4";
	/**
	 * Constant string to mp bgp protocol
	 */
	public static final String MP_BGP = "mp bgp";
	public static final String TIT = "TrafficInspectionTool";
	public static final String TCP_FULL = "tcp_full";
	public static final String defaultTCP_FULLId = "tcp";
	public static final String defaultTITId = "TIT";
//	public static final String BGP4ID = "bgp";
	public static final String GP_BGP = "gp_bgp";
	/**
	 * Id of the type of protocol
	 * @return 
	 */
	public String type();
	/**
	 * Return an object that identifies the protocol
	 * @return the object
	 */
	public Object id();
	/**
	 * Start the protocol
	 */
	public void init();
	/**
	 * Restart the protocol
	 * @param protocol protocol with must be restarted. this method can be changed
	 */
	public void restart();
	/**
	 * Configure a protocol from xml
	 * @param node node where protocol will be
	 * @param config xml variable with the information
	 */
	public void config(Element config, Node node);
	/**
	 * Stop a protocol
	 */
	public void kill();
	/**
	 * Final tasks of the protocol
	 */
	public void endProtocol();
	
	public void configGeneralParameters(Element xml);
	
	public void createLogs(Element sessionElement);
}
