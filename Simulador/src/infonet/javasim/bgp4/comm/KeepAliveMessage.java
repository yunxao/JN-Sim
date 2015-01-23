/**
 * KeepAliveMessage.java
 *
 * @author BJ Premore
 */

package infonet.javasim.bgp4.comm;

import infonet.javasim.bgp4.*;
import infonet.javasim.bgp4.util.*;

// ===== class drcl.inet.protocol.bgp4.comm.KeepAliveMessage =============== //
/**
 * A BGP KeepAlive message.  BGP KeepAlive messages contain no fields in
 * addition to the header fields.  This class is here for completeness only.
 */
public class KeepAliveMessage extends BGPMessage {
    // no additional fields
  
    // ----- constructor KeepAliveMessage ---------------------------------- //
    /**
     * Constructs a KeepAlive message by calling the parent class constructor.
     *
     * @param nh  The NH part of the NHI address of the sender of this message.
     */
    public KeepAliveMessage(PeerConnection peerConnection/*String nh*/) {
	super(BGPMessage.KEEPALIVE, peerConnection/*nh*/);
    }

    public KeepAliveMessage(byte [] bytes)
    {
	super(bytes);
    }

} // end class KeepAliveMessage
