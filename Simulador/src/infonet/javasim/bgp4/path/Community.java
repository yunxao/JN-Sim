// =============================================================== //
// @(#)Community.java
//
// @author BJ Premore
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
//
// @lastdate 09/08/2002
// =============================================================== //

package infonet.javasim.bgp4.path;

import java.util.ArrayList;

// ===== class infonet.javasim.bgp4.path.Community =============== //
/**
 * The community path attribute.  It is used to group routes together
 * in order to simplify the configuration of complex routing policies.
 * A route may be a member of any number of communities. The attribute
 * is optional non-transitive.
 */
public class Community extends Attribute {

    // ......................... constants ....................... //
     
    /** The community path attribute type code. */
    public static final byte TYPECODE= 8;

    /** The name of the attribute as a string. */
    public static final String name= "community";
     
    // ........................ member data ...................... //

    /** The list of community values.
     * WARNING: do not initialize 'vals' here !!!
     */
    public ArrayList<Integer> vals;


    // ----- constructor Community ------------------------------- //
    /**
     * Constructs a community path attribute with the given list of
     * community values. 
     *
     * @param v  A list of the community values.
     */
    public Community(ArrayList<Integer> v) {
	super(TYPECODE, true, false, false);
	vals= v;
    }

    // ----- Community ------------------------------------------- //
    /**
     *
     */
    public Community()
    {
	this(new ArrayList<Integer>());
    }

    // ----- Community ------------------------------------------- //
    /**
     *
     */
    public Community(byte [] bytes)
    {
	super(bytes);
    }

    // ----- Community.append ------------------------------------ //
    /**
     *
     */
    public void append(int comm)
    {
	if (vals == null) {
	    vals= new ArrayList<Integer>();
	} else {
	    // Check that this community does not already exist.
	    for (int i= 0; i < vals.size(); i++)
		if (((Integer) vals.get(i)).intValue() == comm)
		return;
	}
	// Add community
	vals.add(new Integer(comm));
    }

    // ----- Community.copy -------------------------------------- //
    /**
     * Constructs and returns a copy of the attribute.
     *
     * @return a copy of the attribute
     */
    public Attribute copy() {
	ArrayList<Integer> v= null;
	if (vals != null) {
	    v= new ArrayList<Integer>();
	    for (int i=0; i<vals.size(); i++) {
		v.add(vals.get(i));
	    }
	}
	return new Community(v);
    }

    // ----- Community.bytecount --------------------------------- //
    /**
     * Returns the number of octets (bytes) needed to represent this
     * community path attribute in an update message. The number is
     * the sum of the two octets needed for the attribute type (which
     * contains attribute flags and the attribute type code), the one
     * or two octets needed for the attribute length, and the variable
     * number of octets needed for the attribute value.
     *
     * @return the number of octets (bytes) needed to represent this
     *         community path attribute in an update message
     */
    public int data_bytecount() {
	int octets= 0; // 2 octets for the attribute type
	if (vals != null) {
	    octets += 4*vals.size(); // 4 octets per community value
	}
	return octets;
    }

    // ----- Community.equals ------------------------------------ //
    /**
     * Determines whether or not this path attribute is equivalent to
     * another.
     *
     * @param attrib  A path attribute to compare to this one.
     * @return true only if the two attributes are equivalent
     */
    public boolean equals(Attribute attrib) {
	if (attrib == null || !(attrib instanceof Community)) {
	    return false;
	}
	Community c = (Community)attrib;
	if ((vals != null) && (c.vals != null)) {
	    if (vals.size() != c.vals.size()) {
		return false;
	    }
	    for (int i=0; i<vals.size(); i++) {
		if (!c.vals.contains(vals.get(i))) {
		    return false;
		}
	    }
	    return true;
	} else {
	    return (vals == c.vals);
	}
    }

    // ----- Community.toString ---------------------------------- //
    /**
     * Returns this path attribute as a string.
     *
     * @return the attribute as a string
     */
    public final String toString() {
	String str ="";//)= "Community: ";
	if (vals != null) {
	    for (int i=0; i<vals.size(); i++) {
		if (i != 0) {
		    str += " ";
		}
		str += ((Integer) vals.get(i)).intValue();
	    }
	}
	return str;
    }

    // ----- Community.toBytes ----------------------------------- //
    /**
     *
     */
    public byte [] toBytes()
    {
	byte [] header= super.toBytes();
	byte [] bytes= new byte[bytecount()];
	System.arraycopy(header, 0, bytes, 0, header.length);
	int pos= header.length;
	if (vals != null) {
	    for (int i= 0; i < vals.size(); i++) {
		long val= ((Integer) vals.get(i)).intValue();
		bytes[pos++]= (byte) (val >> 24);
		bytes[pos++]= (byte) ((val >> 16) & 255);
		bytes[pos++]= (byte) ((val >> 8) & 255);
		bytes[pos++]= (byte) (val & 255);
	    }
	}

	return bytes;
    }

    // ----- Community.fromBytes --------------------------------- //
    /**
     *
     */
    public int fromBytes(byte [] bytes)
    {
	int pos= super.fromBytes(bytes);
	while (pos < bytes.length) {
	    int val= ((bytes[pos++] << 24)+
		       (bytes[pos++] << 16)+
		       (bytes[pos++] << 8)+
		       bytes[pos++]);
	    if (vals == null)
		vals= new ArrayList<Integer>();
	    vals.add(new Integer(val));
	}
	return pos;
    }

    // ----- Community.build ------------------------------------- //
    /**
     * Converts a string definition of a community to its value. In
     * the future, this method might support "A:B" communities...
     */
    public static int build(String val)
    {
	if ((val == null) || (val.length() == 0))
	    throw new Error("Error: [community] build from bad value !");
	if (val.contains(":")){
		String[] values = val.split(":");
		if (values.length > 2){
			throw new Error("Error: [community] build from bad value !");
		}
		Integer superior = Integer.valueOf(values[0]);
		Integer inferior = Integer.valueOf(values[1]);
		// A:B = A * 2¹⁶ + B; 2¹⁶ = 65536
		// A:B = A * 2^16 + B; 2^16 = 65536
		return superior * 65536 + inferior;
	}
	return Integer.valueOf(val).intValue();
    }
	@Override
	public byte getTypeCode() {
		return Community.TYPECODE;
	}


} // end class Community
