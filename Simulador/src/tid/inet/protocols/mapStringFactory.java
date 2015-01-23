package tid.inet.protocols;

import org.jdom.Element;

import tid.inet.protocols.gp_bgp.GP_BGPSession;

import infonet.javasim.bgp4.BGPSession;

import com.renesys.raceway.DML.Configuration;
import com.renesys.raceway.DML.configException;

import drcl.comp.Component;
/**
 * This class implements a factory that is used to create string to use in {@link drcl.inet.NodeBuilder#build(Object, String)} from a {@link Configuration}.<br>
 * To configure for a new protocol:
 * - There must exist a constant with the String id. I.e:<br> 
 * {@link tid.inet.protocols.Protocol#BGP4 BGP4}, 
 * {@link tid.inet.protocols.Protocol#MP_BGP4 MP_BGP}  <br>
 * - Add in {@link mapStringFactory#factory(Configuration) factory} the new case. <br>
 * - Implemented a new function to do that you want. I.e: {@link mapStringFactory#mapBGP(Configuration)} <br>
 * <br>
 * The general format of this is: '"name_of_the_instance_protocol" "numport"/tcp "route_of_the_class"'<br> 
 * Example: "protocol 1234/tcp infonet.javasim.BGPSession"
 * @author Francisco Huertas
 *
 */
public class mapStringFactory {
	public static String factory(Element sessionElement){
		String value = sessionElement.getAttributeValue("type");
		if (value == null)
			throw new Error ("ProtocolMapStringFactory: Session has not a type");
		else if (value.equals(Protocol.BGP4)){
			return mapBGP(sessionElement);
		}
		else if (value.equals(Protocol.TCP_FULL)){
			return mapTCP_FULL(sessionElement);
		}
		else if (value.equals(Protocol.TIT)){
			return mapTIT(sessionElement);
		}else if (value.equals(Protocol.GP_BGP)){
			return mapGP_BGP(sessionElement);
		}
		throw new Error ("ProtocolMapStringFactory: Protocol type not supported");

	}
	private static String mapTIT(Element sessionElement) {
		Element parameters = sessionElement.getChild("parameters");
		if (parameters  == null)
			throw new Error("mapStringFactory.mapTIT: there is not parameters to TrafficInspectionTool");
		String id = parameters.getAttributeValue("id");
		if (id == null)
			id = Protocol.defaultTITId;
		return id+" tid.inet.protocols.trafficInspectionTool.TrafficInspectionTool";
	}
	private static String mapTCP_FULL(Element sessionElement) {
		Element parameters = sessionElement.getChild("paramenters");
		String id;
		if (parameters == null){
			id = Protocol.defaultTCP_FULLId;
		}else {
			id = parameters.getAttributeValue("id");
			if (id == null)
				id = Protocol.defaultTCP_FULLId;
		}
		return id +" drcl.inet.socket.TCP_full";
	}
	private static String mapGP_BGP(Element sessionElement) {
		Element parameters = sessionElement.getChild("parameters");
		if (parameters == null)
			throw new Error("mapStringFactory.mapGP_BGP: parameters is not definen in the bgp protocol");
		String port = parameters.getAttributeValue("port");
		String tcp_layer = parameters.getAttributeValue("tcpIdLayer");
		if (port == null)
			port = ""+GP_BGPSession.PORT_NUM;
		if (tcp_layer == null)
			tcp_layer = Protocol.defaultTCP_FULLId;

		return Protocol.GP_BGP+"-"+port+" "+port+"/"+tcp_layer+" tid.inet.protocols.gp_bgp.GP_BGPSession";
	}
	private static String mapBGP(Element sessionElement) {
		Element parameters = sessionElement.getChild("parameters");
		if (parameters == null)
			throw new Error("mapStringFactory.mapBGP: parameters is not definen in the bgp protocol");
		String port = parameters.getAttributeValue("port");
		String tcp_layer = parameters.getAttributeValue("tcpIdLayer");
		if (port == null)
			port = ""+BGPSession.PORT_NUM;
		if (tcp_layer == null)
			tcp_layer = Protocol.defaultTCP_FULLId;

		return "bgp"+"-"+port+" "+port+"/"+tcp_layer+" infonet.javasim.bgp4.BGPSession";
	}
}
