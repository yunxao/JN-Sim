// ------------------------------------------------------------------------- //
// @(#)Origin.java
//
// @author BJ Premore
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
//
// @lastdate 18/04/2002
// ------------------------------------------------------------------------- //

package infonet.javasim.bgp4.path;

// ===== class drcl.inet.protocol.bgp4.path.Origin ========================= //
/**
 * The origin path attribute.  It describes the origin of the path information,
 * which can be IGP (Interior Gateway Protocol), EGP (Exterior Gateway
 * Protocol), or INCOMPLETE.  It is well-known and mandatory.
 */
public class Origin extends Attribute {

    // ......................... constants ........................... //

    /** The origin path attribute type code. */
    public static final byte TYPECODE = 1;

    /** The name of the attribute as a string. */
    public static final String name = "origin";

    /** Indicates that the path information was originated by an interior gateway
     *  protocol. */
    public static final byte IGP = 0;

    /** Indicates that the path information was originated by an exterior gateway
     *  protocol. */
    public static final byte EGP = 1;

    /** Indicates that the path information was originated by some means other
     *  than an IGP or an EGP.  In other words, the origin information is
     *  incomplete. */
    public static final byte INC = 2;

    // ........................ member data .......................... //

    /** The origin type value. */
    public byte typ;


    // ----- constructor Origin -------------------------------------------- //
    /**
     * Constructs an origin path attribute with the given type value.
     *
     * @param t  The origin type value.
     */
    public Origin(byte typ) {
	super(TYPECODE, false, true, false);
	this.typ= typ;
    }

    // --- Origin ---------------------------------------------------------- //
    /**
     *
     */
    public Origin(byte [] bytes)
    {
	super(bytes);
    }

    // ----- Origin.copy --------------------------------------------------- //
    /**
     * Constructs and returns a copy of the attribute.
     *
     * @return a copy of the attribute
     */
    public Attribute copy() {
	return new Origin(typ);
    }

    // ----- Origin.bytecount ------------------------------------------------ //
    /**
     * Returns the number of octets (bytes) needed to represent this origin path
     * attribute in an update message.  The number is the sum of the two octets
     * needed for the attribute type (which contains attribute flags and the
     * attribute type code), the one octet needed for the attribute length, and
     * the one octet needed for the attribute value.
     *
     * @return the number of octets (bytes) needed to represent this origin path
     *         attribute in an update message
     */
    public int data_bytecount() {
	return 1;
    }

    // ----- Origin.equals --------------------------------------------------- //
    /**
     * Determines whether or not this path attribute is equivalent to another.
     *
     * @param attrib  A path attribute to compare to this one.
     * @return true only if the two attributes are equivalent
     */
    public boolean equals(Attribute attrib) {
	return (attrib != null &&
		attrib instanceof Origin &&
		typ == ((Origin)attrib).typ);
    }

    // ----- Origin.toString ------------------------------------------------- //
    /**
     * Returns this path attribute as a string.
     *
     * @return the attribute as a string
     */
    public final String toString() {
	switch (typ) {
	case IGP:  return "IGP";
	case EGP:  return "EGP";
	case INC:  return "INC";
	default:   return null;
	}
    }

    // ----- Origin.toBytes ------------------------------------------------ //
    /**
     *
     */
    public byte [] toBytes()
    {
	byte [] header= super.toBytes();
	byte [] bytes= new byte[1+header.length];
	System.arraycopy(header, 0, bytes, 0, header.length);
	int pos= header.length;
	bytes[pos++]= typ;

	return bytes;
    }

    // ---- Origin.fromBytes ----------------------------------------------- //
    /**
     *
     */
    public int fromBytes(byte [] bytes)
    {
	int pos= super.fromBytes(bytes);
	typ= bytes[pos++];

	return pos;
    }
	@Override
	public byte getTypeCode() {
		return Origin.TYPECODE;
	}


} // end class Origin
