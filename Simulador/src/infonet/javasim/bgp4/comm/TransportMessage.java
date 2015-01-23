/**
 * TransportMessage.java
 *
 * @author BJ Premore
 */


package infonet.javasim.bgp4.comm;

import infonet.javasim.bgp4.*;
import infonet.javasim.bgp4.util.*;


// ===== class drcl.inet.protocol.bgp4.comm.TransportMessage =========================== //
/**
 * A BGP transport message.  It is used internally by BGP to indicate when
 * transport messages are received on a socket.
 */
public class TransportMessage extends BGPMessage {

  /** Indicates the type of transport message.  Possible values corresponding
   *  to "open", "close", "open fail", and "fatal error" are enumerated in
   *  BGPSession. */
  public int trans_type;

  /** The NHI prefix of the neighbor/peer with whom this transport message is
   *  associated. */
  // public String nh; Deje declare dans Message !!!


  // ----- constructor TransportMessage ------------------------------------ //
  /**
   * Constructs a transport message given a type code and peer NHI prefix.
   *
   * @param t       The type of the transport message.
   * @param nhipre  The NHI prefix of the neighbor/peer to whom this message
   *                applies.
   */
    public TransportMessage(int t, PeerConnection peerConnection/*String nhipre*/) {
	super(BGPMessage.TRANSPORT, peerConnection/*addr/*nhipre*/);
    trans_type = t;
    //nh = nhipre; Deje declare dans Message !!!
  }


} // end class TransportMessage
