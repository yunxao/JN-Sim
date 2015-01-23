/**
 * StartStopMessage.java
 *
 * @author BJ Premore
 */


package infonet.javasim.bgp4.comm;

import infonet.javasim.bgp4.*;
import infonet.javasim.bgp4.util.*;

// ===== class drcl.inet.protocol.bgp4.comm.StartStopMessage =========================== //
/**
 * Message from the system or a system operator to either initiate or
 * discontinue a BGP connection with a particular (potential) neighbor/peer.
 */
public class StartStopMessage extends BGPMessage {

  /** Whether this is a start or stop message.  (All possible message types and
   *  their values are listed in class BGPSession). */
  public int ss_type;

  /** The NHI prefix of the neighbor/peer whose connection to whom this message
   *  applies. */
    /* deje declare dans Message !!!
  public String nh;
    */


  // ----- constructor StartStopMessage ------------------------------------ //
  /**
   * Initialize the message.
   *
   * @param typ     The type of the message (start or stop).
   * @param nhipre  The NHI prefix of the neighbor/peer whose connection
   *                this message applies to.
   */
    public StartStopMessage(int typ,
			    PeerConnection peerConnection/*String nhipre*/) {
	super(BGPMessage.STARTSTOP, peerConnection/*nhipre*/);
	ss_type = typ;
	//nh = nhipre; Deje declare dans Message !!!
  }


} // end class StartStopMessage
