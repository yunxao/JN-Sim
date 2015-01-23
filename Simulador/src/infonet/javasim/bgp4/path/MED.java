// ========================================================================= //
// @(#)MED.java
//
// @author BJ Premore
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
//
// @lastdate 18/04/2002
// ========================================================================= //

package infonet.javasim.bgp4.path;

// ===== class drcl.inet.protocol.bgp4.path.MED ============================ //
/**
 * The multiple exit discriminator path attribute.  It is used to help
 * discriminate between multiple exit points to the same neighboring AS.  It is
 * optional and non-transitive.
 */
public class MED extends Attribute {

    // ......................... constants ........................... //
     
    /** The multiple exit discriminator path attribute type code. */
    public static final byte TYPECODE = 4;

    /** The name of the attribute as a string. */
    public static final String name = "med";
     
    // ........................ member data .......................... //

    /** The multiple exit discriminator value. */
    public long val;


    // ----- constructor MED ----------------------------------------------- //
    /**
     * Constructs a multiple exit discriminator path attribute with the given
     * value.
     *
     * @param v  The value of the multiple exit discriminator.
     */
    public MED(long v) {
	super(TYPECODE, true, false, false);
	val = v;
    }

    // ----- MED ----------------------------------------------------------- //
    /**
     *
     */
    public MED(byte [] bytes)
    {
	super(bytes);
    }

    // ----- MED.copy ------------------------------------------------------ //
    /**
     * Constructs and returns a copy of the attribute.
     *
     * @return a copy of the attribute
     */
    public Attribute copy() {
	return new MED(val);
    }

    // ----- MED.data_bytecount ------------------------------------------------- //
    /**
     * Returns the number of octets (bytes) needed to represent this multiple
     * exit discriminator path attribute in an update message.  The number is the
     * sum of the two octets needed for the attribute type (which contains
     * attribute flags and the attribute type code), the one octet needed for the
     * attribute length, and the four octets needed for the attribute value.
     *
     * @return the number of octets (bytes) needed to represent this multiple
     *         exit discriminator path attribute in an update message
     */
    public int data_bytecount() {
	return 4;
    }

    // ----- MED.equals ---------------------------------------------------- //
    /**
     * Determines whether or not this path attribute is equivalent to another.
     *
     * @param attrib  A path attribute to compare to this one.
     * @return true only if the two attributes are equivalent
     */
    public boolean equals(Attribute attrib) {
	return (attrib != null &&
		attrib instanceof MED &&
		val == ((MED)attrib).val);
    }

    // ----- MED.toString -------------------------------------------------- //
    /**
     * Returns this path attribute as a string.
     *
     * @return the attribute as a string
     */
    public final String toString() {
	return "" + val;
    }

    // ----- MED.toBytes --------------------------------------------------- //
    /**
     *
     */
    public byte [] toBytes()
    {
	byte [] header= super.toBytes();
	byte [] bytes= new byte[header.length+this.length()];
	System.arraycopy(header, 0, bytes, 0, header.length);
	int pos= header.length;
	// Multi-Exit-Discriminator
	bytes[pos++]= (byte) (val >> 24);
	bytes[pos++]= (byte) ((val >> 16) & 255);
	bytes[pos++]= (byte) ((val >> 8) & 255);
	bytes[pos++]= (byte) (val & 255);

	return bytes;
    }

    // ----- MED.fromBytes ------------------------------------------------- //
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

	return pos;
    }
	@Override
	public byte getTypeCode() {
		return MED.TYPECODE;
	}
	public int length(){
		return 4;
	}


} // end class MED
