// ========================================================================= //// @(#)LocalPref.java
//
// @author BJ Premore
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
//
// @lastdate 18/04/2002
// ========================================================================= //

package infonet.javasim.bgp4.path;

// ===== class drcl.inet.protocol.bgp4.path.LocalPref ================================== //
/**
 * The local preference path attribute.  It is used when comparing the
 * preferability of different routes with the same destinations.  It is
 * well-known and discretionary.
 */
public class LocalPref extends Attribute {

    // ......................... constants ........................... //
     
    /** The local preference path attribute type code. */
    public static final byte TYPECODE = 5;

    /** The name of the attribute as a string. */
    public static final String name = "local_pref";
     
    // ........................ member data .......................... //

    /** The local preference value. */
    public long val;


    // ----- constructor LocalPref ------------------------------------------- //
    /**
     * Constructs a local preference path attribute with the given value.
     *
     * @param v  The value of the local preference.
     */
    public LocalPref(long v) {
	super(TYPECODE, false, false, false);
	val= v;
    }

    // ----- LocalPref ----------------------------------------------------- //
    /**
     *
     */
    public LocalPref(byte [] bytes)
    {
	super(bytes);
    }

    // ----- LocalPref.copy -------------------------------------------------- //
    /**
     * Constructs and returns a copy of the attribute.
     *
     * @return a copy of the attribute
     */
    public Attribute copy() {
	return new LocalPref(val);
    }

    // ----- LocalPref.bytecount --------------------------------------------- //
    /**
     * Returns the number of octets (bytes) needed to represent this local
     * preference path attribute in an update message.  The number is the sum of
     * the two octets needed for the attribute type (which contains attribute
     * flags and the attribute type code), the one octet needed for the attribute
     * length, and the four octets needed for the attribute value.
     *
     * @return the number of octets (bytes) needed to represent this local
     *         preference discriminator path attribute in an update message
     */
    public int data_bytecount() {
	return 4;
    }

    // ----- LocalPref.equals ------------------------------------------------ //
    /**
     * Determines whether or not this path attribute is equivalent to another.
     *
     * @param attrib  A path attribute to compare to this one.
     * @return true only if the two attributes are equivalent
     */
    public boolean equals(Attribute attrib) {
	return (attrib != null &&
		attrib instanceof LocalPref &&
		val == ((LocalPref)attrib).val);
    }

    // ----- LocalPref.toString ---------------------------------------------- //
    /**
     * Returns this path attribute as a string.
     *
     * @return the attribute as a string
     */
    public final String toString() {
	return "" + val;
    }

    // ----- LocalPref.toBytes --------------------------------------------- //
    /**
     *
     */
    public byte [] toBytes()
    {
	byte [] header= super.toBytes();
	byte [] bytes= new byte[header.length+4];
	System.arraycopy(header, 0, bytes, 0, header.length);
	int pos= header.length;

	bytes[pos++]= (byte) (val >> 24);
	bytes[pos++]= (byte) ((val >> 16) & 255);
	bytes[pos++]= (byte) ((val >> 8) & 255);
	bytes[pos++]= (byte) (val & 255);
//	System.out.println("Guardando: "+val);
	return bytes;
    }

    // ----- LocalPref.fromBytes ------------------------------------------- //
    /**
     *
     */
    public int fromBytes(byte [] bytes)
    {
	int pos= super.fromBytes(bytes);
	val= ((bytes[pos++] << 24)+
	      (bytes[pos++] << 16)+
	      (bytes[pos++] << 8)+
	      bytes[pos++]);
//	System.out.println("Cargando: "+val);

	return pos;
    }
	@Override
	public byte getTypeCode() {
		return this.TYPECODE;
	}


} // end class LocalPref
