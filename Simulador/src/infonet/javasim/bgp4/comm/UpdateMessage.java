// ========================================================================= //
// @(#)UpdateMessage.java
//
// @author BJ Premore
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
//
// @lastdate 24/04/2002
// ========================================================================= //

package infonet.javasim.bgp4.comm;

import java.util.*;
import tid.utils.BytesUtils;
import infonet.javasim.bgp4.*;
import infonet.javasim.util.*;
import infonet.javasim.bgp4.path.*;

// ===== class SSF.OS.BGP4.UpdateMessage =================================== //
/**
 * A BGP Update message. An update message is used to transfer routing
 * information between two BGP peers. That routing information consists of new
 * and/or outdated routes. New routes are specified by a destination IP prefix
 * and path attributes which describe the route to that destination. Outdated
 * routes, known as infeasible routes, are routes which are no longer valid.
 * They are indicated only by destination, and are used to inform a peer that
 * routes to that destination (as learned from the sending BGP speaker) are no
 * longer valid.
 */
public class UpdateMessage extends BGPMessage {
  
    // ----- member data --------------------------------------------------- //

    /** A list of the destinations of withdrawn routes.  Each element is an IP
     *  address prefix indicating a route which is no longer being used by the
     *  sending BGP speaker. */
    public ArrayList<Route> withdrawRoutes/*= null*/;

    /** A list of routes being advertised.  Each element includes the NLRI and
     *  path attributes for the route. */
    public ArrayList<Route> announceRoutes/*= null*/;

    /** Whether or not this message serves as an update arrival notification.
     *  Note that by default, this message is treated as both an update arrival
     *  notification and a regular update.
     *  @see Global#notice_update_arrival */
    public boolean treat_as_notice = true;

    /** Whether or not this message should be treated as an actual update (as
     *  opposed to being treated only as an update arrival notification).  Note
     *  that by default, this message is treated as both an update arrival
     *  notification and a regular update.
     *  @see Global#notice_update_arrival */
    public boolean treat_as_update = true;


    // ----- constructor UpdateMessage(String) ----------------------------- //
    /**
     * Constructs the update message with default values.
     *
     * @param nh The NHI address prefix of the sender of this message.  */
    public UpdateMessage(PeerConnection peerConnection) {
	super(BGPMessage.UPDATE, peerConnection);
    }

    // ----- constructor UpdateMessage(String,Route) ----------------------- //
    /**
     * Constructs the update message with the given feasible route.
     *
     * @param nh   The NHI address prefix of the sender of this message.
     * @param rte  The route to advertise in this message.
     */
    public UpdateMessage(PeerConnection peerConnection,
			 Route rte) {
	super(BGPMessage.UPDATE, peerConnection);
	addAnnounce(rte);
    }

    // ----- constructor UpdateMessage(String,IPaddress) ------------------- //
    /*
     * Constructs the update message with the given infeasible NLRI.
     *
     * @param nh   The NHI address prefix of the sender of this message.
     * @param rte  The NLRI with withdraw with this message.
    public UpdateMessage(PeerConnection peerConnection,
			 IPaddress wdnlri) {
	super(BGPMessage.UPDATE, peerConnection);
	addWithdraw(wdnlri);
    }*/

    // ----- UpdateMessage ------------------------------------------------- //
    /**
     *
     */
    public UpdateMessage(byte [] bytes)
    {
    	super(bytes);
	}

    // ----- UpdateMessage.rte --------------------------------------------- //
    /**
     * Returns one of the message's routes.
     *
     * @param ind  The index of the route to return.
     * @return one of the message's routes
     */
    public final Route getAnnounce(int ind) {
	if ((announceRoutes == null) || (ind >= announceRoutes.size())) {
	    return null;
	}
	return (Route) announceRoutes.get(ind);
    }

    // ----- UpdateMessage.rte --------------------------------------------- //
    /**
     * Returns one of the message's withdrawn route addresses.
     *
     * @param ind  The index of the withdrawn route address to return.
     * @return one of the message's withdrawn route addresses
     */
    public final Route getWithdraw(int ind) {
	if ((withdrawRoutes == null) || (ind >= withdrawRoutes.size())) {
	    return null;
	}
	return (Route) withdrawRoutes.get(ind);
    }

    // ----- UpdateMessage.add_route --------------------------------------- //
    /**
     * Adds a route to the message.
     *
     * @param rte  The route to add to the message.
     */
    public final void addAnnounce(Route rte) {
	if (announceRoutes == null) {
	    announceRoutes= new ArrayList<Route>();
	}
	announceRoutes.add(rte);
    }

    // ----- UpdateMessage.add_wd ------------------------------------------ //
    /**
     * Adds the destination of a withdrawn route to this message.
     *
     * @param wd  The destination of the withdrawn route to add.
     */
    public final void addWithdraw(Route wd) {
	if (withdrawRoutes == null) {
	    withdrawRoutes= new ArrayList<Route>();
	}
	withdrawRoutes.add(wd);
    }

    // ----- UpdateMessage.remove_wd --------------------------------------- //
    /**
     * Remove withdrawn route information from the message.
     *
     * @param ipa  The IP address prefix to remove.
     * @return true only if the remove was successful
     */
    public final boolean removeWithdraw(Route route) {
		if (withdrawRoutes != null) {
		    for (int i= 0; i < withdrawRoutes.size(); i++) {
				if (route.equals(withdrawRoutes.get(i))) {
				    withdrawRoutes.remove(i);
				    return true;
				}
		    }
		}
		return false;
    }
    // ----- UpdateMessage.toString ---------------------------------------- //
    /**
     * Returns a string describing the contents of the update message.
     *
     * @return a string representation of the update message
     */
    public String toString() {
	String str = "wds=";
	if (withdrawRoutes == null || withdrawRoutes.size() == 0) {
	    str += "-,ads=";
	} else {
	    for (int i=0; i<withdrawRoutes.size(); i++) {
		str += (withdrawRoutes.get(i)).toString() + " ";
	    }
	    str += ",ads= ";
	}
	if (announceRoutes == null || announceRoutes.size() == 0) {
	    str += "-";
	} else {
	    for (int i=0; i<announceRoutes.size(); i++) {
		str += ((Route)announceRoutes.get(i)).nlri.toString()+" ";
	    }
	    // bqu-begin
	    Attribute[] pas = ((Route) announceRoutes.get(0)).pas;
	    for (int j= 0; j < pas.length; j++) {
		if (pas[j] != null)
		    str+= "["+Attribute.names[j]+"="+pas[j].toString()+"]";
	    }
	    // bqu-end
	}
	return super.toString()+","+str;
    }

    // ----- UpdateMessage.byteCountWithdraw ------------------------------- //
    /**
     *
     */
    protected int byteCountWithdraw()
    {
	int withdrawCount= 0;
	if (withdrawRoutes != null) {
	    for (int i= 0; i < withdrawRoutes.size(); i++) {
		// one octet specifies the length, and 0-4 for the prefix
		// itself
		Route withdrawRoute= (Route)withdrawRoutes.get(i);
		withdrawCount+= withdrawRoute.byteSize(); // size of prefix
		//1+(int)(Math.ceil(withdrawRoute.nlri.prefix_len()/8.0));
	    }
	}
	return withdrawCount;
    }

    // ----- UpdateMessage.byteCountAnnounce ------------------------------- //
    /**
     *
     */
    protected int byteCountAnnounceAttr()
    {
	int pa_octets= 0;

	if ((announceRoutes != null) && (announceRoutes.size() != 0)) {
	    // All routes in the message must have the exact same path attributes, so
	    // looking at the attributes of only the first one will suffice.
	    Attribute[] pas = ((Route) announceRoutes.get(0)).pas;
	    for (int i= 0; i < pas.length; i++) {
		if (pas[i] != null) { // this path attribute is not present
		    pa_octets+= pas[i].bytecount();
		}
	    }
	}
	return pa_octets;
    }

    protected int byteCountAnnounceNLRI()
    {
	int nlri_octets= 0;

	if ((announceRoutes != null) && (announceRoutes.size() != 0)) {
	    for (int i= 0; i < announceRoutes.size(); i++) {
		// one octet specifies the length, and 0-4 for the prefix itself
		IPaddress nlri = ((Route) announceRoutes.get(i)).nlri;
		nlri_octets += 1 + (int)(Math.ceil((nlri.prefix_len())/8.0));
	    }
	}
	return nlri_octets;
    }

    // ----- UpdateMessage.toBytes ----------------------------------------- //
    /**
     *
     */
    public byte [] toBytes()
    {
    	// HEADER (19 bytes) || nº of withdrawRoutes (2bytes) | withdrawRoutes (variable size) | size of annuncedRoutes | AnouncedROutes
	int withdrawCount= byteCountWithdraw();
	int announceCountAttr= byteCountAnnounceAttr();
	int announceCountNLRI= byteCountAnnounceNLRI();
	length= OCTETS_IN_HEADER+4+withdrawCount+announceCountAttr+announceCountNLRI;
	
	byte [] header= super.toBytes();
	byte [] bytes= new byte[length];

	// Set length
	bytes[16]= (byte) ((length >> 8)-128);
	bytes[17]= (byte) ((length & 255)-128);

	System.arraycopy(header, 0, bytes, 0, header.length);
	int pos= header.length;

	// Unfeasible Routes Length (2 bytes)
	// If there aren't wd i put 0 (0-128) in bytes
	if (withdrawRoutes == null){
		bytes[pos++]= (byte) -128;
		bytes[pos++]= (byte) -128;
	}else{ 
		bytes[pos++]= (byte) ((withdrawRoutes.size() >> 8)-128);
		bytes[pos++]= (byte) ((withdrawRoutes.size() & 255)-128);
	}
		
		
	// Withdrawn Routes (variable)
	if ((withdrawRoutes != null) && (withdrawRoutes.size() > 0)) {
	    for (int i= 0; i < withdrawRoutes.size(); i++) {
			Route withdrawRoute= (Route) withdrawRoutes.get(i);
			bytes = tid.utils.BytesUtils.arrayCopy(withdrawRoute.toBytes(), bytes, 0, pos);
			pos += withdrawRoute.byteSize();
	    }
	}

	// Total Path Attribute Length (2 bytes)
	//System.out.println("announce {"+announceCountAttr+"}");
	bytes[pos++]= (byte) ((announceCountAttr >> 8)-128);
	bytes[pos++]= (byte) ((announceCountAttr & 255)-128);
	
	// Path Attributes (variable)
	if ((announceRoutes != null) && (announceRoutes.size() > 0)) {
	    Attribute[] pas = ((Route) announceRoutes.get(0)).pas; // porque solo 0? porque solo anuncias 1 cada vez????
	    for (int i= 0; i < pas.length; i++) {
		if (pas[i] != null) { // this path attribute is not present
			
		    byte [] attr_bytes= pas[i].toBytes();
		    System.arraycopy(attr_bytes, 0, bytes, pos,
				     attr_bytes.length);
		    pos+= attr_bytes.length;
		}
	    }
	}
	// NLRI (variable)
	if ((announceRoutes != null) && (announceRoutes.size() > 0)) {
	    for (int i= 0; i < announceRoutes.size(); i++) {
		IPaddress nlri = ((Route) announceRoutes.get(i)).nlri;
		bytes[pos++]= (byte) nlri.prefix_len();
		for (int j= 0; j < Math.ceil(nlri.prefix_len()/8.0); j++)
		    bytes[pos++]= nlri.bytes()[j];
	    }
	}
	return bytes;
    }

    // ----- UpdateMessage.fromBytes --------------------------------------- //
    /**
     *
     */
    public void fromBytes(byte [] bytes)
    {




	// Header
	super.fromBytes(bytes);
	
	int pos= OCTETS_IN_HEADER;
	// Unfeasible Routes Length (2 bytes)
	int withdrawCount= ((((int) bytes[pos++])+128) << 8)+
	    ((int) bytes[pos++])+128;
	
	// Withdraw Routes (variable)
	while (withdrawCount > 0) {
		Route wd = new Route();
		
		wd.fromBytes(BytesUtils.arrayCopy(bytes,null, pos, 0));
		pos += wd.byteSize();
	    if (withdrawRoutes == null)
	    	withdrawRoutes= new ArrayList<Route>();
	    withdrawRoutes.add(new Route(wd));
	    withdrawCount--;
	    
	}
	// Total Path Attribute Length (2 bytes)
	int tpal= ((((int) bytes[pos++])+128) << 8)+
	    ((int) bytes[pos++])+128;
	//System.out.println("Total Path Attribute Length: "+tpal);
	// Path Attributes (variable)
	Attribute [] attributes= new Attribute[Attribute.MAX_TYPECODE+1];
	while (tpal > 0) {
	    boolean extended_length= false;
	    byte attr_flags= bytes[pos++];
	    byte attr_type= bytes[pos++];
	    int attr_length= (((int) bytes[pos++])+128);
	    // Extended Length ?
	    //System.out.println("attr-flags {"+attr_flags+","+(attr_flags & 16)+"}");
	    if ((attr_flags & 16) != 0) {
			//System.out.println("extended-length");
			attr_length= attr_length << 8;
			attr_length+= ((int) bytes[pos++])+128;
			extended_length= true;
	    }
	    //System.out.println("Flag: "+attr_flags+". Tipo: "+attr_type+". Tamaño: "+attr_length);

	    //System.out.println("attribute-header {"+attr_flags+","+attr_type+","+attr_length+"}");

	    byte [] attr_bytes= new byte[(extended_length?4:3)+attr_length];
	    int attr_pos= 0;
	    attr_bytes[attr_pos++]= attr_flags;
	    attr_bytes[attr_pos++]= attr_type;
	    if (extended_length) {
		attr_bytes[attr_pos++]= (byte) ((attr_length >> 8)-128);
		attr_bytes[attr_pos++]= (byte) ((attr_length & 255)-128);
	    } else {
		attr_bytes[attr_pos++]= (byte) attr_length;
	    }
	    
	    System.arraycopy(bytes, pos, attr_bytes, attr_pos, attr_length);
	    Attribute attr= Attribute.buildNewAttribute(attr_bytes);
	    /*if (attr== null)
	    	System.out.println("attr es null");
	    System.out.println("El valor de type_code es:" +attr.type_code);*/
	    attributes[attr.type_code]= attr;
	    pos+= attr_length;
	    tpal-= attr_bytes.length;
	}


	// NLRI (variable)
	while (pos < length) {
	    byte [] cidr_bytes= new byte[5];
	    cidr_bytes[4]= bytes[pos++]; // prefix-length
	    for (int j= 0; j < Math.ceil(cidr_bytes[4]/8.0); j++)
		cidr_bytes[j]= bytes[pos++];

	    IPaddress ipaddr= new IPaddress(cidr_bytes);
	    if (announceRoutes == null)
		announceRoutes= new ArrayList<Route>();
	    announceRoutes.add(new Route(ipaddr, Route.copy_attribs(attributes)));
	}
    }

} // end class UpdateMessage
