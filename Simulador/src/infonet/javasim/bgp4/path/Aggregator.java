// ========================================================================= //
// @(#)Aggregator.java
//
// @author BJ Premore
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
//
// @lastdate 18/04/2002
// ========================================================================= //

package infonet.javasim.bgp4.path;

import infonet.javasim.util.*;

// ===== class drcl.inet.protocol.bgp4.path.Aggregator ===================== //
/**
 * The aggregator path attribute.  It can only be used with routes which are aggregates, and it indicates the AS number and IP address of the BGP speaker that formed the aggregate route.  It is optional and transitive.
 */
public class Aggregator extends Attribute {

    // ......................... constants ........................... //
     
    /** The aggregator path attribute type code. */
    public static final byte TYPECODE = 7;

    /** The name of the attribute as a string. */
    public static final String name = "aggregator";
     
    // ........................ member data .......................... //

    /** The NHI address prefix of the AS of the BGP speaker that formed the
     *  aggregate route. */
    public int ASNum;

    /** The IP address of the BGP speaker that formed the aggregate route. */
    public IPaddress ipaddr;


    // ----- constructor Aggregator ---------------------------------------- //
    /**
     * Constructs an aggregator path attribute with the AS and IP address
     * of the aggregating BGP speaker.
     *
     * @param nh   The AS NHI address prefix of the aggregating BGP speaker.
     * @param ipa  The IP address of the aggregating BGP speaker.
     */
    public Aggregator(int ASNum, IPaddress ipa) {
	super(TYPECODE, true, false, false);
	this.ASNum= ASNum;
	ipaddr = ipa;
    }

    // ----- Aggregator ---------------------------------------------------- //
    /**
     *
     */
    public Aggregator(byte [] bytes)
    {
	super(bytes);
    }

    // ----- Aggregator.copy ----------------------------------------------- //
    /**
     * Constructs and returns a copy of the attribute.
     *
     * @return a copy of the attribute
     */
    public Attribute copy() {
	return new Aggregator(ASNum, new IPaddress(ipaddr));
    }

    // ----- Aggregator.bytecount ------------------------------------------ //
    /**
     * Returns the number of octets (bytes) needed to represent this aggregator
     * path attribute in an update message.  The number is the sum of the two
     * octets needed for the attribute type (which contains attribute flags and
     * the attribute type code), the one octet needed for the attribute length,
     * and the six octets needed for the attribute value.
     *
     * @return the number of octets (bytes) needed to represent this aggregator
     *         discriminator path attribute in an update message
     */
    public int data_bytecount() {
	return 6;
    }

    // ----- Aggregator.equals --------------------------------------------- //
    /**
     * Determines whether or not this path attribute is equivalent to another.
     *
     * @param attrib  A path attribute to compare to this one.
     * @return true only if the two attributes are equivalent
     */
    public boolean equals(Attribute attrib) {
	return (attrib != null &&
		attrib instanceof Aggregator &&
		(ASNum == ((Aggregator)attrib).ASNum) &&
		ipaddr.equals(((Aggregator)attrib).ipaddr));
    }

    // ----- Aggregator.toString ------------------------------------------- //
    /**
     * Returns this path attribute as a string.
     *
     * @return the attribute as a string
     */
    public final String toString() {
	return ASNum + " " + ipaddr.toString();
    }

    // ----- Aggregator.toBytes -------------------------------------------- //
    /**
     *
     */
    public byte [] toBytes()
    {
	byte [] header= super.toBytes();
	byte [] bytes= new byte[header.length];
	System.arraycopy(header, 0, bytes, 0, header.length);
	int pos= header.length;
	// AS Number (2 bytes)
	bytes[pos++]= (byte) (ASNum >> 8);
	bytes[pos++]= (byte) (ASNum & 255);
	// IP Address (4 bytes)
	long ip_addr= ipaddr.val();
	bytes[pos++]= (byte) (ip_addr >> 24);
	bytes[pos++]= (byte) ((ip_addr >> 16) & 255);
	bytes[pos++]= (byte) ((ip_addr >> 8) & 255);
	bytes[pos++]= (byte) (ip_addr & 255);

	return bytes;
    }

    // ----- Aggregator.fromBytes ------------------------------------------ //
    /**
     *
     */
    public int fromBytes(byte [] bytes)
    {
	int pos= super.fromBytes(bytes);
	// AS Number
	ASNum= (bytes[pos++] << 8)+bytes[pos++];
	// IP Address
	ipaddr= new IPaddress((bytes[pos++] << 24)+
			      (bytes[pos++] << 16)+
			      (bytes[pos++] << 8)+
			      bytes[pos++]);

	return pos;
    }
	@Override
	public byte getTypeCode() {
		return this.TYPECODE;
	}


} // end class Aggregator
