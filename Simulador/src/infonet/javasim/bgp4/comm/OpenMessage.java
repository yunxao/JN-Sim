/**
 * OpenMessage.java
 *
 * @author BJ Premore
 */


package infonet.javasim.bgp4.comm;

import infonet.javasim.bgp4.*;
import infonet.javasim.bgp4.util.*;


// ===== class drcl.inet.protocol.bgp4.comm.OpenMessage ================================ //
/**
 * A BGP Open message.  Used to initiate negotiation of a peering session
 * with a neighboring BGP speaker.
 */
public class OpenMessage extends BGPMessage implements BGPSerializable {

    public byte version;

    /** The NHI prefix address of the autonomous system of the sender. */
    public int ASNum;

    /** The length of time (in logical clock ticks) that the sender proposes for
     *  the value of the Hold Timer.  The value in seconds is
     *  <code>BGPSession.ticks2secs(hold_time)</code>. */
    public long holdTime;

    /** The BGP Identifier of the sender.  Each BGP speaker (router running BGP)
     *  has a BGP Identifier.  A given BGP speaker sets the value of its BGP
     *  Identifier to an IP address assigned to that BGP speaker (randomly picks
     *  one of its interface's addresses, essentially).  It is chosen at startup
     *  and never changes. */
    public long BGPId;
    
//    public long internalId;


    // ----- constructor OpenMessage ----------------------------------------- //
    /**
     * Initializes member data.
     *
     * @param bgp_id  The BGP ID of the BGPSession composing this message.
     * @param bgp_as  The NHI address prefix of the AS of the BGPSession
     *                composing this message.
     * @param nh      The NHI address prefix of the sender of this message.
     * @param ht      The proposed value for the Hold Timer.
     */
    public OpenMessage(long BGPId, int ASNum,
		       PeerConnection peerConnection/*String nh*/,
		       long holdTime) {
	super(BGPMessage.OPEN, peerConnection/*nh*/);
	this.ASNum= ASNum;
	this.holdTime= holdTime;
	this.BGPId= BGPId;
//	this.internalId = internalId;
	length+= 10;
	version= 4;
	
    }

    public OpenMessage(byte [] bytes)
    {
	super(bytes);
    }

    // ----- OpenMessage.toBytes ------------------------------------------- //
    /**
     *
     */
    public byte [] toBytes()
    {
	// Version: 1 byte
	// AS number: 2 bytes
	// Hold Timer: 2 bytes
	// BGP ID: 4 bytes
	// Opt Parm Len: 1 byte (always 0)
	// Optional parameters: ? (not supported in this version)

	byte [] header= super.toBytes();

	// Copy header
	byte [] bytes= new byte [this.length];
	System.arraycopy(header, 0, bytes, 0, header.length);
	int pos= header.length;

	// Version
	bytes[pos++]= version;
	// Autonomous System number
	bytes[pos++]= (byte) (ASNum >> 8);
	bytes[pos++]= (byte) (ASNum & 255);
	// Hold Time
	bytes[pos++]= (byte) (holdTime >> 8);
	bytes[pos++]= (byte) (holdTime & 255);
	// BGP Identifier
	bytes[pos++]= (byte) (BGPId >> 24);
	bytes[pos++]= (byte) ((BGPId >> 16) & 255);
	bytes[pos++]= (byte) ((BGPId >> 8) & 255);
	bytes[pos++]= (byte) (BGPId & 255);
	//Internal IDs
//	bytes[pos++]= (byte) (internalId >> 24);
//	bytes[pos++]= (byte) ((internalId >> 16) & 255);
//	bytes[pos++]= (byte) ((internalId >> 8) & 255);
//	bytes[pos++]= (byte) (internalId & 255);
	// Opt Parm Len
	bytes[pos++]= 0;

	return bytes;
    }

    // ----- OpenMessage.fromBytes ----------------------------------------- //
    /**
     *
     */
    public void fromBytes(byte [] bytes)
    {
	super.fromBytes(bytes);

	int pos= 19;
	// Version
	version= bytes[pos++];
	// Autonomous System number
	ASNum= ((bytes[pos++]&255) << 8)+(bytes[pos++]&255);
	// Hold time
	holdTime= ((bytes[pos++]&255) << 8)+bytes[pos++];
	// BGP Identifier
	BGPId= ((bytes[pos++]&255) << 24)+((bytes[pos++]&255) << 16)+((bytes[pos++]&255) << 8)+	(bytes[pos++]&255);
//	internalId= (bytes[pos++] << 24)+(bytes[pos++] << 16)+(bytes[pos++] << 8)+
//	bytes[pos++];
	// Opt Parm Len (ignored at the moment)
    }

    // ----- OpenMessage.toString() ---------------------------------------- //
    /**
     *
     */
    public String toString()
    {
	return super.toString()+",as="+ASNum+",id="+BGPId+",hold_timer="+holdTime;
    }

} // end class OpenMessage
