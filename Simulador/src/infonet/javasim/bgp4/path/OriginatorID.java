/**
 * OriginatorID.java
 *
 * @author BJ Premore
 */


package infonet.javasim.bgp4.path;


import infonet.javasim.util.IPaddress;


// ===== class drcl.inet.protocol.bgp4.path.OriginatorID =================== //
/**
 * The originator ID path attribute.  It records the IP address identifier of the router that originated the route into the IBGP mesh.  It is optional and non-transitive. Its length is 4 bytes.
 */
public class OriginatorID extends Attribute {

    // ......................... constants ........................... //
     
    /** The originator ID path attribute type code. */
    public static final byte TYPECODE = 9;

    /** The name of the attribute as a string. */
    public static final String name = "originator_id";
     
    // ........................ member data .......................... //

    /** The ID of the originating router. */
    public IPaddress id;


    // ----- constructor OriginatorID ---------------------------------------- //
    /**
     * Constructs an originator ID path attribute with the given router ID.
     *
     * @param ipa  The IP address ID of the originating router.
     */
    public OriginatorID(IPaddress ipa) {
	super(TYPECODE, true, false, false);
	id = ipa;
    }

    public OriginatorID(byte[] bytes)
    {
    	super(bytes);
    }
    // ----- OriginatorID.copy ----------------------------------------------- //
    /**
     * Constructs and returns a copy of the attribute.
     *
     * @return a copy of the attribute
     */
    public Attribute copy() {
	return new OriginatorID(new IPaddress(id));
    }

    // ----- OriginatorID.bytecount ------------------------------------------ //
    /**
     * Returns the number of octets (bytes) needed to represent this originator
     * ID attribute in an update message.  The number is the sum of the two
     * octets needed for the attribute type (which contains attribute flags and
     * the attribute type code), the one octet needed for the attribute length,
     * and the four octets needed for the attribute value.
     *
     * @return the number of octets (bytes) needed to represent this originator
     *         ID attribute in an update message
     */
    public int bytecount() {
	return 7;
    }

    // ----- OriginatorID.equals --------------------------------------------- //
    /**
     * Determines whether or not this path attribute is equivalent to another.
     *
     * @param attrib  A path attribute to compare to this one.
     * @return true only if the two attributes are equivalent
     */
    public boolean equals(Attribute attrib) {
	return (attrib != null &&
		attrib instanceof OriginatorID &&
		id.equals(((OriginatorID)attrib).id));
    }

    // ----- OriginatorID.toString ----------------------------------------- //
    /**
     * Returns this path attribute as a string.
     *
     * @return the attribute as a string
     */
    public final String toString() {
	return id.toString();
    }

    // ----- OriginatorID.data_bytecount ----------------------------------- //
    /**
     */
    public int data_bytecount()
    {
	return 4;
    }
    
    
    //<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
    // ----- OriginatorID.toBytes ----------------------------------------------- //
    /**
     *
     */
    public byte [] toBytes()
    {
	byte [] header= super.toBytes();
	byte [] bytes= new byte[header.length+data_bytecount()];
	System.arraycopy(header, 0, bytes, 0, header.length);
	int pos= header.length;

	byte [] ipaddr_bytes= id.bytes();

	/*String s= "< ";
	for (int i= 0; i < 5; i++)
	    s+= ipaddr_bytes[i]+" ";
	    System.out.println(s+">");*/

	System.arraycopy(ipaddr_bytes, 0, bytes, pos, 4);
	pos+= 4;

	return bytes;
    }

    // ----- OriginatorID.fromBytes --------------------------------------------- //
    /**
     *
     */
    public int fromBytes(byte [] bytes)
    {
	int pos= super.fromBytes(bytes);

	byte [] ipaddr_bytes= new byte[5];
	System.arraycopy(bytes, pos, ipaddr_bytes, 0, 4);
	ipaddr_bytes[4]= 32;

	/*String s= "< ";
	for (int i= 0; i < 5; i++)
	    s+= ipaddr_bytes[i]+" ";
	    System.out.println(s+">");*/

	id = new IPaddress(ipaddr_bytes);
	pos+= 4;
	
	return pos;
    }
	@Override
	public byte getTypeCode() {
		return OriginatorID.TYPECODE;
	}

    //>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
    

} // end class OriginatorID




