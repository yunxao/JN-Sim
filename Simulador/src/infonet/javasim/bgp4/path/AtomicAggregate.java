// ========================================================================= //
// @(#)AtomicAggregate.java
//
// @author BJ Premore
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
//
// @lastdate 18/04/2002
// ========================================================================= //


package infonet.javasim.bgp4.path;


// ===== class drcl.inet.protocol.bgp4.path.AtomicAggregate ================ //
/**
 * The atomic aggregate path attribute.  It is used to inform other BGP
 * speakers that the local system selected a less specific route without
 * selecting a more specific route which is included in it.  It is well-known
 * and discretionary.
 */
public class AtomicAggregate extends Attribute {

    // ......................... constants ........................... //
     
    /** The atomic aggregate path attribute type code. */
    public static final byte TYPECODE = 6;

    /** The name of the attribute as a string. */
    public static final String name = "atomic_agg";
     
    // ........................ member data .......................... //


    // ----- constructor AtomicAggregate ----------------------------------- //
    /**
     * Constructs an atomic aggregate path attribute.
     */
    public AtomicAggregate() {
	super(TYPECODE, false, false, false);
    }

    // ----- AtomicAggregate ----------------------------------------------- //
    /**
     *
     */
    public AtomicAggregate(byte [] bytes)
    {
	super(bytes);
    }

    // ----- AtomicAggregate.copy ------------------------------------------ //
    /**
     * Constructs and returns a copy of the attribute.
     *
     * @return a copy of the attribute
     */
    public Attribute copy() {
	return new AtomicAggregate();
    }

    // ----- AtomicAggregate.bytecount ------------------------------------- //
    /**
     * Returns the number of octets (bytes) needed to represent this atomic
     * aggregate path attribute in an update message.  The number is the sum of
     * the two octets needed for the attribute type (which contains attribute
     * flags and the attribute type code), the one octet needed for the attribute
     * length, and the zero octets needed for the attribute value.
     *
     * @return the number of octets (bytes) needed to represent this atomic
     *         aggregate path attribute in an update message
     */
    public int data_bytecount() {
	return 0;
    }

    // ----- AtomicAggregate.equals ---------------------------------------- //
    /**
     * Determines whether or not this path attribute is equivalent to another.
     *
     * @param attrib  A path attribute to compare to this one.
     * @return true only if the two attributes are equivalent
     */
    public boolean equals(Attribute attrib) {
	return (attrib != null && attrib instanceof AtomicAggregate);
    }

    // ----- AtomicAggregate.toString -------------------------------------- //
    /**
     * Returns this path attribute as a string.
     *
     * @return the attribute as a string
     */
    public final String toString() {
	return "";
    }

    // ----- AtomicAggregate.toBytes --------------------------------------- //
    /**
     *
     */
    public byte [] toBytes()
    {
	return super.toBytes();
    }

    // ----- AtomicAggregate.fromBytes ------------------------------------- //
    /**
     *
     */
    public int fromBytes(byte [] bytes)
    {
	int pos= super.fromBytes(bytes);

	return pos;
    }

	@Override
	public byte getTypeCode() {
		return this.TYPECODE;
	}

} // end class AtomicAggregate
