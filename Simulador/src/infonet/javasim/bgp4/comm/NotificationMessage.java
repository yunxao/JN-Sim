// ========================================================================= //
// @(#)NotificationMessage.java
//
// @author BJ Premore
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
//
// @lastdate 02/05/2002
// ========================================================================= //

package infonet.javasim.bgp4.comm;

import infonet.javasim.bgp4.*;
import infonet.javasim.bgp4.util.*;


// ===== class drcl.inet.protocol.bgp4.comm.NotificationMessage ======================== //
/**
 * Contains all of the fields that one would find in a BGP Notification
 *  message.
 */
public class NotificationMessage extends BGPMessage {

    /** Indicates the type of error which occurred. */
    public byte error_code;

    /** Provides more specific information about the nature of the
     *  error.  Interpretation varies depending on the type of error. */
    public byte error_subcode;

    // There is also a data field which can be used to diagnose the reason for
    // the Notification message.  It is omitted here but could be added later if
    // deemed useful to the simulation.


    // ----- constructor NotificationMessage --------------------------------- //
    /**
     * Initializes member data.
     *
     * @param nh  The NH part of the NHI address of the sender of this message.
     * @param ec  The error code that this message will indicate.
     * @param ec  The error subcode that this message will indicate.
     */
    public NotificationMessage(PeerConnection peerConnection/*long addr/*String nh*/, byte ec, byte esc) {
	super(BGPMessage.NOTIFICATION, peerConnection/*addr/*nh*/);
	error_code    = ec;
	error_subcode = esc;
	length+= 2;
    }

    public NotificationMessage(byte [] bytes)
    {
	super(bytes);
	fromBytes(bytes);
    }

    // ----- NotificationMessage.toBytes ----------------------------------- //
    /**
     *
     */
    public byte [] toBytes()
    {
	byte [] header= super.toBytes();
	byte [] bytes= new byte [header.length+2];
	System.arraycopy(header, 0, bytes, 0, header.length);
	int pos= header.length;
	
	// Error code
	bytes[pos++]= error_code;
	// Error subcode
	bytes[pos++]= error_subcode;
	// Data (not used at this time)	

	return bytes;
    }

    // ----- NotificationMessage.fromBytes --------------------------------- //
    /**
     *
     */
    public void fromBytes(byte [] bytes)
    {
	super.fromBytes(bytes);
	int pos= OCTETS_IN_HEADER;
	// Error code
	error_code= bytes[pos++];
	// Error subcode
	error_subcode= bytes[pos++];
	// Data (not used at this time)
    }

    // ----- NotificationMessage.toString ---------------------------------- //
    /**
     *
     */
    public String toString()
    {
	return super.toString()+",code=\""+codeToString(error_code, error_subcode)+"\"";
    }

    // ----- NotificationMessage.codeToString ------------------------------ //
    /**
     *
     */
    public static String codeToString(byte error_code, byte error_subcode)
    {
	String str;

	switch (error_code) {
	case 1: // Message Header Error
	    str= "Message Header Error ";
	    switch (error_subcode) {
	    case 1: str+= "(Connection Not Synchronized)"; break;
	    case 2: str+= "(Bad Message Length)"; break;
	    case 3: str+= "(Bad Message Type)"; break;
	    default: str+= "("+error_subcode+")";
	    }
	    break;
	case 2: // OPEN Message Error
	    str= "OPEN Message Error ";
	    switch (error_subcode) {
	    case 1: str+= "(Unsupported Version Number)"; break;
	    case 2: str+= "(Bad Peer AS)"; break;
	    case 3: str+= "(Bad BGP Identifier)"; break;
	    case 4: str+= "(Unsupported Optional Parameter)"; break;
	    case 5: str+= "(Authentication Failure)"; break;
	    case 6: str+= "(Unacceptable Hold Time)"; break;
	    default: str+= "("+error_subcode+")";
	    }
	    break;
	case 3: // UPDATE Message Error
	    str= "UPDATE Message Error ";
	    switch (error_subcode) {
	    case 1: str+= "(Malformed Attribute List)"; break;
	    case 2: str+= "(Unrecognized Well-Known Attribute)"; break;
	    case 3: str+= "(Missing Well-Known Attribute)"; break;
	    case 4: str+= "(Attribute Flags Error)"; break;
	    case 5: str+= "(Attribute Length Error)"; break;
	    case 6: str+= "(Invalid ORIGIN Attribute)"; break;
	    case 7: str+= "(AS Routing Loop)"; break;
	    case 8: str+= "(Invalid NEXT_HOP Attribute)"; break;
	    case 9: str+= "(Optional Attribute Error)"; break;
	    case 10: str+= "(Invalid Network Field)"; break;
	    case 11: str+= "(Malformed AS_PATH)"; break;
	    default: str+= "("+error_subcode+")";
	    }
	    break;
	case 4: // Hold Timer Expired
	    str= "Hold Timer Expired";
	    break;
	case 5: // FSM Error
	    str= "FSM Error";
	    break;
	case 6: // Cease
	    str= "Cease";
	    break;
	default:
	    str= ""+error_code;
	}

	return str;
    }

} // end class NotificationMessage
