/**
 * BGPMessage.java
 *
 * @author BJ Premore
 */


package infonet.javasim.bgp4.comm;

import infonet.javasim.bgp4.*;
import infonet.javasim.bgp4.util.*;
import java.util.Arrays;

// ===== class drcl.inet.protocol.bgp4.comm.Message ==================================== //
/**
 * This class holds the header fields of a BGP message.  It serves as a parent class for the more specific types of BGP messages which are derived from it (Open, Update, Notification, and KeepAlive messages).  It has also also been extended to serve as a timeout notification message and start/stop message.
 */
public class BGPMessage implements BGPMessageConstants, BGPSerializable
{

    // ----- member data --------------------------------------------------- //
    /** The developer's version string of this implementation of BGP-4. */
    public static String version;
    
    /** The type code of the message. */
    public byte typ;
    
    /** The length of the message. */
    public int length;

    /** The NHI prefix of the router of the neighbor/peer with whom this
     * message is associated. For traditional message types (open, update,
     * notification, and keepalive), it is the NHI prefix of the peer who sent
     * the message.  For timeout, transport, and start/stop messages, it is the
     * NHI prefix of the neighbor/peer that the action associated with the
     * message is directed towards. */
    //public String nh;
    public PeerConnection peerConnection= null;


  // ----- constructor Message --------------------------------------------- //
  /**
   * Constructs a message with the given sender NHI prefix address and message
   * type.
   *
   * @param mtyp    The type of the message.
   * @param nhipre  The NHI prefix of the router of the neighbor/peer with
   *                whom this message is associated.
   */
    public BGPMessage(byte mtyp, PeerConnection peerConnection) {
	super();
	typ = mtyp;
	this.peerConnection= peerConnection;
	length= OCTETS_IN_HEADER;
    }

    public BGPMessage(byte mtyp)
    {
	super();
	typ= mtyp;
	peerConnection= null;
	length= OCTETS_IN_HEADER;
    }

    public BGPMessage(byte [] bytes)
    {
	super();
	fromBytes(bytes);
    }

  // ----- Message.version ------------------------------------------------- //
  /**
   * Returns the developer's version string of this BGP-4 implementation.
   *
   * @return the developer's version string of this BGP-4 implementation
   */
  public String version() {
    return "bgp::" + version;
  }

  // ----- Message.type2str ------------------------------------------------ //
  /**
   * Returns a string representation of the message type name.
   *
   * @param typ  An integer indicating a message type.
   * @return a string representation of the message type name
   */
  public static String type2str(int typ) {
    return typeNames[typ];
  }

  // ----- Message.toString ------------------------------------------------ //
  /**
   * Returns a string briefly summarizing the message.
   *
   * @return a string representation of the message
   */
  public String toString() {
      if (peerConnection != null) {
	  return "typ=" + typeNames[typ] + ",src="+peerConnection.toString();
      }
      return "typ=" + typeNames[typ];
  }

    // ----- BGPMessage.toBytes -------------------------------------------- //
    /**
     *
     */
    public byte [] toBytes()
    {
	// Marker: 16 bytes
	// Length: 2 bytes
	// Type: 1 byte

	byte [] bytes= new byte [19];

	// We don't use authentication, so fill marker with 1's
	// (RFC1771, section 4.1.)
	Arrays.fill(bytes, 0, 15, (byte) 255);
	// Length
	bytes[16]= (byte) ((length >> 8)-128);
	bytes[17]= (byte) ((length & 255)-128);
	// Type
	bytes[18]= typ;

	return bytes;
    }
    // ----- BGPMessage.fromBytes ------------------------------------------ //
    /**
     *
     */
    public void fromBytes(byte [] bytes)
    {
	// Marker field is ignored
	// Length
	length= ((((int) bytes[16])+128) << 8) + ((int) bytes[17])+128;
	// Type
	typ= bytes[18];
    }

    // ----- BGPMessage.buildNewMessage ----------------------------------- //
    /**
     *
     */
    public static BGPMessage buildNewMessage(byte [] bytes)
    {
	switch (bytes[18]) {
	case BGPMessage.OPEN:
	    return new OpenMessage(bytes);
	case BGPMessage.UPDATE:
	    return new UpdateMessage(bytes);
	case BGPMessage.NOTIFICATION:
	    return new NotificationMessage(bytes);
	case BGPMessage.KEEPALIVE:
	    return new KeepAliveMessage(bytes);
	default:
	    System.out.println("ERROR: UNKNOWN MESSAGE TYPE (buildNewMessage)");
	    return null;
	}
    }

} // end class Message
