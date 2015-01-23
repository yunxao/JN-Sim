package tid.inet;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import tid.Enviroment;
import tid.inet.protocols.GeneralParametersFactory;
import tid.inet.protocols.doConfigFactory;
import tid.inet.protocols.mapStringFactory;

import com.renesys.raceway.DML.Configuration;
import com.renesys.raceway.DML.configException;

import drcl.comp.Component;
import drcl.comp.Port;
import drcl.inet.CoreServiceLayer;
import drcl.inet.Link;
import drcl.inet.Node;
import drcl.inet.NodeBuilder;
import drcl.inet.data.RTEntry;
import drcl.inet.data.RTKey;

/**
 * This class has a methods to automate the creation and configuration a Network
 * to simulate.
 * 
 * @author Francisco Huertas
 * @see drcl.inet.InetUtil
 * 
 */
public class InetUtil {

	public static void doConfigFromXML(String fname) throws JDOMException, IOException {
		/*
		 * Parse the XML configuration file
		 */

		/*
		 * General information
		 */
		File xmlFile = new File(fname);
		SAXBuilder builder = new SAXBuilder();
		org.jdom.Document doc = builder.build(xmlFile);
		org.jdom.Element root = doc.getRootElement();
		if (!root.getName().equals("JSimConfig"))
			throw new Error("XML file should contain a JSimConfig");
		String value;
		value = root.getAttributeValue("memoryOptimization");
		if (value == null) {
			System.out.println("Warning: Memory optimization(memoryOptimization) has not value. Take default value: disable");
			tid.Enviroment.MEMORY_OPTIMIZATION = false;
		} else {
			tid.Enviroment.MEMORY_OPTIMIZATION = value.equals("enable");
		}
		org.jdom.Element debug = root.getChild("debug");
		if (debug != null) {
			if ((value = debug.getAttributeValue("debugFlag")) != null)
				Enviroment.debugFlag = value.equals("enable");
			if ((value = debug.getAttributeValue("errorFlag")) != null)
				Enviroment.errorFlag = value.equals("enable");
			if ((value = debug.getAttributeValue("trace_dir")) != null) {
				if (value.charAt(value.length() - 1) != '/') {
					Enviroment.tracedir = value.concat("/");
				} else
					Enviroment.tracedir = new String(value);
			}
		}
		org.jdom.Element topology = root.getChild("topology");
		if (topology != null) {
			if ((value = topology.getAttributeValue("nodesPerLink")) != null)
				tid.Enviroment.nodesPerLink = Integer.valueOf(value);
		}
		//	
		@SuppressWarnings("unchecked")
		List<org.jdom.Element> protocols = root.getChildren("protocol");

		for (int i = 0; i < protocols.size(); i++) {
			GeneralParametersFactory.factory(protocols.get(i));
		}
		/*
		 * Node configuration
		 */
		org.jdom.Element components = root.getChild("components");
		@SuppressWarnings("unchecked")
		List<org.jdom.Element> routers = (List<org.jdom.Element>) components.getChildren("router");
		Integer nFuentes = routers.size();
		System.out.println("Number of nodes: " + nFuentes);
		for (int i = 0; i < nFuentes; i++) {

			String RouterID = routers.get(i).getAttributeValue("id");
			if (RouterID == null)
				throw new Error("XMLUTILS.doConfig: Router has not ID");
			Node node = new Node(RouterID);

			// Adding node to the network
			Enviroment.getNetwork().addComponent(node);

			// Adding links if this isn't
			@SuppressWarnings("unchecked")
			List<org.jdom.Element> connections = routers.get(i).getChild("topology").getChildren("link");
			ListIterator<Element> listConnections = connections.listIterator();
			if (listConnections != null) {
				int nodePortNum = 0;
				/*
				 * Conecting node to the link
				 */
				while (listConnections.hasNext()) {
					Element xmlLink = listConnections.next();
					String idLink = xmlLink.getAttributeValue("id");
					if (idLink == null)
						throw new Error("The link must have id");
					// check if the link is in the enviroment
					Link componentLink;
					if ((componentLink = (Link) Enviroment.getNetwork().getComponent(idLink)) == null) {
						componentLink = new Link(idLink);
						Enviroment.getNetwork().addComponent(componentLink);
					}
					// TODO añadir los puertos
					// TODO check default group port

					// connecting link and node

					Port portLink = componentLink.findAvailable();
					// Port portNode =
					// node.addPort(""+node.getNumOfInterfaces()); it's the same
					// but without csl
					Port portNode = node.addPort("" + findFreeInterface(node));
					portLink.connectTo(portNode);
					portNode.connectTo(portLink);
					// Replace nodePortNum++ by findAviable
				}
			}

			/*
			 * Adding interfaces to the node
			 */
			@SuppressWarnings("unchecked")
			List<Element> ifaces = routers.get(i).getChildren("interface");
			if (ifaces != null) {
				ListIterator<Element> ifacesList = ifaces.listIterator();
				while (ifacesList.hasNext()) {

					Element ifaceElement = ifacesList.next();
					addInterfaceToNode(ifaceElement, node);

				}
			} else if (Enviroment.debugFlag)
				System.out.println("XMLConfig.doConfig: Warning, " + RouterID + " has not interfaces");
			/*
			 * Adding virtual interfaces to the node
			 */
			// Iterator<Element> virtualInterfaces =
			// routers.get(i).getChildren("virtualInterface").iterator();
			// while(virtualInterfaces.hasNext()){
			// Element vi = virtualInterfaces.next();
			// addVirtualIfaceToNode(vi, node);
			// }
		}
		/*
		 * Building nodes
		 */
		Iterator<Element> routersList = routers.iterator();
		while (routersList.hasNext()) {
			Element routerElement = routersList.next();
			Component tmp = Enviroment.getNetwork().getComponent(routerElement.getAttributeValue("id"));
			if (!(tmp instanceof Node))
				continue;
			Node node = (Node) tmp;
			@SuppressWarnings("unchecked")
			List<Element> sessions = routerElement.getChildren("session");

			if (sessions != null) {
				/*
				 * Building sessions
				 */
				ListIterator<Element> sessionList = sessions.listIterator();
				String map = "";
				while (sessionList.hasNext()) {
					Element sessionElement = sessionList.next();
					map += mapStringFactory.factory(sessionElement) + "\n";

				}
				NodeBuilder nodeBuilder = new NodeBuilder();
				nodeBuilder.build(node, map);
			}
		}
		/*
		 * Adding address (must be after build nodes)
		 */
		routersList = routers.iterator();
		while (routersList.hasNext()) {
			Element routerElement = routersList.next();

			Component tmp = Enviroment.getNetwork().getComponent(routerElement.getAttributeValue("id"));
			if (tmp instanceof Node) {
				Node node = (Node) tmp;
				Component[] componentsOfNode = node.getAllComponents();
				for (Component aComponent : componentsOfNode) {
					if (aComponent instanceof PhysicalNetworkInterface) {
						PhysicalNetworkInterface iFace = (PhysicalNetworkInterface) aComponent;
						ArrayList<Link> listOfLinks = iFace.getLinks();
						// if there are links
						if (listOfLinks != null && listOfLinks.size() > 0) {
							Iterator<Link> it = listOfLinks.iterator();
							while (it.hasNext()) {
								Link l = it.next();
								addAddress(node, iFace.getAddress(), l);
							}
						}
						// if there isn't link, connect with all nodes
						else
							addAddress(node, iFace.getAddress());
					} else if (aComponent instanceof VirtualStaticP2PNetworkInterface) {
						addStaticPointToPointVirtualAddress(node, (VirtualStaticP2PNetworkInterface) aComponent);
					} else if (aComponent instanceof VirtualMultipointInterface) {
						addMultiPointVirutalAddress(node, (VirtualMultipointInterface) aComponent);
					}
				}

			}

		}
		/*
		 * Configuring protocols of the node
		 */
		routersList = routers.iterator();
		while (routersList.hasNext()) {
			Element routerElement = routersList.next();
			Component tmp = Enviroment.getNetwork().getComponent(routerElement.getAttributeValue("id"));
			if (!(tmp instanceof Node))
				continue;
			Node node = (Node) tmp;
			@SuppressWarnings("unchecked")
			List<Element> sessions = routerElement.getChildren("session");

			if (sessions != null) {
				ListIterator<Element> sessionList = sessions.listIterator();
				while (sessionList.hasNext()) {
					Element sessionElement = sessionList.next();
					doConfigFactory.factory(sessionElement, node);
				}
			}
		}
	}

	private static void addInterfaceToNode(Element ifaceElement, Node node) {
		String type = ifaceElement.getAttributeValue("type");
		if (type == null) {
			throw new Error("InetUtil.doConfigFromXML: There is not type for the interface");
		}
		if (type.equals(NetworkInterface.PHYSICAL_TYPE)) {
			String ifaceName = ifaceElement.getAttributeValue("name");
			if (ifaceName == null)
				throw new Error("InetUtil.doConfigFromXML: interface must be a name");
			PhysicalNetworkInterface iface = new PhysicalNetworkInterface(ifaceName);
			node.addComponent(iface);
			String sAddress = ifaceElement.getAttributeValue("address");
			if (sAddress == null)
				throw new Error("InetUtil.doConfigFromXML: interface must be a address");
			try {
				Long address = tid.utils.Utils.stringAddressToLong(sAddress);
				iface.setAddress(address);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				throw new Error("InetUtil (addInterfaceToNode): addres of the interface is not correct");
			}

			@SuppressWarnings("unchecked")
			Iterator<Element> listOfLinks = ifaceElement.getChildren("link").iterator();
			while (listOfLinks.hasNext()) {
				Element linkElement = listOfLinks.next();
				String linkId = linkElement.getAttributeValue("id");
				if (linkId != null) {
					Component linkComponent = Enviroment.getNetwork().getComponent(linkId);
					if (linkComponent == null || !(linkComponent instanceof Link))
						throw new Error("InetUtil.doConfigFromXML: " + linkId + " not exist or isn't a link");
					iface.addLink((Link) linkComponent);

				}
			}
		} else if (type.equals(NetworkInterface.VIRTUAL_STATIC_P2P_TYPE)) {
			String VIId = ifaceElement.getAttributeValue("name");
			String VIAddress = ifaceElement.getAttributeValue("address");
			String baseInterfaceId = ifaceElement.getAttributeValue("baseInterface");
			String sRemoteHost = ifaceElement.getAttributeValue("remoteHost");
			String sRemoteAddress = ifaceElement.getAttributeValue("remoteAddress");

			if (VIId == null)
				throw new Error("tid.inet.InetUtil: The interfaz has not a name");
			if (VIAddress == null)
				throw new Error("tid.inet.InetUtil: The interfaz has not a address");
			if (baseInterfaceId == null)
				throw new Error("tid.inet.InetUtil: The interfaz has not a base interface");
			if (sRemoteHost == null)
				throw new Error("tid.inet.InetUtil: The interfaz has not a remote host");
			Component baseInterfaceComponent = node.getComponent(baseInterfaceId);
			if (baseInterfaceComponent == null || !(baseInterfaceComponent instanceof NetworkInterface)) {
				throw new Error("tid.inet.InetUtil: " + baseInterfaceId + " is not a correct base interface. " + "Check the configuration file because baseInterface must be after");
			}
			if (sRemoteAddress == null) {
				if (Enviroment.debugFlag) {
					System.out.println("tid.inet.InetUtil: warning, the virtual interfaz has not network");
				}
			}
			try {
				VirtualStaticP2PNetworkInterface vni = new VirtualStaticP2PNetworkInterface(VIId, (NetworkInterface) baseInterfaceComponent, tid.utils.Utils.stringAddressToLong(VIAddress),
						tid.utils.Utils.stringAddressToLong(sRemoteHost), tid.utils.Utils.stringAddressToLong(sRemoteAddress));
				node.addComponent(vni);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				throw new Error("InetUtil (addInterfaceToNode): Address are not correct");
			}

		} else if (type.equals(NetworkInterface.VIRTUAL_MULTIPOINT_TYPE)) {
			String VIId = ifaceElement.getAttributeValue("name");
			String VIAddress = ifaceElement.getAttributeValue("address");
			String baseInterfaceId = ifaceElement.getAttributeValue("baseInterface");
			if (VIId == null)
				throw new Error("tid.inet.InetUtil: The interfaz has not a name");
			if (VIAddress == null)
				throw new Error("tid.inet.InetUtil: The interfaz has not a address");
			if (baseInterfaceId == null)
				throw new Error("tid.inet.InetUtil: The interfaz has not a base interface");
			Component baseInterfaceComponent = node.getComponent(baseInterfaceId);
			if (baseInterfaceComponent == null || !(baseInterfaceComponent instanceof NetworkInterface)) {
				throw new Error("tid.inet.InetUtil: " + baseInterfaceId + " don't exist or is not a correct base interface. "
						+ "Check the configuration file because baseInterface must be after in the file");
			}
			try {
				VirtualMultipointInterface vi = new VirtualMultipointInterface(VIId, tid.utils.Utils.stringAddressToLong(VIAddress), (NetworkInterface) baseInterfaceComponent);
				node.addComponent(vi);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				throw new Error("InetUtil (addInterfaceToNode): addres of the interface is not correct");
			}

		} else {
			throw new Error("InetUtil.doConfigFromXML: unsuported type (" + type + ")");
		}

	}

	/**
	 * Create a physical topology among nodes. This funciton is a alternative to
	 * {@link drcl.inet.InetUtil#createTopology(Component, String, String, Object[], int[][], long[], Link, boolean)
	 * createTopoplogy} and childs
	 * 
	 * @param network_
	 *            Network where the topology will be created
	 * @param link_
	 *            master link
	 * @param config
	 *            configuration
	 * @throws configException
	 * @see tid.utils.Confg
	 * @see drcl.inet.InetUtil#createTopology(Component, String, String,
	 *      Object[], int[][], long[], Link, boolean)
	 */
	public static void createTopology(Component network_, Link link_, Configuration config) throws configException {

		String[] nodesNames = (String[]) config.findSingle("IDS");

		Configuration links = (Configuration) config.findSingle("TOPOLOGY");
		if (network_ == null || links == null)
			return;
		for (int i = 0; i < nodesNames.length; i++) {
			String nodeName = nodesNames[i];
			Node node = new Node();
			node.setID(nodeName);

			network_.addComponent(node);

			// node.setDebugEnabled(true);
			// node.setErrorNoticeEnabled(true);
		}
		@SuppressWarnings("unchecked")
		Enumeration<String> listLinks = (Enumeration<String>) config.find("LINKS");
		Configuration topology = (Configuration) config.findSingle("TOPOLOGY");
		// Configuration topology = Enviroment.links;

		if (listLinks != null) {
			while (listLinks.hasMoreElements()) {
				String linkName = listLinks.nextElement();
				if (link_ == null)
					throw new Error("Link dont exist");
				if (network_.containsComponent(linkName))
					throw new Error("Link id " + linkName + " is duplicated");
				@SuppressWarnings("unchecked")
				Enumeration<String> nodesNameToConnect = topology.find(linkName);
				Vector<Node> nodesToConnect = new Vector<Node>();
				while (nodesNameToConnect.hasMoreElements()) {
					Component nodeTemp = (Component) network_.getComponent(nodesNameToConnect.nextElement());
					// FIXME quitarlo o arreglarlo
					if (!(nodeTemp instanceof Node))
						System.out.println("Error de componentes");
					// throw new Error ("Component is not Node");

					nodesToConnect.add((Node) nodeTemp);
				}
				if (nodesToConnect.size() > Enviroment.nodesPerLink)
					throw new Error(linkName + ": Number of nodes per link is exceded");

				Link link = (Link) link_.clone();
				link.setID(linkName);
				network_.addComponent(link);

				for (int i = 0; i < nodesToConnect.size(); i++) {
					Port portLink = link.addPort("" + i);
					Node node = nodesToConnect.get(i);
					int nodePortNum = 0;
					while (node.getPort("" + nodePortNum) != null)
						nodePortNum++;

					Port portNode = node.addPort("" + nodePortNum);
					portLink.connectTo(portNode);
					portNode.connectTo(portLink);
				}
			}
		}
	}

	/**
	 * Disconnect a link or a Node. If it's a Node disconnect all the node
	 * links, if it's a link disconnect only it
	 * 
	 * @param id
	 *            Link or node that will be disconnected
	 */
	public static void disconnect(String id) {
		Component net = Enviroment.getNetwork();
		Component object = net.getComponent(id);
		if (object == null)
			throw new Error("There isn't component" + id + ".");
		if (object instanceof Link) {
			((Link) object).setEnabled(false);
		} else if (/* object instanceof Link || */object instanceof Node)
			object.disconnectAllPeers();
		else
			System.out.println(id + " can't be disconnected");
	}

	/**
	 * Connect new nodes to a link.
	 * 
	 * @param sLink
	 * @param sNodes
	 */
	public static void connect(String sLink, Enumeration<String> sNodes) {
		Component net = (Component) Enviroment.getNetwork();
		Component link_ = net.getComponent(sLink);
		Link link;
		if (link_ == null) {
			link = new Link(sLink);
			net.addComponent(link);
		} else if (!(link_ instanceof Link))
			throw new Error("tid.InetUtil (connect): the link " + sLink + " exist and it's not a link");
		else {
			link = (Link) link_;
			// if (link.getAllNodesConnected().size() + sNodes.size() >=
			// Enviroment.nodesPerLink){
			// throw new
			// Error("tid.InetUtil (connect): you've crossed the maximum number of nodes per link. Maximum="+Enviroment.nodesPerLink);
			// }
		}
		Vector<Node> nodes = new Vector<Node>();
		while (sNodes.hasMoreElements()) {
			String sNode = sNodes.nextElement();
			Component node = net.getComponent(sNode);
			if ((node == null) || !(node instanceof Node))
				throw new Error("tid.InetUtil (connect): Node " + sNode + " don't exist or it's not a Node");
			nodes.add((Node) node);

		}
		int linkPortNum = 0;
		for (Node node : nodes) {
			link.stop();

			int nodePortNum = 0;
			while (link.getPort("" + linkPortNum) != null) {
				linkPortNum++;
			}
			while (node.getPort("" + nodePortNum) != null) {
				nodePortNum++;
			}
			Port linkPort = link.addPort("" + linkPortNum++);
			Port nodePort = node.addPort("" + nodePortNum++);
			linkPort.connectTo(nodePort);
			nodePort.connectTo(linkPort);

		}

	}

	/**
	 * Reconnect a link or node. If id is a node then reconnect with all
	 * neighbors
	 * 
	 * @param id
	 *            link or node to reconnect
	 */
	public static void reConnect(String id) {
		Component comp = Enviroment.getNetwork().getComponent(id);

		if (comp instanceof Link) {
			Link link = (Link) comp;
			link.setEnabled(true);
		} else if (comp instanceof Node) {
			throw new Error("Reconnect a node is not suported");
		}

	}

	private static void addAddress(Node node, long address, Link link) {
		Port[] linkPorts = link.getAllPorts();
		node.addAddress(address);
		RTKey key_ = new RTKey(0, 0, address, -1, 0, 0);
		for (Port port : linkPorts) {
			Port[] portsOfPeers = __getPeers(port);
			for (Port portOfPeer : portsOfPeers) {
				Component peerHost = portOfPeer.getHost();
				if (!(peerHost instanceof Node) || peerHost.equals(node))
					continue;
				Node neighbor_ = (Node) peerHost;
				if (!neighbor_.hasRoutingCapability())
					continue;

				int interfaceIndex_ = Integer.parseInt(portOfPeer.getID());
				drcl.data.BitSet bitset_ = new drcl.data.BitSet();
				bitset_.set(interfaceIndex_);
				neighbor_.addRTEntry(key_, new RTEntry(bitset_, "NEIGHBOR_ROUTE"), -1.0); // no
																							// timeout
			}
		}
	}

	private static void addStaticPointToPointVirtualAddress(Node node, VirtualStaticP2PNetworkInterface vi) {
		Component csl_ = node.getComponent(drcl.inet.InetConstants.ID_CSL);
		Integer ivn = findFreeInterface(node);
		node.addAddress(vi.getAddress());
		if (csl_ instanceof CoreServiceLayer) {
			CoreServiceLayer csl = (CoreServiceLayer) csl_;
			csl.setupVIF(ivn, vi.getBaseInterface().getAddress(), vi.getRemoteHost(), -1);
		}
		RTKey key = new RTKey(0, 0, vi.getRemoteAddress(), -1, 0, 0);
		drcl.data.BitSet bitset_ = new drcl.data.BitSet();
		bitset_.set(ivn);
		RTEntry entry = new RTEntry(bitset_, "VIRTUAL_NEIGHBOR_ROUTE");
		node.addRTEntry(key, entry, -1);
		// check this
	}

	private static void addMultiPointVirutalAddress(Node node, VirtualMultipointInterface vi) {
		node.addAddress(vi.getAddress());
	}

	public static boolean stabilizeVirtualConnection(Node node, VirtualMultipointInterface vi, Long remoteHost, Long remoteAddress) {
		Component csl_ = node.getComponent(drcl.inet.InetConstants.ID_CSL);
		Integer ivn = findFreeInterface(node);
		if (csl_ instanceof CoreServiceLayer) {
			CoreServiceLayer csl = (CoreServiceLayer) csl_;
			csl.setupVIF(ivn, vi.getBaseInterface().getAddress(), remoteHost, -1);
		}
		RTKey key = new RTKey(0, 0, remoteAddress, -1, 0, 0);
		drcl.data.BitSet bitset_ = new drcl.data.BitSet();
		bitset_.set(ivn);
		RTEntry entry = new RTEntry(bitset_, "VIRTUAL_NEIGHBOR_ROUTE");
		node.addRTEntry(key, entry, -1);
		return true;
	}

	/**
	 * Add address to a node. This address add to route table of all nodes
	 * connect to same link that the node
	 * 
	 * @param node
	 * @param address
	 */
	public static void addAddress(Node node, long address) {
		Port[] ports = node.getAllPorts();
		node.addAddress(address);
		RTKey key_ = new RTKey(0, 0, address, -1, 0, 0);
		for (Port port : ports) {

			Port[] peers = __getPeers(port);
			for (Port peer : peers) {
				Component peerHost = peer.getHost();
				if (!(peerHost instanceof Node) || peerHost.equals(node))
					continue;
				Node neighbor_ = (Node) peerHost;
				if (!neighbor_.hasRoutingCapability())
					continue;

				int interfaceIndex_ = Integer.parseInt(peer.getID());
				drcl.data.BitSet bitset_ = new drcl.data.BitSet();
				bitset_.set(interfaceIndex_);
				neighbor_.addRTEntry(key_, new RTEntry(bitset_, "NEIGHBOR_ROUTE"), -1.0); // no
																							// timeout
			}
		}

	}

	/**
	 * Get ports of the element connect to the port
	 * 
	 * @param out_
	 * @return
	 */
	private static Port[] __getPeers(Port out_) {
		Port[] pp_ = out_.getPeers();
		Vector<Port> vpeers_ = new Vector<Port>();
		for (int i = 0; i < pp_.length; i++) {
			Component tmp_ = pp_[i].getHost();
			if (tmp_ instanceof Node) {
				if (vpeers_.indexOf(pp_[i]) < 0)
					vpeers_.addElement(pp_[i]);
			} else if (tmp_ instanceof Link) {
				___getPeers(vpeers_, tmp_, pp_[i]);
			}
		}

		Port[] result_ = new Port[vpeers_.size()];
		vpeers_.copyInto(result_);
		return result_;
	}

	static void ___getPeers(Vector<Port> v_, Component link_, Port oneLinkPort_) {
		Port[] all_ = link_.getAllPorts();
		for (int j = 0; j < all_.length; j++) {
			if (all_[j] == oneLinkPort_)
				continue;
			Port[] allpeers_ = all_[j].getPeers();
			for (int k = 0; k < allpeers_.length; k++) {
				Component host_ = allpeers_[k].getHost();
				if (host_ instanceof Node) {
					if (v_.indexOf(allpeers_[k]) < 0)
						v_.addElement(allpeers_[k]);
				} else if (host_ instanceof Link)
					___getPeers(v_, host_, allpeers_[k]);
			}
		}
	}

	/**
	 * Find a free interface number port. Only work if the number of the
	 * interface is the same that one + the largest number of interface
	 * 
	 * @param node
	 * @return
	 */
	static public int findFreeInterface(Node node) {
		if (node.getCSL() == null)
			return Integer.valueOf(node.findAvailable().id);
		return node.getNumOfInterfaces();
	}
}
