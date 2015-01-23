// ===========================================================================
// @(#) BGPSession.java
//
// @author BJ Premore
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
//
// @lastdate 14/08/2002
// ===========================================================================
// $Id: BGPSession.java,v 1.22 2002/11/19 15:15:09 bqu Exp $

package infonet.javasim.bgp4;

import infonet.javasim.bgp4.comm.BGPMessage;
import infonet.javasim.bgp4.comm.BGPMessageConstants;
import infonet.javasim.bgp4.comm.IncorrectUpdateMessage;
import infonet.javasim.bgp4.comm.KeepAliveMessage;
import infonet.javasim.bgp4.comm.NotificationMessage;
import infonet.javasim.bgp4.comm.OpenMessage;
import infonet.javasim.bgp4.comm.StartStopMessage;
import infonet.javasim.bgp4.comm.TransportMessage;
import infonet.javasim.bgp4.comm.UpdateMessage;
import infonet.javasim.bgp4.path.Attribute;
import infonet.javasim.bgp4.path.ClusterList;
import infonet.javasim.bgp4.path.ExtendedCommunities;
import infonet.javasim.bgp4.path.ExtendedCommunity;
import infonet.javasim.bgp4.path.LocalPref;
import infonet.javasim.bgp4.path.Origin;
import infonet.javasim.bgp4.path.RedistributionCommunity;
import infonet.javasim.bgp4.path.Segment;
import infonet.javasim.bgp4.policy.Action;
import infonet.javasim.bgp4.policy.AtomicAction;
import infonet.javasim.bgp4.policy.AtomicPredicate;
import infonet.javasim.bgp4.policy.Clause;
import infonet.javasim.bgp4.policy.Predicate;
import infonet.javasim.bgp4.policy.Rule;
import infonet.javasim.bgp4.timing.EventTimer;
import infonet.javasim.bgp4.timing.MRAIPerPeerTimer;
import infonet.javasim.bgp4.timing.MRAITimer;
import infonet.javasim.bgp4.timing.TimeoutMessage;
import infonet.javasim.bgp4.timing.TimerConstants;
import infonet.javasim.bgp4.util.Pair;
import infonet.javasim.util.IPaddress;
import infonet.javasim.util.TimerMaster;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Vector;

import org.jdom.Element;

import tid.Enviroment;
import tid.graphic.GraphicBGPEventManager;
import tid.inet.protocols.Protocol;
import drcl.comp.Component;
import drcl.comp.Port;
import drcl.comp.lib.bytestream.ByteStreamContract;
import drcl.inet.Node;
import drcl.inet.contract.RTConfig;
import drcl.inet.data.RTEntry;
import drcl.inet.data.RTKey;
import drcl.inet.protocol.Routing;
import drcl.inet.protocol.UnicastRouting;
import drcl.inet.socket.InetSocket;
import drcl.inet.socket.NonblockingSocketHandler;
import drcl.inet.socket.SocketContract;
import drcl.inet.socket.SocketMaster;
import drcl.util.random.UniformDistribution;

// ===== class drcl.inet.protocol.BGPSession =============================== //
/**
 * The BGP-4 inter-domain routing protocol. Despite the name of the class, each
 * instance does not represent an individual peering session between two BGP
 * speakers, but a BGP-4 protocol session running on a single router. In other
 * words, an instance of this class is an instance of the protocol running on a
 * router.
 * 
 * @author BJ Premore
 */

public class BGPSession extends Routing implements BGPSessionConstants,
		NonblockingSocketHandler, UnicastRouting, tid.inet.protocols.Protocol {
	// public Vector<RouteInfo> pendingToAdd = new Vector<RouteInfo>();

	// private static double tiempoAcumulado = 78;
	// private static double incremento = 10;

	public boolean printDebug;
	protected boolean printMsg;
	/**
	 * clusters is a list of the cluster of a BGP red. The value of the it is
	 * automatical assigned
	 */
	private static HashMap<Long, Long> clusters = new HashMap<Long, Long>();

	/**
	 * Add a cluster to the list of clusters
	 * 
	 * @param addr
	 *            Address of the router reflector of the cluster
	 */
	public void addCluster(long addr) {
		clusters.put(new Long(addr), new Long(clusters.size()));
	}

	/**
	 * Get number of the cluster associated to a router reflector
	 * 
	 * @param addr
	 *            address of the router
	 * @return Number of cluster
	 */
	public Long getClusterHashTable(long addr) {

		return clusters.get(addr).longValue();
	}

	/**
	 * Port by default of BGP. This may be modified at runtime.
	 */
	public static int PORT_NUM = 179;
	/**
	 * Number of port used by a instance of BGPSession to connect with another
	 * BGPSession
	 */
	// private int port;
	public int port;

	/**
	 * Getter of port
	 * 
	 * @return
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Value by default of local_preference
	 */
	public Integer defaultLocalPreference;

	protected static final int PEER_SHUTDOWN_NOTIFY = 0;
	protected static final int PEER_SHUTDOWN_BREAK_TCP = 1;

	/**
	 * Routes_compare_level indicates how it must be compared. level 0 is
	 * nothing to be compared see infonet.javasim.bgp4.Global to see more.
	 */
	protected int routes_compare_level;
	/**
	 * always_compare_med indicates if med value must be compared always
	 */
	protected boolean always_compare_med;
	/**
	 * random_tie_breaking indicates how must be break the tie. Random (true) or
	 * less id value (false)
	 */
	protected boolean random_tie_breaking;

	// ........................ member data .......................... //

	/**
	 * Whether or not the BGP process represented by this BGP object is actually
	 * alive. If it is not (for example, if its router has crashed), then the
	 * protocol will not interact with anything else in the simulation until the
	 * process is restarted.
	 */
	public boolean alive = false;

	/** A reference to the timer manager on the local router. */
	public TimerMaster timerMaster = new TimerMaster(addEventPort("timer"),
			this);

	/** A reference to the Sockets protocol running on the local router. */
	public SocketMaster socketMaster = null;

	/**
	 * A socket listening for connection requests from (potential) peers (both
	 * internal and external).
	 */
	public InetSocket listenSocket = null;

	/**
	 * The NHI address prefix uniquely identifying this BGP speaker's AS. We use
	 * this in lieu of an AS number whenever possible since it is easier to use
	 * and functionally equivalent. Should we ever need an actual AS number
	 * instead of an NHI prefix, a mapping is kept in
	 * <code>Util.AS_descriptor</code>.
	 * 
	 * @see AS_descriptor
	 */
	// public String as_nh;

	/** The AS number of this BGP speaker's AS. */
	public int ASNum;

	/**
	 * The 'Tie-Breaking-ID' used as an alternative to random/router-id
	 * tie-breaking...
	 */
	public int TBID;

	/** The IP address prefix which is representative of this BGP's AS. */
	public ArrayList<IPaddress> ASPrefixList = null;

	/** The NH part of the NHI address for this BGP's router. */
	// public String nh;
	public IPaddress ip_addr;

	/**
	 * The BGP Identifier for this BGP speaker. Each BGP speaker (router running
	 * BGP) has a BGP Identifier. A given BGP speaker sets the value of its BGP
	 * Identifier to an IP address assigned to that BGP speaker. It is chosen at
	 * startup and never changes.
	 */
	public IPaddress bgp_id;

	/**
	 * The Adj-RIBs-In. It stores routing information that has been learned from
	 * inbound update messages. The table is keyed by peer entries.
	 */
	public HashMap<PeerEntry, AdjRIBIn> ribs_in = new HashMap<PeerEntry, AdjRIBIn>();

	/**
	 * The Loc-RIB. It stores the local routing information that this BGP
	 * speaker has selected by applying its local policies to the routing
	 * information contained in the Adj-RIBs-In.
	 */
	public LocRIB loc_rib;

	/**
	 * The Adj-RIBs-Out. It stores the information that this BGP speaker has
	 * selected for advertisement to its peers. The table is keyed by peer
	 * entries.
	 */
	public HashMap<PeerEntry, AdjRIBOut> ribs_out = new HashMap<PeerEntry, AdjRIBOut>();

	/** Whether or not this instance of BGP serves as a route reflector. */
	public boolean reflector = false;

	/**
	 * If this is a route reflector, the number of the cluster of which it is a
	 * member.
	 */
	public long cluster_num = -1;

	/**
	 * The next integer available to be assigned as a cluster number. Note that
	 * we are making cluster numbers globally unique, though they need only be
	 * unique within an AS. There's no particular reason for this, except that
	 * perhaps it's a bit easier to code and reduces the number of required data
	 * structures.
	 */
	private static int NEXT_FREE_CL_NUM = 1;

	/** A hash table which maps AS NHI address prefixes to cluster numbers. */
	private static HashMap<String, Integer> nh2cl_map = new HashMap<String, Integer>();

	/**
	 * A table of data for each neighboring router (potential BGP peer), keyed
	 * by the NHI address prefix of that neighbor. A router is considered a
	 * neighbor of the local router if there is a point-to-point connection
	 * between the two. Every neighboring router ("neighbor" for short) is
	 * considered to be a potential peer at simulation start-up. A peer is
	 * simply a neighbor with whom a BGP connection, or peering session, has
	 * been established. Thus, a neighbor is not necessarily a peer, but a peer
	 * is always a neighbor. This difference between neighbors and peers is
	 * important, and the terminology used here attempts to be consistent with
	 * these definitions.
	 */
	// public HashMap nbs = new HashMap();

	/**
	 * A table of data for each neighboring router (potential BGP peer), keyed
	 * by the IP address of the interface on that router to which the local
	 * router has a point-to-point connection.
	 */
	public HashMap<Long, PeerEntry> peersByIP = new HashMap<Long, PeerEntry>();
	public HashMap<PeerConnection, PeerEntry> peersByConnection = new HashMap<PeerConnection, PeerEntry>();
	public ArrayList<PeerEntry> allPeers = null;
	public HashMap<Long, Boolean> socketsStatus = new HashMap<Long, Boolean>();

	/**
	 * The amount of time (in clock ticks) that should elapse between attempts
	 * to establish a session with a particular peer.
	 */
	public long connretry_interval = 120; // default value

	/**
	 * The Minimum AS Origination Interval: the minimum amount of time (in clock
	 * ticks) that must elapse between successive advertisements of update
	 * messages that report changes within this BGP speaker's AS.
	 */
	public long masoi = 15; // default value

	/** Startup time. */
	public double total_startup_wait = Global.base_startup_wait;

	/** Keep Alive Interval. */
	public long keep_alive_interval = KEEP_ALIVE_DEFAULT;

	/** Jitter factor for Keep Alive Interval. */
	public double keep_alive_jitter = 1.0;
	/** Jitter factor for Minimum AS Origination Interval. */
	public double masoi_jitter = 1.0;
	/** Jitter factor for Minimum Route Advertisement Interval. */
	public double mrai_jitter = 1.0;

	/**
	 * Whether or not rate-limiting should be applied on a per-peer,
	 * per-destination basis. The default is false: rate-limiting is applied
	 * only on a per-peer basis, without regard to destination.
	 */
	public static boolean rate_limit_by_dest = false;

	/** The Minimum AS Origination Timer. */
	// public EventTimer masoiTimer;

	/**
	 * A buffer through which all new BGP events, including incoming BGP
	 * messages, must pass. No explicit simulation time delay is imposed while a
	 * message/event is in the queue. However, other BGP mechanisms may cause
	 * simulation time to pass while an event/message waits in the queue.
	 */
	private TwoLevelInBuffer inbuf = new TwoLevelInBuffer(this);

	/**
	 * A queue for all BGP events which require non-zero CPU time. For example,
	 * the processing of an incoming BGP message might require non-zero CPU
	 * time. After the actual processing is done, the message, along with the
	 * calculated CPU time required, gets put in the queue. Once such a message
	 * is removed from the queue, it is discarded and no further action is
	 * taken. Outgoing messages for which we wish to impose a delay (for the
	 * time required to compose them) get composed first, then put in the queue
	 * with the calculated CPU time required. When such a message is removed
	 * from the queue, it is sent on its way.
	 */
	private ArrayList<Object[]> cpuq = new ArrayList<Object[]>();

	/**
	 * A timer used for modeling processing time of certain BGP events/messages.
	 */
	// private CPUTimer cputimer;
	public CPUTimer cputimer;

	/** Indicates whether or not the CPU is currently busy (with BGP work). */
	private boolean cpu_busy = false;

	/**
	 * A special peer entry which represents the local BGP speaker. Obviously,
	 * this entry does not actually represent a peer at all, but it is useful
	 * when dealing with routes which were originated by this BGP speaker or
	 * configured statically.
	 */
	public PeerEntry self;

	/** A helper to manage debugging. */
	public Debug debug;

	/**
	 * Random number generators: - rng1: used for workload generation. - rng2:
	 * used to introduce jitter for KeepAlive, MinASOriginationInterval,
	 * MinRouteAdvertisementInterval timers. Jitter must be uniformely
	 * distributed in the range 0.75 to 1.0 (RFC1771).
	 */
	public static UniformDistribution rng1 = new UniformDistribution(0.0, 1.0);
	/**
	 * Random number generators: - rng2: used to introduce jitter for KeepAlive,
	 * MinASOriginationInterval,
	 */
	public static UniformDistribution rng2 = new UniformDistribution(0.75, 1.0);
	public static UniformDistribution rng3 = new UniformDistribution(0.0, 1.0);
	public static UniformDistribution rng4 = new UniformDistribution(2.0, 4.0);

	public static void setSeed(long seed_) {
		java.util.Random r = new java.util.Random(seed_);
		rng1.setSeed(r.nextLong());
		rng2.setSeed(r.nextLong());
		rng3.setSeed(r.nextLong());
		rng4.setSeed(r.nextLong());
	}

	/**
	 * Event ports: - msg: log for every received message - fsm: log for every
	 * FSM state change - dbg: log for various debugging information - rt : log
	 * for routing table changes (add/rmv)
	 */
	private Port logMsgPort = addEventPort("msg");
	private Port logFSMPort = addEventPort("fsm");
	private Port logDebugPort = addEventPort("dbg");
	private Port logRTPort = addEventPort("rt");
	/** Log knobs: */
	public boolean log_permit_deny = false;
	public boolean log_policies = false;

	/** The NH part of the NHI address for this BGP's router. */
	public String nh;
	/**
	 * The interface name that BGP use, this is the id of the interface, if you
	 * do getComponent(iFaceName), you get the Interface object (
	 * {@link tid.inet.PhysicalNetworkInterface}) it's can be a subclass IVface.
	 */
	protected String iFaceName;

	// ----- constructor BGP ----------------------------------------------- //
	/**
	 * Constructs a BGP protocol session.
	 */
	public BGPSession() {
		loc_rib = new LocRIB(this);
		debug = new Debug(this);
		allPeers = new ArrayList<PeerEntry>();
		// bgpsess = this;
	}

	public long getKeepAliveInterval() {
		return keep_alive_interval;
	}

	public void setKeepAliveInterval(long i) {
		keep_alive_interval = i;
	}

	// ----- BGPSession.addPeer -------------------------------------------- //

	// ----- BGPSession.addPeer -------------------------------------------- //
	static public GraphicBGPEventManager gem = null;

	// ----- BGPSession.setPeerInFilter ------------------------------------ //
	/**
	 * Sets the input filter for the peer identified by @param addr.
	 */
	public void setPeerInFilter(long addr, Rule rule) {
		try {

			PeerEntry peer = null;

			if (peersByIP.containsKey(new Long(addr))) {
				peer = (PeerEntry) peersByIP.get(new Long(addr));
				peer.in_policy = rule;
				if (log_policies && printDebug)
					System.out.println("AS " + ASNum + " PEER " + peer.ASNum
							+ " IN-FILTER: " + rule);
			} else
				throw new Error("Error: [in-filter] peer " + addr
						+ " does not exist !");

		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw new Error("Error: " + e.getMessage());
		}
	}

	// ----- BGPSession.getPeerInFilter ------------------------------------ //
	/**
	 * Return the input filter defined for the peer identified by @param addr.
	 */
	public Rule getPeerInFilter(long addr) {
		PeerEntry peer = null;

		if (peersByIP.containsKey(new Long(addr))) {
			peer = (PeerEntry) peersByIP.get(new Long(addr));
			return peer.in_policy;
		} else
			return null;
	}

	// ----- BGPSession.setPeerOutFilter ----------------------------------- //
	/**
	 * Sets the output filter for the peer identified by @param addr.
	 */
	public void setPeerOutFilter(long addr, Rule rule) {
		PeerEntry peer = null;

		if (peersByIP.containsKey(new Long(addr))) {
			peer = (PeerEntry) peersByIP.get(new Long(addr));
			peer.out_policy = rule;
			if (log_policies && printDebug)
				System.out.println("AS " + ASNum + " PEER " + peer.ASNum
						+ " OUT-FILTER: " + rule);
		} else
			throw new Error("Warning: [in-filter] peer " + addr
					+ " does not exist !");
	}

	// ----- BGPSession.getPeerOutFilter ----------------------------------- //
	/**
	 * Return the output filter defined for the peer identified by @param addr.
	 */
	public Rule getPeerOutFilter(long addr) {
		PeerEntry peer = null;

		if (peersByIP.containsKey(new Long(addr))) {
			peer = (PeerEntry) peersByIP.get(new Long(addr));
			return peer.out_policy;
		} else
			return null;
	}

	// ----- BGPSession.nh2cl ---------------------------------------------- //
	/**
	 * Returns a unique cluster number associated with a given NHI prefix
	 * address.
	 * 
	 * @param nh
	 *            The NH address to be converted.
	 * @return the unique cluster number associated with the NH address
	 */
	public static long nh2cl(String nh) {
		if (nh2cl_map.containsKey(nh)) {
			// This NHI prefix is already mapped to a cluster number.
			return ((Integer) nh2cl_map.get(nh)).intValue();
		} else {
			// This NHI prefix is not yet mapped to a cluster number.
			nh2cl_map.put(nh, new Integer(NEXT_FREE_CL_NUM));
			return NEXT_FREE_CL_NUM++;
		}
	}

	// ----- BGPSession.event2str ------------------------------------------ //
	/**
	 * Returns a string representation of a given BGP event number.
	 * 
	 * @param An
	 *            integer representing a BGP event.
	 * @return a string representation of a BGP event
	 */
	public static String event2str(int eventnum) {
		return eventNames[eventnum];
	}

	private boolean is_local_prefix(IPaddress prefix) {
		if (ASPrefixList != null) {
			for (int prefix_index = 0; prefix_index < ASPrefixList.size(); prefix_index++)
				if (((IPaddress) ASPrefixList.get(prefix_index)).equals(prefix))
					return true;
		}
		return false;
	}

	// ----- BGPSession.ftadd ---------------------------------------------- //
	/**
	 * Adds a route to the local forwarding table.
	 * 
	 * @param info
	 *            Route information about the route to be added.
	 */
	public void ftadd(RouteInfo info) /*
									 * throws
									 * tid.inet.exceptions.RouteTableException
									 */{
		if (!is_local_prefix(info.route.nlri)) {
			long dst = info.route.nlri.val();
			long dstmask = ((long) -1) << (32 - info.route.nlri.prefix_len());
			long nexthop = info.route.nexthop().val();
			// Find the output interface for the nexthop (should be
			// defined statically or by IGP)
			Object objBestNhEntry = retrieveBestRTEntryDest(nexthop);
			if (objBestNhEntry == null)
				throw new Error("Error: [ftadd] no route to nexthop !");
			if (!(objBestNhEntry instanceof RTEntry))
				throw new Error("Error: [ftadd] incorrect route to nexthop !");
			// Insert new routing entry
			// (note: nexthop is purely informational)
			addRTEntry(new RTKey(0, 0, dst, dstmask, 0, 0), nexthop,
					((RTEntry) objBestNhEntry).getOutIf(), null, Double.NaN);

			logRT(info, "add");
		}
	}

	// ----- BGPSession.ftrmv ---------------------------------------------- //
	/**
	 * Removes a route from the local forwarding table.
	 * 
	 * @param info
	 *            Route information about the route to be removed.
	 */
	public void ftrmv(RouteInfo info) {

		long dst = info.route.nlri.val();
		long dstmask = ((long) -1) << (32 - info.route.nlri.prefix_len());
		removeRTEntry(new RTKey(0, 0, dst, dstmask, 0, 0), RTConfig.MATCH_EXACT);

		logRT(info, "rmv");

	}

	// ----- BGPSession.addPrefix ------------------------------------------ //
	/**
	 * add a network to the BGP prefix list. TODO add support to IPv6
	 * 
	 * @throws UnknownHostException
	 * 
	 */
	public void addPrefix(String addr) {
		long long_addr;
		try {
			String[] dir = addr.split("/");
			if (dir.length != 2)
				throw new Error("BGPSession.config: " + addr
						+ " is not a correct address format");
			long_addr = tid.utils.Utils.inetAddressToLong(InetAddress
					.getByName(dir[0]));
			int mask = Integer.valueOf(dir[1]);
			addPrefix(long_addr, mask);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * add a network to the BGP prefix list
	 * 
	 * @param addr
	 * @param prefix_len
	 */
	public void addPrefix(long addr, int prefix_len) {
		if (ASPrefixList == null)
			this.ASPrefixList = new ArrayList<IPaddress>();
		IPaddress prefix = new IPaddress(addr, prefix_len);
		ASPrefixList.add(prefix);
	}

	// ----- BGP.init ------------------------------------------------- //
	/**
	 * Creates an SSF process whose primary purpose is to perform certain
	 * one-time-only BGP setup tasks.
	 */
	public void init() {
		logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM, "init");

		// - - - - - - - - get socket reference - - - - - - - -
		// socketMaster= new SocketMaster(this, addPort("down"));
		// START EVENTS
		Port downPort = addPort("down");
		socketMaster = new SocketMaster(downPort, this);
		// - - - - - - - - set jitter factors - - - - - - - -

		// jitter factors may vary between 0.75 and 1.00
		if (Global.jitter_masoi) {
			masoi_jitter = rng2.nextDouble();
		}
		if (Global.jitter_keepalive) {
			keep_alive_jitter = rng2.nextDouble();
		}
		if (Global.jitter_mrai) {
			mrai_jitter = rng2.nextDouble();
		}

		// - - - - - - initialize a special peer entry - - - - - -
		self = new PeerEntry(this);

		// - - - - - - set certain interval values - - - - - -
		// these two intervals don't vary by peer
		masoi = (long) (masoi_jitter * masoi);

		// this implementation doesn't actually use this timer (yet)
		// masoiTimer = new EventTimer(this, masoi, 1, 1);

		// The base startup wait is for letting other parts of the simulation
		// get
		// set up (such as an internal gateway protocol like OSPF) before BGP
		// begins.

		total_startup_wait *= rng2.nextDouble();
		// if (gem == null)
		// gem = new GraphicBGPEventManager("bgp 4");
		// Start the timer ticking. (It will "bring up" BGP when it goes off.)
		logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_LOW,
				"spawn-startup-thread");
//		(new MsjSender(timerMaster,100,this)).set();
		(new StartupTimer(timerMaster, total_startup_wait)).set();
		// new EventsTimer(timerMaster, this).set();

	} // end of init method

	// ----- BGP.listen ----------------------------------------------- //
	/**
	 * Wait for a completed socket connection (with a neighbor).
	 */
	public final void listen() {
		if (!alive) {
			debug.warn("socket listen attempted while dead");
			return;
		}
		listenSocket = socketMaster.newSocket();
		socketMaster.bind(listenSocket, bgp_id.val(), this.port);
		socketMaster.listen(listenSocket, 100);
		// at most 100 peers can connect at the same time
		if (alive) {
			try {
				socketMaster.aAccept(listenSocket, this);
			} catch (IOException e) {
				throw new Error("Error: [listen] " + e.getMessage());
			}
		}
	}

	// ----- BGPSession.peerConnectThread ---------------------------- //
	/**
	 * Manage an incoming peer connection. Note that it is possible that this
	 * router has already tried to establish a connection with the peer.
	 */
//	public void peerConnectThread(InetSocket peerSocket) {
//		// Check that the peer that opened the connection is an
//		// authorized neighbor. If it is not the case, close the
//		// connection.
//		PeerEntry peerEntry = (PeerEntry) peersByIP.get(new Long(peerSocket
//				.getRemoteAddress()));
//		if (peerEntry == null) {
//			System.out.println("Warning: an unknown peer ("
//					+ peerSocket.getRemoteAddress() + ") tries to connect !!!");
//			throw new Error("Error: an unknown peer (" + peerSocket.toString()
//					+ ") tries to connect !!!");
//			// Close the connecting socket.
//
//		}
//		// System.out.println("Succesful peer connection: "+peerSocket.toString());
//
//		// Check that the peer concerned by the new connection is not
//		// already in the ESTABLISHED state. If it is the case, the
//		// new connection is immediately closed.
//		if (peerEntry.isConnectionEstablished()) {
//			System.out.println(this.getTime() + ": Warning: peer ("
//					+ peerSocket.getRemoteAddress()
//					+ ") tries to open a new connection while"
//					+ " already in the ESTABLISHED state !!!");
//			return;
////			throw new Error(
////					"Error: peer ("
////							+ peerSocket.toString()
////							+ ") tries to open a new connection while already connected !");
//
//		}
//
//		// Create a peer connection object with an existing, connected
//		// socket ...
//		PeerConnection peerConnection = new PeerConnection(peerEntry,
//				peerSocket);
//		peersByConnection.put(peerConnection, peerEntry);
//		// Fire TransConnOpen event ...
//		peerConnection.manageConnect();
//	}

	// ----- BGP.version ---------------------------------------------- //
	/**
	 * Returns the developer's version string of this BGP-4 implementation.
	 * 
	 * @return the developer's version string
	 */
	public final String version() {
		return "bgp";
	}

	// ----- BGP.reset_timer ------------------------------------------ //
	/**
	 * Resets the indicated type of timer for the given peer (if applicable). If
	 * the timer had not been previously set, then the cancel has no effect, but
	 * the timer is still set normally.
	 * 
	 * @param peer
	 *            The peer entry for the peer with whom the timer is associated
	 *            (if applicable).
	 * @param timer
	 *            The timer to be reset.
	 */
	public void reset_timer(PeerConnection peerConnection, int timertype) {
		PeerEntry peerEntry = peerConnection.getEntry();

		switch (timertype) {
		case TimerConstants.CONNRETRY:
			if (peerEntry.connectRetryTimer != null) {
				peerEntry.connectRetryTimer.cancel();
			} else {
				peerEntry.connectRetryTimer = new EventTimer(this,
						connretry_interval, ConnRetryTimerExp, peerConnection);
			}
			peerEntry.connectRetryTimer.set(connretry_interval);
			break;
		case TimerConstants.HOLD:
			// if the negotiated Hold Timer interval is 0, then we don't
			// bother with the Hold Timer or the KeepAlive timer
			if (peerEntry.hold_timer_interval > 0) {
				if (peerConnection.holdTimer != null) {
//					if (printDebug)
//						System.out.println("["+getTime()+"] cancel_before_set_hold_timer");
					peerConnection.holdTimer.cancel();
				} else {
					peerConnection.holdTimer = new EventTimer(this,
							peerEntry.hold_timer_interval, HoldTimerExp,
							peerConnection);
				}
//				if (printDebug)
//					System.out.println("["+getTime()+"] set_hold_timer");
				peerConnection.holdTimer.set(peerEntry.hold_timer_interval);
			} else {
				if (peerConnection.holdTimer != null) {
//					if (printDebug)
//						System.out.println("["+getTime()+"] cancel_hold_timer");
					peerConnection.holdTimer.cancel();
				}
			}
			break;
		case TimerConstants.KEEPALIVE:
			// if the negotiated Hold Timer interval is 0, then we don't
			// bother with the Hold Timer or the KeepAlive timer
			if (peerEntry.hold_timer_interval > 0) {
				if (peerConnection.keepAliveTimer != null) {
					peerConnection.keepAliveTimer.cancel();
				} else {
					peerConnection.keepAliveTimer = new EventTimer(this,
							peerEntry.keep_alive_interval, KeepAliveTimerExp,
							peerConnection);
				}
				peerConnection.keepAliveTimer
						.set(peerEntry.keep_alive_interval);
			}
			break;
		case TimerConstants.MASO:
			debug.err("Min AS Origination Timer is unused!");
			break;
		case TimerConstants.MRAI:
			// This method shouldn't be called for this timer. It's easier just
			// to
			// take care of it inline because it requires two arguments and
			// occurs
			// less often (in the code) than the other timer resets.
			debug.err("invalid Min Route Advertisement Timer reset");
			break;
		default:
			debug.err("unknown timer type: " + timertype);
		}
	}

	// ----- BGP.handle_update ---------------------------------------- //
	/**
	 * This method takes all necessary action when an update message is
	 * received. This includes handling optional attributes, adding/removing
	 * entries from Adj-RIBs-In, running the Decision Process, etc.
	 * 
	 * @param msg
	 *            An update message received by this BGP speaker.
	 */
	public void handle_update(UpdateMessage msg) {

		// Extract each route from the update message (there will be one
		// route for each separate IP address prefix in the NLRI).
		PeerEntry peer = (PeerEntry) peersByConnection.get(msg.peerConnection);

		peer.inUpdates++;
		ArrayList<Route> rcvd_rtes = msg.announceRoutes;
		ArrayList<Route> rcvd_wds = msg.withdrawRoutes;

		if (rcvd_rtes == null) {
			rcvd_rtes = new ArrayList<Route>();
		}
		if (rcvd_wds == null) {
			rcvd_wds = new ArrayList<Route>();
		}
		debug.valid(Global.WITHDRAWALS, 2, msg.getAnnounce(0));

		// For now, no optional attributes are used,
		// so they don't need to be checked.

		boolean rundp = false; // whether or not to run the Decision Process
		@SuppressWarnings("unchecked")
		ArrayList changedinfo = new ArrayList(); // changed rtes to run DP Ph. 2
		// on

		// - - - - - - - check feasibility of new routes - - - - - - - //
		// check for cluster loops

		logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_LOW,
				"check-cluster-loops");

		if (rcvd_rtes.size() > 0) {

			// Apply in policy to inbound routes
			Iterator<Route> it = rcvd_rtes.iterator();
			ArrayList<Route> routesToRemove = new ArrayList<Route>();
			while (it.hasNext()) {
				Route nextRoute = null;
				nextRoute = it.next();

				if (!peer.in_policy.apply_to(nextRoute)) {
					if (gem != null) {
						String message = "Inbound Route denied:\n";
						message += "- Route Announced: " + nextRoute.nlri
								+ "\n";
						message += "Policy: \n";
						message += peer.in_policy + "\n";
						message += "Information of route: \n";
						message += "- Next Hop: " + nextRoute.nexthop() + "\n";
						message += "- As-path: " + nextRoute.aspath() + "\n";
						if (nextRoute.pas != null) {
							for (int j = 0; j < nextRoute.pas.length; j++)
								if (nextRoute.pas[j] != null) {
									message += "- " + Attribute.names[j] + ": "
											+ nextRoute.pas[j] + "\n";
								}
						}
						gem.addClause(this.getTime(), this.bgp_id.toString(),
								peer.ip_addr.toString(),
								GraphicBGPEventManager.APPLIED_IN_POLICIES,
								message, null);
					}
					// rcvd_rtes.remove(nextRoute);
					routesToRemove.add(nextRoute);

				}

				else {
					if (gem != null) {
						String message = "Inbound Route permited:\n";
						message += "- Route Announced: " + nextRoute.nlri
								+ "\n";
						message += "Policy: \n";
						message += peer.in_policy + "\n";
						message += "Information of route: \n";
						message += "- Next Hop: " + nextRoute.nexthop() + "\n";
						message += "- As-path: " + nextRoute.aspath() + "\n";
						if (nextRoute.pas != null) {
							for (int j = 0; j < nextRoute.pas.length; j++)
								if (nextRoute.pas[j] != null) {
									message += "- " + Attribute.names[j] + ": "
											+ nextRoute.pas[j] + "\n";
								}
						}
						gem.addClause(this.getTime(), this.bgp_id.toString(),
								peer.ip_addr.toString(),
								GraphicBGPEventManager.APPLIED_IN_POLICIES,
								message, null);
					}
					// rcvd_rtes.remove(nextRoute);

				}
			}
			for (Route r : routesToRemove) {
				rcvd_rtes.remove(r);
			}
			for (Route rte : rcvd_rtes) {
				// Route rte = (Route) rcvd_rtes.get(0);
				if (reflector && peer.typ == PeerEntry.INTERNAL) {
					// this is a route reflector and has received an internal
					// update
					if (peer.subtyp == PeerEntry.NONCLIENT) {
						// it was from a non-client, so check cluster list for
						// loops
						ClusterList cluster = rte.cluster_list();

						if ((cluster != null)
								&& (rte.cluster_list().contains(cluster_num))) {
							logDebug(
									GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
									"check-cluster-loops: ERROR <<infeasible routes>>");

							// there was a loop, so all new routes in the update
							// are
							// infeasible
							for (int i = 0; i < rcvd_rtes.size(); i++) {
								Route r = (Route) rcvd_rtes.get(i);
								// treat infeasible route as a withdrawal
								rcvd_wds.add(r);
							}
							rcvd_rtes.clear();
						}

					}
				}
			}
		}
		// check for AS path loops
		logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_LOW,
				"check-AS_PATH-loops");

		if (rcvd_rtes.size() > 0) {
			// All routes from the same update have the same ASpath, so just
			// look at
			// first one.
			Route rte = (Route) rcvd_rtes.get(0);
			if (rte.aspath().contains(ASNum)) {
				logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
						"check-AS_PATH-loops: ERROR <<infeasible routes>>");
				logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_LOW, "AS-PATH="
						+ rte.aspath() + " contains " + ASNum);
				// a loop exists, so all routes in this update are infeasible
				for (int i = 0; i < rcvd_rtes.size(); i++) {
					Route r = (Route) rcvd_rtes.get(i);
					// treat infeasible route as a withdrawal
					rcvd_wds.add(r);
				}
				rcvd_rtes.clear();
			}
		}

		// - - - - - - - - - - handle withdrawals - - - - - - - - - - //

		logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
				"handle-withdrawals {" + rcvd_wds.size() + "}");

		int num_wds = (rcvd_wds == null) ? 0 : rcvd_wds.size();
		for (int i = 0; i < num_wds; i++) {
			Route wd = rcvd_wds.get(i);
			if (peer.hasMed()) {
				wd.set_med(peer.getMed());
			}
			logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
					"handle-withdraw {" + wd.toString() + "}");
			// The route Must be OriginatorID attirbute in the same case as
			// announced because must be compare and the route is with attribute
			if (reflector && peer.typ == PeerEntry.INTERNAL) {
				// this is a route reflector and has received an internal update
				if (!wd.has_orig_id()) {
					// there is no ORIGINATOR_ID attribute, so add it
					wd.set_orig_id(peer.bgp_id);
				}

			}
			// FIXME las politicas son posteriores a esto
			if (peer.typ == PeerEntry.EXTERNAL) {
				wd.set_localpref(peer.getLocal_pref());
			}

			// We may want to consider filtering here, though filtering on
			// withdrawn
			// routes is probably not necessary, since presumably the a
			// withrawal
			// that would match a filter would be matched by the NLRI, and the
			// filter
			// that matched it would also have matched the original
			// advertisement
			// with the same NLRI. So the withdrawal, if not filtered, would
			// only be
			// attempting to withdraw a route which was not in the local RIBs
			// anyway,
			// and it would essentially be ignored.

			// Remove the route from the appropriate Adj-RIB-In.
			// (If it's not actually in there, then no harm is done.)
			if (!peer.in_policy.apply_to(wd)) {
				if (gem != null) {
					String message = "Inbound Route denied:\n";
					message += "- Route Announced: " + wd.nlri + "\n";
					message += "Policy: \n";
					message += peer.in_policy + "\n";
					message += "Information of route: \n";
					message += "- Next Hop: " + wd.nexthop() + "\n";
					message += "- As-path: " + wd.aspath() + "\n";
					if (wd.pas != null) {
						for (int j = 0; j < wd.pas.length; j++)
							if (wd.pas[j] != null) {
								message += "- " + Attribute.names[j] + ": "
										+ wd.pas[j] + "\n";
							}
					}
					gem.addClause(this.getTime(), this.bgp_id.toString(),
							peer.ip_addr.toString(),
							GraphicBGPEventManager.APPLIED_IN_POLICIES,
							message, null);
				}
			} else {
				if (gem != null) {
					String message = "Inbound Route permited:\n";
					message += "- Route Announced: " + wd.nlri + "\n";
					message += "Policy: \n";
					message += peer.in_policy + "\n";
					message += "Information of route: \n";
					message += "- Next Hop: " + wd.nexthop() + "\n";
					message += "- As-path: " + wd.aspath() + "\n";
					if (wd.pas != null) {
						for (int j = 0; j < wd.pas.length; j++)
							if (wd.pas[j] != null) {
								message += "- " + Attribute.names[j] + ": "
										+ wd.pas[j] + "\n";
							}
					}
					gem.addClause(this.getTime(), this.bgp_id.toString(),
							peer.ip_addr.toString(),
							GraphicBGPEventManager.APPLIED_IN_POLICIES,
							message, null);
				}
				RouteInfo rmvdinfo = peer.rib_in.remove(wd);
				if (rmvdinfo != null) {
					rmvdinfo.feasible = false; // mark it infeasible
					rundp = true;

					logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
							"handle_update(BGPSession): adding RouteInfo(old to remove) to changeinfo(1): "
									+ rmvdinfo);

					changedinfo.add(rmvdinfo);

				}
			}
		}

		ArrayList newinfo_list = new ArrayList();
		// - - - - - - - - - - handle new routes - - - - - - - - - - //

		logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
				"handle-announces {" + rcvd_rtes.size() + "}");

		for (int i = 0; i < rcvd_rtes.size(); i++) {
			Route rte = (Route) rcvd_rtes.get(i);
			logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
					"handle-announce {" + rte.toString() + "}");

			rundp = false;
			if (peer.hasMed()) {
				rte.set_med(peer.getMed());
			}
			// NOTE: do this later (?) (during filtering?)
			if (reflector && peer.typ == PeerEntry.INTERNAL) {
				// this is a route reflector and has received an internal update
				if (!rte.has_orig_id()) {
					// there is no ORIGINATOR_ID attribute, so add it
					rte.set_orig_id(peer.bgp_id);
				}

			}
			// FIXME busco la peer que ya tengo
			if (peer.typ == PeerEntry.EXTERNAL) {
				rte.set_localpref(peer.getLocal_pref());
			}

			// If the update message contains a feasible route, it shall be
			// placed in
			// the appropriate Adj-RIB-In, unless it is identical to a route
			// which is
			// already in the Adj-RIB-In, in which case it is ignored.
			RouteInfo newinfo = new RouteInfo(this, rte, RouteInfo.MIN_DOP,
					true, peer);
			newinfo.set_permissibility(true);
			RouteInfo oldinfo = peer.rib_in.findWorst(newinfo.route.nlri);
			peer.rib_in.add(newinfo);
			if (oldinfo != null && newinfo.route.equals(oldinfo.route)) {
				continue; // they are identical, so skip it
			} else {
				logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
						"handle_update(BGPSession): adding RouteInfo(new) to newinfo_list(2): "
								+ newinfo);

				newinfo_list.add(newinfo);
			}

			// i) If the NLRI is identical to the one of a route currently
			// stored
			// in the Adj-RIB-In, then the new route shall replace the older
			// route
			// in the Adj-RIB-In, thus implicitly withdrawing the older route
			// from
			// service. The BGP speaker shall run its Decision Process since the
			// older route is no longer available for use.
			if (oldinfo == null) {
				logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_LOW,
						"There is not ald information for the Route "
								+ newinfo.route.nlri);

			}
			if (oldinfo != null) { // we replaced an older route
				// There are routes that are worst than newinfo
				if (oldinfo.compare(newinfo) < 0) {
					for (Iterator<RouteInfo> it = peer.rib_in.find(
							newinfo.route.nlri).iterator(); it.hasNext();) {
						RouteInfo ri = it.next();
						ri.feasible = false;
						logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
								"handle_update(BGPSession): adding RouteInfo(old) to changeinfo(3): "
										+ ri);

						changedinfo.add(ri);
						rundp = true;
					}
					/*
					 * oldinfo.feasible = false; // the replacement is an
					 * implicit withdrawallogDebug(
					 * "handle_update(BGPSession): adding RouteInfo(old) to changeinfo(3): "
					 * +oldinfo); changedinfo.add(oldinfo); rundp = true;
					 */
				}
			}

			// ii) If the new route is an overlapping route that is included in
			// an
			// earlier route contained in the Adj-RIB-In, the BGP speaker shall
			// run
			// its Decision Process since the more specific route has implicitly
			// made a portion of the less specific route unavailable for use.
			ArrayList less_specifics = peer.rib_in.get_less_specifics(rte.nlri);
			if (less_specifics.size() > 0) { // there was a less specific route
				rundp = true;
				// Question: What if the path attributes are identical to that
				// of one
				// of the less specific routes? It seems like we wouldn't need
				// to
				// run the DP (see iii below).
			}

			// iii) If the new route has identical path attributes to an earlier
			// route contained in the Adj-RIB-In, and is more specific than the
			// earlier route, no further actions are necessary.
			boolean same_attribs = false;
			for (int j = 0; j < less_specifics.size() && !same_attribs; j++) {
				for (int k = 0; k < ((Vector) less_specifics.get(j)).size()
						&& !same_attribs; k++) {
					RouteInfo ri = (RouteInfo) ((Vector) less_specifics.get(j))
							.get(k);
					if (ri.route.equal_attribs(rte.pas)) {
						same_attribs = true;
					}
				}
			}

			if (!same_attribs) {
				// XXX duda: este if else de abajo se cumple siempre que rundp
				// == true no?
				// iv) If the the new route has NLRI that is not present in
				// [does not
				// overlap with] any of the routes currently stored in the
				// Adj-RIB-In,
				// then the new route shall be placed in the Adj-RIB-In. The BGP
				// speaker shall run its Decision Process.
				if (!peer.rib_in.is_less_specific(rte.nlri)) {
					rundp = true;
				} else {
					// v) If the new route is an overlapping route that is less
					// specific than an earlier route contained in the
					// Adj-RIB-In, the
					// BGP speker shall run its Decision Process on the set of
					// destinations described only by the less specific route.
					rundp = true;
				}
			}
		} // end for each received route

		if (rundp) {
			for (int i = 0; i < newinfo_list.size(); i++) {
				logDebug(
						GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
						"handle_update(BGPSession): adding RouteInfo(from newinfo_list) to changeinfo(4): "
								+ newinfo_list.get(i));

				changedinfo.add(newinfo_list.get(i));
			}
			// Note: Decision_process_1 must be do before add the route to the
			// RIB & FIB (it is changed)
			decision_process_1(newinfo_list);
			ArrayList locribchanges = decision_process_2(changedinfo);
			logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
					"1) decision_process_3");

			decision_process_3(locribchanges, null);
		}

	} // end of handle_update method

	// ----- BGP.dop -------------------------------------------------- //
	/**
	 * Calculates the degree of preference of a route. It is a non-negative
	 * integer, and higher values indicate more preferable routes.
	 * 
	 * @param rte
	 *            A route for which to calculate the degree of preference.
	 * @return the degree of preference of the route
	 */
	public final int dop(Route rte) {
		// public int dop(Route rte) {
		int dop = 0, numhops = 0;

		// Currently, the degree of preference calculation works as follows. If
		// the LOCAL_PREF attribute exists, then the value of LOCAL_PREF is used
		// as
		// the DoP. If not, the DoP is set to (100-n), where n is the number of
		// AS
		// hops from this AS to the destination AS. n can be calculated by
		// counting the number of ASs in the AS_PATH attribute. A higher value
		// for
		// DoP indicates a more preferable route.

		/*
		 * if (rte.has_localpref()) { dop = (int) rte.localpref(); } else
		 */{
			if (rte.aspath() != null) {
				for (int j = 0; j < rte.aspath().segs.size(); j++) {
					numhops += ((Segment) rte.aspath().segs.get(j)).size();
				}
				dop = 100 - numhops;
			} else {
				// No AS_PATH, so must've been from internal peer advertising
				// local AS
				// (and thus our AS prefix should be the same as the route's
				// NLRI).
				boolean internal_prefix = is_local_prefix(rte.nlri);
				if (!internal_prefix/* !rte.nlri.equals(ASPrefix) */) {
					debug.err("route missing AS_PATH attribute");
					dop = RouteInfo.MIN_DOP; // assures that it is not selected
				}
			}
		}
		return dop;
	}

	// ----- BGP.decision_process_1 ----------------------------------- //
	/**
	 * Runs Phase 1 of the Decision Process, which is responsible for
	 * calculating the degree of preference of newly added or updated routes.
	 * 
	 * @param infolist
	 *            A list of route information for which to calculate the degrees
	 *            of preference.
	 */
	public final void decision_process_1(ArrayList infolist) {

		for (int i = 0; i < infolist.size(); i++) {
			RouteInfo info = (RouteInfo) infolist.get(i);
			// First, run the route through the input policy filter.
			if (!info.peer.in_policy.apply_to(info.route)) {
				// the route was denied
				info.set_permissibility(false);
				if (gem != null) {
					String message = "Inbound Route denied:\n";
					message += "- Route Announced: " + info.route.nlri + "\n";
					message += "Policy: \n";
					message += info.peer.in_policy + "\n";
					message += "Information of route: \n";
					message += "- Next Hop: " + info.route.nexthop() + "\n";
					message += "- As-path: " + info.route.aspath() + "\n";
					if (info.route.pas != null) {
						for (int j = 0; j < info.route.pas.length; j++)
							if (info.route.pas[j] != null) {
								message += "- " + Attribute.names[j] + ": "
										+ info.route.pas[j] + "\n";
							}
					}

					gem.addClause(this.getTime(), this.bgp_id.toString(),
							info.peer.ip_addr.toString(),
							GraphicBGPEventManager.APPLIED_IN_POLICIES,
							message, null);
				}

			} else {
				info.set_permissibility(true);
				if (gem != null) {
					String message = "Inbound Route permited:\n";
					message += "- Route Announced: " + info.route.nlri + "\n";
					message += "Policy: \n";
					message += info.peer.in_policy + "\n";
					message += "Information of route: \n";
					message += "- Next Hop: " + info.route.nexthop() + "\n";
					message += "- As-path: " + info.route.aspath() + "\n";
					if (info.route.pas != null) {
						for (int j = 0; j < info.route.pas.length; j++)
							if (info.route.pas[j] != null) {
								message += "- " + Attribute.names[j] + ": "
										+ info.route.pas[j] + "\n";
							}
					}

					gem.addClause(this.getTime(), this.bgp_id.toString(),
							info.peer.ip_addr.toString(),
							GraphicBGPEventManager.APPLIED_IN_POLICIES,
							message, null);
				}
			}
			// Calculate degree of preference whether or not the route was
			// permissible. We probably don't actually have to bother for routes
			// which are not permissible, but just to be safe, I guess.
			info.dop = dop(info.route);

			// Here we determine if an internal update is necessary, that is, if
			// this
			// new route is going to be used in the local forwarding table.
			// Essentially we do what Phase 2 of the Decision Process does,
			// except
			// for just one route. (I'm not exactly sure why the BGP RFC says it
			// should be done here and not in Phase 2 of the Decision Process,
			// but I
			// suspect that it's because no time should be wasted in keeping all
			// BGP
			// speakers in the same AS synchronized.)

			// On second thought, I think most implementations in practice just
			// leave
			// it to Phase 2, so that's what I'm going to do. Wish I knew for
			// sure,
			// though.

			// make sure not to forward anything received from internal peers
			// if (!info.peer.as_nh.equals(as_nh)) {
			// this info is the result of an external update
			// if (info is best route) {
			// an internal update is necessary, so do it
			// UpdateMessage um = null;
			// for (Enumeration enum=nbs.elements(); enum.hasMoreElements();) {
			// Instead of putting these routes into the Adj-RIBs-Out for
			// the internal routers, we just do the send here.
			// PeerEntry nb = (PeerEntry)enum.nextElement();
			// if (nb.typ == PeerEntry.INTERNAL) {
			// not implemented yet
			// }
			// }
			// }
			// }
		}
	}

	// ----- BGP.remove_all_routes ------------------------------------ //
	/**
	 * Removes from the Loc-RIB all routes learned from a given peer, then runs
	 * Phases 2 and 3 of the Decision Process to replace the routes with backups
	 * (if possible), and update neighbors with the changes.
	 * 
	 * @param peer
	 *            The peer whose routes are to be invalidated.
	 */
	public void remove_all_routes(PeerEntry peer) {

		logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
				"remove-all-routes(As: " + peer.ASNum + ")");

		ArrayList<RouteInfo> changedroutes = peer.rib_in.remove_all();
		logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_LOW, "removing "
				+ changedroutes.size() + " Routes");

		for (int i = 0; i < changedroutes.size(); i++) {
			// logDebug("Route: "+changedroutes.get(i));
			RouteInfo ri = changedroutes.get(i);
			ri.feasible = false;
		}
		// Run Decision Process Phase 2, since removing all routes from a
		// particular peer (which usually results from peering session
		// termination)
		// is essentially identical to receiving withdrawals for every route
		// previously advertised by that peer.
		ArrayList locribchanges = decision_process_2(changedroutes);
		logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_LOW,
				"2) decision_process_3");

		decision_process_3(locribchanges, null);
	}

	// ----- BGP.decision_process_2 ----------------------------------- //
	/**
	 * Runs Phase 2 of the Decision Process, which is responsible for selecting
	 * which routes (from Adj-RIBs-In) should be installed in Loc-RIB.
	 * 
	 * @param changedroutes
	 *            A list of info on recent route changes.
	 * @return a list of route changes made to the Loc-RIB
	 */
	public final ArrayList decision_process_2(ArrayList changedroutes) {

		logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_LOW,
				"decision-process {Phase-II}");

		// bqu: logDebug("# changes: "+changedroutes.size());

		// For each destination in Adj-RIBs-In, examine the set of feasible
		// routes
		// to that destination and choose the one with the highest preference
		// and
		// install it in Loc-RIB. Actually, there's no need to run on every
		// single
		// route each time there's a change. We'll only look at what changes
		// there
		// were and act according to those.

		ArrayList locribchanges = new ArrayList(); // changes to Loc-RIB (for
		// DP3)
		ArrayList infoToSend = new ArrayList();
		for (int i = 0; i < changedroutes.size(); i++) {
			RouteInfo info = (RouteInfo) changedroutes.get(i);
			// - - - - - withdrawals - - - - -
			if (!info.feasible) { // an infeasible route
				// bqu: logDebug("infeasible route");
				if (info.inlocrib) { // it was in the Loc-RIB
					RouteInfo oldinfo = loc_rib.remove(info);
					logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
							"Deleting a route" + oldinfo);

					locribchanges.add(info);
					debug.valid(Global.WITHDRAWALS, 5);
					// We removed a route from the Loc-RIB. See if we can
					// replace it
					// with another route (with the same NLRI) from the
					// Adj-RIBs-In.
					// (And if there's more than one choice, find the most
					// preferable.)
					RouteInfo bestnewinfo = null;
					Vector<RouteInfo> newsInfos = new Vector<RouteInfo>();
					for (Iterator<PeerEntry> it = peersByIP.values().iterator(); it
							.hasNext();) {

						PeerEntry peer = (PeerEntry) it.next();
						Vector<RouteInfo> routes;
						logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_LOW,
								"Looking for best route for " + peer);

						if ((routes = peer.rib_in.find(info.route.nlri)) != null) {
							logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_LOW,
									"Tamaño de routes: " + routes.size());

							for (Iterator<RouteInfo> it2 = routes.iterator(); it2
									.hasNext();) {
								RouteInfo riTmp = it2.next();
								logDebug(
										GraphicBGPEventManager.DEBUG_MESSAGE_LOW,
										"Looking in " + riTmp);

								if (bestnewinfo == null
										|| riTmp.compare(bestnewinfo) > 0) {
									logDebug(
											GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
											"decision_process_2(BGPSession): adding information to replace removed info(1) find best path:");

									logDebug(
											GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
											"decision_process_2(BGPSession): "
													+ riTmp);
									newsInfos.clear();
									newsInfos.add(riTmp);
									bestnewinfo = riTmp;
								} else if (riTmp.compare(bestnewinfo) == 0) {
									logDebug(
											GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
											"decision_process_2(BGPSession): adding information to replace removed info(2): ");
									logDebug(
											GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
											"decision_process_2(BGPSession): "
													+ riTmp);

									newsInfos.add(riTmp);
								}
							}
						}
					}
					if (bestnewinfo != null && bestnewinfo.compare(oldinfo) < 0) {
						// I must look at if bestnewinfo is in lock_rib,
						// New routes are better or dont exist old routes
						// Second part of if (tmproute.compare(bestnewinfo))
						// must be false because if tmproute is in loc_rib is
						// because
						// is same than removed info and routes in rib_in are
						// worse or similar than removed info

						for (Iterator<RouteInfo> it = newsInfos.iterator(); it
								.hasNext();) {
							RouteInfo infoToAdd = it.next();
							logDebug(
									GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
									"decision_process_2(BGPSession): adding information to replace removed info(3): ");

							loc_rib.add(infoToAdd);
							locribchanges.add(infoToAdd);
						}

						// We found a replacement for the withdrawn route. Keep
						// in mind
						// that we have not yet checked any newly advertised
						// routes, which
						// may be better than the replacement we just found.
						// Those will be
						// checked in the 'advertisements' section of code
						// below.

					}
				} // else it was not in the Loc-RIB
				else {
				}
			} else { // it's a feasible route
				logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_LOW,
						"feasible route");

				// - - - - - advertisements - - - - -

				if (info.permissible) { // our policy allows it
					logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_LOW,
							"permissible");

					// See if this new feasible, permissible route is better
					// than the
					// current route with the same NLRI in Loc-RIB (if one
					// exists).

					RouteInfo curinfo = loc_rib.findWorst(info.route.nlri);
					// bqu: logDebug((curinfo ==
					// null)?"other route do not exist":"other route already exist");

					// Si la infotmacion anterior no existe o es factible
					// entrarla
					if (curinfo == null || info.compare(curinfo) >= 0) {
						// bqu: logDebug("other or better");
						boolean found_ad = false, found_wd = false;
						// La informacion existe
						if (curinfo != null) { // we're about to replace Loc-RIB
							// info
							// bqu: logDebug("replace previous route");
							// es peor, voy a meterla
							if (info.compare(curinfo) > 0) {
								Vector<RouteInfo> nodes = loc_rib
										.find(info.route.nlri);
								for (int j = 0; j < nodes.size(); j++)
									loc_rib.remove(nodes.get(i));

							}

							// It's possible that we handled a withdrawal for
							// this very route
							// just a moment ago (in the 'withdrawals' section
							// of code
							// above). If that is the case, then we may also
							// have, at that
							// time, found a replacement for the route already.
							// If so, then
							// at this point, the newly advertised route (which
							// was likely,
							// but not necessarily, an implicit withdrawal) is
							// about to
							// replace that replacement which was found above.
							// Rather than
							// considering this as two changes to the Loc-RIB,
							// it would
							// simplifiy things to treat it as just one, since
							// they're
							// happening simultaneously. So here we check to see
							// if we are
							// in fact about to replace a replacement.
							for (int j = 0; j < locribchanges.size(); j++) {
								RouteInfo ri = (RouteInfo) locribchanges.get(j);
								// bqu: logDebug(ri.route.nlri + " =?= "+
								// info.route.nlri);
								if (ri.route.nlri.equals(info.route.nlri)) {
									if (ri.feasible) {
										// We can leave the Loc-RIB change
										// regarding the
										// withdrawal, but should overwrite the
										// Loc-RIB change
										// regarding the new route.
										loc_rib.add(info);
										locribchanges.set(j, info);
										found_ad = true;
									} else {
										found_wd = true;
									}
								}
							}

							if (found_ad) { // See big comment block above.
								debug.affirm(found_wd,
										"withdrawal change missing");
							} else {
								if (info.compare(curinfo) != 0)
									locribchanges.add(curinfo);
							}
						}
						if (!found_ad) { // See big comment block above.
							loc_rib.add(info);
							locribchanges.add(info);
						}
					} else { // not better than current best
					}
				} else { // not permissible
				}
			}
		}
		debug.valid(Global.SELECT, 3);

		// bqu:
		/*
		 * for (int x= 0; x<locribchanges.size(); x++)
		 * logDebug("locribchange("+x+") : "+locribchanges.get(x));
		 */
		// bqu
		locribchanges.addAll(infoToSend);
		return locribchanges;
	}

	// ----- BGP.decision_process_3 ----------------------------------- //
	/**
	 * Runs Phase 3 of the Decision Process, which is responsible for
	 * disseminating routes to peers. This is done by inserting certain routes
	 * from Loc-RIB into Adj-RIBs-Out.
	 * 
	 * @param locribchanges
	 *            A list of changes to the Loc-RIB.
	 * @param peer
	 *            It's where changes are sended. if peer is null then changes
	 *            are sended to all peer in "peersByIP"
	 */
	public final void decision_process_3(ArrayList locribchanges, PeerEntry peer) {
		// MIOS aqui es donde se decide enviar o no
		logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_LOW,
				"decision-process {Phase-III}");

		// bqu: logDebug("# changes: " + locribchanges.size());

		// Normally executed after Phase 2, but must also be executed when
		// routes
		// to local destinations (in Loc-RIB) have changed, when locally
		// generated
		// routes learned by means outside of BGP have changed, or when a new
		// peering session has been established.

		HashMap<PeerEntry, ArrayList<RouteInfo>> wds_tbl = new HashMap<PeerEntry, ArrayList<RouteInfo>>(); // lists
		// of
		// withdrawals,
		// keyed
		// by
		// peer
		HashMap<PeerEntry, ArrayList<RouteInfo>> ads_tbl = new HashMap<PeerEntry, ArrayList<RouteInfo>>(); // lists
		// of
		// route
		// info,
		// keyed
		// by peer
		ArrayList<RouteInfo> wds2send;
		ArrayList<RouteInfo> ads2send;
		// handle withdrawals first
		for (Iterator<PeerEntry> it = peersByIP.values().iterator(); it
				.hasNext();) {
			PeerEntry peerEntry = it.next();

			if ((peer != null) && (peer != peerEntry))
				continue;

			wds2send = new ArrayList<RouteInfo>();
			wds_tbl.put(peerEntry, wds2send);

			for (int i = 0; i < locribchanges.size(); i++) {
				// bqu: copie de la route. Necessaire avant l'application du
				// filtre out.
				RouteInfo _info = (RouteInfo) locribchanges.get(i);
				RouteInfo info = new RouteInfo(this, (RouteInfo) locribchanges
						.get(i));
				info.inlocrib = _info.inlocrib;
				if (!info.inlocrib && advertisable(info, peerEntry)) {
					// We're not using the route any more, so withdraw it.
					peerEntry.rib_out.remove(info.route);
					// We shouldn't be trying to remove a route that we haven't
					// tried to
					// advertise, so we do the following check. (It is currently
					// commented out because the call to OSPF in Phase 2 can
					// lead to some
					// weird behavior, since OSPF gets to execute before BGP is
					// done with
					// Phase 2. This is because BGP and OSPF are not modeled as
					// separate
					// threads. We should fix this.)
					// debug.affirm(oldinfo!=null,
					// "inconsistency in Adj-RIB-Out");

					logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
							"withdraw {" + info.route.nlri.toString() + "} to "
									+ peerEntry.ASNum);

					if (peerEntry.connected) {
						wds2send.add(info);
					} // else no updates have yet been sent to this peer, so no
					// need
					// to send withdrawals
				}

			}
		}
		// Handle new routes next. (Must do after all withdrawals since new
		// routes
		// imply withdrawal and thus we remove any withdrawals with the same
		// NLRI.)
		for (Iterator it = peersByIP.values().iterator(); it.hasNext();) {
			PeerEntry peerEntry = (PeerEntry) it.next();

			// bqu-test
			if ((peer != null) && (peer != peerEntry))
				continue;
			// bqu-test

			ads2send = new ArrayList<RouteInfo>();
			ads_tbl.put(peerEntry, ads2send);
			wds2send = wds_tbl.get(peerEntry);

			for (int i = 0; i < locribchanges.size(); i++) {

				// bqu: copie de la route. Necessaire avant l'application du
				// filtre out.
				RouteInfo _info = (RouteInfo) locribchanges.get(i);
				RouteInfo info = new RouteInfo(this, (RouteInfo) locribchanges
						.get(i));
				info.inlocrib = _info.inlocrib;
				/*
				 * if (info.compare(_info) != 0)
				 * System.out.println("^^^ \n"+_info+"\n"+info);
				 */
				// RouteInfo info= (RouteInfo) locribchanges.get(i);
				// bqu: -
				if (info.inlocrib && advertisable(info, peerEntry)) {
					peerEntry.rib_out.add(info); // will replace previous, if
					// any
					logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
							"announce {" + info.route.toString() + "} to "
									+ peerEntry.ASNum);

					ads2send.add(info);
					for (int k = 0; k < wds2send.size(); k++) { // adv implies
						// withdrawal
						if (info.route.equals(wds2send.get(k))) {
							wds2send.remove(k);
						}
					}
				}

			}
		}
		// update the connectedness of each neighbor
		for (Iterator it = peersByIP.values().iterator(); it.hasNext();) {
			PeerEntry peerEntry = (PeerEntry) it.next();

			// bqu-test
			if ((peer != null) && (peer != peerEntry))
				continue;
			// bqu-test

			if (!peerEntry.connected) {
				// As of the last update of the Adj-RIBs-Out, there was no
				// peering
				// session with this neighbor (or there was no previous
				// update--this is
				// the first).
				if (peerEntry.isConnectionEstablished()) {
					// There is a new peering session with this neighbor. Rather
					// than
					// just sending the newest changes to the Loc-RIB, we want
					// to send
					// everything that's in the Adj-RIB-Out.
					peerEntry.connected = true;
					wds2send = wds_tbl.get(peerEntry);
					debug.affirm(wds2send.size() == 0,
							"unexpected withdrawals to be sent");
					ads2send = peerEntry.rib_out.get_all_routes();
					logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
							"Send to new neighbour: " + peerEntry.addr
									+ ", size:" + ads2send.size());

					ads_tbl.put(peerEntry, ads2send);
				}
			}
		}
		external_update(wds_tbl, ads_tbl);
	}

	// ----- BGP.external_update -------------------------------------- //
	/**
	 * Tries to send update messages to each external peer if there is any new
	 * route information in Adj-RIBs-Out to be shared with them. Currently, this
	 * method also handles updating internal peers.
	 * 
	 * @param wds
	 *            A table of NLRI of withdrawn routes which need to be sent.
	 * @param ads
	 *            A table of routes which need to be advertised.
	 */
	public void external_update(
			HashMap<PeerEntry, ArrayList<RouteInfo>> wds_table,
			HashMap<PeerEntry, ArrayList<RouteInfo>> ads_table) {
		HashMap<PeerEntry, ArrayList<Pair<Route, IPaddress>>> wds_table2 = new HashMap<PeerEntry, ArrayList<Pair<Route, IPaddress>>>();

		for (Iterator<PeerEntry> it = peersByIP.values().iterator(); it
				.hasNext();) {
			PeerEntry rcvr = it.next();
			ArrayList<RouteInfo> wds = (ArrayList<RouteInfo>) wds_table
					.get(rcvr); // a list of RouteInfos
			// bqu

			if (wds == null)
				continue;
			// bqu

			ArrayList<Pair<Route, IPaddress>> newads = new ArrayList<Pair<Route, IPaddress>>(); // this
			// will
			// be
			// a
			// list
			// of
			// Pairs
			for (int i = 0; i < wds.size(); i++) {

				RouteInfo info = wds.get(i);
				PeerEntry sender = info.peer;
				Route newrte = new Route(info.route); // make copy to
				// put in update
				newrte.set_tbid(TBID);
				// ----- make any necessary modifications to the route -----
				if (reflector && sender.typ == PeerEntry.INTERNAL
						&& rcvr.typ == PeerEntry.INTERNAL
						&& (rcvr.bgp_id == null || // no peering session yet, so
								// not originator
								!info.route.has_orig_id() || // no originator ID
						// attribute
						// exists
						!rcvr.bgp_id.equals(info.route.orig_id())) && // not
						// originator
						(sender.subtyp == PeerEntry.CLIENT || rcvr.subtyp == PeerEntry.CLIENT)) { // reflecting
					debug.valid(Global.REFLECTION, 3, info.route.nlri);
					if (rcvr.subtyp == PeerEntry.NONCLIENT) { // to a non-client
						newrte.append_cluster(cluster_num); // append cluster
						// number
					}
				} else { // not reflecting
					newrte.set_nexthop(rcvr.return_ip); // set next hop
				}
				if (rcvr.typ == PeerEntry.EXTERNAL) { // sending to external
					// peer
					newrte.prepend_as(ASNum); // add my AS to the AS path
					newrte.remove_attrib(LocalPref.TYPECODE);
					newrte.set_origin(Origin.EGP);
				}

				newads.add(new Pair<Route, IPaddress>(newrte, sender.ip_addr));
			}
			wds_table2.put(rcvr, newads); // replace entry with new routes
		}

		// UNa forma rara de hacerlo porque en la ultima parada es en la que
		// sigue
		// First, make copies of the routes and modify them if necessary.
		// Change info in ads_table. ArrayList<RouteInfo> -->
		// ArrayList<Pair<Route,IPaddress>>

		HashMap<PeerEntry, ArrayList<Pair<Route, IPaddress>>> ads_table2 = new HashMap<PeerEntry, ArrayList<Pair<Route, IPaddress>>>();
		for (Iterator<PeerEntry> it = peersByIP.values().iterator(); it
				.hasNext();) {
			PeerEntry rcvr = it.next();
			ArrayList<RouteInfo> ads = (ArrayList<RouteInfo>) ads_table
					.get(rcvr); // a list of RouteInfos
			// bqu

			if (ads == null)
				continue;
			// bqu

			ArrayList<Pair<Route, IPaddress>> newads = new ArrayList<Pair<Route, IPaddress>>(); // this
			// will
			// be
			// a
			// list
			// of
			// Pairs
			for (int i = 0; i < ads.size(); i++) {
				RouteInfo info = ads.get(i);
				PeerEntry sender = info.peer;
				Route newrte = new Route(info.route); // make copy to
				// put in update
				newrte.set_tbid(TBID);
				// ----- make any necessary modifications to the route -----
				if (reflector && sender.typ == PeerEntry.INTERNAL
						&& rcvr.typ == PeerEntry.INTERNAL
						&& (rcvr.bgp_id == null || // no peering session yet, so
								// not originator
								!info.route.has_orig_id() || // no originator ID
						// attribute
						// exists
						!rcvr.bgp_id.equals(info.route.orig_id())) && // not
						// originator
						(sender.subtyp == PeerEntry.CLIENT || rcvr.subtyp == PeerEntry.CLIENT)) { // reflecting
					debug.valid(Global.REFLECTION, 3, info.route.nlri);
					if (rcvr.subtyp == PeerEntry.NONCLIENT) { // to a non-client
						newrte.append_cluster(cluster_num); // append cluster
						// number
					}
				} else { // not reflecting
					newrte.set_nexthop(rcvr.return_ip); // set next hop
				}
				if (rcvr.typ == PeerEntry.EXTERNAL) { // sending to external
					// peer
					newrte.prepend_as(ASNum); // add my AS to the AS path
					newrte.remove_attrib(LocalPref.TYPECODE);
					newrte.set_origin(Origin.EGP);
				}
				if (rcvr.typ == PeerEntry.INTERNAL) {
					newrte.set_origin(Origin.IGP);
				}

				newads.add(new Pair<Route, IPaddress>(newrte, sender.ip_addr));
			}
			ads_table2.put(rcvr, newads); // replace entry with new routes
		}

		// WD:

		// put together update messages to send
		for (Iterator<PeerEntry> it = peersByIP.values().iterator(); it
				.hasNext();) {

			PeerEntry peer = it.next();
			ArrayList<Pair<Route, IPaddress>> wds = wds_table2.get(peer);
			ArrayList<Pair<Route, IPaddress>> ads = ads_table2.get(peer); // a
			// list
			// of
			// Pairs

			// bqu

			if ((wds == null) || (ads == null))
				continue;
			// bqu
			logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_LOW,
					"Peer is connected?: " + peer.connected);

			logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_LOW, "Size of wds: "
					+ wds.size());
			if (peer.connected) {
				// bqu-debug: null instead of connection/address of sender ...
				if (wds.size() > 0) {
					UpdateMessage msg = new UpdateMessage((PeerConnection) null/* nh */);
					logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_LOW,
							"Size of wds: " + wds.size());
					msg.addWithdraw(wds.get(0).item1);
					ArrayList<IPaddress> senders = new ArrayList<IPaddress>(1);

					senders.add(wds.get(0).item2);
					try_send_update(msg, senders, peer);
					for (int i = 1; i < wds.size(); i++) {
						msg = new UpdateMessage((PeerConnection) null);
						msg.addWithdraw(wds.get(i).item1);
						senders = new ArrayList<IPaddress>(1);
						senders.add(wds.get(i).item2);
						try_send_update(msg, senders, peer);
					}
				}

				logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_LOW,
						"Size of ads: " + ads.size());

				if (ads.size() > 0) {
					UpdateMessage msg = new UpdateMessage((PeerConnection) null/* nh */);
					msg.addAnnounce((ads.get(0)).item1);
					ArrayList<IPaddress> senders = new ArrayList<IPaddress>(1);
					senders.add((ads.get(0)).item2);
					try_send_update(msg, senders, peer);
					for (int i = 1; i < ads.size(); i++) {
						// bqu-debug: null instead of connection/address of
						// sender
						// Update message can be make with ads but not with wdr
						msg = new UpdateMessage(null/* nh */,
								((ads.get(i)).item1));
						senders = new ArrayList<IPaddress>(1);
						senders.add((ads.get(i)).item2);

						try_send_update(msg, senders, peer);
					}
				} // else neither announcements nor withdrawals

			}
		}

	}

	private class RedCommStruct {
		public boolean in_set;
		public int state;
		public int param;

		public static final int STATE_NONE = 0;
		public static final int STATE_INCLUDE = 1;
		public static final int STATE_EXCLUDE = 2;
		public static final int STATE_IGNORE = 3;
	}

	// ----- BGP.redcomm_match ------------------------------------ //
	/**
     *
     */
	private boolean redcomm_match(RedistributionCommunity red, PeerEntry rcvr) {
		switch (red.getFilterType()) {
		case RedistributionCommunity.RED_FTYPE_AS:
			if (red.getFilterAS() == rcvr.ASNum)
				return true;
			break;
		default:
			throw new Error("ERROR: unsupported redcomm filter type !");
		}
		return false;
	}

	// ----- BGP.redcomm_can_add ---------------------------------- //
	/**
     *
     */
	private void redcomm_update(RedCommStruct rs, RedistributionCommunity red,
			PeerEntry rcvr) {
		boolean include = red.getFilterInclude();

		if (redcomm_match(red, rcvr)) {
			if ((rs.state == RedCommStruct.STATE_NONE)
					|| ((rs.state == RedCommStruct.STATE_INCLUDE) && (include))
					|| ((rs.state == RedCommStruct.STATE_EXCLUDE) && (!include))) {
				rs.in_set = true;
				rs.state = (include ? RedCommStruct.STATE_INCLUDE
						: RedCommStruct.STATE_EXCLUDE);
				if (printDebug)
					System.out.println("MATCH");
			} else {
				rs.state = RedCommStruct.STATE_IGNORE;
				if (printDebug)
					System.out.println("IGNORE");
			}
		}
	}

	// ----- BGP.redcomm_policy_apply_to ---------------------------- //
	/**
     *
     */
	private boolean redcomm_policy_apply_to(Route rt, PeerEntry rcvr) {
		ExtendedCommunities tmp = (ExtendedCommunities) rt.pas[ExtendedCommunities.TYPECODE];
		int action;
		int param;
		RedCommStruct ignore = new RedCommStruct();
		RedCommStruct no_export = new RedCommStruct();
		RedCommStruct prepend = new RedCommStruct();

		ignore.in_set = false;
		ignore.state = RedCommStruct.STATE_NONE;
		ignore.param = 0;
		no_export.in_set = false;
		no_export.state = RedCommStruct.STATE_NONE;
		no_export.param = 0;
		prepend.in_set = false;
		prepend.state = RedCommStruct.STATE_NONE;
		prepend.param = 1;

		for (int index = 0; index < tmp.vals.size(); index++) {
			ExtendedCommunity ext = (ExtendedCommunity) tmp.vals.get(index);
			if (ext instanceof RedistributionCommunity) {
				RedistributionCommunity red = (RedistributionCommunity) ext;
				action = red.getAction();
				param = red.getActionParam();
				switch (action) {
				case RedistributionCommunity.RED_ACTION_PREPEND:
					if ((param < 1) || (param > 7))
						throw new Error("ERROR: incorrect red-comm param !");
					if ((prepend.state == RedCommStruct.STATE_NONE)
							|| (param == prepend.param)) {
						prepend.param = param;
						redcomm_update(prepend, red, rcvr);
					} else
						prepend.state = RedCommStruct.STATE_IGNORE;
					break;
				case RedistributionCommunity.RED_ACTION_NO_EXPORT:
					redcomm_update(no_export, red, rcvr);
					break;
				case RedistributionCommunity.RED_ACTION_IGNORE:
					redcomm_update(ignore, red, rcvr);
					break;
				default:
					throw new Error("ERROR: unknown red-comm action !");
				}
			}
		}

		// Do not announce
		if (ignore.in_set && (ignore.state == RedCommStruct.STATE_INCLUDE)) {
			if (printDebug)
				System.out.println("red-comm \"ignore\" " + rcvr.ASNum);
			return false;
		}

		// NO_EXPORT community
		if (no_export.in_set
				&& (no_export.state == RedCommStruct.STATE_INCLUDE)) {
			if (printDebug)
				System.out.println("red-comm \"no-export\" " + rcvr.ASNum);
			throw new Error("red-comm NO_EXPORT not implemented !!!");
		}

		// AS-PATH prepending
		if (prepend.in_set && (prepend.state == RedCommStruct.STATE_INCLUDE)) {
			if (printDebug)
				System.out.println(getTime() + " AS" + ASNum
						+ "> red-comm \"prepend(" + prepend.param + ")\" "
						+ rcvr.ASNum);
			for (; prepend.param > 0; prepend.param--)
				rt.prepend_as(ASNum);
		}

		return true;
	}

	// ----- BGP.extcomm_remove_non_transitive ---------------------- //
	/**
	 * Remove all non-transitive extended communities from the route. This
	 * function must be called when a route is redistributed to an eBGP peer.
	 */
	private void extcomm_remove_non_transitive(Route rt) {
		rt.remove_non_transitive_extcomm();
	}

	// ----- BGP.extcomm_policy_apply_to ---------------------------- //
	/**
     *
     */
	private boolean extcomm_policy_apply_to(Route rt, PeerEntry rcvr) {
		boolean allow = true;

		if (rt.has_extcomm()) {

			allow = redcomm_policy_apply_to(rt, rcvr);

			// Remove non-transitive extended communities when
			// redistributing to an external (eBGP) peer.
			if (rcvr.typ == PeerEntry.EXTERNAL)
				extcomm_remove_non_transitive(rt);
		}
		return allow;
	}

	// ----- BGP.advertisable ----------------------------------------- //
	/**
	 * Determines if a route should be advertised to a particular peer.
	 * 
	 * @param info
	 *            The route in question.
	 * @param rcvr
	 *            The peer to whom the route may be advertised.
	 * @return true only if the route should be advertised to the given peer
	 */
	private boolean advertisable(RouteInfo info, PeerEntry rcvr) {

		PeerEntry sender = info.peer; // who sent it to us
		if (sender == rcvr) {
			return false; // don't advertise back to sender
		}
		if (!extcomm_policy_apply_to(info.route, rcvr)) {

			if (log_permit_deny && printDebug)
				System.out.println("DENY ROUTE \"" + info.route + "\" TO "
						+ rcvr.ASNum + " FROM " + ASNum);
			return false;
		}

		if (!rcvr.out_policy.apply_to(info.route)) {
			if (gem != null) {
				String message = "OutBound Route Denied:\n";
				message += "- Route Announced: " + info.route.nlri + "\n";
				message += "Policy: \n";
				message += rcvr.out_policy + "\n";
				message += "Information of route: \n";
				message += "- Next Hop: " + info.route.nexthop() + "\n";
				message += "- As-path: " + info.route.aspath() + "\n";
				if (info.route.pas != null) {
					for (int j = 0; j < info.route.pas.length; j++)
						if (info.route.pas[j] != null) {
							message += "- " + Attribute.names[j] + ": "
									+ info.route.pas[j] + "\n";
						}
				}
				gem.addClause(this.getTime(), this.bgp_id.toString(),
						rcvr.ip_addr.toString(),
						GraphicBGPEventManager.APPLIED_OUT_POLICIES, message,
						null);
			}
			if (log_permit_deny && printDebug)
				System.out.println("DENY ROUTE \"" + info.route + "\" TO "
						+ rcvr.ASNum + " FROM " + ASNum);
			return false; // policy didn't allow the route
		} else {
			if (gem != null) {
				String message = "OutBound Route permited:\n";
				message += "- Route Announced: " + info.route.nlri + "\n";
				message += "Policies: \n";
				message += rcvr.out_policy + "\n";
				message += "Information of route: \n";
				message += "- Next Hop: " + info.route.nexthop() + "\n";
				message += "- As-path: " + info.route.aspath() + "\n";
				if (info.route.pas != null) {
					for (int j = 0; j < info.route.pas.length; j++)
						if (info.route.pas[j] != null) {
							message += "- " + Attribute.names[j] + ": "
									+ info.route.pas[j] + "\n";
						}
				}
				gem.addClause(this.getTime(), this.bgp_id.toString(),
						rcvr.ip_addr.toString(),
						GraphicBGPEventManager.APPLIED_OUT_POLICIES, message,
						null);
			}
			if (log_permit_deny && printDebug)
				System.out.println("PERMIT ROUTE \"" + info.route + "\" TO "
						+ rcvr.ASNum + " FROM " + ASNum);
		}

		// - - - - - sender-side loop detection - - - - - //
		if (Global.ssld) {
			if (info.route.aspath().contains(rcvr.ASNum)) {
				// A loop would exist for our peer, so don't send it.
				return false;
			}
		}
		// Is a route annunced by me
		if (sender.bgp_id.equals(this.bgp_id) && rcvr.typ == PeerEntry.INTERNAL) {
			return true;
		}

		if (sender.typ == PeerEntry.EXTERNAL) {
			return true; // route was received externally
		}
		// XXX Anuncion de rutas internamente enable y disable
		if (sender == self && rcvr.typ == PeerEntry.INTERNAL && !reflector) {
			// it's the route to our own AS, which internal peers will already
			// know
			// Changed for true because I want send internal to other
			return false;
		}
		if (rcvr.typ == PeerEntry.EXTERNAL) {

			// we received the route internally, but the peer to send to is
			// external
			return true;
		}

		if (reflector) {
			// The route is the first time that

			if (sender.subtyp == PeerEntry.CLIENT) {
				if (!info.route.has_orig_id() || // no originator ID attribute
						!rcvr.bgp_id.equals(info.route.orig_id())) { // not
					// originator
					// The route was received internally, but this is a route
					// reflector,
					// the route was sent to us by a client, and the peer to
					// send to was
					// not the originator.

					return true;
				} else {

					// the peer to send to was the originator, don't forward
					return false;
				}
			} else if (rcvr.subtyp == PeerEntry.CLIENT) {

				if (rcvr.bgp_id == null || // no peering session yet, so not
						// originator
						!info.route.has_orig_id() || // no originator ID
						// attribute exists
						!rcvr.bgp_id.equals(info.route.orig_id())) { // not
					// originator
					// The route was received internally, but this is a route
					// reflector
					// and though it was sent to us by a reflector non-client,
					// the peer
					// to send to is a reflector client (and was not the
					// originator) so
					// it's OK.
					return true;
				} else {
					// the peer to send to was the originator, don't forward
					return false;
				}
			} else {

				// Route not being forwarded. Both the peer that sent it to us
				// and the
				// peer to send to are internal (reflector) non-clients.
				return false;
			}
		} else {
			// Route not being forwarded. It was received internally,
			// the peer is internal, and this is not a route reflector.
			return false;
		}
	}

	// ----- BGP.send ------------------------------------------------- //
	/**
	 * Generic procedure to take any kind of BGP message and push it onto the
	 * protocol below this one in the stack. If CPU delay is in use, then they
	 * are simply added to a CPU delay queue and will be sent when they reach
	 * the front of it.
	 * 
	 * @param msg
	 *            The BGP message to be sent out.
	 * @param peer
	 *            The entry for the peer to whom the message should be sent.
	 */
	public final void send(BGPMessage msg, PeerConnection peerConnection) {
		send(msg, peerConnection, -1);
	}

	// ----- BGP.send ------------------------------------------------- //
	/**
	 * Generic procedure to take any kind of BGP message and push it onto the
	 * protocol below this one in the stack. If CPU delay is in use, then they
	 * are simply added to a CPU delay queue and will be sent when they reach
	 * the front of it.
	 * 
	 * @param msg
	 *            The BGP message to be sent out.
	 * @param peer
	 *            The entry for the peer to whom the message should be sent.
	 * @param casenum
	 *            Indicates info about this send for event recording.
	 */
	public final void send(BGPMessage msg, PeerConnection peerConnection,
			int casenum) {
		double out_wait_time = outgoing_delay(msg);

		if (Global.max_proc_time == 0.0) { // not modeling CPU delay
			sendmsg(msg, peerConnection, 0.0, casenum);
		} else {

			// Even though we're modeling CPU delay, we still might want to have
			// certain messages take 0 CPU time.
			if (out_wait_time == 0.0 && cpuq.size() == 0) {
				// This message requires 0 CPU time, and there's nothing in the
				// CPU
				// delay queue, so just send this message now.
				if (casenum == -1) {
					sendmsg(msg, peerConnection, out_wait_time);
				} else {
					sendmsg(msg, peerConnection, out_wait_time, casenum);
				}
			} else { // add the message to the CPU delay queue
				Object[] outtuple;
				if (casenum == -1) {
					outtuple = new Object[3];
				} else {
					outtuple = new Object[4];
					outtuple[3] = new Integer(casenum);
				}
				outtuple[0] = new Double(out_wait_time);
				outtuple[1] = msg;
				outtuple[2] = peerConnection;

				cpuq.add(outtuple);
				if (cpuq.size() == 1) {
					if (!cpu_busy) {
						cpu_busy = true;
					}
					cputimer.set(out_wait_time);
				}
			}
		}
	}

	// ----- BGP.sendmsg ---------------------------------------------- //
	/**
	 * Does the actual pushing of a message to the protocol below this one on
	 * the protocol stack. The <code>send</code> method is just a public
	 * interface for sending messages, and if outgoing messages are being
	 * delayed with jitter, it will not actually send the message but simply add
	 * it to the queue of outgoing messages.
	 * 
	 * @param msg
	 *            The BGP message to be sent out.
	 * @param peer
	 *            The entry for the peer to whom the message should be sent.
	 */
	private final void sendmsg(BGPMessage msg, PeerConnection peerConnection,
			double processing_time) {
		sendmsg(msg, peerConnection, processing_time, -1);
	}

	private final void sendmsg(BGPMessage msg, PeerConnection peerConnection,
			double processing_time, int casenum) {
		if (peerConnection != null) {
			if (msg instanceof UpdateMessage) {
				peerConnection.incOutUpdates();
			} else if (msg instanceof OpenMessage) {
			} else if (msg instanceof KeepAliveMessage) {
			} else if (msg instanceof NotificationMessage) {
//			} else if (msg instanceof StartStopMessage) {
			}else {
				debug.err("unrecognized BGP message: " + msg);
			}

			peerConnection.send(msg);
		} else {
			if (printDebug)
				System.out.println("ERROR: peerConnection is null in sendmsg");
		}
	}

	// ----- BGP.try_send_update -------------------------------------- //
	/**
	 * Handles the sending of an update message. If for any reason it cannot be
	 * sent right away, it takes the proper actions.
	 * 
	 * @param msg
	 *            The update message to send.
	 * @param senders
	 *            The NHI addresses of the senders of each route in the update
	 *            message; this information is required if the route cannot be
	 *            advertised right away.
	 * @param peer
	 *            The peer to whom the message should be sent.
	 */
	public final void try_send_update(UpdateMessage msg,
			ArrayList<IPaddress> senders, PeerEntry peer) {
		logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
				"try-send-update {" + msg.toString() + "}");

		if (!rate_limit_by_dest && Global.continuous_mrai_timers
				&& peer.mraiTimer.isExpired()) {
			// We are rate limiting by peer, and we are using continuous MRAI
			// timers,
			// and this is the first update to be sent to this particular peer
			// (since
			// the MRAI timer is not ticking), so go ahead and randomize it
			// (acting
			// as if it had already been ticking continuously).
			debug.affirm(peer.mraiTimer.isExpired(),
					"expected MRAI timer to be unset");
			peer.mraiTimer.set(rng2.nextDouble() * (double) peer.mrai);
		}

		if (!rate_limit_by_dest && !peer.mraiTimer.isExpired()) {
			// We're doing rate limiting by peer only, and the MRAI timer is not
			// yet
			// expired so no advertisments can be sent. Any prefixes to be
			// advertised must be put in the waiting list. However, if rate
			// limiting
			// is not being applied to withdrawals, any withdrawn routes may be
			// sent.
			// Otherwise, the prefixes to be withdrawn must be put on the
			// withdrawals
			// waiting list. Before doing any of this, though we must check a
			// couple
			// of things:

			// - - - - - remove redundant withdrawals - - - - - //
			if (msg.announceRoutes != null) { // the message contains NLRI
				// First we need to check the following. If there's a prefix D
				// in both
				// the NLRI and the withdrawn routes then the new advertisement
				// will
				// suffice to serve as both the withdrawal and the new
				// advertisement.
				// (whether or not withdrawal rate limiting is being used). In
				// that
				// case, we remove the withdrawn route from the message.
				IPaddress nlri;
				if (msg.withdrawRoutes != null) {
					for (int i = 0; i < msg.announceRoutes.size(); i++) {
						// nlri = ((Route)msg.announceRoutes.get(i)).nlri;
						if (msg.removeWithdraw(msg.announceRoutes.get(i))) {
						}
					}
					if (msg.withdrawRoutes.size() == 0) {
						msg.withdrawRoutes = null;
					}
				}
			}

			// - - - - - updating waiting routes list - - - - - //
			if (msg.withdrawRoutes != null) { // the msg contains withdrawals
				// Next, make sure there were no routes with the withdrawn
				// destinations
				// in the waiting routes list. (If so, remove them.)
				for (int i = 0; i < msg.withdrawRoutes.size(); i++) {
					Pair wrtepair = (Pair) peer.waiting_adv
							.remove(msg.withdrawRoutes.get(i));
					if (wrtepair != null) {
					}
				}
			}

			// Finally, we can go ahead with putting the new prefixes in the
			// waiting
			// lists and removing them from the update message.
			if (msg.announceRoutes != null) {
				for (int i = msg.announceRoutes.size() - 1; i >= 0; i--) {
					peer.waiting_adv.put(msg.announceRoutes.get(i), new Pair(
							msg.announceRoutes.get(i), senders.get(i)));
					msg.announceRoutes.remove(i);
				}
			}

			if (Global.wrate) {
				// We're not applying rate limiting to withdrawals, so go ahead
				// and
				// stick prefixes to be withdrawn in the waiting list.
				if (msg.withdrawRoutes != null) {
					for (int i = 0; i < msg.withdrawRoutes.size(); i++) {
						Route wdrte = (Route) msg.withdrawRoutes.get(i);
						peer.waiting_wds.put(wdrte, wdrte);
					}
				}
			} else {
				// We can send the update with withdrawn routes only.

				// We may have just removed some withdrawals and/or routes from
				// the
				// message--if it's now completely empty then don't sent it!
				debug.affirm(msg.announceRoutes == null
						|| msg.announceRoutes.size() == 0,
						"unexpected non-empty NLRI in update");

				if (msg.withdrawRoutes != null) { // the message is non-empty
					send(msg, peer.connection, 0);
					reset_timer(peer.connection, TimerConstants.KEEPALIVE); // reset
					// the
					// KeepAlive
					// timer
				}
			}
			return;
		}

		if (!Global.wrate) {
			// -- -- -- -- not applying MRAI to withdrawals -- -- -- -- //

			// - - - - - remove redundant withdrawals - - - - - //
			if (msg.announceRoutes != null) { // the message contains NLRI
				// First we need to check the following. If we are advertising a
				// route
				// to destination D and also withdrawing an old route to
				// destination D,
				// then the new advertisement will suffice to serve as both the
				// withdrawal and the new advertisement (whether or not the
				// update is
				// put on the wait list). In that case, we remove the withdrawn
				// route
				// from the message.
				IPaddress nlri;
				if (msg.withdrawRoutes != null) {
					for (int i = 0; i < msg.announceRoutes.size(); i++) {
						// nlri = ((Route)msg.announceRoutes.get(i)).nlri;
						if (msg.removeWithdraw(msg.announceRoutes.get(i))) {
						}
					}
					if (msg.withdrawRoutes.size() == 0) {
						msg.withdrawRoutes = null;
					}
				}
			}

			// - - - - - updating waiting routes list - - - - - //
			if (msg.withdrawRoutes != null) { // the msg contains withdrawals
				// Make sure there were no routes with the withdrawn
				// destinations in
				// the waiting routes list. (If so, remove them.)
				for (int i = 0; i < msg.withdrawRoutes.size(); i++) {
					Pair wrtepair = (Pair) peer.waiting_adv
							.remove(msg.withdrawRoutes.get(i));
					if (wrtepair != null) {
					}
				}
			}

			// - - - - - check Minimum Route Advertisement Interval - - - - - //
			if (msg.announceRoutes != null && rate_limit_by_dest) { // the msg
				// contains
				// NLRI
				if (peer.mrai > 0) {
					// IPaddress nlri;
					for (int i = msg.announceRoutes.size() - 1; i >= 0; i--) {
						Route route = msg.announceRoutes.get(i);
						if (peer.adv_nlri.containsKey(route)) {
							// Can't send this route right now (since another
							// with the same
							// NLRI was sent to the same peer recently), so
							// remove it from
							// the update message and put it on the waiting
							// list. Note that
							// if there was already a route with the same NLRI
							// on the waiting
							// list, it will be replaced.
							peer.waiting_adv.put(route,
									new Pair<Route, IPaddress>(
											msg.announceRoutes.get(i), senders
													.get(i)));
							msg.announceRoutes.remove(i);
						}
					}
				}

				if (msg.announceRoutes.size() == 0) {
					msg.announceRoutes = null;
				}
			}

			// - - - - - send the message - - - - - //

			// We may have just removed some withdrawals and/or routes from the
			// message--if it's now completely empty then don't sent it!
			if (msg.withdrawRoutes != null || msg.announceRoutes != null) { // the
				// message
				// is
				// non-empty
				send(msg, peer.connection, 0);
				debug.valid(Global.PROPAGATION, 3, msg.getAnnounce(0));
				debug.valid(Global.ROUTE_DISTRIB, 1);

				reset_timer(peer.connection, TimerConstants.KEEPALIVE); // reset
				// the
				// KeepAlive
				// timer

				if (msg.announceRoutes != null && rate_limit_by_dest
						&& peer.mrai > 0) {
					// add routes to sent routes table
					MRAITimer tmr;
					for (int i = 0; i < msg.announceRoutes.size(); i++) {
						Route rte = (Route) msg.announceRoutes.get(i);
						peer.adv_nlri.put(rte, rte);
						tmr = new MRAITimer(this, peer.mrai, rte, peer);
						tmr.set();
						// bqu: set_timer(tmr);
						peer.mrais.put(rte, tmr);
					}
				} else if (msg.announceRoutes != null && !rate_limit_by_dest
						&& peer.mrai > 0) {
					// The two-argument version of set_timer is used instead the
					// one-argument version just in case the
					// continuous_mrai_timers
					// option is in use, in which case the previous timer could
					// have been
					// set for a fraction of the full MRAI.
					if ((peer != null) && (peer.mraiTimer != null))
						peer.mraiTimer.set(peer.mrai);
				}
			}

		} else {
			// -- -- -- -- applying MRAI to withdrawals -- -- -- -- //

			// - - - - - remove redundant withdrawals - - - - - //
			if (msg.announceRoutes != null) {
				// First we need to check the following. If we are advertising a
				// route
				// to destination D and also withdrawing an old route to
				// destination D,
				// then the new advertisement will suffice to serve as both the
				// withdrawal and the new advertisement (whether or not the
				// update is
				// put on the wait list). In that case, we remove the withdrawn
				// route
				// from the message.
				IPaddress nlri;
				if (msg.withdrawRoutes != null) {
					for (int i = 0; i < msg.announceRoutes.size(); i++) {
						// nlri = ((Route)msg.announceRoutes.get(i)).nlri;
						if (msg.removeWithdraw(msg.announceRoutes.get(i))) {
						}
					}
					if (msg.withdrawRoutes.size() == 0) {
						msg.withdrawRoutes = null;
					}
				}
			}

			// - - - - - check advertisements against MRAI - - - - - //
			if (msg.announceRoutes != null && rate_limit_by_dest) {
				if (peer.mrai > 0) {
					Route route;
					for (int i = msg.announceRoutes.size() - 1; i >= 0; i--) {
						route = (msg.announceRoutes.get(i));
						if (peer.adv_nlri.containsKey(route)) {
							// Can't send this route right now (since an
							// advertisement with
							// the same NLRI was sent to the same peer
							// recently), so remove
							// it from the update message and put it on the
							// waiting list. If
							// a withdrawal with the same NLRI is in the
							// withdrawal waiting
							// list, it must be removed. Note that if there was
							// already a
							// route with the same NLRI on the advertisement
							// waiting list, it
							// will be replaced.
							peer.waiting_adv.put(route,
									new Pair<Route, IPaddress>(
											msg.announceRoutes.get(i), senders
													.get(i)));
							msg.announceRoutes.remove(i);
							peer.waiting_wds.remove(route);
						} else if (peer.wdn_nlri.containsKey(route)) {
							// Can't send this route right now (since a
							// withdrawal with the
							// same NLRI was sent to the same peer recently), so
							// remove it
							// from the update message and put it on the waiting
							// list. If a
							// withdrawal with the same NLRI is in the
							// withdrawal waiting
							// list, it must be removed.
							peer.waiting_adv.put(route,
									new Pair<Route, IPaddress>(
											msg.announceRoutes.get(i), senders
													.get(i)));
							msg.announceRoutes.remove(i);
							peer.waiting_wds.remove(route);
						}
					}
				}

				if (msg.announceRoutes.size() == 0) {
					msg.announceRoutes = null;
				}
			}

			// - - - - - check withdrawals against MRAI - - - - - //
			if (msg.withdrawRoutes != null && rate_limit_by_dest) {
				if (peer.mrai > 0) {
					Route wdnlri;
					for (int i = msg.withdrawRoutes.size() - 1; i >= 0; i--) {
						wdnlri = (Route) msg.withdrawRoutes.get(i);
						if (peer.adv_nlri.containsKey(wdnlri)) {
							// Can't send this withdrawal right now (since an
							// advertisement
							// with the same NLRI was sent to the same peer
							// recently), so
							// remove it from the update message and put it on
							// the waiting
							// list. If an advertisement with the same NLRI is
							// in the
							// advertisement waiting list, it must be removed.
							// Note that if
							// there was already an entry with the same NLRI on
							// the
							// withdrawal waiting list, it will be replaced.
							peer.waiting_wds.put(wdnlri, wdnlri);
							msg.withdrawRoutes.remove(i);
							peer.waiting_adv.remove(wdnlri);
						} else if (peer.wdn_nlri.containsKey(wdnlri)) {
							// Can't send this withdrawal right now (since a
							// withdrawal with
							// the same NLRI was sent to the same peer
							// recently), so remove
							// it from the update message and put it on the
							// waiting list. If
							// an advertisement with the same NLRI is in the
							// advertisement
							// waiting list, it must be removed. Note that if
							// there was
							// already an entry with the same NLRI on the
							// withdrawal waiting
							// list, it will be replaced.
							peer.waiting_wds.put(wdnlri, wdnlri);
							msg.withdrawRoutes.remove(i);
							peer.waiting_adv.remove(wdnlri);
						}
					}
				}

				if (msg.withdrawRoutes.size() == 0) {
					msg.withdrawRoutes = null;
				}
			}

			// - - - - - send the message - - - - - //

			// We may have just removed some withdrawals and/or routes from the
			// message--if it's now completely empty then don't sent it!
			if (msg.withdrawRoutes != null || msg.announceRoutes != null) { // the
				// message
				// is
				// non-empty
				send(msg, peer.connection, 0);
				debug.valid(Global.PROPAGATION, 3, msg.getAnnounce(0));
				debug.valid(Global.ROUTE_DISTRIB, 1);

				reset_timer(peer.connection, TimerConstants.KEEPALIVE); // reset
				// the
				// KeepAlive
				// timer

				if (msg.announceRoutes != null && rate_limit_by_dest
						&& peer.mrai > 0) {
					// add routes to sent routes table
					MRAITimer tmr;
					for (int i = 0; i < msg.announceRoutes.size(); i++) {
						Route rte = (Route) msg.announceRoutes.get(i);
						peer.adv_nlri.put(rte, rte);
						tmr = new MRAITimer(this, peer.mrai, rte, peer);
						tmr.set();
						// bqu:set_timer(tmr);
						peer.mrais.put(rte, tmr);
					}
				}

				if (msg.withdrawRoutes != null && rate_limit_by_dest
						&& peer.mrai > 0) {
					// add withdrawn prefixes to sent withdrawn prefixes table
					MRAITimer tmr;
					for (int i = 0; i < msg.withdrawRoutes.size(); i++) {
						Route wdpref = (Route) msg.withdrawRoutes.get(i);
						peer.wdn_nlri.put(wdpref, wdpref);
						tmr = new MRAITimer(this, peer.mrai, wdpref, peer);
						tmr.set();
						// bqu: set_timer(tmr);
						peer.mrais.put(wdpref, tmr);
					}
				}

				if (!rate_limit_by_dest && peer.mrai > 0) {
					// The two-argument version of set_timer is used instead the
					// one-argument version just in case the
					// continuous_mrai_timers
					// option is in use, in which case the previous timer could
					// have been
					// set for a fraction of the full MRAI.
					peer.mraiTimer.set(peer.mrai);
				}
			}
		}
	} // end of try_send_update method

	// ----- BGP.incoming_delay --------------------------------------- //
	/**
	 * Calculates and returns the amount of time, in seconds, required for
	 * processing the given incoming BGP message.
	 * 
	 * @param message
	 *            The incoming message.
	 * @return the number of seconds required to process the message
	 */
	private double incoming_delay(BGPMessage message) {
		if (Global.max_proc_time == 0.0) {
			return 0.0; // not modeling CPU delay
		}

		// modeling CPU delay

		// For now, the minimum processing time will be imposed on incoming
		// Open,
		// Notification, and KeepAlive messages. Update messages have a random
		// time imposed (see below), and all other messages have no processing
		// time
		// imposed.
		double waittime = Global.min_proc_time;

		if (message instanceof StartStopMessage
				|| message instanceof TransportMessage
				|| message instanceof TimeoutMessage) {
			// for now, no processing time is imposed for these types of
			// messages
			waittime = 0.0;
		} else if (message instanceof UpdateMessage) {
			if (((UpdateMessage) message).treat_as_update) {
				waittime = (Global.min_proc_time + rng1.nextDouble()
						/ (1.0 / (Global.max_proc_time - Global.min_proc_time))) / 2.0;
			} else {
				waittime = 0.0; // see Global.notice_update_arrival
			}
		}

		return waittime;
	}

	// ----- BGP.outgoing_delay --------------------------------------- //
	/**
	 * Calculates and returns the amount of time, in seconds, required for
	 * processing the given outgoing BGP message.
	 * 
	 * @param message
	 *            The outgoing message.
	 * @return the number of seconds required to process the message
	 */
	private double outgoing_delay(BGPMessage msg) {
		if (Global.max_proc_time == 0.0) {
			return 0.0; // not modeling CPU delay
		}

		// modeling CPU delay

		// For now, the minimum processing time will be imposed on Open,
		// Notification, and KeepAlive messages. Update messages have a random
		// time imposed (see below), and all other messages have no processing
		// time
		// imposed.
		double waittime = Global.min_proc_time;

		if (msg instanceof StartStopMessage || msg instanceof TransportMessage
				|| msg instanceof TimeoutMessage) {
			// for now, no processing time is imposed for these types of
			// messages
			waittime = 0.0;
		} else if (msg instanceof UpdateMessage) {
			waittime = (Global.min_proc_time + rng1.nextDouble()
					/ (1.0 / (Global.max_proc_time - Global.min_proc_time))) / 2.0;
		}

		return waittime;
	}

	// ----- BGP.msg_arrival ------------------------------------------ //
	/**
	 * Prints any output appropriate to the arrival of a given message.
	 * 
	 * @param message
	 *            The message which has just arrived at this BGP speaker.
	 * @param processing_time
	 *            The amount of processing time which this incoming message will
	 *            require.
	 */
	private void msg_arrival(BGPMessage msg) {

		// Get the peer with whom this message is associated.
		PeerEntry peerEntry = (PeerEntry) peersByConnection
				.get(msg.peerConnection);
		if (peerEntry == null) {
			return;
			// debug.err("unknown neighbor: " + msg.addr/*nh*/);
		}

		// MP-BGP extension - debug prints
		// if(PeerEntry.CTCLIENT == peerEntry.subtyp)
		// {
		// System.out.println("msg_arrival(): "+toStringPeer(peerEntry));
		// System.out.println("	this.bgp_id: "+this.bgp_id);
		// }

		logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM, "msg_arrival: "
				+ msg);

		// print debug message about what type of event just arrived
		switch (msg.typ) {
		case BGPMessage.STARTSTOP:
			switch (((StartStopMessage) msg).ss_type) {
			case BGPstart:
				break;
			case BGPstop:
				break;
			default:
				debug.err("unknown BGP start/stop event type");
			}
			break;
		case BGPMessage.TRANSPORT:
			switch (((TransportMessage) msg).trans_type) {
			case TransConnOpen:
				break;
			case TransConnClose:
				break;
			case TransConnOpenFail:
				break;
			case TransFatalError:
				break;
			default:
				debug.err("unknown BGP transport event type");
			}
			break;
		case BGPMessage.TIMEOUT:
			switch (((TimeoutMessage) msg).to_type) {
			case ConnRetryTimerExp:
				break;
			case HoldTimerExp:
				if (printDebug)
					System.out.println("["+getTime()+"] hold_timer_has_expired");
				break;
			case KeepAliveTimerExp:
				break;
			default:
				debug.err("unknown BGP timeout event type");
			}
			break;
		case BGPMessage.OPEN:
			break;
		case BGPMessage.UPDATE:
			debug.valid(Global.DROP_PEER2, 2, msg);
			debug.valid(Global.RECONNECT, 3, msg);
			break;
		case BGPMessage.NOTIFICATION:
			break;
		case BGPMessage.KEEPALIVE:
			break;
		}
	}

	// ===== inner class CPUTimer =========================================== //
	/**
	 * A timer used to model CPU processing time.
	 */
	public class CPUTimer extends infonet.javasim.util.Timer {
		// A reference to the calling BGP protocol session.
		BGPSession bgp;

		// Construct a timer with the given duration.
		public CPUTimer(BGPSession bgp, double duration) {
			super(bgp.timerMaster, duration);
			this.bgp = bgp;
		}

		// ----- CPUTimer.handle_next_event ------------------------------------
		// Handles the next event in the CPU delay queue when this timer
		// expires.
		//
		/*
		 * private void handle_next_event() {
		 * 
		 * Object[] cputuple = (Object[])cpuq.remove(0);
		 * 
		 * if (cputuple.length == 1) { // A tuple of size 1 contains only the
		 * CPU time that was // charged for an event. No further action is
		 * required. } else { // The tuple contains an update message waiting to
		 * be sent. double out_wait_time = ((Double)cputuple[0]).doubleValue();
		 * BGPMessage msg = (BGPMessage)cputuple[1]; PeerEntry peer =
		 * (PeerEntry)cputuple[2]; if (cputuple.length == 4) { // Contains
		 * additional parameter used for event recording.
		 * sendmsg(msg,peer,out_wait_time,((Integer)cputuple[3]).intValue()); }
		 * else { sendmsg(msg,peer,out_wait_time); } }
		 * 
		 * }
		 */

		// ----- CPUTimer.handle_empty_queue -----------------------------------
		//
		// Handles the situation when the CPU delay queue becomes empty.
		//
		/*
		 * private void handle_empty_queue() {
		 * 
		 * if (inbuf.size() != 0) { Object[] next_intuple =
		 * (Object[])inbuf.next(); BGPMessage inmsg =
		 * (BGPMessage)next_intuple[0]; ProtocolSession fromSession =
		 * (ProtocolSession)next_intuple[1]; receive(inmsg,fromSession);
		 * 
		 * // The following while loop is required because otherwise it would be
		 * // possible for the inbuf to build up a bunch of waiting messages
		 * while // CPU delay is being enforced. When the cpuq finally becomes
		 * empty, // it will remove the next message from the inbuf and handle
		 * it. But // if that message doesn't prompt any new CPU delay, then the
		 * cpuq will // be empty and no more items in the inbuf will ever get
		 * processed. while (inbuf.size() != 0 && cpuq.size() == 0) {
		 * next_intuple = (Object[])inbuf.next(); inmsg =
		 * (BGPMessage)next_intuple[0]; fromSession =
		 * (ProtocolSession)next_intuple[1]; receive(inmsg,fromSession); }
		 * 
		 * if (inbuf.size() == 0 && cpuq.size() == 0) { }
		 * 
		 * } else { cpu_busy = false; } }
		 */

		// ----- CPUTimer.callback --------------------------------------------
		//
		// A method, to be performed when the timer expires.
		//
		public void callback() {

			/*
			 * handle_next_event();
			 * 
			 * // This code is somewhat uglier than it otherwise would be if we
			 * did not // allow zero-processing-delay events/messages. (We allow
			 * them because // there may be some trivial events which are most
			 * closely approximated // by imposing a CPU delay of 0.) if
			 * (cpuq.size() > 0) { Object[] nexttuple = (Object[])cpuq.get(0);
			 * double cpu_time = ((Double)nexttuple[0]).doubleValue(); while
			 * (cpu_time == 0.0) { handle_next_event(); if (cpuq.size() > 0) {
			 * nexttuple = (Object[])cpuq.get(0); cpu_time =
			 * ((Double)nexttuple[0]).doubleValue(); } else { cpu_time = -1.0; }
			 * } if (cpuq.size() > 0) { cputimer.set(Net.seconds(cpu_time)); }
			 * else { handle_empty_queue(); } } else { handle_empty_queue(); }
			 */
		}

	} // end inner class CPUTimer

	// ----- BGP.push ------------------------------------------------- //
	/**
	 * This process optionally imposes a processing delay for certain BGP
	 * events, then passes them on to the <code>receive</code> method to be
	 * handled. All thirteen types of events (both externally and internally
	 * generated) pass through this method in the BGP flow of control. For
	 * externally generated events, <code>push</code> is not called by the
	 * protocol directly below BGP (which is Sockets) to pass a message up, but
	 * is called by BGP methods which are reading from sockets. If the option to
	 * model processing delay is in use, this method uses a queue to delay
	 * certain events/messages accordingly. Message ordering is always preserved
	 * for all messages coming through <code>push</code>.
	 * 
	 * @param message
	 *            The incoming event/message.
	 * @param fromSession
	 *            The protocol session from which the message came.
	 * @return true if the method executed without error
	 */
	public boolean push(BGPMessage msg) {
		logDebug("Start push");
		if (!alive) { // if the BGP process is dead
			if (((BGPMessage) msg).typ == BGPMessage.RUN) {
				// The only "message" that is recognized in the dead state is a
				// "run"
				// directive.
				alive = true;

				// Add statically configured routes to the Loc-RIB.
				// (For now all that means is to add one route for our AS's
				// IP prefixes).
				ArrayList<RouteInfo> locribchanges = new ArrayList<RouteInfo>();

				for (int i = 0; i < ASPrefixList.size(); i++) {
					Route rte = new Route();
					rte.set_nlri((IPaddress) ASPrefixList.get(i));
					rte.set_origin(Origin.IGP);
					rte.set_nexthop(bgp_id);
					RouteInfo info = new RouteInfo(this, rte,
							RouteInfo.MAX_DOP, true, self);
					loc_rib.add(info);
					locribchanges.add(info);
					logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
							"route-added {nlri:" + rte.nlri.toString() + "}");

					for (Iterator<PeerEntry> it = peersByIP.values().iterator(); it
							.hasNext();) {
						PeerEntry peerEntry = (PeerEntry) it.next();
						HashMap<PeerEntry, ArrayList<?>> wds_tbl = new HashMap<PeerEntry, ArrayList<?>>();

						ArrayList<?> wds2send = new ArrayList();
						wds_tbl.put(peerEntry, wds2send);
					}

				}

				if (Global.auto_advertise) {
					// By inserting a route to this AS in the Loc-RIB and then
					// starting Phase 3 of the Decision Process, we
					// effectively cause update messages to be sent to each of
					// our peers. Note that we insert into the Loc-RIB but
					// *not* into the local router's forwarding table.
					// run Phase 3 of the Decision Process so that the changes
					// to the Loc-RIB will get propagated to the Adj-RIBs-Out.
					logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_LOW,
							"3) decision_process_3");

					decision_process_3(locribchanges, null);
				}

				listen();

				// Send a BGPstart event for each potential external peering
				// session
				// and for each potential internal peering session. This will
				// cause
				// BGP to start actively trying to connect to neighbors.
				logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
						"send-StartStopMessage-to {"
								+ peersByIP.values().size() + " peers}");

				for (Iterator<PeerEntry> it = peersByIP.values().iterator(); it
						.hasNext();) {
					PeerEntry peerEntry = (PeerEntry) it.next();

					// TODO hacer el connected
					/*
					 * else if (peerEntry.accessible == PeerEntry.CONNECTED){
					 * 
					 * }
					 */

					if (ip_addr.val() < peerEntry.ip_addr.val()) {
						logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_LOW,
								"peer {" + peerEntry.addr + "}");

						if (!peerEntry.accessible(this)) {
							logDebug(
									GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
									"Peer aren't accessible: " + peerEntry.addr);

						}

						PeerConnection peerConnection = new PeerConnection(
								peerEntry);
						logFSM("UNKNOWN", "Run", "IDLE", peerConnection);
						peersByConnection.put(peerConnection, peerEntry);
						this.connect(peerConnection,0.0);
						// (new StartConnectionTimer(timerMaster, rng3
						// .nextDouble(), peerConnection,this)).set();
					} else {
						logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_LOW,
								"peer <" + peerEntry.addr + ">");

					}
				}
			}
			return true;
		}

		msg_arrival(msg); // Report that msg has arrived at this BGP speaker.

		if (Global.max_proc_time == 0.0) { // not modeling CPU delay
			return receive(msg);
		} else { // modeling CPU delay

			if (inbuf.size() == 0 && !cpu_busy) {
				// The BGP session is currently idle, so handle this
				// message/event
				// immediately.
				return receive(msg);
			} else {
				// The BGP session is not idle, so queue this message.
				inbuf.add(msg);
			}
		}

		return true;
	}

	// ----- BGP.receive ---------------------------------------------- //
	/**
	 * This process receives and handles both externally and internally
	 * generated BGP events. If any processing delay is to be imposed, the
	 * method <code>push</code> will have been called first to manage the delay.
	 * 
	 * @param message
	 *            The incoming event/message.
	 * @param fromSession
	 *            The protocol session from which the message came.
	 * @return true if the method executes without error
	 */
	public synchronized boolean receive(BGPMessage msg) {

		byte not_error_code = 0;
		byte not_error_subcode = 0;
		String trans_str = "?";

		// ignore all (external) messages while dead
		if (!alive) {
			return true;
		}

		if (msg.typ == BGPMessage.RUN) {
			debug.warn("run directive received while running");
			return true;
		}

		if (Global.max_proc_time != 0.0) { // modeling CPU delay
			// Calculate amount of CPU time required to process this message.
			double indelay = incoming_delay(msg);
			if (indelay != 0.0) {
				Object[] qitem = { new Double(indelay) };
				cpuq.add(qitem);
				if (cpuq.size() == 1) {
					if (!cpu_busy) {
						cpu_busy = true;
					}
					cputimer.set(indelay);
				}
			}
		}

		int event_type;

		// Get the peer with whom this message is associated.
		PeerConnection peerConnection = msg.peerConnection;
		PeerEntry peerEntry = (PeerEntry) peersByConnection.get(peerConnection);
		if (peerEntry == null) {
			debug.err("unknown neighbor");
			return false;
		}

		// If it's an update message, it could just be a "notice" only (see
		// Global.notice_update_arrival). In that case handle it specially and
		// return.
		if (msg instanceof UpdateMessage
				&& ((UpdateMessage) msg).treat_as_notice) {
			if (!((UpdateMessage) msg).treat_as_update) { // it's a notice only
				// turn it into an update only for next time
				((UpdateMessage) msg).treat_as_notice = false;
				((UpdateMessage) msg).treat_as_update = true;
				reset_timer(peerConnection, TimerConstants.HOLD);
				return true;
			} // else it's a notice and an update, so just let the timer get
			// reset as
			// it is normally, down below
		}

		// This switch statement is used mainly to set the event_type parameter,
		// though it also handles a few other message-type-specific issues.
		switch (msg.typ) {
		case BGPMessage.OPEN:
			event_type = RecvOpen;
			logMsg(msg);
			// Since we don't start out with full information about each
			// peer, we need to add it as we hear from them.
			if (peerEntry.typ == PeerEntry.INTERNAL) {
				try {
					debug
							.affirm(
									(((OpenMessage) msg).ASNum == ASNum),
									"unexpected AS mismatch ("
											+ ((OpenMessage) msg).ASNum
											+ ":"
											+ ASNum
											+ ")"
											+ "(IPS: "
											+ this.bgp_id.val2str()
											+ ":"
											+ tid.utils.Utils
													.addrLongToString(((OpenMessage) msg).BGPId));
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				// peerEntry.as_nh = as_nh;
				peerEntry.ASNum = ((OpenMessage) msg).ASNum;// AS_descriptor.nh2as(peerEntry.as_nh);
			}
			// peerEntry.bgp_id= ((OpenMessage) msg).BGPId;
			// logMsg(msg);
			break;
		case BGPMessage.UPDATE:
			event_type = RecvUpdate;
			logMsg(msg);
			break;
		case BGPMessage.NOTIFICATION:
			event_type = RecvNotification;
			logMsg(msg);
			break;
		case BGPMessage.KEEPALIVE:
			event_type = RecvKeepAlive;
			logMsg(msg);
			break;
		case BGPMessage.TIMEOUT:
			event_type = ((TimeoutMessage) msg).to_type;
			break;
		case BGPMessage.TRANSPORT:
			event_type = ((TransportMessage) msg).trans_type;
			break;
		case BGPMessage.STARTSTOP:
			event_type = ((StartStopMessage) msg).ss_type;
			break;
		default:
			debug.err("illegal BGP message type");
			event_type = -1; // to avoid compiler errors
		}

		// bqu-debug
		if ((msg.typ == BGPMessage.TIMEOUT)
				&& (event_type == ConnRetryTimerExp)) {
			logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM, ">>> " + msg
					+ " <<<");

		}
		// bqu-debug

		if (msg.peerConnection.isClosed() && (msg.typ != BGPMessage.STARTSTOP)) {
			logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
					"Message received from CLOSED connection {"
							+ msg.toString() + "}");

			return false;
		}

		// switch based on the state of the BGP connection with the sender
		switch (peerConnection.getState()) {
		// - - - - - - - - - - - - - - - IDLE - - - - - - - - - - - - - - - - //
		case PeerConnectionConstants.IDLE:
			switch (event_type) {
			case BGPstart:
				// 1. initialize resources
				// 2. start ConnectRetry timer
				// 3. initiate a transport connection
				peerConnection.setSocket(null);
				peerConnection.clearWriteQueue(); // just to be safe (especially
				// for reboot kludge)
				reset_timer(peerConnection, TimerConstants.CONNRETRY);
				peerConnection.setState(PeerConnectionConstants.CONNECT);
				peerConnection.manageConnect();
				logFSM("IDLE", "BGPStart", "CONNECT", peerConnection);
				break;
			case KeepAliveTimerExp:
				debug.warn("rcvd KeepAlive Timer Expired msg while Idle");
				break;
			case HoldTimerExp:
				debug.warn("rcvd Hold Timer Expired msg while Idle");
				break;
			case TransConnOpen:
				debug.warn("rcvd Transport Connection Open msg while Idle");
				break;
			default:
				debug.msg("ignoring msg from bgp@" + peerEntry.addr
						+ " rcvd while Idle: " + event2str(event_type));
			}
			break;
		// - - - - - - - - - - - - - - - CONNECT - - - - - - - - - - - - - - -
		// //
		case PeerConnectionConstants.CONNECT:
			switch (event_type) {
			case BGPstart: // ignore
				break;
			case TransConnOpen:
				// 1. clear ConnectRetry timer
				// 2. send OPEN message

				peerEntry.cancelConnectRetryTimer();
				send(new OpenMessage(bgp_id.val(), ASNum, null,
						peerEntry.hold_timer_interval), peerConnection);
				// RFC1771 section 8 suggests setting the Hold Timer to 4
				// minutes here
				logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
						"CONNECT,TransConnOpen,set_hold_timer_4 min");
				peerConnection.holdTimer = new EventTimer(this, 240,
						HoldTimerExp, peerConnection);
				peerConnection.holdTimer.set();
				peerConnection.setState(PeerConnectionConstants.OPENSENT);
				logFSM("CONNECT", "TransConnOpen", "OPENSENT", peerConnection);
				peerConnection.manageReceive();
				break;
			case TransConnOpenFail:
				// 1. restart ConnectRetry timer

				// peerConnection.close(); // close the sockets that couldn't
				// connect
				reset_timer(peerConnection, TimerConstants.CONNRETRY);
				peerConnection.setState(PeerConnectionConstants.ACTIVE);
				logFSM("CONNECT", "TransConnOpenFail", "ACTIVE", peerConnection);
				if (auto_reconnect(peerEntry, peerConnection)) {
					this.startConnection(peerEntry.addr,0.0);
					// push(new StartStopMessage(BGPstart, peerConnection));
				}
				break;
			case ConnRetryTimerExp:
				if (printDebug)
					System.out.println("ASNum " + ASNum
							+ " tries to restart connection");

				// 1. restart ConnectRetry timer
				// 2. initiate a transport connection

				reset_timer(peerConnection, TimerConstants.CONNRETRY);
				peerConnection.manageConnect();
				/*
				 * if (peer.writeconnecting.get(peer.writesocket) == null) {
				 * peer.connect(); } // else the previous connect() is still
				 * trying
				 * 
				 * // I'm not sure that it's safe to call connect() again if the
				 * previous // call hasn't yet completed. Ideally, I'd like to
				 * abort the previous // attempt, but that can't easily be done,
				 * it seems. For example, // aborting a socket connection
				 * attempt while the underlying TCP // connection is not yet in
				 * the established state yields an error (in
				 */
				break;
			default: // for BGPstop, TransConnClosed, TransFatalError,
				// HoldTimerExp, KeepAliveTimerExp, RecvOpen,
				// RecvKeepAlive, RecvUpdate, RecvNotification
				// 1. release resources

				peerConnection.close();
				// bqu: peerEntry.cancel_timers();
				peerConnection.setState(PeerConnectionConstants.IDLE);
				logFSM("CONNECT", "IDLE", peerConnection);

				if (auto_reconnect(peerEntry, peerConnection)) {
					this.startConnection(peerEntry.addr,0.0);
					// push(new StartStopMessage(BGPstart, peerConnection));
				}
			}
			break;
		// - - - - - - - - - - - - - - - ACTIVE - - - - - - - - - - - - - - - //
		case PeerConnectionConstants.ACTIVE:
			switch (event_type) {
			case BGPstart: // ignored
				break;
			case TransConnOpen:
				// 1. complete initialization
				// 2. clear ConnectRetry timer
				// 3. send OPEN message

				peerEntry.cancelConnectRetryTimer();
				send(new OpenMessage(bgp_id.val(), ASNum, null,
						peerEntry.hold_timer_interval), peerConnection);
				// RFC1771 section 8 suggests setting the Hold Timer to 4
				// minutes here
				peerConnection.holdTimer = new EventTimer(this, 240,
						HoldTimerExp, peerConnection);
				peerConnection.holdTimer.set();
				peerConnection.setState(PeerConnectionConstants.OPENSENT);
				logFSM("ACTIVE", "TransConnOpen", "OPENSENT", peerConnection);
				break;
			case TransConnOpenFail:
				// 1. close connection
				// 2. restart ConnectRetry timer

				peerConnection.close(); // close the sockets that couldn't
				// connect
				reset_timer(peerConnection, TimerConstants.CONNRETRY);
				if (auto_reconnect(peerEntry, peerConnection)) {
					this.startConnection(peerEntry.addr,0.0);
					// push(new StartStopMessage(BGPstart, peerConnection));
				}
				break;
			case ConnRetryTimerExp:
				reset_timer(peerConnection, TimerConstants.CONNRETRY);
				peerConnection.manageConnect();
				// It is safe to call connect() again here because the previous
				// call
				// must necessarily have completed. The only two ways to get to
				// into
				// the Active state (with TransConnOpenFail or TransConnClose)
				// require
				// that the call completed.
				break;
			default: // for BGPstop, TransConnClosed, TransFatalError,
				// HoldTimerExp, KeepAliveTimerExp, RecvOpen,
				// RecvKeepAlive, RecvUpdate, RecvNotification
				// 1. release resources

				peerConnection.close();
				peerEntry.cancel_timers();
				peerConnection.setState(PeerConnectionConstants.IDLE);
				logFSM("ACTIVE", "IDLE", peerConnection);
				if (auto_reconnect(peerEntry, peerConnection)) {
					this.startConnection(peerEntry.addr,0.0);
					// push(new StartStopMessage(BGPstart, peerConnection));
				}
			}
			break;
		// - - - - - - - - - - - - - - OPENSENT - - - - - - - - - - - - - - - //
		case PeerConnectionConstants.OPENSENT:
			switch (event_type) {
			case BGPstart: // ignored
				trans_str = "BGPstart";
				break;
			case TransConnClose:
				// 1. close transport connection
				// 2. restart ConnectRetry timer
				trans_str = "TransConnClose";

				reset_timer(peerConnection, TimerConstants.CONNRETRY);
				peerConnection.setState(PeerConnectionConstants.ACTIVE);
				logFSM("OPENSENT", "TransConnClose", "ACTIVE", peerConnection);
				break;
			case TransFatalError:
				// 1. release resources

				trans_str = "TransFatalError";
				peerConnection.close();
				// peerEntry.cancel_timers();
				peerConnection.setState(PeerConnectionConstants.IDLE);
				logFSM("OPENSENT", "TransFatalError", "IDLE", peerConnection);
				if (auto_reconnect(peerEntry, peerConnection)) {
					this.startConnection(peerEntry.addr,0.0);
					// push(new StartStopMessage(BGPstart, peerConnection));
				}
				break;
			case RecvOpen:
				// 1. if process OPEN is OK
				// - send KEEPALIVE message
				// 2. if process OPEN failed (this case never happens in
				// simulation)
				// - send NOTIFICATION message

				// Collision detection
				trans_str = "RecvOpen";
				peerEntry.setConnection(peerConnection);

				peerConnection.cancelHoldTimer();
				send(new KeepAliveMessage(peerConnection), peerConnection);

				// Determine negotiated Hold Timer interval (it is the minimum
				// of the
				// value we advertised and the value that the (potential) peer
				// advertised to us.
				if (((OpenMessage) msg).holdTime < peerEntry.hold_timer_interval) {
					peerEntry.hold_timer_interval = ((OpenMessage) msg).holdTime;
				}
				logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
						"RECVOPEN: hold_interval="
								+ peerEntry.hold_timer_interval);

				// Set the Keep Alive Timer Interval for this peer based upon
				// the
				// negotiated Hold Timer Interval for this peer, preserving the
				// ratio
				// of the configured values for the two timer intervals, and
				// adding
				// jitter.
				peerEntry.keep_alive_interval = (long) (keep_alive_jitter
						* peerEntry.hold_timer_interval * peerEntry.keephold_ratio);

				reset_timer(peerConnection, TimerConstants.KEEPALIVE);
				reset_timer(peerConnection, TimerConstants.HOLD);

				if (peerEntry.hold_timer_interval > 0) {
					if (peerEntry.hold_timer_interval < 3.0) {
						// if the interval is not 0, then the minimum
						// recommended
						// value is 3
						debug.warn("non-zero Hold Timer value is < min "
								+ "recommended value of 3s (val="
								+ peerEntry.hold_timer_interval + "s)");
					}
				} else {
					debug.warn("hold timer value is 0 for peer " + peerEntry);
				}
				peerConnection.setState(PeerConnectionConstants.OPENCONFIRM);
				logFSM("OPENSENT", "RecvOpen", "OPENCONFIRM", peerConnection);

				// If process OPEN were to fail, the code below should execute.
				// peer.close();
				// peer.cancel_timers();
				// peer.connection_state = IDLE;
				// if (Global.auto_reconnect) {
				// push(new StartStopMessage(BGPstart,peer.nh),this);
				// }
				break;
			default: // for BGPstop, TransConnOpen, TransConnOpenFail,
				// ConnRetryTimerExp, HoldTimerExp, KeepAliveTimerExp,
				// RecvKeepAlive, RecvUpdate, RecvNotification
				// 1. close transport connection
				// 2. release resources
				// 3. send NOTIFICATION message
				switch (event_type) {
				case BGPstop:
					not_error_code = 6;
					trans_str = "BGPstop";
					break;
				case TransConnOpen:
					not_error_code = 5;
					trans_str = "TransConnOpen";
					break;
				case TransConnOpenFail:
					not_error_code = 5;
					trans_str = "TransConnOpenFail";
					break;
				case ConnRetryTimerExp:
					not_error_code = 5;
					trans_str = "ConnRetryTimerExp";
					break;
				case HoldTimerExp:
					not_error_code = 4;
					trans_str = "HoldTimerExp";
					break;
				case KeepAliveTimerExp:
					not_error_code = 5;
					trans_str = "KeepAliveTimerExp";
					break;
				case RecvKeepAlive:
					not_error_code = 5;
					trans_str = "RecvKeepAlive";
					break;
				case RecvUpdate:
					not_error_code = 5;
					trans_str = "RecvUpdate";
					break;
				case RecvNotification:
					not_error_code = 6;
					trans_str = "RecvNotification";
					break;
				case RecvOpen:
					not_error_code = 6;
					trans_str = "RecvOpen";
					break;
				default:
					not_error_code = 0;
					trans_str = "default";
				}
				logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
						"FSM{state=" + peerConnection.getState()
								+ ",event_type=" + event_type + ",error_code="
								+ not_error_code + "}");

				// (the two 0's in the line below should be changed to the
				// appropriate error code and subcode values, eventually ...)
				logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
						"NOTIFICATION becoz OPENSENT.default");
				send(new NotificationMessage(peerConnection, not_error_code,
						not_error_subcode), peerConnection);
				// the Hold Timer may have been set (in the Active or Connect
				// state)
				peerConnection.cancelHoldTimer();
				peerConnection.close();
				// bqu: peerConnection.cancel_timers();
				peerConnection.setState(PeerConnectionConstants.IDLE);
				logFSM("OPENSENT", trans_str, "IDLE", peerConnection);

				if (auto_reconnect(peerEntry, peerConnection)) {
					this.startConnection(peerEntry.addr,0.0);
					// push(new StartStopMessage(BGPstart, peerConnection));
				}
			}
			break;

		// - - - - - - - - - - - - - - OPENCONFIRM - - - - - - - - - - - - - -
		// //

		case PeerConnectionConstants.OPENCONFIRM:
			switch (event_type) {
			case BGPstart: // ignored
				break;
			case TransConnClose:
				trans_str = "TransConnClose";
			case TransFatalError: // (same for both cases)
				// 1. release resources

				peerConnection.close();
				// peerEntry.cancel_timers();
				peerConnection.setState(PeerConnectionConstants.IDLE);
				if (event_type == TransFatalError)
					trans_str = "TransFatalError";
				logFSM("OPENCONFIRM", trans_str, "IDLE", peerConnection);

				if (auto_reconnect(peerEntry, peerConnection)) {
					this.startConnection(peerEntry.addr,0.0);
					// push(new StartStopMessage(BGPstart, peerConnection));
				}
				break;
			case KeepAliveTimerExp:
				// 1. restart KeepAlive timer
				// 2. resend KEEPALIVE message

				reset_timer(peerConnection, TimerConstants.KEEPALIVE);
				send(new KeepAliveMessage(peerConnection), peerConnection);
				break;
			case RecvKeepAlive:
				// 1. complete initialization
				// 2. restart Hold Timer
				peerEntry.inUpdates = 0;
				peerEntry.outUpdates = 0;
				logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_LOW,
						"RECVKEEPALIVE: reset-hold-timer");
				reset_timer(peerConnection, TimerConstants.HOLD);
				peerConnection.setState(PeerConnectionConstants.ESTABLISHED);
				logFSM("OPENCONFIRM", "RecvKeepAlive", "ESTABLISHED",
						peerConnection);

				// debug.valid(Global.RECONNECT, 2, peer);
				// By running Phase 3 of the Decision Process, we advertise the
				// local
				// address space to our new peer.
				logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_LOW,
						"4) decision_process_3");
				decision_process_3(new ArrayList(), null);
				break;
			case RecvNotification:
				// 1. close transport connection
				// 2. release resources
				// 3. send NOTIFICATION message

				// (the two 0's in the line below should be changed to the
				// appropriate error code and subcode values, eventually ...)
				// send(new NotificationMessage(peerConnection, (byte) 0, (byte)
				// 0));
				peerConnection.close();
				// peerEntry.cancel_timers();
				peerConnection.setState(PeerConnectionConstants.IDLE);

				logFSM("OPENCONFIRM", "RecvNotification", "IDLE",
						peerConnection);
				if (auto_reconnect(peerEntry, peerConnection)) {
					this.startConnection(peerEntry.addr,0.0);
					// push(new StartStopMessage(BGPstart, peerConnection));
				}
				break;
			default: // for BGPstop, TransConnOpen, TransConnOpenFail,
				// ConnRetryTimerExp, HoldTimerExp, RecvUpdate, RecvOpen
				// 1. close transport connection
				// 2. release resources
				// 3. send NOTIFICATION message

				logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
						"FSM{state=" + peerConnection.getState()
								+ ",event_type=" + event_type + ",error_code="
								+ not_error_code + "}");

				switch (event_type) {
				case BGPstop:
					not_error_code = 6;
					trans_str = "BGPstop";
					break;
				case TransConnOpen:
					not_error_code = 5;
					trans_str = "TransConnOpen";
					break;
				case TransConnOpenFail:
					not_error_code = 5;
					trans_str = "TransConnOpenFail";
					break;
				case ConnRetryTimerExp:
					not_error_code = 5;
					trans_str = "ConnRetryTimerExp";
					break;
				case HoldTimerExp:
					not_error_code = 4;
					trans_str = "HoldTimerExp";
					break;
				case KeepAliveTimerExp:
					not_error_code = 5;
					trans_str = "KeepAliveTimerExp";
					break;
				case RecvKeepAlive:
					not_error_code = 5;
					trans_str = "RecvKeepAlive";
					break;
				case RecvUpdate:
					not_error_code = 5;
					trans_str = "RecvUpdate";
					break;
				case RecvNotification:
					not_error_code = 6;
					trans_str = "RecvNotification";
					break;
				case RecvOpen:
					not_error_code = 6;
					trans_str = "RecvOpen";
					break;
				default:
					not_error_code = 0;
					trans_str = "default";
				}

				// (the two 0's in the line below should be changed to the
				// appropriate error code and subcode values, eventually ...)
				logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
						"NOTIFICATION becoz OPENCONFIRM.default");
				send(
						new NotificationMessage(peerConnection, (byte) 0,
								(byte) 0), peerConnection);
				peerConnection.close();
				// peerEntry.cancel_timers();
				peerConnection.setState(PeerConnectionConstants.IDLE);
				logFSM("OPENCONFIRM", trans_str, "IDLE", peerConnection);

				if (auto_reconnect(peerEntry, peerConnection)) {
					this.startConnection(peerEntry.addr,0.0);
					// push(new StartStopMessage(BGPstart, peerConnection));
				}
			}
			break;
		// - - - - - - - - - - - - - - ESTABLISHED - - - - - - - - - - - - - -
		// //
		case PeerConnectionConstants.ESTABLISHED:
			switch (event_type) {
			case BGPstart: // ignored
				break;
			case TransConnClose:
				trans_str = "TransConnClose";
			case TransFatalError: // (same for both cases)
				// 1. release resources
				debug.msg("TransConnClose or TransFatalError occurred");
				// peerEntry.cancel_timers();
				peerEntry.connected = false;
				peerConnection.setState(PeerConnectionConstants.IDLE);
				if (event_type == TransFatalError)
					trans_str = "TransFatalError";
				logFSM("ESTABLISHED", trans_str, "IDLE", peerConnection);
				remove_all_routes(peerEntry);
				peerConnection.close();

				if (auto_reconnect(peerEntry, peerConnection)) {
					this.startConnection(peerEntry.addr,0.0);
					// push(new StartStopMessage(BGPstart, peerConnection));
				}
				break;
			case KeepAliveTimerExp:
				// 1. restart KeepAlive timer
				// 2. send KEEPALIVE message
				reset_timer(peerConnection, TimerConstants.KEEPALIVE);
				send(new KeepAliveMessage(peerConnection), peerConnection);
				break;
			case RecvKeepAlive:
				// 1. restart Hold Timer
				debug.valid(Global.KEEP_PEER, 1);
				reset_timer(peerConnection, TimerConstants.HOLD);
				break;
			case RecvUpdate:
				debug.valid(Global.ROUTE_DISTRIB, 2);
				debug.valid(Global.PROPAGATION, 2, msg);
				debug.valid(Global.SELECT, 2, msg);
				debug.valid(Global.AGGREGATION, 2);
				debug.valid(Global.IBGP, 1, msg);
				debug.valid(Global.REFLECTION, 2, ((UpdateMessage) msg)
						.getAnnounce(0));
				debug.valid(Global.LOOPBACK, 1, ((UpdateMessage) msg)
						.getAnnounce(0));

				// 1. if process UPDATE is OK
				// ???
				// 2. if process UPDATE failed
				// send NOTIFICATION message
				// peer.close();
				// peer.cancel_timers();
				// peer.connected = false;
				// peer.connection_state = IDLE;
				// remove_all_routes(peer);
				// if (Global.auto_reconnect) {
				// push(new StartStopMessage(BGPstart,peer.nh),this);
				// }

				// 1. restart Hold Timer
				if (((UpdateMessage) msg).treat_as_notice) {
					reset_timer(peerConnection, TimerConstants.HOLD);
				}

				debug.valid(Global.DROP_PEER, 1, new Double(
						peerEntry.keep_alive_interval));

				handle_update((UpdateMessage) msg);
				break;
			case RecvNotification:
				// 1. close transport connection,
				// 2. release resources

				debug.valid(Global.DROP_PEER, 3);
				peerConnection.close();
				// peerEntry.cancel_timers();
				peerEntry.connected = false;
				peerConnection.setState(PeerConnectionConstants.IDLE);
				logFSM("ESTABLISHED", "RecvNotification", "IDLE",
						peerConnection);
				remove_all_routes(peerEntry);
				if (printDebug)
					System.out.println("WARNING: Notification received");
				if (auto_reconnect(peerEntry, peerConnection)) {
					if (printDebug)
						System.out.println("WARNING: Automatic reconnection...");
					this.startConnection(peerEntry.addr,0.0);
					// push(new StartStopMessage(BGPstart, peerConnection));
				}
				break;
			case BGPstop:
				trans_str = "BGPstop";
			case TransConnOpen:
				// these 'ifs' are because there are no breaks at the end of
				// each case
				if (event_type == TransConnOpen) {
					trans_str = "TransConnOpen";
				}
			case TransConnOpenFail:
				if (event_type == TransConnOpenFail) {
					trans_str = "TransConnOpenFail";
				}
			case ConnRetryTimerExp:
				if (event_type == ConnRetryTimerExp) {
					trans_str = "ConnRetryTimerExp";
				}
			case HoldTimerExp:
				if (event_type == HoldTimerExp) {
					not_error_code = 4; // Hold Timer Exipred
					trans_str = "HoldTimerExp";
					debug.valid(Global.DROP_PEER, 2);
				}
			case RecvOpen:
				if (event_type == RecvOpen) {
					trans_str = "RecvOpen";
				}
			default: // for BGPstop, TransConnOpen, TransConnOpenFail,
				// ConnRetryTimerExp, HoldTimerExp, RecvOpen
				// 1. send NOTIFICATION message
				// 2. close transport connection
				// 3. release resources

				logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
						"FSM{state=" + peerConnection.getState()
								+ ",event_type=" + event_type + ",error_code="
								+ not_error_code + "}");

				// (the two 0's in the line below should be changed to the
				// appropriate error code and subcode values, eventually ...)
				logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
						"NOTIFICATION becoz ESTABLISHED.default");
				send(new NotificationMessage(peerConnection, not_error_code,
						not_error_subcode), peerConnection);
				debug.valid(Global.DROP_PEER2, 1);
				debug.valid(Global.RECONNECT, 1);
				peerConnection.close();
				peerEntry.connected = false;
				peerConnection.setState(PeerConnectionConstants.IDLE);
				logFSM("ESTABLISHED", trans_str, "IDLE", peerConnection);

				remove_all_routes(peerEntry);

				if (auto_reconnect(peerEntry, peerConnection)) {
					this.startConnection(peerEntry.addr,0.0);
					// push(new StartStopMessage(BGPstart, peerConnection));
				}
			}
			break;
		default:
			debug.err("unrecognized BGP state:" + peerConnection.getState());
		}

		return true;

	} // end of receive()

	// ----- BGP.die -------------------------------------------------- //
	/**
	 * Kills the BGP process. All BGP activity stops.
	 */
	public void die() {
		debug.affirm(alive, "die() called while dead");
		alive = false;
		if (printDebug)
			System.out.println(bgp_id.toString() + " is going down:");
		(new DieTimer(timerMaster)).set();
	}

	// ----- BGP.dieThread ------------------------------------------- //
	/**
     *
     */
	public void dieThread() {

		// Technically, we shouldn't be able to call close() on the listening
		// socket, since it is (almost certainly) in the middle of a blocking
		// accept() call (see BGP.listen() method). Calling it will cause
		// an error which results in a failed accept() call and the socket being
		// closed, so that's good enough for this hack.
		try {
			if (printDebug)
				System.out.println(" * waking up listening socket ...");
			// hack so that 'active' is tested
			//
			InetSocket dummySocket = socketMaster.newSocket();
			socketMaster.bind(dummySocket, bgp_id.val(), 0);
			socketMaster.connect(dummySocket, bgp_id.val(), PORT_NUM);
			socketMaster.close(dummySocket);
			//
			if (printDebug)
				System.out.println(" * closing listening socket ...");
			socketMaster.close(listenSocket);
		} catch (IOException e) {
			debug.err("problem closing listen socket");
			e.printStackTrace();
		}
		if (printDebug)
			System.out.println("   done.");
		listenSocket = null;
		if (printDebug)
			System.out.println(" * closing opened connections ...");
		for (Iterator<PeerEntry> it = peersByIP.values().iterator(); it
				.hasNext();) {
			PeerEntry peer = (PeerEntry) it.next();

			// This is a hack. No Notification message should be sent when the
			// BGP
			// process dies, nor does the write queue need to be cleared here,
			// because it is cleared when peer.close() is called. (How could
			// it?)
			// We do it here for the purpose of one of our simulations. It will
			// be
			// removed when ready for general purpose use. Also, note that it is
			// conveniently sent *after* the write message queue has been
			// cleared, so
			// that it is guaranteed to be sent out without delay.
			if (peer.connected) {
				peer.connection.writeQueue.clear();
				// Cease Notification
				logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
						"NOTIFICATION becoz DIE");
				send(new NotificationMessage(peer.connection, (byte) 6,
						(byte) 0), peer.connection);
			}
			peer.connection.close();
			peer.cancel_timers();
			peer.connected = false;
			peer.connection.setState(PeerConnection.IDLE);
			// empty per-peer routing tables
			peer.rib_in.remove_all();
			peer.rib_out.remove_all();
		}
		
		if (printDebug){
			System.out.println("   done.");
			System.out.println(" * clear Loc-RIB ...");
		}
		loc_rib.remove_all();
		if (printDebug)
			System.out.println("   done.");
	}

	// ----- BGP.restart ---------------------------------------------- //
	/**
	 * Restarts the BGP process. Ideally, all state should be reset to its
	 * initial state. However, the current implementation is kludgy and not all
	 * state is lost when BGP dies.
	 */
	/*
	 * public void restart() { debug.affirm(!alive,
	 * "restart() called while alive"); push(new BGPMessage(BGPMessage.RUN,
	 * bgp_id.val())); }
	 */

	// ===== inner class WrapupThread ========================================
	// //
	/**
	 * A thread which is to be run at the end of the simulation to perform any
	 * desired wrap-up functions.
	 */
	/*
	 * private class WrapupThread implements Runnable {
	 * 
	 * public void run() { Iterator it; PeerEntry peer;
	 * System.out.println(StringManip.repeat('.',72+nh.length()));
	 * System.out.println(StringManip.repeat('.',27) + "   bgp@" + nh +
	 * " wrap-up   " + StringManip.repeat('.',27));
	 * System.out.println(StringManip.repeat('.',72+nh.length())); // ----- dump
	 * the Adj-RIBs-In ----- for (it=nbs.values().iterator(); it.hasNext();) {
	 * peer = (PeerEntry)it.next(); } // ----- dump the Loc-RIB ----- // -----
	 * dump the Adj-RIBs-Out ----- for (it=nbs.values().iterator();
	 * it.hasNext();) { peer = (PeerEntry)it.next(); } // ----- dump the
	 * forwarding table ----- // ----- show final stability state -----
	 * 
	 * debug.valid(Global.GOODGADGET, 1); }
	 * 
	 * } // end of inner class WrapupThread
	 */

	// ----- BGPSession.toString ------------------------------------------- //
	/**
     *
     */
	public String toString() {
		if (bgp_id == null)
			return "Bgp not iniciated";
		return "bgp-id=" + bgp_id.val();
	}

	// ///////////////////////////////////////////////////////////////
	// The following method processes the component's events:
	// * socket events
	// * timer events
	// * other events (?)
	// ///////////////////////////////////////////////////////////////

	/**
	 * This method processes incoming events
	 */
	protected void process(Object data, Port inPort) {
		// Process timer events
		if (timerMaster.processTimer(data, inPort))
			return;
		// Process socket-master events
		if (((data instanceof SocketContract.Message) || (data instanceof ByteStreamContract.Message))
				&& (socketMaster.processSocket(data, inPort)))
			return;

		// Process un-handled events
		super.process(data, inPort);
	}

	// ///////////////////////////////////////////////////////////////
	// The following inner classes are used to trigger asynchronous
	// events...
	// ///////////////////////////////////////////////////////////////

	// ----- inner class StartupTimer ---------------------------- //
	/**
	 * A timer used to apply a waiting period at startup before the BGP process
	 * becomes active (is run).
	 */
	private class StartupTimer extends infonet.javasim.util.Timer {
		public StartupTimer(TimerMaster timerMaster, double duration) {
			super(timerMaster, duration);
		}

		// A method to be performed when the timer expires.
		// It essentially starts the BGP process running.
		public void callback() {
			push(new BGPMessage(BGPMessage.RUN, null));
		}
	} // end inner class StartupTimer

	// ----- inner class StartConnectionTimer -------------------- //
	/**
	 * A timer used to start connection establishment at different moment in
	 * time (to avoid simultaneous events in the simulator).
	 */
	private class StartConnectionTimer extends infonet.javasim.util.Timer {
		private PeerConnection peerConnection;
		private BGPSession owner;

		public StartConnectionTimer(TimerMaster timerMaster, double duration,
				PeerConnection peerConnection, BGPSession owner) {
			super(timerMaster, duration);
			this.owner = owner;
			this.peerConnection = peerConnection;
		}

		public void callback() {
			connect(peerConnection,0.0);
//			if (peersByConnection.get(peerConnection).accessible(owner))
//				push(new StartStopMessage(BGPstart, peerConnection));
//			else
//				(new StartConnectionTimer(timerMaster, rng4.nextDouble(),
//						peerConnection,owner)).set();
		}
	} // end inner class StartConnectionTimer

	// ----- inner class PeerSocketTimer ------------------------- //
	/**
     *
     */
	private class PeerSocketTimer extends infonet.javasim.util.Timer {
		InetSocket peerSocket;

		public PeerSocketTimer(TimerMaster master, InetSocket peerSocket) {
			super(master, 0);
			this.peerSocket = peerSocket;
		}

		public void callback() {
			//			peerConnectThread(peerSocket);
			// Check that the peer that opened the connection is an
			// authorized neighbor. If it is not the case, close the
			// connection.
			PeerEntry peerEntry = (PeerEntry) peersByIP.get(new Long(peerSocket
					.getRemoteAddress()));
			if (peerEntry == null) {
				if (printDebug)
					System.out.println("Warning: an unknown peer ("
							+ peerSocket.getRemoteAddress() + ") tries to connect !!!");
				throw new Error("Error: an unknown peer (" + peerSocket.toString()
						+ ") tries to connect !!!");
				// Close the connecting socket.

			}
			// System.out.println("Succesful peer connection: "+peerSocket.toString());

			// Check that the peer concerned by the new connection is not
			// already in the ESTABLISHED state. If it is the case, the
			// new connection is immediately closed.
			if (peerEntry.isConnectionEstablished()) {
				if (Enviroment.debugFlag){
					if (printDebug)
						System.out.println(getTime() + ": Warning ("+bgp_id.val()+"): peer ("
								+ peerSocket.getRemoteAddress()
								+ ") tries to open a new connection while"
								+ " already in the ESTABLISHED state !!!");
					
					//				return;
//					throw new Error(
//							"Error: peer ("
//							+ peerSocket.toString()
//							+ ") tries to open a new connection while already connected !");
				}
//				PeerConnection peerConnection = peerEntry.connection; 
//				peerEntry.connection.setState(PeerConnectionConstants.IDLE);
//				disconnectPeer(peerEntry, (byte)0, (byte)0);
//				if (auto_reconnect(peerEntry, peerConnection )) {
//					if (printDebug)
//						System.out.println("WARNING: Automatic reconnection...");
//					startConnection(peerEntry.addr, true);
//				}

			}else{

				// Create a peer connection object with an existing, connected
				// socket ...
				PeerConnection peerConnection = new PeerConnection(peerEntry,
						peerSocket);
				peersByConnection.put(peerConnection, peerEntry);
				// Fire TransConnOpen event ...
				peerConnection.manageConnect();
			}
		}

	} // end inner class PeerSocketTimer

	// ----- inner class DieTimer -------------------------------- //
	/**
     *
     */
	private class DieTimer extends infonet.javasim.util.Timer {
		public DieTimer(TimerMaster master) {
			super(master, 0);
		}

		public void callback() {
			dieThread();
		}
	}

	/**
	 * This class ejecute the routines of the BGPSession
	 * 
	 * @author Francisco Huertas
	 * 
	 */
	private class EventsTimer extends infonet.javasim.util.Timer {
		private BGPSession bgp;
		TimerMaster timerMaster;

		public EventsTimer(TimerMaster timerMaster, BGPSession bgp) {
			super(timerMaster, rng4.nextDouble());
			this.timerMaster = timerMaster;
			this.bgp = bgp;
		}

		// A method to be performed when the timer expires.
		// It essentially starts the BGP process running.
		public void callback() {

			// RoutesPendingToAddRoutine();

			checkNeighborAccessibility();
			new EventsTimer(this.timerMaster, bgp).set();
		}

		/**
		 * Check if a route pending to add can be added
		 */
		/*
		 * private void RoutesPendingToAddRoutine(){ if
		 * (this.bgp.pendingToAdd.size() > 0){ Vector<RouteInfo> oldRoutes =
		 * this.bgp.pendingToAdd; this.bgp.pendingToAdd = new
		 * Vector<RouteInfo>(); for (int i = 0; i < oldRoutes.size();i++){
		 * this.bgp.ftadd(oldRoutes.get(i)); } } }
		 */
		private void checkNeighborAccessibility() {
			for (int i = 0; i < allPeers.size(); i++) {
				PeerEntry peer = allPeers.get(i);
				RTEntry entry = retrieveBestRTEntryDest(peer.addr);
				boolean isAccessible = ((Node) this.bgp.getParent())
						.isConnectTo(peer.addr);
				// if (peer.accessible(this)) {
				// if ((entry == null) || !isAccessible) {
				// peer.accessible = false;
				// logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,"EventsTimer: Connection with "
				// + peer.addr
				// + " has been lost.");
				// // TODO comprobar que no mete la pata
				// // if (bgp.alive)
				// // // XXX no sabemos si funciona
				// // push(new StartStopMessage(BGPstop, peer.connection));
				// }
				// } else {
				// if ((entry != null) && isAccessible) {
				// peer.accessible = true;
				// if (bgp.alive && auto_reconnect(peer,peer.connection) &&
				// (peer.connection.getState() == PeerConnection.CONNECT)) {
				// // push(new StartStopMessage(BGPstart, peer.connection));
				// }
				// }
				// }
			}
			// TODO Auto-generated method stub
			// System.out.println("caca de vaca");

		}
	} // end inner class StartupTimer

	// ----- BGPSession.logMsg ----------------------------------- //
	/**
     *
     */
	public void logMsg(BGPMessage msg) {
		String message = null;
		String origin = msg.peerConnection.peerEntry.ip_addr.toString();
		String destiny = this.ip_addr.toString();
		String as_origin = "" + msg.peerConnection.peerEntry.ASNum;
		String as_destinity = "" + this.ASNum;
		String stringType = BGPMessageConstants.typeNames[msg.typ];
		int type = GraphicBGPEventManager.MESSAGE_TRAFIC_UNKNOW;
		if (msg instanceof KeepAliveMessage) {
			type = GraphicBGPEventManager.MESSAGE_TRAFIC_KAI;
			KeepAliveMessage msg2 = (KeepAliveMessage) msg;
			message = "Type: " + stringType + "\n" + "- Source: address="
					+ origin + ", as=" + as_origin + "\n"
					+ "- Destiny: address=" + destiny + ", as=" + as_destinity;

		} else if (msg instanceof NotificationMessage) {
			type = GraphicBGPEventManager.MESSAGE_TRAFIC_NOTIFICATION;
			NotificationMessage msg2 = (NotificationMessage) msg;
			message = "Type: "
					+ stringType
					+ "\n"
					+ "- Source: address="
					+ origin
					+ ", as="
					+ as_origin
					+ "\n"
					+ "- Destiny: address="
					+ destiny
					+ ", as="
					+ as_destinity
					+ "\n"
					+ "- Message Code : "
					+ msg2.error_code
					+ ", Subcode: "
					+ msg2.error_subcode
					+ "\n"
					+ "- Description: "
					+ NotificationMessage.codeToString(msg2.error_code,
							msg2.error_subcode);

			// msg2.
		} else if (msg instanceof OpenMessage) {
			type = GraphicBGPEventManager.MESSAGE_TRAFIC_OPEN;
			message = "Type: " + stringType + "\n" + "- Source: address="
					+ origin + ", as=" + as_origin + "\n"
					+ "- Destiny: address=" + destiny + ", as=" + as_destinity;
		} else if (msg instanceof StartStopMessage) {
			type = GraphicBGPEventManager.MESSAGE_TRAFIC_STAR_STOP;
			StartStopMessage msg2 = (StartStopMessage) msg;
			message = "Type: " + stringType + "\n" + "- Source: address="
					+ origin + ", as=" + as_origin + "\n"
					+ "- Destiny: address=" + destiny + ", as=" + as_destinity
					+ "\n" + "- Subtype: " + msg2.ss_type;

		} else if (msg instanceof TransportMessage) {
			type = GraphicBGPEventManager.MESSAGE_TRAFIC_TRANSPORT;
			TransportMessage msg2 = (TransportMessage) msg;
			message = "Type: " + stringType + "\n" + "- Source: address="
					+ origin + ", as=" + as_origin + "\n"
					+ "- Destiny: address=" + destiny + ", as=" + as_destinity
					+ "\n" + "- Subtype: " + msg2.trans_type;

		} else if (msg instanceof UpdateMessage) {
			type = GraphicBGPEventManager.MESSAGE_TRAFIC_UPDATE;
			UpdateMessage msg2 = (UpdateMessage) msg;
			message = "Type: " + stringType + "\n" + "- Source: address="
					+ origin + ", as=" + as_origin + "\n"
					+ "- Destiny: address=" + destiny + ", as=" + as_destinity
					+ "\n";

			if (msg2.announceRoutes != null && msg2.announceRoutes.size() > 0) {
				message += "- Anounced routes\n";
				for (int i = 0; i < msg2.announceRoutes.size(); i++) {
					message += "   + " + msg2.announceRoutes.get(i).toString();
				}
			}
			if (msg2.withdrawRoutes != null && msg2.withdrawRoutes.size() > 0) {
				message += "- Anounced routes\n";
				for (int i = 0; i < msg2.withdrawRoutes.size(); i++) {
					message += "   + " + msg2.withdrawRoutes.get(i).toString();
				}
			}

		} else {
			message = "Type: " + stringType + "\n" + "- Source: address="
					+ origin + ", as=" + as_origin + "\n"
					+ "- Destiny: address=" + destiny + ", as=" + as_destinity;
		}
		if (Global.logMsgEnable) {
			logMsgPort.exportEvent("msg", toString() + "," + msg.toString(),
					"msg-received");
		}
		// TODO hacer el tema de estado
		if (gem != null) {
			gem.addClause(this.getTime(), origin, destiny, type, message, null);
		}
	}

	// ----- BGPSession.logFSM ----------------------------------- //
	/**
     *
     */
	public void logFSM(String fromState, String toState,
			PeerConnection peerConnection) {
		String message = null;
		String origin = this.ip_addr.toString();
		String destiny = peerConnection.peerEntry.ip_addr.toString();
		message = "Change state of the the peer\n" + "- Peer=" + destiny
				+ "- Previous state=" + fromState + "\n" + "- To state="
				+ toState;

		if (Global.logFSMEnable) {
			logFSMPort.exportEvent("fsm", toString() + ",peer="
					+ peerConnection.toString() + "," + fromState + "=(?)=>"
					+ toState, "fsm-change-state");
		}
		// TODO hacer el estado
		if (gem != null) {

			gem.addClause(this.getTime(), origin, destiny,
					GraphicBGPEventManager.FSM_MESSAGE, message, null);
		}
	}

	// ----- BGPSession.logFSM ----------------------------------- //
	/**
     *
     */
	public void logFSM(String fromState, String transition, String toState,
			PeerConnection peerConnection) {
		String message = null;
		String origin = this.ip_addr.toString();
		String destiny = peerConnection.peerEntry.ip_addr.toString();
		message = "Change state of the the peer\n" + "- Peer=" + destiny + "\n"
				+ "- Previous state=" + fromState + "\n" + "- To state="
				+ toState + "\n" + "- Transition=" + transition;
		if (Global.logFSMEnable) {
			logFSMPort.exportEvent("fsm", toString() + ",peer="
					+ peerConnection.toString() + "," + fromState + "=("
					+ transition + ")=>" + toState, "fsm-change-state");
		}
		if (gem != null) {
			gem.addClause(this.getTime(), origin, destiny,
					GraphicBGPEventManager.FSM_MESSAGE, message, null);
		}
	}

	// ----- BGPSession.logDebug --------------------------------- //
	/**
     *
     */
	public void logDebug(String msg) {
		if (Global.logDebugEnable) {
			logDebugPort.exportEvent("dbg", toString() + "," + msg, "debug");
		}

	}

	public void logDebug(int level, String msg) {
		if (gem != null)
			gem.addClause(this.getTime(), this.ip_addr.toString(), null, level,
					msg, null);
		logDebug(msg);

	}

	// ----- BGPSession.logRT ------------------------------------ //
	/**
	 * @param ri
	 *            route information
	 * @param op
	 *            operation (add / remove)
	 */
	public void logRT(RouteInfo ri, String op) {
		if (Global.logRTEnable) {
			logRTPort.exportEvent("rt", toString() + ",op=" + op + ","
					+ ri.route.nlri + "," + ri.route.nexthop() + ","
					+ ri.route.aspath(), "route-change");
		}
		if (gem != null) {
			String message = "Operation On Route Table: " + op + "\n"
					+ "- Route to: " + ri.route.nlri + "\n" + "- Next Hop: "
					+ ri.route.nexthop() + "\n" + "- As-path: "
					+ ri.route.aspath() + "\n";
			if (ri.route.pas != null) {
				for (int i = 0; i < ri.route.pas.length; i++)
					if (ri.route.pas[i] != null) {
						message += "- " + Attribute.names[i] + ": "
								+ ri.route.pas[i] + "\n";
					}
			}
			gem.addClause(this.getTime(), this.bgp_id.toString(), null,
					GraphicBGPEventManager.RT_MESSAGE, message, null);
		}
	}

	// ///////////////////////////////////////////////////////////////
	// The following methods implement the NonblockingSocketHandler
	// interface.
	// ///////////////////////////////////////////////////////////////

	// ----- BGPSession.acceptFinished --------------------------- //
	public void acceptFinished(InetSocket serverSocket, InetSocket clientSocket) {
 		if (alive) {
			try {
				socketMaster.aAccept(listenSocket, this);
			} catch (IOException e) {
				throw new Error("Error: acceptFinished - " + e.getMessage());
			}
		}
			(new PeerSocketTimer(timerMaster, clientSocket)).set();
	}

	// ----- BGPSession.connectFinished -------------------------- //
	public void connectFinished(InetSocket socket) {
	}

	// ----- BGPSession.closeFinished ---------------------------- //
	public void closeFinished(InetSocket socket) {
		if (printDebug)
			System.out.println("socket-close-finished");
	}

	// ----- BGPSession.error ------------------------------------ //
	public void error(InetSocket socket, IOException error) {
		throw new Error("Error: socket-error \"" + error.getMessage() + "\"");
	}

	// ///////////////////////////////////////////////////////////////
	// The following methods can be used during the simulation to
	// generate various events, configuration changes or to grab
	// information on the router's state.
	// ///////////////////////////////////////////////////////////////

	// --- TEST lsw
	public void dumpRIBin() {
		Object[] keys = ribs_in.keySet().toArray();

		for (int i = 0; i < keys.length; ++i) {
			PeerEntry current_key = (PeerEntry) keys[i];
			if (printDebug){
				System.out.println("Peer information: addr=" + current_key.addr
						+ ", return_ip=" + current_key.return_ip.toString()
						+ " ip_addr=" + current_key.ip_addr.toString() + "\n");
				System.out.println((ribs_in.get(current_key)).toString());
				System.out.println("\n");
			}
		}/*
		 * System.out.println("dump-RIB-in("+bgp_id.toString()+") {");
		 * System.out.println(ribs_in.toString()); System.out.println("}");
		 */
	}

	// ----- BGPSession.dumpLocRIB ----- //
	/**
     *
     */
	public void dumpLocRIB() {
		if (printDebug){
			System.out.println("dump-Loc-RIB (" + bgp_id.toString() + ") {");
			System.out.println(loc_rib.toString());
			System.out.println("}");
		}
	}

	public void dumpLocRIBtoFile(String fileName) {
		try {
			java.io.RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
			if (raf.length() > 0)
				raf.seek(raf.length());

			String toWrite = "dump-loc-RIB (" + bgp_id.toString() + ") {\n";
			raf.writeBytes(toWrite);
			toWrite = loc_rib.toString() + "\n}\n";
			raf.writeBytes(toWrite);
			raf.close();
		} catch (Exception ex) {
			System.out.println("[dumpLocRIBtoFile]:");
			ex.printStackTrace();
		}
	}

	// ----- BGPSession.dumpAdjRIBIn ----- //
	/**
     *
     */
	public void dumpAdjRIBIn(long addr) {
		PeerEntry peer = (PeerEntry) peersByIP.get(new Long(addr));
		if (peer == null)
			throw new Error("ERROR: peer " + ip_addr + " not found !");

		if (printDebug){
			System.out.println("dump-Adj-RIB-In (" + bgp_id.toString() + ", "
					+ addr + ") {");
			System.out.println(peer.rib_in.toString());
			System.out.println("}");
		}
	}

	// ----- BGPSession.dumpAdjRIBOut ----- //
	/**
     *
     */
	public void dumpAdjRIBOut(long addr) {
		PeerEntry peer = (PeerEntry) peersByIP.get(new Long(addr));
		if (peer == null)
			throw new Error("ERROR: peer " + ip_addr + " not found !");
		if (printDebug){
			System.out.println("dump-Adj-RIB-Out (" + bgp_id.toString() + ", "
					+ addr + ") {");
			System.out.println(peer.rib_out.toString());
			System.out.println("}");
		}
	}

	// ----- BGPSession.dumpRT ----- //
	/**
     *
     */
	public void dumpRT() {
		if (printDebug)
			System.out.println("dump-RT (" + bgp_id.toString() + ") {");
		RTEntry[] rt_entries = retrieveAllRTEntries();
		if (rt_entries != null) {
			for (int i = 0; i < rt_entries.length; i++) {
				if (printDebug)
					System.out.println("  (" + i + ") " + rt_entries[i].toString());
			}
		} else
			if (printDebug)
				System.out.println("(null)");
		if (printDebug)
			System.out.println("}");
	}

	public void dumpRTtoFile(String fileName) {
		try {
			java.io.RandomAccessFile log = new RandomAccessFile(fileName, "rw");
			if (log.length() > 0)
				log.seek(log.length());

			log.writeBytes("dump-RT (" + bgp_id.toString() + ") {");
			RTEntry[] rt_entries = retrieveAllRTEntries();
			if (rt_entries != null) {
				for (int i = 0; i < rt_entries.length; i++) {
					log.writeBytes("  (" + i + ") " + rt_entries[i].toString()
							+ '\n');
				}
			} else
				log.writeBytes("(null)\n");
			log.writeBytes("}\n");

		} catch (Exception ex) {
			System.out.println("Error during dumpRTtoFile\n");
			ex.printStackTrace();
		}

	}

	// ----- BGPSession.shutdownPeering ----- //
	/*
	 * Shutdown a peering session. The peer is identified by its IP address. A
	 * type of shutdown must be specified (PEER_SHUTDOWN_NOTIFY,
	 * PEER_SHUTDOWN_BREAK_TCP).
	 */
	public void shutdownPeering(long addr, int type) {
		/*
		 * // Find the connection... PeerEntry peer= (PeerEntry) peersByIP(new
		 * IPAddress(addr)); if (peer == null) throw new
		 * Error("Error: Unable to find peer "+ (new IPAddress(addr))+"!");
		 * 
		 * peer.shutdownConnection(type);
		 */
	}

	// ----- BGPSession.updatePeerFilters ----- //
	/**
	 * Update the filters of one peer. The peer is identified
	 */
	public void updatePeerFilters(long addr, Rule inFilter, Rule outFilter) {
		// XXX: to be continued...
		// shutdownPeering(addr, PEER_SHUTDOWN_NOTIFY);
		setPeerInFilter(addr, inFilter);
		setPeerOutFilter(addr, outFilter);
	}

	// ----- BGPSession.updatePeerOutFilter ----- //
	/**
	 * Update the output filter of one peer.
	 */
	public void updatePeerOutFilter(long addr, Rule outFilter) {

		setPeerOutFilter(addr, outFilter);
	}

	// ----- BGPSession.refreshSession ------------------------------
	/**
     *
     */
	synchronized public void refreshSession(long addr) {
		PeerEntry peer = (PeerEntry) peersByIP.get(new Long(addr));
		if (peer == null)
			throw new Error("ERROR: peer " + ip_addr + " not found !");

		ArrayList locribchanges = peer.rib_out.get_all_routes();

		peer.rib_out.remove_all();
		logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_LOW,
				"4) decision_process_3");
		decision_process_3(locribchanges, peer);
	}

	// ----- BGPSession.restartSession ------------------------------
	/**
	 * Restart the session with the peer which has address 'addr'.
	 */
	public void restartSession(long addr) {
		PeerEntry peer = (PeerEntry) peersByIP.get(new Long(addr));
		if (peer == null)
			throw new Error("ERROR: peer " + ip_addr + " not found !");

		/*
		 * peer.rib_out.remove_all();
		 */

		peer.shutdownConnection(PEER_SHUTDOWN_NOTIFY);

		/*
		 * ArrayList locribchanges= new ArrayList(); for (int i= 0; i <
		 * ASPrefixList.size(); i++) { Route rte = new Route();
		 * rte.set_nlri((IPaddress) ASPrefixList.get(i));
		 * rte.set_origin(Origin.IGP); rte.set_nexthop(bgp_id); RouteInfo info=
		 * new RouteInfo(this,rte, RouteInfo.MAX_DOP, true,self);
		 * loc_rib.remove_all(); loc_rib.add(info); locribchanges.add(info);
		 * logDebug("route-added {nlri:"+rte.nlri.toString()+"}"); }
		 * 
		 * if (Global.auto_advertise) { // By inserting a route to this AS in
		 * the Loc-RIB and then // starting Phase 3 of the Decision Process, we
		 * // effectively cause update messages to be sent to each of // our
		 * peers. Note that we insert into the Loc-RIB but // *not* into the
		 * local router's forwarding table. // run Phase 3 of the Decision
		 * Process so that the changes // to the Loc-RIB will get propagated to
		 * the Adj-RIBs-Out. decision_process_3(locribchanges, null); }
		 */

		PeerConnection peerConnection = new PeerConnection(peer);
		peersByConnection.put(peerConnection, peer);
		(new StartConnectionTimer(timerMaster, 120, peerConnection, this))
				.set();
	}

	public Long getCluster_num() {
		return new Long(cluster_num);
	}

	public void setCluster_num(long cluster_num) {
		this.cluster_num = cluster_num;
	}

	/**
	 * print information about BGPSession
	 * 
	 */

	public void startConnection(long addr, Double delay) {
		PeerEntry peerEntry = (PeerEntry) this.peersByIP.get(new Long(addr));
		if (peerEntry == null) {
			logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
					"Error: trying to connect with a node that it is not a peer");
		}
		this.startConnection(peerEntry,delay);
	}

	public void startConnection(PeerEntry peerEntry, Double delay) {
		if ((ip_addr.val() < peerEntry.ip_addr.val())) {
			logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_LOW, "peer {"
					+ peerEntry.addr + "}");
			PeerConnection peerConnection;
			// if ((peerConnection = peerEntry.connection) != null)
			// peerConnection = peerEntry.connection;
			// else
			peerConnection = new PeerConnection(peerEntry);
			peerEntry.cancelConnectRetryTimer();

			logFSM("UNKNOWN", "Run", "IDLE", peerConnection);
			peersByConnection.put(peerConnection, peerEntry);
			this.connect(peerConnection,delay);

		} else {
			logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_LOW, "peer <"
					+ peerEntry.addr + ">");
		}
	}

	protected void connect(PeerConnection peerConnection,Double delay) {
		if (peersByConnection.get(peerConnection).accessible(this) && (delay == null || delay <=0))
			push(new StartStopMessage(BGPstart, peerConnection));
//			send(new StartStopMessage(BGPstart, peerConnection), peerConnection);
		else
			(new StartConnectionTimer(timerMaster, delay + rng4.nextDouble(),
					peerConnection, this)).set();
	}

	@Override
	public String type() {
		return Protocol.BGP4;
	}

	@Override
	public Object id() {
		return this.bgp_id;
	}

	@Override
	public void restart() {
		// obtain the object that indentifies the protocol to connect to it.
		Collection<PeerEntry> peers_ = this.peersByIP.values();
		Iterator<PeerEntry> peers = peers_.iterator();
		while (peers.hasNext()) {
			PeerEntry peer = peers.next();
			if (peer.connection == null
					|| (peer.connection.getState() == PeerConnectionConstants.IDLE))
				this.startConnection(peer,0.0);
		}
	}

	@Override
	public void kill() {
		// TODO Aun no hace nada pero tendra que hacerlo

	}

	@Override
	public void endProtocol() {
		if (gem != null)
			gem.writeDocuemnt();
		gem = null;

	}

	protected boolean auto_reconnect(PeerEntry peerEntry,
			PeerConnection peerConnection) {
		return Global.auto_reconnect;
	}

	@Override
	public void createLogs(Element sessionElement) {
		drcl.comp.io.FileComponent file;

		String filename = sessionElement.getAttributeValue("log_file_name");
		if (filename == null) {
			filename = this.getParent().getID();
			filename += "-bgp 4-" + this.port;
		}

		// DEBUG LOG
		if (Global.logDebugEnable) {
			file = new drcl.comp.io.FileComponent(filename + "-dbg");
			this.addComponent(file);
			file.open(Enviroment.tracedir + filename + ".dbg");
			file.setEventFilteringEnabled(true);
			this.getPort("dbg").connect(file.findAvailable());
		}
		// FSM LOG
		if (infonet.javasim.bgp4.Global.logFSMEnable) {
			file = new drcl.comp.io.FileComponent(filename + "-fsm");
			this.addComponent(file);
			file.open(Enviroment.tracedir + filename + ".fsm");
			file.setEventFilteringEnabled(true);
			this.getPort("fsm").connect(file.findAvailable());
		}
		// ROUTE TABLE LOG
		if (infonet.javasim.bgp4.Global.logRTEnable) {
			file = new drcl.comp.io.FileComponent(filename + "-rt");
			this.addComponent(file);
			file.open(Enviroment.tracedir + filename + ".rt");
			file.setEventFilteringEnabled(true);
			this.getPort("rt").connect(file.findAvailable());

		}
		// MSG LOG
		if (infonet.javasim.bgp4.Global.logMsgEnable) {
			file = new drcl.comp.io.FileComponent(filename + "-msg");
			this.addComponent(file);
			file.open(Enviroment.tracedir + filename + ".msg");
			file.setEventFilteringEnabled(true);
			this.getPort("msg").connect(file.findAvailable());

		}
	}

	@Override
	public void config(Element sessionElement, Node node) {
		this.printDebug = Enviroment.debugFlag;
		this.printMsg = Enviroment.errorFlag;
		Element parameters = sessionElement.getChild("parameters");
		this.ASPrefixList = new ArrayList<IPaddress>();
		String value;
		Object type;
		value = parameters.getAttributeValue("as");
		if (value == null) {
			throw new Error("BGPSession.config: there are no as-id");
		}
		this.ASNum = Integer.valueOf(value);

		// TODO quiza haya que poner algo mas general. El id de la interfaz que
		// no sabemos muy bien lo que es
		this.iFaceName = parameters.getAttributeValue("interface");
		if (this.iFaceName == null)
			throw new Error(
					"BGPSession.Config: BGPSession need a interface parameter");
		Component iface = node.getComponent(this.iFaceName);
		if (iface == null || !(iface instanceof tid.inet.NetworkInterface)) {
			throw new Error(
					"BGPSession.config: The interface don't exist or is invalid");
		}
		// FIXME tema de interface virtuales y ids que no sean exactamente una
		// direccion!!
		ip_addr = new IPaddress((Long) ((tid.inet.NetworkInterface) iface)
				.getAddress());
		// FIXME cambiar el id por otra cosa (quiza un nº de itnerfaz??????)
		bgp_id = ip_addr;

		value = parameters.getAttributeValue("default_local_pref");
		if (value == null)
			defaultLocalPreference = Global.default_local_pref;
		else
			defaultLocalPreference = Integer.valueOf(value);

		@SuppressWarnings("unchecked")
		List<Element> networks = parameters.getChildren("network");
		// TODO comprobar que devuelve cuando hay 0

		ListIterator<Element> netList = networks.listIterator();

		while (netList.hasNext()) {
			Element net = netList.next();
			// format (www.xxx.yyy.zzz/mm) for IPv4
			value = net.getAttributeValue("address");

			if (value == null)
				throw new Error("BGPSession.config: " + value
						+ " there are not address to the network");
			String sAddress[] = value.split("/");

			this.addPrefix(value);

		}
		// TODO med
		// value = net.getAttributeValue("med");
		// if (value != null)
		value = parameters.getAttributeValue("med_type");

		// always compare med
		if (value == null)
			this.always_compare_med = Global.always_compare_med;
		else
			this.always_compare_med = value
					.equals(Global.STRING_ALWAYS_COPMARE_MED);

		// random tie brakling?
		if ((value = parameters.getAttributeValue("random_tie_breaking")) != null)
			this.random_tie_breaking = value
					.equals(Global.STRING_RANDOM_TIE_BREAKING);
		else
			this.random_tie_breaking = Global.random_tie_breaking;

		// routes_compare_level?
		if ((value = parameters.getAttributeValue("routes_compare_level")) != null) {
			if (Global.ROUTES_COMPARE_LEVELS.containsKey(value)) {
				this.routes_compare_level = Global.ROUTES_COMPARE_LEVELS
						.get(value);
			} else
				this.routes_compare_level = Global.routes_compare_level;
		} else {
			this.routes_compare_level = Global.routes_compare_level;
		}
		//		

		// port
		if ((value = parameters.getAttributeValue("port")) != null)
			this.port = Integer.valueOf(value);
		else
			this.port = BGPSession.PORT_NUM;
		// Keep alive interval
		if ((value = parameters.getAttributeValue("kai")) != null)
			this.keep_alive_interval = Integer.valueOf(value);
		else
			this.keep_alive_interval = Global.KeepAliveInterval;

		logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM, "config{as="
				+ this.ASNum + "}");
		this.TBID = (int) (rng1.nextDouble() * Integer.MAX_VALUE);

		// XXX codigo descomentado
		@SuppressWarnings("unchecked")
		List<Element> neighbours = sessionElement.getChildren("neighbour");
		// Prefix
		this.reflector = false;
		Iterator<Element> neighboursList = neighbours.iterator();
		if (neighbours != null) {
			while (neighboursList.hasNext()) {
				Element neighborElement = neighboursList.next();
				value = neighborElement.getAttributeValue("rrc");
				if (!reflector && (value != null) && (value.equals("yes"))) {
					this.reflector = true;
					// TODO ckeck if exist a neighbor reflector to use the same
					// cluster num
					this.addCluster(this.ip_addr.val());
					this.cluster_num = this.getClusterHashTable(this.ip_addr
							.val());

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
		if (netList != null) {
			Iterator<Element> netsIteratorList = netsList.iterator();
			while (netsIteratorList.hasNext()) {
				Element netElement = netsIteratorList.next();
				value = netElement.getAttributeValue("address");
				if (value == null)
					throw new Error(
							"BGPSession.config: the network has not a addess atribute");
				String[] net = value.split("/");
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
					throw new Error("BGPSEssion (config): The address has not a correct value");
				}

			}
		}
		this.createLogs(sessionElement);
	}

	public void addPeer(Element xmlPeer) {
		// COMENZAMOS A CONFIGURAR LOS NEIBORGS
		// COGEMOS LA LISTA DE LOS NEIBORGS
		// this.setDebugEnabled(true);
		// Enumeration nbs_config = cfg.find("NEIGHBOURS_POINTERS");

		// UNO A UNO COGEMOS TODOS LOS NEIBORGS DE LA LISTA
		String value;
		value = xmlPeer.getAttributeValue("IP");
		if (value == null)
			throw new Error("BGPSession.addPeer(" + this.bgp_id
					+ "): neighbor has not ip");
		IPaddress address = new IPaddress(value);
		Long long_addr = address.val();
		value = xmlPeer.getAttributeValue("remote-as");
		if (value == null)
			throw new Error("BGPSession.addPeer(" + this.bgp_id
					+ "): neighbor " + address + " has not as");
		Integer asnum = Integer.valueOf(value);
		PeerEntry peer = new PeerEntry(
				this,
				(asnum == this.ASNum) ? PeerEntry.INTERNAL : PeerEntry.EXTERNAL,
				0, asnum);
		peer.ASNum = asnum;
		peer.addr = long_addr;
		peer.ip_addr = new IPaddress(long_addr);
		peer.return_ip = new IPaddress(bgp_id);
		peer.bgp_id = new IPaddress(long_addr);
		if (this.reflector) {
			value = xmlPeer.getAttributeValue("rrc");
			if (peer.typ == PeerEntry.INTERNAL && (value != null)
					&& (value.equals("yes"))) {
				peer.subtyp = PeerEntry.CLIENT;
			} else
				peer.subtyp = PeerEntry.NONCLIENT;
		}
		peer.subtyp = PeerEntry.NONCLIENT;
		if (xmlPeer.getAttribute("rrc") != null
				&& xmlPeer.getAttribute("rrc").getValue().equals("yes")) {
			if (peer.typ == PeerEntry.INTERNAL) {
				peer.subtyp = PeerEntry.CLIENT;
			}
		}
		value = xmlPeer.getAttributeValue("local_pref");
		if (value != null) {
			peer.setLocal_pref(Integer.valueOf(value));
		} else
			peer.setLocal_pref(this.defaultLocalPreference);
		value = xmlPeer.getAttributeValue("med");
		if (value != null) {
			peer.setMed(Integer.valueOf(value));
		}
		@SuppressWarnings("unchecked")
		List<Element> inPolicies = xmlPeer.getChildren("inPolicy");
		if (inPolicies != null && inPolicies.size() > 0) {
			for (Element policy : inPolicies) {
				if (policy != null) {
					if ((value = policy.getAttributeValue("id")) == null) {
						throw new Error(
								"BGPSEssion (addPeere): id of inbound policy is not defined in ("
										+ this.bgp_id + ")");
					}
					Rule rule = Global.rules.get(value);
					if (rule == null) {
						throw new Error("BGPSEssion (addPeere): " + value
								+ " is not a valid inbound policy id ("
								+ this.bgp_id + ")");
					}

					peer.in_policy = (Rule) rule.clone();
					peer.in_policy.bgpSession = this;
				}
			}
		} else {
			// If there are not policies, permit all routes
			peer.in_policy = new Rule(true, this);
		}

		@SuppressWarnings("unchecked")
		List<Element> outPolicies = xmlPeer.getChildren("outPolicy");
		if (outPolicies != null && outPolicies.size() > 0) {
			for (Element policy : outPolicies) {
				if (policy != null) {
					if ((value = policy.getAttributeValue("id")) == null) {
						throw new Error(
								"BGPSEssion (addPeere): id of outbound policy is not defined in ("
										+ this.bgp_id + ")");
					}
					Rule rule = Global.rules.get(value);
					if (rule == null) {
						throw new Error("BGPSEssion (addPeere): " + value
								+ " is not a valid outbound policy id ("
								+ this.bgp_id + ")");
					}
					peer.out_policy = (Rule) rule.clone();
					peer.out_policy.bgpSession = this;
				}
			}
		} else
			peer.out_policy = new Rule(true, this);

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
		allPeers.add(peer);

	}

	@Override
	public void configGeneralParameters(Element xml) {
		Element bgpParams = (Element) xml.getChild("parameters");
		if ((bgpParams == null) && Enviroment.debugFlag) {
			System.out
					.println("infonet.javasim.bgp4.BGPSession.configGeneralParameters: "
							+ "There are not general parameters defined to the protocol");
		}

		// Initialize variables of BGP
		Global.tbid_tiebreaking = false;
		Global.random_tiebreaking = false;
		// MP-BGP extension
		Global.mp_bfd = false;
		Global.mp_bfd_poll_interval = Global.MP_DEFAULT_BFD_POLLING_INTERVAL;

		String value;
		if ((value = bgpParams.getAttributeValue("rtlog")) != null)
			Global.logRTEnable = value.equals("enable");
		if ((value = bgpParams.getAttributeValue("dbglog")) != null)
			Global.logDebugEnable = value.equals("enable");
		if ((value = bgpParams.getAttributeValue("fsmlog")) != null)
			Global.logFSMEnable = value.equals("enable");
		if ((value = bgpParams.getAttributeValue("tracelog")) != null)
			Global.logMsgEnable = value.equals("enable");
		if ((value = bgpParams.getAttributeValue("med")) != null)
			Global.always_compare_med = value
					.equals(Global.STRING_ALWAYS_COPMARE_MED);
		if ((value = bgpParams.getAttributeValue("tie_breaking")) != null)
			Global.random_tiebreaking = value
					.equals(Global.STRING_RANDOM_TIE_BREAKING);
		if ((value = bgpParams.getAttributeValue("default_port")) != null)
			BGPSession.PORT_NUM = Integer.valueOf(value);
		if ((value = bgpParams.getAttributeValue("kai")) != null)
			Global.KeepAliveInterval = Integer.valueOf(value);

		if ((value = bgpParams.getAttributeValue("routes_compare_level")) != null) {
			if (Global.ROUTES_COMPARE_LEVELS.containsKey(value)) {
				Global.routes_compare_level = Global.ROUTES_COMPARE_LEVELS
						.get(value);
			}
		}
		if ((value = bgpParams.getAttributeValue("default_local_pref")) != null)
			Global.default_local_pref = Integer.parseInt(value);

		/*-----------------POLICIES------------------*/
		Element bgpPolicy = xml.getChild("policyList");
		if (bgpPolicy != null) {
			@SuppressWarnings("unchecked")
			List<Element> policies = bgpPolicy.getChildren("policy");
			for (int i = 0; i < policies.size(); i++) {
				boolean permit = true;
				if ((value = policies.get(i).getAttributeValue("permit")) != null) {
					permit = !value.equals("false");
				}
				Rule policy = new Rule();
				String ruleId = policies.get(i).getAttributeValue("id");
				Global.rules.put(ruleId, policy);
				@SuppressWarnings("unchecked")
				List<Element> clauses = policies.get(i).getChildren("clause");

				// List of clauses for a Policy
				for (int j = 0; j < clauses.size(); j++) {
					Predicate p = new Predicate(null);
					// XXX Por defecto hay que ponerlo en true o false???
					Action a = null;
					Element element;
					// IF exist predicates && atomic predicates
					if ((element = clauses.get(j).getChild("predicate")) != null) {
						@SuppressWarnings("unchecked")
						List<Element> atomicPredicates = element
								.getChildren("atomicPredicate");
						if (atomicPredicates != null) {
							// List of atomic predicates for a predicates
							for (int k = 0; k < atomicPredicates.size(); k++) {
								String attribute = atomicPredicates.get(k)
										.getAttributeValue("attribute");
								if (attribute == null)
									throw new Error(
											"There isn't attribute in a atomicPredicate. Policy ID="
													+ ruleId);
								String matchString = atomicPredicates.get(k)
										.getAttributeValue("match_string");
								if (matchString == null)
									throw new Error(
											"There isn't match_String in a atomicPredicate. Policy ID="
													+ ruleId);
								AtomicPredicate ap = new AtomicPredicate(
										attribute, matchString);
								p.add_atom(ap);
							}
						}
					}

					if ((element = clauses.get(j).getChild("action")) != null) {
						permit = true;
						if ((value = element.getAttributeValue("permit")) != null) {
							permit = !value.equals("false");
						}
						a = new Action(permit, null);
						@SuppressWarnings("unchecked")
						List<Element> atomicActions = element
								.getChildren("atomicAction");
						if (atomicActions != null) {
							for (int k = 0; k < atomicActions.size(); k++) {
								// see Values in AtomicActions
								String attribute = atomicActions.get(k)
										.getAttributeValue("attribute");
								if (attribute == null)
									throw new Error(
											"There isn't attribute in a atomicAction. Policy ID="
													+ ruleId);
								// See values in AtomicActions
								String action = atomicActions.get(k)
										.getAttributeValue("action");
								if (action == null)
									throw new Error(
											"There isn't action in a atomicAction. Policy ID="
													+ ruleId);
								// Multi value string must be separated with a
								// space
								String val = atomicActions.get(k)
										.getAttributeValue("value");
								String[] values;
								if (val != null) {
									values = new String[1];
									values[0] = val;
								} else {
									val = atomicActions.get(k)
											.getAttributeValue("values");
									if (val == null)
										throw new Error(
												"There isn't \"value\" or \"values\" in a atomicAction. Policy ID="
														+ ruleId);
									values = val.split(" ");

								}
								AtomicAction aa = new AtomicAction(attribute,
										action, values);
								a.add_atom(aa);
							}
						}

					} else
						// XXX cual es el valor por defecto
						a = new Action(true, null);
					Clause clause = new Clause(p, a);
					policy.add_clause(clause);

				}
			}
		}

	}
	public void sendIncorrectMessage(long destiny, int type){
		switch (type) {
		case tid.events.INCORRECT_BGPUPDATE_MSG.CODE:
			PeerEntry peer = peersByIP.get(destiny);
			if (peer == null)
				throw new Error("There are not a neighbor with the address: "+destiny);
			if (peer.connection == null)
				throw new Error("The neighbor "+destiny+" is not correctely connected");
			IncorrectUpdateMessage msg = new IncorrectUpdateMessage(peer.connection);
			send(msg,peer.connection);
			
			
			break;
		default:
			break;
		} 
	}
	private class MsjSender extends infonet.javasim.util.Timer {
		private BGPSession owner;
		private long retard;
		public MsjSender(TimerMaster master, long retard, BGPSession owner) {
			super(master, retard);
			this.retard = retard;
			this.owner = owner;
		}

		@Override
		public void callback() {
			if (owner.bgp_id.toString().equals("10.0.1.2/32")){
				for (PeerConnection pc  : peersByConnection.keySet()){
					PeerEntry pe = peersByConnection.get(pc);
					if (pe.accessible(owner)){
						NotificationMessage msg = new NotificationMessage(pc,(byte) 0, (byte) 0);
						{
							// Sustituir por una ruta del nodo o una creada por ti
							Route rte = new Route();
							rte.prepend_as(12);
							UpdateMessage msg1 = new UpdateMessage(pc, rte);
							msg1.announceRoutes.get(0).aspath().prepend_as(12);
							msg1.getAnnounce(0).toBytes();
						}
						
						send(msg,pc);
					}
				}
			}
			(new MsjSender(timerMaster,retard,owner)).set();
		}
		
	}
	/**
	 * TODO
	 * @param peer
	 * @param codeError
	 * @param subCodeError
	 */
	protected void disconnectPeer(PeerEntry peer, Byte codeError, Byte subCodeError){
		if (peer.connected) {
			peer.connection.writeQueue.clear();
			// Cease Notification
			logDebug(GraphicBGPEventManager.DEBUG_MESSAGE_MEDIUM,
					"NOTIFICATION becoz DIE");
			if (codeError != null){
				NotificationMessage msg = new NotificationMessage(peer.connection, codeError,
						subCodeError); 
				send(msg, peer.connection);
			}
			peer.connection.setState(PeerConnection.IDLE);
			PeerConnection peerConnection = peer.connection;
			peerConnection.close();
			peer.cancel_timers();
			peer.connected = false;
			remove_all_routes(peer);
//			if (auto_reconnect(peer, peerConnection )) {
//				if (printDebug)
//					System.out.println("WARNING: Automatic reconnection...");
//				this.startConnection(peer.addr, false);
//			}

		}

	}
}
