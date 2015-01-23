package tid.inet.protocols.bgp_ct;

import java.util.ArrayList;
import java.util.Enumeration;

import com.renesys.raceway.DML.Configuration;
import com.renesys.raceway.DML.configException;

import tid.inet.protocols.Protocol;
import infonet.javasim.bgp4.AdjRIBIn;
import infonet.javasim.bgp4.AdjRIBOut;
import infonet.javasim.bgp4.BGPSession;
import infonet.javasim.bgp4.Global;
import infonet.javasim.bgp4.PeerEntry;
import infonet.javasim.bgp4.Route;
//import infonet.javasim.bgp4.BGPSession.CPUTimer;
import infonet.javasim.bgp4.comm.UpdateMessage;
import infonet.javasim.bgp4.policy.Rule;
import infonet.javasim.bgp4.timing.MRAIPerPeerTimer;
import infonet.javasim.bgp4.util.Pair;
import infonet.javasim.util.IPaddress;

public class BGPCTSession extends BGPSession implements Protocol
{
	private int bfd_polling_interval;
	
	public void config(Configuration cfg) throws configException{
		//super.config(cfg);
    	this.ASPrefixList = new ArrayList<IPaddress>();

    	Object type;
    	if((type = cfg.findSingle("PROTOCOL")) == null || !type.equals(Protocol.MP_BGP))
    		System.err.println("This isn't MP_BGP protocol");
    	if(cfg.findSingle("ASNUM") != null)
    		this.ASNum = (Integer) cfg.findSingle("ASNUM");
    	else
    		throw new configException("There isn't ASNUM");
    	ip_addr = new IPaddress((Long)cfg.findSingle("INTERFACE"));
    	
    	bgp_id= ip_addr;
    	Enumeration<Pair<Long,Integer>> networks = cfg.find("NETWORKS");
    	if (networks != null) {
	    	while (networks.hasMoreElements()){
	    		Pair<Long,Integer> network = networks.nextElement();
	    		this.addPrefix(network.item1, network.item2);
	    	}
    	}

//   		this.always_compare_med = (Boolean)cfg.findSingle("MED");
//   		this.random_tie_breaking = (Boolean)cfg.findSingle("TIE_BREAKING");
//   		this.routes_compare_level = (Integer)cfg.findSingle("ROUTE_COMPARE_LEVEL");
   		Object portnum;
   		if ((portnum = cfg.findSingle("PORT")) != null && (portnum instanceof Integer))
   			this.port = (Integer)cfg.findSingle("PORT");
   		else
			this.port = BGPSession.PORT_NUM;
    	
    	if(cfg.findSingle("KAI")!= null)
    		this.setKeepAliveInterval((Long) cfg.findSingle("KAI"));
	
		logDebug("config{as="+this.ASNum+"}");
		this.TBID= (int) (rng1.nextDouble()*Integer.MAX_VALUE);
		
		
		// MP BGP stuff 
		if (Global.mp_bfd) {
			bfd_polling_interval = Global.mp_bfd_poll_interval;
		}
		
	    
	    rate_limit_by_dest = Global.rate_limit_by_dest;
	
	    if (Global.max_proc_time > 0.0) {
		   cputimer = new CPUTimer(this, 0.0);
	    }

		this.addPeer((Enumeration<Configuration>)cfg.find("NEIGHBOURS"));

    } // end of config method

	public String id(){
		return Protocol.MP_BGP;
	}
	public void init(){
		super.init();
	}
	public void restart(){
		super.restart();
	}
	
	public void addPeer(Enumeration<Configuration> cfg) throws configException{
		System.err.println("MP_BGP addPeer()...");
		//super.addPeer(cfg);
		
		if (cfg == null){
			System.out.println("Warning: the node hasn't neighbors");
		}
		while(cfg.hasMoreElements())
		{
			Configuration nb_cfg = (Configuration) cfg.nextElement();
			long addr;
			int local_pref = Global.default_local_pref;
			int asnum;
			if ((nb_cfg.findSingle("ASNUM")!= null)&&(nb_cfg.findSingle("ADDR")!= null))
			{
				
//				String strAddr = ((InetAddress) nb_cfg.findSingle("ADDR")).getHostAddress();
				addr = (Long)nb_cfg.findSingle("ADDR");
				
				asnum = (Integer) nb_cfg.findSingle("ASNUM");
			}
			
			else
				throw new configException("There isn't ASNUM or ADDR");
    		
			// Create the peer
			PeerEntry peer= new PeerEntry(this,
				      PeerEntry.EXTERNAL,
				      0,
				    asnum);
			peer.ASNum=asnum;
    		peer.addr= addr;
    		peer.ip_addr= new IPaddress(addr);
    		peer.return_ip= new IPaddress(bgp_id);
    		peer.setLocal_pref(local_pref);
    		
    		// HACEMOS LA CONFIGURACION DE SUBTIPO
    		try {

    			// Value is used to take values of configuration var 
    			String value;
    			if ((value = (String)nb_cfg.findSingle("IN_POLICY"))!= null){
    				Rule rule = Global.rules.get(value);
    				if (rule != null){
    					peer.in_policy = (Rule)rule.clone();
    					peer.in_policy.bgpSession = this;
    				}
    				else 
    					peer.in_policy = new Rule(true,this);
    			}
    			else
    				peer.in_policy = new Rule(true,this);
    			
    			if ((value = (String)nb_cfg.findSingle("OUT_POLICY"))!= null){
    				Rule rule = Global.rules.get(value);
    				if (rule != null){
    					peer.out_policy = rule;
    					peer.out_policy.bgpSession = this;
    				}
    				else 
    					peer.out_policy = new Rule(true,this);
    			}
    			else
    				peer.out_policy = new Rule(true,this);

//    			peer.out_policy= new Rule(true);
//    			peer.in_policy= new Rule(true);
    			peer.rib_in= new AdjRIBIn(this, peer); //made to public
    			peer.rib_out= new AdjRIBOut(this, peer); //made to public
    			ribs_in.put(peer, peer.rib_in);
    			ribs_out.put(peer, peer.rib_out);
    			peer.hold_timer_interval= HOLD_TIMER_DEFAULT;
    			
    			if (Global.mp_bfd && (bfd_polling_interval < keep_alive_interval))
    				peer.keep_alive_interval= bfd_polling_interval;
    			else
    				peer.keep_alive_interval= keep_alive_interval;
    			// external neighbor
    			peer.mrai = (long)(mrai_jitter * EBGP_MRAI_DEFAULT);
    			
    			if (!rate_limit_by_dest) {
    				peer.mraiTimer= new MRAIPerPeerTimer(this,peer.mrai,peer);
    			}
			    // NOTE: The value of the Keep Alive Timer Interval may change
			    // during the peering session establishment process.
			    /*
			    peer.keepAliveTimer= new EventTimer(this, peer.keep_alive_interval,
								KeepAliveTimerExp, peer);
			    */
    			Integer local_preference = (Integer)nb_cfg.findSingle("ROUTE_LOCAL_PREF");
    			if (local_preference != null){
    				peer.setLocal_pref(local_preference);
   				}
    		
    			peer.subtyp = PeerEntry.CTCLIENT;
//    			peer.subtyp = PeerEntry.CTCLIENT;
//				if (this.isDebugEnabled())
//					System.out.println("MP_BGP addPeer: "+toStringPeer(peer));
//    			Enumeration clients;
			} catch (Exception e) {
			    new Error("Exception: "+e.getMessage());
			}
	 
			if (peersByIP.containsKey(addr))
			    throw new Error("Error: [add] peer "+addr+" was already configured !");
			else
			{
				peersByIP.put(addr, peer);
			}
		}

	}
	public void handle_update(UpdateMessage msg){
		System.err.println("MP_BGP handle_update()...");
		super.handle_update(msg);
	}
	
//	public int dop(Route rte){
//		System.err.println("MP_BGP dop()...");
//		return super.dop(rte);
//	}
    public String toString()
    {
    	if (bgp_id == null) return "Mp_bgp not iniciated";
	return "mp_bgp-id="+bgp_id.val();
    }

	
}
