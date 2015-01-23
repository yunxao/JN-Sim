package tid.inet.protocols.gp_bgp;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.jdom.Element;

import drcl.comp.Component;
import drcl.inet.Node;
import drcl.util.random.UniformDistribution;
import tid.Enviroment;
import tid.graphic.GraphicBGPEventManager;
import tid.inet.VirtualMultipointInterface;
import tid.inet.protocols.Protocol;
import infonet.javasim.bgp4.AdjRIBIn;
import infonet.javasim.bgp4.AdjRIBOut;
import infonet.javasim.bgp4.BGPSession;
import infonet.javasim.bgp4.Global;
import infonet.javasim.bgp4.PeerConnection;
import infonet.javasim.bgp4.PeerEntry;
import infonet.javasim.bgp4.BGPSession.CPUTimer;
import infonet.javasim.bgp4.comm.StartStopMessage;
import infonet.javasim.bgp4.policy.Action;
import infonet.javasim.bgp4.policy.AtomicAction;
import infonet.javasim.bgp4.policy.AtomicPredicate;
import infonet.javasim.bgp4.policy.Clause;
import infonet.javasim.bgp4.policy.Predicate;
import infonet.javasim.bgp4.policy.Rule;
import infonet.javasim.bgp4.timing.MRAIPerPeerTimer;
import infonet.javasim.util.IPaddress;
import infonet.javasim.util.TimerMaster;

public class GP_BGPSession extends BGPSession implements Protocol{
	public HashMap<PeerEntry,IPaddress> hostAddressesByPeer = new HashMap<PeerEntry,IPaddress>();

	public static UniformDistribution rng5 = new UniformDistribution(20.0, 40.0);

	/**
	 * Port by default of BGP. This may be modified at runtime.
	 */
	public static int PORT_NUM = 180;
	VirtualMultipointInterface iface = null;

	private boolean logConfigured = false;
	@Override
	public void config(Element sessionElement,Node node) {
		Element parameters = sessionElement.getChild("parameters"); 
		this.ASPrefixList = new ArrayList<IPaddress>();
		String value;
		Object type;
		value = parameters.getAttributeValue("as");
		if (value == null){
			throw new Error ("BGPSession.config: there are no as-id");
		}
		this.ASNum = Integer.valueOf(value);
		
		// TODO quiza haya que poner algo mas general. El id de la interfaz que no sabemos muy bien lo que es
		this.iFaceName = parameters.getAttributeValue("interface");
		if (this.iFaceName == null)
			throw new Error("BGPSession.Config: BGPSession need a interface parameter");
		Component comp = node.getComponent(this.iFaceName);
		if (!(comp instanceof VirtualMultipointInterface)){
			throw new Error("GP_BGPSession.config: Generic path bgp need a virtual multipoint interface to work");
		}else {
			iface = (VirtualMultipointInterface) comp;
			// FIXME tema de interface virtuales y ids que no sean exactamente una direccion!!
			ip_addr = new IPaddress((Long) ((tid.inet.NetworkInterface)iface).getAddress());
			// FIXME cambiar el id por otra cosa (quiza un nº de itnerfaz??????)
			bgp_id = ip_addr;
			
			value = parameters.getAttributeValue("default_local_pref");
			if (value == null)
				defaultLocalPreference = Global.default_local_pref;
			else
				defaultLocalPreference = Integer.valueOf(value);
			
			@SuppressWarnings("unchecked")
			List<Element>networks = parameters.getChildren("network");
			// TODO comprobar que devuelve cuando hay 0
			
			ListIterator<Element> netList = networks.listIterator();
			
			while (netList.hasNext()){
				Element net = netList.next();
				// format (www.xxx.yyy.zzz/mm) for IPv4
				value = net.getAttributeValue("address");
				
				if (value == null)
					throw new Error("BGPSession.config: "+value+" there are not address to the network");
				String sAddress[] = value.split("/");
	
	
				this.addPrefix(value);
					
				
			}
			// TODO med
	//		value = net.getAttributeValue("med");
	//		if (value != null)		
			value = parameters.getAttributeValue("med_type");
			
			// always compare med
			if (value  == null)
				this.always_compare_med = Global.always_compare_med;
			else
				this.always_compare_med = value.equals(Global.STRING_ALWAYS_COPMARE_MED);
			
	
			// random tie brakling?
			if ((value = parameters.getAttributeValue("random_tie_breaking"))!= null)
				this.random_tie_breaking = value.equals(Global.STRING_RANDOM_TIE_BREAKING);
			else
				this.random_tie_breaking = Global.random_tie_breaking;
	
			// routes_compare_level?
			if ((value = parameters.getAttributeValue("routes_compare_level"))!= null){
				if (Global.ROUTES_COMPARE_LEVELS.containsKey(value)){
					this.routes_compare_level = Global.ROUTES_COMPARE_LEVELS.get(value);
				}
				else
					this.routes_compare_level = Global.routes_compare_level;
			}
			else{
				this.routes_compare_level = Global.routes_compare_level;
			}
	//		
		
			// port
			if ((value = parameters.getAttributeValue("port")) != null)
				this.port = Integer.valueOf(value);
			else
				this.port = GP_BGPSession.PORT_NUM;
			// Keep alive interval
			if ((value = parameters.getAttributeValue("kai")) != null)
				this.keep_alive_interval = Integer.valueOf(value);
			else
				this.keep_alive_interval = Global_GP.KeepAliveInterval;
			
			
			logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,"config{as=" + this.ASNum + "}");
			this.TBID = (int) (rng1.nextDouble() * Integer.MAX_VALUE);
	
			// XXX codigo descomentado
			@SuppressWarnings("unchecked")
			List<Element> neighbours = sessionElement.getChildren("neighbour");
			// Prefix
			this.reflector = false;
			Iterator<Element> neighboursList = neighbours.iterator();
			if (neighbours != null){
				while (neighboursList.hasNext()){
					Element neighborElement = neighboursList.next();
					value = neighborElement.getAttributeValue("rrc");
					if (!reflector && (value != null)&&(value.equals("yes"))){
						this.reflector = true;
						// TODO ckeck if exist a neighbor reflector to use the same cluster num
						this.addCluster(this.ip_addr.val());
						this.cluster_num = this.getClusterHashTable(this.ip_addr.val());
						
					}
					addPeer(neighborElement);
				}
			}
			rate_limit_by_dest = Global.rate_limit_by_dest;
			if (Global.max_proc_time > 0.0) {
				cputimer = new CPUTimer(this, 0.0);
			}
			// Adding networks
			@SuppressWarnings("unchecked")
			List<Element> netsList = sessionElement.getChildren("network");
			if (netList != null){
				Iterator<Element> netsIteratorList = netsList.iterator();
				while (netsIteratorList.hasNext()){
					Element netElement = netsIteratorList.next();
					value = netElement.getAttributeValue("address");
					if (value == null)
						throw new Error("BGPSession.config: the network has not a addess atribute");
					String [] net = value.split("/");
					// if only has a element, CIRD = 32
					int CIRD;
					if (net.length == 1)
						CIRD = 32;
					else
						CIRD = Integer.valueOf(net[1]);
					
					long address;
					try {
						address = tid.utils.Utils.stringAddressToLong(net[0]);
						this.addPrefix(address, CIRD);
					} catch (UnknownHostException e) {
						e.printStackTrace();
						throw new Error("GP_BGPSEssion (config): The address has not a correct value");
					}


					
				}
			}
			this.createLogs(sessionElement);

		}
		
	}
	// Override because the file name must be different
	@Override
	public void createLogs(Element sessionElement){
		drcl.comp.io.FileComponent file;
		if (!logConfigured)
			logConfigured = true;
		else 
			return;
		String filename = sessionElement.getAttributeValue("log_file_name");
		if (filename == null){
			String value;
			Element parameters = sessionElement.getChild("parameters");
			if ((value = parameters.getAttributeValue("port")) == null)
				value = ""+GP_BGPSession.PORT_NUM;
			filename = this.getParent().getID()+"-gp_bgp-"+value;
		}

		// DEBUG LOG
		if (Global.logDebugEnable){
			file = new drcl.comp.io.FileComponent(filename+"-dbg");
			this.addComponent(file);
			file.open(Enviroment.tracedir+filename+".dbg");
			file.setEventFilteringEnabled(true);
			this.getPort("dbg").connect(file.findAvailable());
		}			
			// FSM LOG
		if (infonet.javasim.bgp4.Global.logFSMEnable){
			file = new drcl.comp.io.FileComponent(filename+"-fsm");
			this.addComponent(file);
			file.open(Enviroment.tracedir+filename+".fsm");
			file.setEventFilteringEnabled(true);
			this.getPort("fsm").connect(file.findAvailable());
		}
			//ROUTE TABLE LOG
		if (infonet.javasim.bgp4.Global.logRTEnable){
			file = new drcl.comp.io.FileComponent(filename+"-rt");
			this.addComponent(file);
			file.open(Enviroment.tracedir+filename+".rt");
			file.setEventFilteringEnabled(true);
			this.getPort("rt").connect(file.findAvailable());

		}
			// MSG LOG
		if (infonet.javasim.bgp4.Global.logMsgEnable){
			file = new drcl.comp.io.FileComponent(filename+"-msg");
			this.addComponent(file);
			file.open(Enviroment.tracedir+filename+".msg");
			file.setEventFilteringEnabled(true);
			this.getPort("msg").connect(file.findAvailable());

		}
	}
	// Override because the port is different
	@Override
	public void configGeneralParameters(Element xml) {
		Element bgpParams = (Element) xml.getChild("parameters");
		if ((bgpParams == null) && Enviroment.debugFlag){
			System.out.println("infonet.javasim.bgp4.BGPSession.configGeneralParameters: " +
					"There are not general parameters defined to the protocol");
		}
			
		
		// Initialize variables of BGP   
		Global.tbid_tiebreaking = false;
		Global.random_tiebreaking = false;
		// MP-BGP extension
		Global.mp_bfd = false;
		Global.mp_bfd_poll_interval = Global.MP_DEFAULT_BFD_POLLING_INTERVAL;
		
		String value;
		if ((value=bgpParams.getAttributeValue("rtlog") )!= null)
			Global.logRTEnable = value.equals("enable");
		if ((value=bgpParams.getAttributeValue("dbglog") )!= null)
			Global.logDebugEnable = value.equals("enable");
		if ((value=bgpParams.getAttributeValue("fsmlog") )!= null)
			Global.logFSMEnable = value.equals("enable");
		if ((value=bgpParams.getAttributeValue("tracelog") )!= null)
			Global.logMsgEnable = value.equals("enable");
		if ((value = bgpParams.getAttributeValue("med"))!= null)
			Global.always_compare_med = value.equals(Global.STRING_ALWAYS_COPMARE_MED);
		if ((value = bgpParams.getAttributeValue("tie_breaking")) != null)
			Global.random_tiebreaking = value.equals(Global.STRING_RANDOM_TIE_BREAKING);
		if ((value = bgpParams.getAttributeValue("default_port"))!= null)
			GP_BGPSession.PORT_NUM = Integer.valueOf(value);
		if ((value=bgpParams.getAttributeValue("kai") )!= null)
			Global_GP.KeepAliveInterval = Integer.valueOf(value);

		if ((value = bgpParams.getAttributeValue("routes_compare_level")) != null){
			if (Global.ROUTES_COMPARE_LEVELS.containsKey(value)){
				Global.routes_compare_level = Global.ROUTES_COMPARE_LEVELS.get(value);
			}
		}
		if ((value = bgpParams.getAttributeValue("default_local_pref")) != null)
			Global.default_local_pref = Integer.parseInt(value);
		
		
		/*-----------------POLICIES------------------*/
		Element bgpPolicy = xml.getChild("policyList");
		if (bgpPolicy != null){
			@SuppressWarnings("unchecked")
			List <Element> policies = bgpPolicy.getChildren("policy");
			for (int i = 0; i <policies.size();i++){
				boolean permit = true;
				if ((value = policies.get(i).getAttributeValue("permit")) != null){
					permit = !value.equals("false");
				}
				Rule policy = new Rule();
				String ruleId = policies.get(i).getAttributeValue("id");
				Global.rules.put(ruleId, policy);
				@SuppressWarnings("unchecked")
				List <Element> clauses = policies.get(i).getChildren("clause");
				
				// List of clauses for a Policy
				for (int j = 0; j < clauses.size();j++){
					Predicate p = new Predicate(null);
					// XXX Por defecto hay que ponerlo en true o false???
					Action a = null;
					Element element;
					// IF exist predicates && atomic predicates
					if ((element = clauses.get(j).getChild("predicate")) != null){
						@SuppressWarnings("unchecked")
						List <Element> atomicPredicates = element.getChildren("atomicPredicate");
						if (atomicPredicates != null){
							// List of atomic predicates for a predicates
							for (int k = 0; k < atomicPredicates.size();k++){
								String attribute = atomicPredicates.get(k).getAttributeValue("attribute");
								if (attribute == null)
									throw new Error("There isn't attribute in a atomicPredicate. Policy ID="+ruleId);
								String matchString = atomicPredicates.get(k).getAttributeValue("match_string");
								if (matchString == null)
									throw new Error("There isn't match_String in a atomicPredicate. Policy ID="+ruleId);
								AtomicPredicate ap = new AtomicPredicate(attribute,matchString);
								p.add_atom(ap);
							}
						}
					}

					if ((element = clauses.get(j).getChild("action")) != null){
						permit = true;
						if ((value = element.getAttributeValue("permit")) != null){
							permit = !value.equals("false");
						}
						a = new Action (permit,null);
						@SuppressWarnings("unchecked")
						List <Element> atomicActions = element.getChildren("atomicAction");
						if (atomicActions != null){
							for (int k = 0; k < atomicActions.size();k++){
								// see Values in AtomicActions
								String attribute = atomicActions.get(k).getAttributeValue("attribute");
								if (attribute == null)
									throw new Error("There isn't attribute in a atomicAction. Policy ID="+ruleId);
								// See values in AtomicActions 
								String action = atomicActions.get(k).getAttributeValue("action");
								if (action == null)
									throw new Error("There isn't action in a atomicAction. Policy ID="+ruleId);
								// Multi value string must be separated with a space
								String val = atomicActions.get(k).getAttributeValue("value");
								String []values;
								if (val != null){
									values = new String[1];
									values[0] = val;
								}
								else{
									val = atomicActions.get(k).getAttributeValue("values");
									if (val == null)
										throw new Error("There isn't \"value\" or \"values\" in a atomicAction. Policy ID="+ruleId);
									values = val.split(" ");

								}
								AtomicAction aa = new AtomicAction(attribute, action, values);
								a.add_atom(aa);
							}
						}
						
					}
					else
						// XXX cual es el valor por defecto
						a = new Action (true,null);
					Clause clause = new Clause(p,a);
					policy.add_clause(clause);

				}

			}
		}
		
	}
	
	
	private class StartPipeTimer extends infonet.javasim.util.Timer {
		private PeerEntry peerEntry;
		private GP_BGPSession owner;

		public StartPipeTimer(TimerMaster timerMaster, double duration,
				PeerEntry peerEntry, GP_BGPSession owner) {
			super(timerMaster, duration);
			this.owner = owner; 
			this.peerEntry = peerEntry;
		}

		public void callback() {
			Long hostAddress = hostAddressesByPeer.get(this.peerEntry).val();
			if (owner.retrieveBestRTEntryDest(hostAddress)!= null){
				logDebug("creating a virtual connection");
				tid.inet.InetUtil.stabilizeVirtualConnection((Node)owner.getParent(), owner.iface, hostAddress, peerEntry.addr);
//				tid.inet.InetUtil.stabilizeVirtualConnection((Node)owner.getParent(), owner.iface, peerEntry.addr, peerEntry.addr);
				
			}
			else {
				logDebug("Peer is not accesible, waiting to try to stabilize the pipe");
				new StartPipeTimer(timerMaster, rng5.nextDouble(), peerEntry,owner).set();
			}
		}
	}
	public void init() {
		super.init();
		for (int i = 0; i < allPeers.size();i++){
			(new StartPipeTimer(timerMaster, rng5.nextDouble(), allPeers.get(i),this)).set();
		}
	}
	public void addPeer(Element xmlPeer) {
		// COMENZAMOS A CONFIGURAR LOS NEIBORGS
		// COGEMOS LA LISTA DE LOS NEIBORGS
		// this.setDebugEnabled(true);
		// Enumeration nbs_config = cfg.find("NEIGHBOURS_POINTERS");

		// UNO A UNO COGEMOS TODOS LOS NEIBORGS DE LA LISTA
		String value;
		value = xmlPeer.getAttributeValue ("virtualAddress");
		if (value == null)
			throw new Error("GP_BGPSession.addPeer("+this.bgp_id+"): neighbor has not virtualAddress");
		IPaddress address = new IPaddress(value);
		Long long_addr = address.val();
		value = xmlPeer.getAttributeValue ("hostAddress");
		if (value == null)
			throw new Error("GP_BGPSession.addPeer("+this.bgp_id+"): neighbor has not hostAddress");
		IPaddress hostAddress = new IPaddress(value);
		value = xmlPeer.getAttributeValue("remote-as");
		if (value == null)
			throw new Error("GP_BGPSession.addPeer("+this.bgp_id+"): neighbor "+address+" has not as");
		Integer asnum = Integer.valueOf(value);
		PeerEntry peer = new PeerEntry(this,
				(asnum == this.ASNum) ? PeerEntry.INTERNAL: PeerEntry.EXTERNAL,
						0,
						asnum);
		peer.ASNum = asnum;
		peer.addr = long_addr;
		peer.ip_addr = new IPaddress(long_addr);
		peer.return_ip = new IPaddress(bgp_id);
		peer.bgp_id = new IPaddress(long_addr);
		if (this.reflector){
			value = xmlPeer.getAttributeValue("rrc");
			if (peer.typ == PeerEntry.INTERNAL && (value != null) && (value.equals("yes")) ){
				peer.subtyp = PeerEntry.CLIENT;
			}
			else
				peer.subtyp = PeerEntry.NONCLIENT;
		}
		peer.subtyp = PeerEntry.NONCLIENT;
		if (xmlPeer.getAttribute("rrc") != null && xmlPeer.getAttribute("rrc").getValue().equals("yes")){
			if (peer.typ == PeerEntry.INTERNAL) {
				peer.subtyp = PeerEntry.CLIENT;
			}
		}
		value = xmlPeer.getAttributeValue("local_pref");
	    if (value != null){
	    	peer.setLocal_pref(Integer.valueOf(value));
	    }
	    else
	    	peer.setLocal_pref(this.defaultLocalPreference);
	    value = xmlPeer.getAttributeValue("med");
	    if (value != null){
	    	peer.setMed(Integer.valueOf(value));
	    }
	    @SuppressWarnings("unchecked")
	    List<Element> inPolicies = xmlPeer.getChildren("inPolicy");
	    if (inPolicies != null && inPolicies.size() > 0){
	    	for (Element policy:inPolicies){
	    		if (policy != null){
	    			Rule rule = Global.rules.get(policy.getAttributeValue("id"));
	    			peer.in_policy = (Rule) rule.clone();
					peer.in_policy.bgpSession = this;
	    		}
	    	}
	    }
	    else{
	    	// If there are not policies, permit all routes
	    	peer.in_policy = new Rule(true, this);
	    }
	    
	    
	    @SuppressWarnings("unchecked")
	    List<Element> outPolicies = xmlPeer.getChildren("outPolicy");
	    if (outPolicies != null && outPolicies.size() > 0){
	    	for (Element policy:outPolicies){
	    		if (policy != null){
	    			Rule rule = Global.rules.get(policy.getAttributeValue("id"));
	    			peer.out_policy = (Rule) rule.clone();
					peer.out_policy.bgpSession = this;
	    		}	
	    	}
		}
	    else
	    	peer.out_policy = new Rule (true, this);
	    
	    peer.rib_in = new AdjRIBIn(this, peer);
		peer.rib_out = new AdjRIBOut(this, peer);
		ribs_in.put(peer, peer.rib_in);
		ribs_out.put(peer, peer.rib_out);
		peer.hold_timer_interval = HOLD_TIMER_DEFAULT;
		peer.keep_alive_interval = keep_alive_interval;
		if (peer.typ == PeerEntry.INTERNAL) {
			peer.mrai = (long) (mrai_jitter * IBGP_MRAI_DEFAULT);
		} else { // external neighbor
			peer.mrai = (long) (mrai_jitter * EBGP_MRAI_DEFAULT);
		}
		if (!rate_limit_by_dest) {
			peer.mraiTimer = new MRAIPerPeerTimer(this, peer.mrai, peer);
		}
		peersByIP.put(address.val(), peer);
		hostAddressesByPeer.put(peer,hostAddress);
		allPeers.add(peer);

	}
}
