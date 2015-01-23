// ========================================================================= //
// @(#)PeerEntry.java
//
// @author BJ Premore
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
//
// @lastdate 07/05/2002
//
// Note1: the SSFNet implementation used two distinct TCP connections to handle
// the peering. RFC1771 (section 6.8.) recommands to use a single TCP
// connection and to use a simple connection collision algorithm. We have
// chosen to implement this method in the JavaSim implementation.
//
// Note2: the NHI identification used by SSFNet is not supported by the
// JavaSim implementation.
// ========================================================================= //

package infonet.javasim.bgp4;

import infonet.javasim.bgp4.comm.BGPMessage;
import infonet.javasim.bgp4.comm.NotificationMessage;
import infonet.javasim.bgp4.policy.Rule;
import infonet.javasim.bgp4.timing.EventTimer;
import infonet.javasim.bgp4.timing.MRAIPerPeerTimer;
import infonet.javasim.bgp4.timing.MRAITimer;
import infonet.javasim.bgp4.util.AS_descriptor;
import infonet.javasim.bgp4.util.Pair;
import infonet.javasim.util.IPaddress;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import drcl.inet.core.NI;
import drcl.inet.protocol.Routing;
import drcl.inet.socket.InetSocket;

// ===== class drcl.inet.protocol.bgp4.PeerEntry ======================================= //
/**
 * This class encapsulates the data that a BGP speaker would keep for one of
 * its peers.  The member data are generally named and explained from the point
 * of view of a BGP instance.
 */
public class PeerEntry {

    // ......................... constants ........................... //

    /** Indicates an Internal BGP peer (in the same AS). */
    public static final int INTERNAL =  1;
    /** Indicates an External BGP peer (in a different AS). */
    public static final int EXTERNAL =  2;

    /** Indicates that the peer sub-type value is not applicable. */
    public static final int NA        = 0;
    /** Indicates a route reflector client peer sub-type of internal peer. */
    public static final int CLIENT    = 1;
    /** Indicates a route reflector non-client peer sub-type of internal peer. */
    public static final int NONCLIENT = 2;
    /** Indicates a mp-bgp peer sub-type of external peer. */
    public static final int CTCLIENT = 3;

    
    // ........................ member data .......................... //

    /** The BGPSession to which this peer information applies. */
    public BGPSession bgp;
    /**
     * Value of local preference to route
     */
    private int local_pref;
    /** The type of peer.  (Internal or external.) */
    public int typ;

    /** The sub-type of the peer.  (Internal reflector client or non-client.) */
    public int subtyp;

    /** The NHI address of this peer.  Specifically, the NHI address of
     *  the interface on the peer's router. */
    //public String nhi;

    /** The NH part of the NHI address of this peer. */
    //public String nh;
    public long addr;
    
    private boolean accessible;

    /** The local IP address that this peer uses as a destination when sending
     *  packets here.  For internal peers, this is typically the address of a
     *  virtual (loopback) interface, and for external peers it is typically the
     *  address of a physical interface on a point-to-point link directly
     *  connecting the two BGP speakers. */
    public IPaddress return_ip;

    /** The IP address of the interface on the peer's router which is
     *  linked to an interface on the local router. */
    public IPaddress ip_addr;

    /** The BGP ID of this peer. */
    public IPaddress bgp_id;

    /** The NHI address prefix of the AS in which this peer resides. */
    //public String as_nh;

    /** The number of the AS in which this peer resides. */
    public int ASNum;

    /** The physical interface on the local router through which outgoing
     *  messages to this peer travel. */
    /* bqu
       public NIC iface;*/
    public NI iface;

    /** The socket for sending/receiving messages from this peer */
    public PeerConnection connection= null;

    public HashMap peerConnecting= new HashMap(2);

    /** Maps read sockets to null/non-null (false/true).  This boolean value
     *  indicates whether or not the socket used for receiving messages from this
     *  peer is busy.  A busy socket means that a read is currently in
     *  progress. */
    public HashMap reading = new HashMap(2);

    /** Maps write sockets to null/non-null (false/true).  This boolean value
     *  indicates whether or not the socket used for sending messages to this
     *  peer is busy.  A busy socket means that a write is currently in
     *  progress. */
    public HashMap writing = new HashMap(2);

    /** The maximum amount of time (in clock ticks) which can elapse without any
     *  BGP message being received from this peer before the connection is
     *  broken. */
    public long hold_timer_interval = -1;

    /** The maximum amount of time (in clock ticks) which can elapse between
     *  messages sent to this peer (or else there's a risk that the peer could
     *  break the connection). */
    public long keep_alive_interval = -1;

    /** The Minimum Route Advertisement Interval.  It is the minimum amount of
     *  time (in clock ticks) which must elapse between the transmission of any
     *  two advertisements containing the same destination (NLRI) to this
     *  peer.  This is the value of MRAI after jitter has been applied. */
    public double mrai = -1;

    /** The ratio between the configured values of the Keep Alive and Hold Timer
     *  Intervals. */
    public double keephold_ratio = ((double)BGPSession.KEEP_ALIVE_DEFAULT)/
	((double)BGPSession.HOLD_TIMER_DEFAULT);

    /** The ConnectRetry Timer, for spacing out attempts to establish a
     *  connection with this peer.  (The terminology used here is not quite
     *  correct--technically, it's not a peer until the connection is
     *  established, only a <i>potential</i> peer.) */
    public EventTimer connectRetryTimer;

    /** A table of Minimum Route Advertisement Interval Timers.  These timers
     *  help ensure that this peer isn't flooded with several updates regarding
     *  the same destination (NLRI) in a short time.  It does not apply to
     *  internal peers.  The table is keyed by the NLRI. */
    public HashMap<Route,MRAITimer> mrais;

    /** The Minimum Route Advertisement Interval Timer used when per-peer
     *  rate-limiting only (no per-destination) is in use. */
    public MRAIPerPeerTimer mraiTimer;

    /** A table of NLRI recently advertised to this peer, used only when rate
     *  limiting is done on a per-peer, per-destination basis.  It is kept
     *  because BGP has limits on the number of times that the same NLRI can be
     *  sent within a given period of time to a given external peer.  The NLRI is
     *  used as both key and value in the table. */
    public HashMap <Route,Route> adv_nlri;

    /** A table of NLRI recently withdrawn from this peer, used only when rate
     *  limiting is done on a per-peer, per-destination basis.  It is analogous
     *  to <code>adv_nlri</code>, to be used when the Minimum Route Advertisement
     *  restriction is being applied to withdrawals.  The NLRI is used as both
     *  key and value in the table. */
    public HashMap <Route,Route>wdn_nlri;

    /** A table of prefixes that are waiting to be advertised to this peer.  More
     *  specifically, each entry is keyed by such a prefix, but the value is a
     *  pair of objects indicating the route and the sender of the advertisement
     *  for the route.  Prefixes can be waiting to be sent either because 1) an
     *  update with the same NLRI was sent recently (if per-peer, per-destination
     *  rate limiting is in use) or 2) an update with any prefix was sent
     *  recently (if per-peer rate limiting is in use). */
    public HashMap <Route,Pair<Route,IPaddress>>waiting_adv;

    /** A table of prefixes which are waiting to be withdrawn from this peer.
     *  This is similar to the <code>waiting_adv</code> field, and is only used
     *  when the option to apply the Minimum Route Advertisement Interval
     *  restriction to withdrawals is in use.  The prefix is used as both key and
     *  value in the table. */
    public HashMap <Route,Route> waiting_wds;

    /** The policy rule to be applied for filtering outbound routes. */
    public Rule out_policy;

    /** The policy rule to be applied for filtering inbound routes. */
    public Rule in_policy;

    /** The section of Adj-RIBs-In associated with this peer. */
    public AdjRIBIn rib_in;

    /** The section of Adj-RIBs-Out associated with this peer. */
    public AdjRIBOut rib_out;

    /** The number of updates received from this peer during the current
     *  session. */
    public int inUpdates;

    /** The number of updates sent to this peer during the current session. */
    public int outUpdates;

    /** Whether or not a connection with this (potential) peer has been
     *  established yet. */
    public boolean connected = false;

    /** Used to hold a message which arrives on a socket connecting with
     *  this peer. */
    private Object[] objarray;
	private Integer med = null;

    /** Indicates a null value which is treated as the boolean 'false'. */
    private static final Boolean FALSE = null;

    /** Indicates a non-null value which is treated as the boolean 'true'. */
    private static final Boolean TRUE = new Boolean(true);

    // ----- constructor PeerEntry(BGPSession,int,int) ----------------------- //
    /**
     * Constructs a peer entry with a reference to the associated BGP protocol
     * session as well as type information.
     *
     * @param b   The BGP protocol session with which this peer entry is
     *            associated.
     * @param t   An integer indicating the peer's type.
     * @param st  An integer indicating the peer's sub-type.
     */
    public PeerEntry(BGPSession b, int t, int st, int ASNum) {
	bgp       = b;
	typ       = t;
	subtyp    = st;
	ip_addr   = null;
	this.ASNum= ASNum;
	iface     = null;
	return_ip = null;
	connection= null;

	connectRetryTimer= null;
	mraiTimer= null;

	if (bgp.rate_limit_by_dest) {
	    mrais = new HashMap();
	    adv_nlri = new HashMap();
	    if (Global.wrate) {
		wdn_nlri = new HashMap();
	    }
	}
	waiting_adv = new HashMap();
	if (Global.wrate) {
	    waiting_wds = new HashMap();
	}
    }

    // ----- constructor PeerEntry(BGPSession,String) ------------------------ //
    /**
     * Constructs a peer entry with a reference to the associated BGP protocol
     * session and the NHI prefix of the peer.
     *
     * @param b       The BGP protocol session with which this peer entry is
     *                associated.
     * @param nhipre  The NHI prefix of the peer.
     */
    public PeerEntry(BGPSession b, long addr) {
	this(b, NA, NA, AS_descriptor.NO_AS);
	this.addr= addr;
	connection= null;
    }

    // ----- constructor PeerEntry(BGPSession) ------------------------------- //
    /**
     * Constructs a special peer entry which represents the local BGP speaker.
     * Obviously, this entry does not actually represent a peer at all, but it is
     * useful when dealing with routes which were originated by this BGP speaker
     * or configured statically.
     *
     * @param b  The BGP protocol session at the local router.
     */
    public PeerEntry(BGPSession b) {
	bgp     = b;
	typ     = INTERNAL;
	subtyp  = NA;
	addr= bgp.bgp_id.val();
	bgp_id  = bgp.bgp_id;
	ASNum= bgp.ASNum;
	//connection_state = BGPSession.ESTABLISHED;
    }

    // ----- PeerEntry.isConnectionEstablished ----------------------------- //
    /**
     * Return true if a connection is established with this peer.
     * Return false otherwize.
     */
    public boolean isConnectionEstablished()
    {
	return ((connection != null) &&
		(connection.getState() == PeerConnectionConstants.ESTABLISHED));
    }

    // ----- PeerEntry.setConnection --------------------------------------- //
    /**
     * Connection collision detection algorithm.
     */
    public boolean setConnection(PeerConnection peerConnection)
    {
	connection= peerConnection;
	return true;
    }

    // ----- PeerEntry.clearConnection ------------------------------------- //
    public void clearConnection(PeerConnection peerConnection)
    {
	if (connection == peerConnection) {
	    connection= null;
	}
    }

    // ----- PeerEntry.equals ------------------------------------------------ //
    /**
     * Determines whether two peer entries are equal.  They are equal only if
     * their NHI prefixes are equal.
     *
     * @param pe  The peer entry with which to make the comparison.
     * @return whether or not the two peer entries are equal
     */
    public boolean equals(Object pe) {
	return (pe != null &&
		pe instanceof PeerEntry && (addr == ((PeerEntry)pe).addr)
		/*nh.equals(((PeerEntry)pe).nh)*/);
    }
  
    // ----- PeerEntry.shutdownConnection ------------------------ //
    /**
     * Shutdown the connection with this peer.
     *   PEER_SHUTDOWN_NOTIFY    : clean connection gracefully.
     *   PEER_SHUTDOWN_BREAK_TCP : break transport connection.
     */
    public void shutdownConnection(int type)
    {
	if (connection == null)
 	    throw new Error("Error: Connection is not established !");

	switch (type) {
	case BGPSession.PEER_SHUTDOWN_NOTIFY:
	    connection.send(new NotificationMessage(connection,
						    (byte) 6, (byte) 0));
	    /*bgp.push(new TransportMessage(BGPSession.TransConnClose,
					  connection));*/
	    break;
	case BGPSession.PEER_SHUTDOWN_BREAK_TCP:
	    connection.close();
	    break;
	default:
	    throw new Error("Error: Unknown connection shutdown type !");
	}
    }

    // ----- PeerEntry.cancel_timers --------------------------------------- //
    /**
     * Cancels all timers associated with this peer.
     */
    public void cancel_timers() {
	cancelConnectRetryTimer();
	if (connection != null) {
	    connection.cancelHoldTimer();
	    connection.cancelKeepAliveTimer();
	}
	if (bgp.rate_limit_by_dest) {
	    for (Iterator it= mrais.values().iterator(); it.hasNext();) {
		MRAITimer timer= (MRAITimer)it.next();
		timer.cancel();
	    }
	    // instead of removing each timer individually, just create a new table
	    mrais= new HashMap();
	} else {
	    if (mraiTimer != null) {
		mraiTimer.cancel();
	    }
	}
    }

    // ----- PeerEntry.cancelConnectRetryTimer ----------------------------- //
    /**
     *
     */
    public void cancelConnectRetryTimer()
    {
	if (connectRetryTimer != null)
	    connectRetryTimer.cancel();
    }
	/**
	 *
	 */
	public int getLocal_pref() {
		return local_pref;
	}
	/**
	 *
	 */
	public void setLocal_pref(int local_pref) {
		this.local_pref = local_pref;
	}
	public boolean accessible(BGPSession protocol){
		if (protocol.retrieveBestRTEntryDest(addr) != null)
			return true;

		return false;
	}

	public void setMed(Integer med) {
		// TODO Auto-generated method stub
		this.med = med;
		
	}
	public int getMed(){
		return med;
	}
	public boolean hasMed(){
		return (med == null)?false:true;
	}

} // end of class PeerEntry
