/**
 * TimeoutMessage.java
 *
 * @author BJ Premore
 */


package infonet.javasim.bgp4.timing;


import infonet.javasim.bgp4.*;
import infonet.javasim.bgp4.comm.*;
import infonet.javasim.bgp4.util.*;


// ===== class SSF.OS.BGP4.Timing.TimeoutMessage =========================== //
/**
 * Used to notify BGP that a timeout has occurred (one of the BGP
 * timers has expired).
 */
public class TimeoutMessage extends BGPMessage {

    /** Indicates the type of timeout that occurred (possible types are
     *  listed in class BGPSession). */
    public int to_type;

    /** The NHI prefix of the peer to whom this timeout is relevant. */
    // public String nh; Deje declare dans Message !!!

    // ----- constructor TimeoutMessage -------------------------------------- //
    /**
     * Initialize the message data.
     *
     * @param tt      The type of timeout.
     * @param nhipre  The NHI prefix of the peer to whom this timeout is
     *                relevant.
     */
    public TimeoutMessage(int tt, PeerConnection peerConnection/*String nhipre*/) {
	super(BGPMessage.TIMEOUT, peerConnection/*nhipre*/);
	to_type = tt;
	//nh = nhipre; Deje declare dans Message !!!
    }

} // end of class TimeoutMessage
