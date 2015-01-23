package tid.events;

import java.net.UnknownHostException;

import infonet.javasim.bgp4.BGPSession;
import infonet.javasim.bgp4.BGPSessionConstants;
import infonet.javasim.bgp4.comm.UpdateMessage;

import org.jdom.Element;

import drcl.comp.Component;
import drcl.inet.Network;
import drcl.inet.Node;

import tid.Enviroment;

public class INCORRECT_BGPUPDATE_MSG extends Event{
	public final static int CODE = 1;
	private BGPSession bgp;
	private long destiny;
	@Override
	public void execute() {
		// TODO Auto-generated method stub
		bgp.sendIncorrectMessage(destiny, CODE);
	}

	@Override
	public void fromXML(Element xml) {
		// TODO Auto-generated method stub
//		bgp = tid.inet.InetUtil.
		String value;
		value = xml.getAttributeValue("type");
		if (value == null || !value.equals("INCORRECT_BGPUPDATE_MSG")){
			throw new Error("INCORRECT_BGPUPDATE_MSG (fromXML): Incorrect type event");
		}
		value = xml.getAttributeValue("id");
		if (value == null)
			throw new Error("INCORRECT_BGPUPDATE_MSG (fromXML): Event must have a id");
		id = value;
		this.message = xml.getAttributeValue("message");
		Element parameters = xml.getChild("parameters");
		if (parameters == null)
			throw new Error("INCORRECT_BGPUPDATE_MSG (fromXML): The Event has not \"parameters\" tag");
		value = parameters.getAttributeValue("node");
		if (value == null){
			throw new Error("INCORRECT_BGPUPDATE_MSG (fromXML): \"node\" atributte is not definded in parameters");
		}
		Component c = Enviroment.getNetwork().getComponent(value);
		if (c == null || !(c instanceof Node)){
			throw new Error("INCORRECT_BGPUPDATE_MSG (fromXML): \"node\" has value "+value+". This node don't exist in the network");
			
		}
		Node node = (Node)c;
		value = parameters.getAttributeValue("port");
		if (value == null)
			value = ""+BGPSessionConstants.PORT_NUM;
		String bgpName = "bgp-"+value;
		c = node.getComponent(bgpName);
		if (c == null || !(c instanceof BGPSession)){
			throw new Error("INCORRECT_BGPUPDATE_MSG (fromXML): The node "+node.id+" has not a BGPSession in the port "+value);
		}
		bgp = (BGPSession)c;
		value = parameters.getAttributeValue("destiny");
		if (value == null){
			throw new Error("INCORRECT_BGPUPDATE_MSG (fromXML): \"destiny\" atributte is not definded in parameters");
		}
		try {
			destiny = tid.utils.Utils.stringAddressToLong(value);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new Error("INCORRECT_BGPUPDATE_MSG (fromXML): "+value+" is not a correct value for \"destiny\" atributte");
		}
	}

}
