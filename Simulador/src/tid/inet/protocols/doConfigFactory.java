package tid.inet.protocols;

import infonet.javasim.bgp4.BGPSession;
import infonet.javasim.bgp4.Global;
import infonet.javasim.bgp4.util.Pair;
import infonet.javasim.util.IPaddress;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;

import org.jdom.Element;

import tid.inet.protocols.bgp_ct.BGPCTSession;
import tid.inet.protocols.gp_bgp.GP_BGPSession;
import tid.inet.protocols.trafficInspectionTool.EnviromentTIT;
import tid.inet.protocols.trafficInspectionTool.TITOperation;
import tid.inet.protocols.trafficInspectionTool.TrafficInspectionTool;
import tid.utils.Config;

import com.renesys.raceway.DML.Configuration;
import com.renesys.raceway.DML.configException;

import drcl.comp.Component;
import drcl.inet.Node;
/**
 * This Class has the methods that are used to create 
 * {@link com.renesys.raceway.DML.Configuration Configuration} variable of the protocols <br>
 * Create a new method:<br>
 * - There must exist a constant with the String id. I.e: 
 * {@link tid.inet.protocols.Protocol#BGP4 BGP4}, 
 * {@link tid.inet.protocols.Protocol#MP_BGP4 MP_BGP}  <br>
 * - Implemented a new function to do that you want. I.e: {@link doConfigFactory#doConfigBGP(Element, Configuration)}<br>
 * - Add in {@link doConfigFactory#factory(Element, Config) factory } the new case
 * @author Francisco Huertas
 *
 */
public class doConfigFactory {
	
	public static void factory(Element sessionElement, Node node) {
		String type = sessionElement.getAttributeValue("type");
		// FIXME esto esta bien?
		if (type == null){
			throw new Error ("ProtocolXMLFactory: protocol type is null");
		}
		else if (type.equals(Protocol.BGP4)) {
			Element parameters = sessionElement.getChild("parameters");
			if (parameters == null)
				throw new Error("mapStringFactory.mapBGP: parameters is not definen in the bgp protocol");
			String port = parameters.getAttributeValue("port");
			if (port == null)
				port = ""+BGPSession.PORT_NUM;
			Component bgpSession = node.getComponent("bgp"+"-"+port);
			if (!(bgpSession instanceof BGPSession)){
				throw new Error("doConfigFactory.factory: "+bgpSession.id+" is not correct BGPSession element");
			}
			else
				((BGPSession)bgpSession).config(sessionElement,node);
		}
		else if (type.equals(Protocol.TIT)){
			Element parameters = sessionElement.getChild("parameters");
			if (parameters  == null)
				throw new Error("doConfigFactory.factory: there is not parameters to TrafficInspectionTool");
			String id = parameters.getAttributeValue("id");
			if (id == null)
				id = Protocol.defaultTITId;
			Component tit = node.getComponent(id);
			if (!(tit instanceof TrafficInspectionTool)){
				throw new Error("doConfigFactory.factory: there is not correct TrafficInspectionTool element");
			}
			((TrafficInspectionTool)tit).config(sessionElement,node);
//			doConfigTrafficInspsctionTools(sessionElement, route);
		}
		else if (type.equals(Protocol.TCP_FULL)){
			// nothing to do
		}else if (type.equals(Protocol.GP_BGP)){
			Element parameters = sessionElement.getChild("parameters");
			if (parameters == null)
				throw new Error("doConfigFactory.factory: parameters is not definen in the gp bgp protocol");
			String port = parameters.getAttributeValue("port");
			if (port == null)
				port = ""+GP_BGPSession.PORT_NUM;
			Component bgpSession = node.getComponent(Protocol.GP_BGP+"-"+port);
			if (!(bgpSession instanceof GP_BGPSession)){
				throw new Error("doConfigFactory.factory: there is not correct BGPSession element");
			}
			else
				((GP_BGPSession)bgpSession).config(sessionElement,node);
		}
		else 
			throw new Error ("tid.inet.protocol.doConfiguFactory: Session not supported. type: " + type);
		
	}

	
//	/**
//	 * This function call the factory method for a concrete Protocol
//	 * @param xml Where is the configuration 
//	 * @param route Partial configuration of the router. This variable is modificated
//	 * @return Configuration of the Protocol
//	 * @throws UnknownHostException
//	 * @throws configException
//	 * @see com.renesys.raceway.DML.Configuration
//	 */
//	public static Configuration factory (Element xml, Config route) throws UnknownHostException, configException{
//		
//		String type = xml.getAttributeValue("type");
//		// FIXME esto esta bien?
//		if (type == null){
//			throw new Error ("ProtocolXMLFactory: protocol type is null");
//		}
//		if (type.equals(Protocol.BGP4)) {
//			return doConfigBGP(xml,route);
//		}
//		if (type.equals(Protocol.MP_BGP)){
//			return doConfigBGP_CT(xml, route);
//		}
//		if (type.equals(Protocol.TIT)){
//			return doConfigTrafficInspsctionTools(xml, route);
//		}
//		throw new Error ("ProtocolXMLFactory: Session not supported. type" + type);
//	}
	/**
	 * This method do the Configuration for BGP_CT
	 */
//	private static Config doConfigBGP_CT(Element xml, Configuration route) throws configException, UnknownHostException {
////		Config config = doConfigBGP(xml, route);
////		return config;
//		/*-----Protocol-----*/
//		String value;
//		String properties = "";
//		
//		Config protocol = new Config();
//		Element bgpParams = (Element) xml.getChild("parameters"); 
//		protocol.addElement("PROTOCOL",Protocol.MP_BGP);
//		protocol.addElement("CLASSNAME",BGPCTSession.class.getName());
//
//		/*-----ADDR-----*/
////		protocol.addElement("ADDR", route.findSingle("ADDR"));
//
//		
//		/*-----Interface-----*/
//		@SuppressWarnings("unchecked")
//		HashMap<String,Long> interfaces = (HashMap<String,Long>)route.findSingle("INTERFACES");
//
//
//		/*-----As-num-----*/
//		Integer as = Integer.valueOf(bgpParams.getAttributeValue("as"));
//		protocol.addElement("ASNUM", as);
//		properties = "As-num="+as;
//		/*-----PORT & ID-----*/
//		if ((value = bgpParams.getAttributeValue("port"))!= null){
//			protocol.addElement("PORT",(Integer.valueOf(value)));
//			protocol.addElement("ID", "bgp_ct-"+value);
//		}
//		else 
//			protocol.addElement("ID", "bgp_ct-"+BGPSession.PORT_NUM);
//
//		if (Global.mp_bfd) 
//			protocol.addElement("MP_BFD_POLL_INTERVAL", Global.mp_bfd_poll_interval);
//							
//		
//		/*-----LOCAL PREFERENCE-----*/
////		value = bgpParams.getAttributeValue("local_pref");
//		// TODO Eliminate or not???
////		if (value != null){
////			protocol.addElement("LOCAL_PREF", Integer.valueOf(value));
////		}
////		else 
//		value = bgpParams.getAttributeValue("default_local_pref");
//		if (value != null){
//			protocol.addElement("DEFAULT_LOCAL_PREF", Integer.valueOf(value));
//			properties += "||default local preference="+Integer.valueOf(value);
//		}
//		else {
//			protocol.addElement("DEFAULT_LOCAL_PREF", Global.default_local_pref);
//			properties += "||default local preference="+Global.default_local_pref;
//		}
//		
//		
//		/*-----NET-----*/
//		@SuppressWarnings("unchecked")
//		List < org.jdom.Element > networks =
//		    (List < org.jdom.Element >) xml.getChildren ("network");
//		// TODO cambiar para que lea desde aqui las local_preferences
//		for (org.jdom.Element network:networks){
//			String[] strNetwork = network.getAttributeValue("address").split("/");
//			protocol.addElement("NETWORKS",  new Pair<Long,Integer>(tid.utils.Utils.inetAddressToLong(InetAddress.getByName(strNetwork[0])),Integer.valueOf(strNetwork[1])));
//		}
//
//		/*-----KAI-----*/
//		String KAI = bgpParams.getAttributeValue("KAI");
//		if (KAI == null)
//			protocol.addElement("KAI",Long.valueOf(30000));
//		else
//			protocol.addElement("KAI", Long.valueOf(KAI));
//
//		Long addr_long = interfaces.get(bgpParams.getAttributeValue("interface"));
//		Integer port = (Integer)protocol.findSingle("PORT");		
//		if (port == null)
//			port = BGPSession.PORT_NUM;
//		String addr_str = tid.utils.Utils.addrLongToString(addr_long);
//		protocol.addElement("INTERFACE", interfaces.get(bgpParams.getAttributeValue("interface")));
//		/*-----LOGS-----*/
//		
//		if ((value = bgpParams.getAttributeValue("log_file_name")) != null)
//			protocol.addElement("LOG_FILE_NAME",value);
//		else{
//			String name = (String)route.findSingle("ID");
//			name += "-"+Protocol.MP_BGP+"-"+port;
//			protocol.addElement("LOG_FILE_NAME",name);
//		}
//		
//		// allow both BE and AE neighbours ;-)
//		@SuppressWarnings("unchecked")
//		List < org.jdom.Element > neighbours =
//		    (List < org.jdom.Element >) xml.getChildren ("neighbour");
//		/*
//		 * Now get all the neighbours
//		 */
//		boolean rr = false;
//		for (org.jdom.Element neighbour:neighbours) {
//			Config neighbourConfig = new Config();
//			
//			String neighbourIP = neighbour.getAttributeValue ("IP");
//			Long long_addr = tid.utils.Utils.inetAddressToLong(InetAddress.getByName(neighbourIP));
//			neighbourConfig.addElement("ADDR", long_addr);
//		    value = (String) neighbour.getAttributeValue("remote-as");
//		    neighbourConfig.addElement("ASNUM", Integer.valueOf(value));
//		    // FIXME yo soy cliente de el o el de mi?
//		    if (neighbour.getAttribute("rrc") != null && neighbour.getAttribute("rrc").getValue().equals("yes")){
//		    	// TODO arreglar que haga falta poner RRC tambien en el protocolo
//		    	neighbourConfig.addElement("RRC", Boolean.TRUE);
//		    	// only one time
//		    	if (!rr){
//		    		protocol.addElement("RR", Boolean.TRUE);
//		    		properties += "||Router Reflector";
//		    		rr = true;
//		    	}
//		    }
//
//		    
//		    String local_pref_str;
//		    if ( (local_pref_str = neighbour.getAttributeValue("local_pref")) != null){
//		    	neighbourConfig.addElement("LOCAL_PREF", Integer.valueOf(local_pref_str));
//		    	properties += "||Local pref to "+neighbourIP+"="+local_pref_str;
//		    }
//		    
//		    protocol.addElement("NEIGHBOURS", neighbourConfig);
//		}		
//		String []arrayProperities = properties.split("||");
//		protocol.addElement("PROPERITIES", arrayProperities);
//		return protocol;
//	}
//	/**
//	 * This method do the Configuration for BGP_4
//	 */
//	private static Config doConfigBGP(Element xml, Configuration route) throws configException, UnknownHostException {
//		/*-----Protocol-----*/
//		String value;
//		Config protocol = new Config();
//		Element bgpParams = (Element) xml.getChild("parameters"); 
//		protocol.addElement("PROTOCOL",Protocol.BGP4);
//		protocol.addElement("CLASSNAME",BGPSession.class.getName());
//
//		/*-----ADDR-----*/
////		protocol.addElement("ADDR", route.findSingle("ADDR"));
//
//		
//		/*-----Interface-----*/
//		@SuppressWarnings("unchecked")
//		HashMap<String,Long> interfaces = (HashMap<String,Long>)route.findSingle("INTERFACES");
//
//
//		/*-----As-num-----*/
//		Integer as = Integer.valueOf(bgpParams.getAttributeValue("as"));
//		protocol.addElement("ASNUM", as);
//		/*-----PORT & ID-----*/
//		if ((value = bgpParams.getAttributeValue("port"))!= null){
//			protocol.addElement("PORT",(Integer.valueOf(value)));
//			protocol.addElement("ID", "bgp-"+value);
//		}
//		else 
//			protocol.addElement("ID", "bgp-"+BGPSession.PORT_NUM);
//		
//		/*-----LOCAL PREFERENCE-----*/
//		value = bgpParams.getAttributeValue("default_local_pref");
//		// TODO Eliminate or not???
//		if (value != null){
//			protocol.addElement("DEFAULT_LOCAL_PREF", Integer.valueOf(value));
//		}
//		else 
//			protocol.addElement("DEFAULT_LOCAL_PREF", Global.default_local_pref);
//		
//		/*-----ALWAYS COMPARE MED-----*/
//		value = bgpParams.getAttributeValue("med_type"); 
//		if (value != null)
//			protocol.addElement("MED", value.equals(Global.STRING_ALWAYS_COPMARE_MED));
//		else
//			protocol.addElement("MED", Global.always_compare_med);
//		/*-----ROUTE LEVELS COMPARE-----*/
////		System.out.println(bgpParams.getAttributeValue("routes_compare_level"));
//		if ((value = bgpParams.getAttributeValue("routes_compare_level"))!= null){
//			if (Global.ROUTES_COMPARE_LEVELS.containsKey(value)){
//				protocol.addElement("ROUTE_COMPARE_LEVEL", Global.ROUTES_COMPARE_LEVELS.get(value));
//			}
//			else
//				protocol.addElement("ROUTE_COMPARE_LEVEL", Global.routes_compare_level);
//		}	
//		else
//			protocol.addElement("ROUTE_COMPARE_LEVEL", Global.routes_compare_level);
//		
//		/*-----RANDOM TIE BRAEKING -----*/
//		value = bgpParams.getAttributeValue("tie_breaking");
//		if (value != null)
//			protocol.addElement("TIE_BREAKING", value.equals(Global.STRING_RANDOM_TIE_BREAKING));
//		else 
//			protocol.addElement("TIE_BREAKING", Global.random_tie_breaking);
//		/*-----NET-----*/
//		@SuppressWarnings("unchecked")
//		List < org.jdom.Element > networks =
//		    (List < org.jdom.Element >) xml.getChildren ("network");
//		// TODO cambiar para que lea desde aqui las local_preferences
//		for (org.jdom.Element network:networks){
//			String[] strNetwork = network.getAttributeValue("address").split("/");
//			protocol.addElement("NETWORKS",  new Pair<Long,Integer>(tid.utils.Utils.inetAddressToLong(InetAddress.getByName(strNetwork[0])),Integer.valueOf(strNetwork[1])));
//		}
//		/*-----KAI-----*/
//		String KAI = bgpParams.getAttributeValue("KAI");
//		if (KAI == null)
//			protocol.addElement("KAI",Long.valueOf(30000));
//		else
//			protocol.addElement("KAI", Long.valueOf(KAI));
//
//		
//		Long addr_long = interfaces.get(bgpParams.getAttributeValue("interface"));
//		Integer port = (Integer)protocol.findSingle("PORT");
//		if (port == null)
//			port = BGPSession.PORT_NUM;
//		String addr_str = tid.utils.Utils.addrLongToString(addr_long);
//		protocol.addElement("INTERFACE", interfaces.get(bgpParams.getAttributeValue("interface")));
//		protocol.addElement("INTERFACENAME", bgpParams.getAttributeValue("interface"));
//		/*-----LOGS-----*/
//		
//		if ((value = bgpParams.getAttributeValue("log_file_name")) != null)
//			protocol.addElement("LOG_FILE_NAME",value);
//		else{
//			String name = (String)route.findSingle("ID");
//			name += "-"+Protocol.BGP4+"-"+port;
//			protocol.addElement("LOG_FILE_NAME",name);
//		}
//
//
//		
//		
//		// allow both BE and AE neighbours ;-)
//		@SuppressWarnings("unchecked")
//		List < org.jdom.Element > neighbours =
//		    (List < org.jdom.Element >) xml.getChildren ("neighbour");
//		/*
//		 * Now get all the neighbours
//		 */
//		for (org.jdom.Element neighbour:neighbours) {
//			Config neighbourConfig = new Config();
//			
//			String neighbourIP = neighbour.getAttributeValue ("IP");
//			IPaddress address = new IPaddress(neighbourIP);
//			Long long_addr = tid.utils.Utils.inetAddressToLong(InetAddress.getByName(neighbourIP));
//			neighbourConfig.addElement("ADDR", long_addr);
//			neighbourConfig.addElement("ADDRIPADDRESS", address);
//		    value = (String) neighbour.getAttributeValue("remote-as");
//		    neighbourConfig.addElement("ASNUM", Integer.valueOf(value));
//		    if (neighbour.getAttribute("rrc") != null && neighbour.getAttribute("rrc").getValue().equals("yes")){
//		    	
//		    	neighbourConfig.addElement("RRC", Boolean.TRUE);
//		    	protocol.addElement("RR", Boolean.TRUE);
//		    }
//		    List<Element> inPolicies = neighbour.getChildren("inPolicy");
//		    for (Element policy:inPolicies){
//		    	if (policy != null){
//		    		neighbourConfig.addElement("IN_POLICY", policy.getAttributeValue("id"));
//		    	}
//		    }
//		    List<Element> outPolicies = neighbour.getChildren("outPolicy");
//		    for (Element policy:outPolicies){
//		    	if (policy != null){
//			    	neighbourConfig.addElement("OUT_POLICY", policy.getAttributeValue("id"));
//			    }	
//		    }
//		    
//		    value = neighbour.getAttributeValue("local_pref");
//		    if (value != null){
//		    	neighbourConfig.addElement("LOCAL_PREF", value);
//		    }
//		    	
//
//		    
//		    String local_pref_str;
//		    if ( (local_pref_str = neighbour.getAttributeValue("LOCAL_PREF")) != null){
//		    	neighbourConfig.addElement("ROUTE_LOCAL_PREF", Integer.valueOf(local_pref_str));
//		    }
//		    
//		    protocol.addElement("NEIGHBOURS", neighbourConfig);
//		}		
//		return protocol;
//	}
//	private static Config doConfigTrafficInspsctionTools(Element xml, Configuration route) throws configException{
//		Config protocol = new Config();
//		String nodeName = (String)route.findSingle("ID");
//		protocol.addElement("PROTOCOL", Protocol.TIT);
//		protocol.addElement("NODE", nodeName);
//		boolean consoleEnableByDefault = false;
//		Element parameters = xml.getChild("parameters");
//		String sPeriodicity;
//		Double defaultPerodicity = null;
//		String fileLogNameByDefault = null;
//		if (parameters != null){
//			sPeriodicity = parameters.getAttributeValue("periodicity");
//			HashMap<String,Long> interfaces = (HashMap<String,Long>)route.findSingle("INTERFACES");
//			String interfaceUsed = parameters.getAttributeValue("interface");
//			if (interfaceUsed == null){
//				throw new Error("Traffic Inspection tools needs a interface associated");
//			}
//			Long addr_long = interfaces.get(interfaceUsed);
//			if (addr_long == null){
//				throw new Error("Traffic Inspection tools needs a interface associated. "+interfaceUsed+" doesnt exist");
//			}
//			protocol.addElement("ADDRESS", addr_long);
//			String id = parameters.getAttributeValue("id");
//			if (id != null)
//				protocol.addElement("ID", id);
//			else {
//				System.out.println("Warning, there is not id for Traffic Inspection Tool. Value by default: tit");
//				protocol.addElement("ID", "tit");
//			}
//			String sConsoleEnableByDefault = parameters.getAttributeValue("consoleEnable");
//			
//			if (sConsoleEnableByDefault != null){
//				consoleEnableByDefault = sConsoleEnableByDefault.compareToIgnoreCase("enable") == 0;
//			}
//			else {
//				consoleEnableByDefault = EnviromentTIT.consoleEnable;
//			}
//			fileLogNameByDefault = parameters.getAttributeValue("fileDebug");
//			//
//		}
//		else{
//			throw new Error("There is not paramaters for the Traffic Inspection Tool");
//		}
//		List<org.jdom.Element> ListOfTraceRT = xml.getChildren("traceRoute");
//		
//		if (sPeriodicity != null){
//			defaultPerodicity = Double.valueOf(sPeriodicity);
////			protocol.addElement("PERIODICITY", defaultPerodicity);
//		}
//		else {
//			defaultPerodicity = tid.inet.protocols.trafficInspectionTool.TrafficInspectionToolConstants.TRACEROUTE_PERIODICITY_BY_DEFAULT;
//			System.out.println("Warning: periodicity is not defined. Value by default="+defaultPerodicity);
//		}
//		
//		
//		for (int i = 0;i<ListOfTraceRT.size();i++){
//			Element traceRouteElement = ListOfTraceRT.get(i);
//			String sAddress = traceRouteElement.getAttributeValue("destiny");
//			try {
//				TITOperation op;
//				Long address = tid.utils.Utils.inetAddressToLong(InetAddress.getByName(sAddress));
//				String sConsoleEnable= traceRouteElement.getAttributeValue("consoleEnable");
//				boolean consoleEnable = (sConsoleEnable == null)?consoleEnableByDefault:sConsoleEnable.compareToIgnoreCase("enable") == 0;
//				sPeriodicity = traceRouteElement.getAttributeValue("periodicity");
//				double perioidicity = (sPeriodicity == null)?defaultPerodicity:new Double(sPeriodicity);
//				
//				String fileLogName = traceRouteElement.getAttributeValue("fileDebug");
//				if (fileLogName != null)
//					op = new TITOperation(TITOperation.TRACEROUTE, address, perioidicity, consoleEnable,fileLogName);
//				else if (fileLogNameByDefault != null)
//					op = new TITOperation(TITOperation.TRACEROUTE, address, perioidicity, consoleEnable,fileLogNameByDefault);
//				else
//					op = new TITOperation(TITOperation.TRACEROUTE, address, perioidicity, consoleEnable);
//				protocol.addElement("OPERATIONS", op);
//			} catch (UnknownHostException e) {
//				System.err.println("Incorrect address in xml file for TrafficInspectionTool. Node: "+nodeName+" address: "+sAddress);
//				
//			}
//		}
//		return protocol;
//	}

}
