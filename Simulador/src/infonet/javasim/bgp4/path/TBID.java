// =============================================================== //
// @(#)TBID.java
//
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
//
// @lastdate 15/11/2002
// =============================================================== //

package infonet.javasim.bgp4.path;

// ===== class infonet.javasim.bgp4.path.TBID =============== //
/**
 *
 */
public class TBID extends Attribute {

    // ......................... constants ....................... //
     
    /** The community path attribute type code. */
    public static final byte TYPECODE= 17;

    /** The name of the attribute as a string. */
    public static final String name= "tbid";
     
    public int val;
    
    // ----- constructor TBID ------------------------------------ //
    /**
     * Constructs a community path attribute with the given list of
     * community values. 
     *
     * @param v  A list of the community values.
     */
    public TBID(int val) {
	super(TYPECODE, false, true, false);
	this.val= val;
    }

    // ----- TBID ------------------------------------------------ //
    /**
     *
     */
    public TBID(byte [] bytes)
    {
	super(bytes);
    }

    // ----- TBID.copy ------------------------------------------- //
    /**
     * Constructs and returns a copy of the attribute.
     *
     * @return a copy of the attribute
     */
    public Attribute copy() {
	return new TBID(val);
    }

    // ----- TBID.bytecount -------------------------------------- //
    /**
     *
     */
    public int data_bytecount() {
	return 2;
    }

    // ----- TBID.equals ----------------------------------------- //
    /**
     * Determines whether or not this path attribute is equivalent to
     * another.
     *
     * @param attrib  A path attribute to compare to this one.
     * @return true only if the two attributes are equivalent
     */
    public boolean equals(Attribute attrib) {
	if (attrib == null || !(attrib instanceof TBID)) {
	    return false;
	}
	TBID tbid = (TBID) attrib;
	return (tbid.val == val);
    }

    // ----- TBID.toString --------------------------------------- //
    /**
     * Returns this path attribute as a string.
     *
     * @return the attribute as a string
     */
    public final String toString() {
	return ""+val;
    }

    // ----- TBID.toBytes ---------------------------------------- //
    /**
     *
     */
    public byte [] toBytes()
    {
	byte [] header= super.toBytes();
	byte [] bytes= new byte[bytecount()];
	System.arraycopy(header, 0, bytes, 0, header.length);
	int pos= header.length;
	bytes[pos++]= (byte) ((val >> 8)-128);
	bytes[pos++]= (byte) ((val & 255)-128);
	return bytes;
    }

    // ----- TBID.fromBytes -------------------------------------- //
    /**
     *
     */
    public int fromBytes(byte [] bytes)
    {
	int pos= super.fromBytes(bytes);
	if (pos == bytes.length-2)
	    val= ((((int) bytes[pos++])+128) << 8) + ((int) bytes[pos++])+128;
	else
	    throw new Error("TBID: incorrect attribute length");
	return pos;
    }
	@Override
	public byte getTypeCode() {
		return TBID.TYPECODE;
	}


} // end class TBID
