/////////////////////////////////////////////////////////////////////
// @(#)Attribute.java
//
// @author BJ Premore
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
// @lastdate 18/04/2002
/////////////////////////////////////////////////////////////////////

package infonet.javasim.bgp4.path;

import tid.utils.BytesUtils;

// ===== class drcl.inet.protocol.bgp4.path.Attribute ====================== //
/**
 * A BGP path attribute.  Path attributes are used to keep information about
 * routes helps in making routing decisions.
 */
public abstract class Attribute {

    // ......................... constants ........................... //

    /** For undefined string values. */
    public static final String undefined = "undefined";
     
    /** The lowest path attribute type code value. */
    public static final int MIN_TYPECODE =  1;

    /** The highest path attribute type code value used in the simulation.  The
     *  actual maximum typecode used in practice may be higher. */
    public static final int MAX_TYPECODE = 17;

    /** The names of the attributes as strings. */
    public static final String[] names = { undefined,
					   Origin.name,
					   ASpath.name,
					   NextHop.name,
					   MED.name,
					   LocalPref.name,
					   AtomicAggregate.name,
					   Aggregator.name,
					   Community.name,
					   OriginatorID.name,
					   ClusterList.name,
					   undefined,
					   undefined,
					   undefined,
					   undefined,
					   undefined,
					   ExtendedCommunities.name,
					   TBID.name
    };

    // ........................ member data .......................... //

    /** Defines whether the path attribute is optional (true) or well-known
     *  (false). */
    public boolean opt;
  
    /** Defines whether an optional attribute is transitive (true) or
     *  non-transitive (false).  For well-known attributes it must be true. */
    public boolean trans;

    /** Defines whether or not the information contained in the optional
     *  transitive attribute is partial (true) or complete (false).  For
     *  well-known attributes and for optional non-transitive attributes, it
     *  must be false. */
    public boolean partial;

    public byte type_code;

    // ----- constructor Attribute ----------------------------------------- //
    /**
     * Constructs a path attribute of the given type and categories.
     *
     * @param o    Whether or not this is an optional path attribute.
     * @param t    Whether or not this is a transitive path attribute.
     * @param p    Whether or not this is a partial path attribute.
     */
    public Attribute(byte type_code, boolean o, boolean t, boolean p) {
	opt     = o;
	trans   = t;
	partial = p;
	this.type_code= type_code;
    }

    // ----- Attribute ----------------------------------------------------- //
    /**
     *
     */
    public Attribute(byte [] bytes)
    {
	super();
	fromBytes(bytes);
    }

    // ----- Attribute.copy ------------------------------------------------ //
    /**
     * Constructs and returns a copy of the attribute.
     *
     * @return a copy of the attribute
     */
    public abstract Attribute copy();

    // ----- Attribute.bytecount ------------------------------------------- //
    /**
     * Calculates and returns the number of octets (bytes) needed to represent
     * this path attribute in an update message.  The number is the sum of the
     * two octets needed for the attribute type (which contains attribute flags
     * and the attribute type code), the one or two octets needed for the
     * attribute length, and the variable number of octets needed for the
     * attribute value.
     *
     * @return the number of octets (bytes) needed to represent this path
     *         attribute in an update message
     */
    public abstract int data_bytecount();

    public int bytecount()
    {
	int data_bc= data_bytecount();
	if (data_bc > 255)
	    return data_bc+4;
	else
	    return data_bc+3;
    }

    // ----- Attribute.equals ---------------------------------------------- //
    /**
     * Determines whether or not this path attribute is equivalent to another.
     *
     * @param attrib  A path attribute to compare to this one.
     * @return true only if the two attributes are equivalent
     */
    public abstract boolean equals(Attribute attrib);

    // ----- Attribute.toBytes --------------------------------------------- //
    /**
     *
     */
    public byte [] toBytes()
    {
	int attr_length= data_bytecount();
	
	byte [] bytes= new byte[(attr_length < 256)?3:4];
	int pos= 0;
	// Attribute Flags
	bytes[pos++]= (byte) ((opt?128:0)+ // Optional
			      (trans?64:0)+ // Transitive
			      (partial?32:0)+ // Partial
			      ((attr_length > 255)?16:0)); // Extended Length
	// Attibute Type Code
	bytes[pos++]= type_code;
	// Length
	if (attr_length < 256) {
	    bytes[pos++]= (byte) (attr_length-128);
	} else {
	    bytes[pos++]= (byte) ((attr_length >> 8)-128);
	    // Extended Length
	    bytes[pos++]= (byte) ((attr_length & 255)-128);
	}

	return bytes;
    }

    // ----- Attribute.fromBytes ------------------------------------------- //
    /**
     *
     */
    public int fromBytes(byte [] bytes)
    {
	int pos= 0;
	byte flags= bytes[pos++];
	opt= ((flags & 128) != 0);
	trans= ((flags & 64) != 0);
	partial= ((flags & 32) != 0);
	type_code= bytes[pos++];
	int length= (((int) bytes[pos++])+128);
	// Extended length ?
	if ((flags & 16) != 0) {
	    length<<= 8;
	    length+= (((int) bytes[pos++])+128);
	}

	return pos;
    }

    // ----- Attribute.buildNewAttribute ----------------------------------- //
    /**
     *
     */
    public static Attribute buildNewAttribute(byte [] bytes)
    {
    	// If bytes hat more bytes that needed, trunk the array
    	byte flags= bytes[0];
    	int length = 0;
    	// Extended length ?
    	// If it's normal length size is 3 (Header) + bytes[2]
    	// If it's extended length, size is 4 (header) + bytes[2-3]
    	if ((flags & 16) != 0) {
    		
    		length= (((int) bytes[2])+128);
    	    length<<= 8;
    	    length+= (((int) bytes[3])+128)+4;
    	    
    	}
    	else 
    		length= (((int) bytes[2])+128)+3;

    	
    	if (bytes.length > length){
    		bytes = BytesUtils.arrayCopy(bytes, length);
    	}
	
	switch (bytes[1]) {
	case Origin.TYPECODE:
	    return new Origin(bytes);
	case ASpath.TYPECODE:
	    return new ASpath(bytes);
	case NextHop.TYPECODE:
	    return new NextHop(bytes);
	case MED.TYPECODE:
	    return new MED(bytes);
	case LocalPref.TYPECODE:
	    return new LocalPref(bytes);
	case AtomicAggregate.TYPECODE:
	    return new AtomicAggregate(bytes);
	case Aggregator.TYPECODE:
	    return new Aggregator(bytes);
	case Community.TYPECODE:
	    return new Community(bytes);
	case ClusterList.TYPECODE:
	    return new ClusterList(bytes);
	case ExtendedCommunities.TYPECODE:
	    return new ExtendedCommunities(bytes);
	case TBID.TYPECODE:
	    return new TBID(bytes);
	case OriginatorID.TYPECODE:
		return new OriginatorID(bytes);
	default:
	    return null;
	}
    }
    public abstract byte getTypeCode();
    
} // end class Attribute
