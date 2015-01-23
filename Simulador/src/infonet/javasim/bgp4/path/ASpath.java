/////////////////////////////////////////////////////////////////////
//
// @author BJ Premore
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
// @lastdate 18/04/2002
/////////////////////////////////////////////////////////////////////

package infonet.javasim.bgp4.path;

import infonet.javasim.bgp4.Debug;
import infonet.javasim.util.BytesUtil;

import java.util.ArrayList;


// ===== class drcl.inet.protocol.bgp4.path.ASpath ===================================== //
/**
 * The AS path attribute.  An AS path is composed of a sequence of AS path
 * segments, where each segment is either an unordered set of AS numbers or an
 * ordered sequence of AS numbers.  It is well-known and mandatory.
 */
public class ASpath extends Attribute {

    // ......................... constants ........................... //
     
    /** The AS path attribute type code. */
    public static final byte TYPECODE = 2;
     
    /** The name of the attribute as a string. */
    public static final String name = "as_path";
     
    // ........................ member data .......................... //

    /** An ordered list of AS path segments. */
    public ArrayList<Segment> segs;


    // ----- constructor ASpath -------------------------------------------- //
    /**
     * Constructs an AS path attribute given a list of path segments.
     *
     * @param l  A list of path segments.
     */
    public ASpath(ArrayList<Segment> l) {
	super(TYPECODE, false, true, false);
	segs = l;
    }

    // ----- ASpath -------------------------------------------------------- //
    /**
     *
     */
    public ASpath(byte [] bytes)
    {
	super(bytes);
    }

    // ----- ASpath.copy --------------------------------------------------- //
    /**
     * Constructs and returns a copy of the attribute.
     *
     * @return a copy of the attribute
     */
    public Attribute copy() {
	ArrayList<Segment> l = new ArrayList<Segment>();
	if (segs != null)
		for (int i=0; i<segs.size(); i++) {
			l.add(new Segment((Segment)segs.get(i)));
		}
	return new ASpath(l);
    }

    // ----- ASpath.length --------------------------------------------------- //
    /**
     * Returns the number of ASes in the path.
     *
     * @return the number of ASes in the path
     */
    public final int length() {
	if (segs.size() == 0) {
	    return 0;
	} else {
	    int len = 0;
	    for (int i=0; i<segs.size(); i++) {
		len += ((Segment)segs.get(i)).size();
	    }
	    return len;
	}
    }

    // ----- ASpath.contains ------------------------------------------------- //
    /**
     * Determines whether or not the path contains a given AS.
     *
     * @param asnh  The NHI prefix address of the AS to look for in the AS path.
     * @return true only if the AS was in the AS path
     */
    public final boolean contains(int ASNum/*String asnh*/) {
	for (int i=0; i<segs.size(); i++) {
	    if (((Segment)segs.get(i)).contains(ASNum)) {
		return true;
	    }
	}
	return false;
    }

    // ----- ASpath.append_segment ------------------------------------------- //
    /**
     * Appends a path segment to the list of segments.
     *
     * @param ps  The path segment to append to the list of segments.
     */
    public final void append_segment(Segment ps) {
	segs.add(ps);
    }

    // ----- ASpath.prepend_as ----------------------------------------------- //
    /**
     * Prepends an AS NHI address prefix to an AS_SEQUENCE segment at the
     * beginning of the list of segments.  A new AS_SEQUENCE segment is created
     * at the beginning if necessary.
     *
     * @param asnh  The AS NHI address prefix to prepend.
     */
    public final void prepend_as(int ASNum/*String asnh*/) {
	if (segs.size() == 0) {
	    Segment seg = new Segment(Segment.SEQ, new ArrayList<Integer>());
	    seg.append_as(ASNum/*asnh*/);
	    segs.add(seg);
	} else {
	    if (((Segment)segs.get(0)).typ == Segment.SEQ) {
		// insert the AS number at the beginning of the list
		((Segment)segs.get(0)).prepend_as(ASNum/*asnh*/);
	    } else {
		// create a new segment of type AS_SEQUENCE
		Segment seg = new Segment(Segment.SEQ, new ArrayList<Integer>());
		seg.append_as(ASNum/*asnh*/);
		segs.add(0,seg);
	    }
	}
    }

    // ----- ASpath.bytecount ------------------------------------------------ //
    /**
     * Returns the number of octets (bytes) needed to represent this AS path
     * attribute in an update message.  The number is the sum of the two octets
     * needed for the attribute type (which contains attribute flags and the
     * attribute type code), the one or two octets needed for the attribute
     * length, and the variable number of octets needed for the attribute value.
     *
     * @return the number of octets (bytes) needed to represent this AS path
     *         attribute in an update message
     */
    public int data_bytecount() {
	int octets= 0;
	if (segs == null)
		segs = new ArrayList<Segment>();
	try{
	for (int i=0; i<segs.size(); i++) {
	    // 1 octet for the seg type, 1 for seg length, 2 per AS# in segment
	    octets += 1 + 1 + 2*(((Segment)segs.get(i)).size());
	}
	} catch (Exception e){
		System.out.println("");
	}
	return octets;
    }

    // ----- ASpath.equals --------------------------------------------------- //
    /**
     * Determines whether or not this path attribute is equivalent to another.
     *
     * @param attrib  A path attribute to compare to this one.
     * @return true only if the two attributes are equivalent
     */
    public boolean equals(Attribute attrib) {
	if (attrib == null || !(attrib instanceof ASpath)) {
	    return false;
	}
	ASpath asp = (ASpath)attrib;
	if (segs.size() != asp.segs.size()) {
	    return false;
	}
	for (int i=0; i<segs.size(); i++) {
	    if (!segs.get(i).equals(asp.segs.get(i))) {
		return false;
	    }
	}
	return true;
    }

    // ----- ASpath.toMinString(char,boolean) -------------------------------- //
    /**
     * Returns the AS path as a string, leaving out set/sequence information.
     *
     * @param sepchar  The character used to separate AS numbers in the list.
     * @param usenhi   Whether or not to show AS numbers as NHI address prefixes.
     * @return the AS path as a string, without set/sequence info
     */
    public final String toMinString(char sepchar) {
	String str = "";
	for (int i=0; i<segs.size(); i++) {
	    if (i != 0) {
		str += sepchar;
	    }
	    str += ((Segment)segs.get(i)).toMinString(sepchar);
	}
	return str;
    }

    // ----- ASpath.toMinString() -------------------------------------------- //
    /**
     * Returns the AS path as a string, leaving out set/sequence information.
     *
     * @return the AS path as a string, without set/sequence info
     */
    public final String toMinString() {
	return toMinString(' ');
    }

    // ----- ASpath.toString ------------------------------------------------- //
    /**
     * Returns the AS path as a string.
     *
     * @return the AS path as a string
     */
    public final String toString() {
	String str = "";
	for (int i=0; i<segs.size(); i++) {
	    if (i != 0) {
		str += " ";
	    }
	    str += segs.get(i);
	}
	return str;
    }

    // Moved from Monitor.java ... (bqu)


    // ----- Monitor.aspath2bytes -------------------------------------------- //
    /**
     * Converts a simple AS path (no set or sequence information) to a series of
     * bytes.  The conversion may be done using either traditional AS numbers
     * (integers) or AS-NHI addresses.  If NHI addressing is used, each AS-NHI
     * value is preceded by one byte which indicates the total number of bytes in
     * that AS-NHI.  The very first byte in the overall conversion represents the
     * total number of ASes in the path list.
     *
     * @param aspath  The AS path to convert to bytes.
     * @param bytes   A byte array in which to place the results.
     * @param bindex  The index into the given byte array at which to begin
     *                placing the results.
     * @param usenhi  Whether or not to use NHI addressing.
     * @return  the total number of bytes after conversion (including size byte)
     */
    public static int aspath2bytes(ASpath aspath, byte[] bytes, int bindex/*,
									    boolean usenhi*/) {

	if (aspath == null || aspath.length() == 0) {
	    // no AS path
	    bytes[bindex] = (byte)0;
	    return 1;
	}

	int startindex = bindex;

	bytes[bindex++] = (byte)aspath.length();

	/*
	  if (usenhi) {
	  String aspathnhi = aspath.toMinString(' ',true);
	  String nh = null;

	  int previndex = 0, curindex = 0;
	  while ((curindex = aspathnhi.indexOf(" ", previndex)) >= 0) {
	  nh = aspathnhi.substring(previndex,curindex);
	  bindex += nh2bytes(nh, bytes, bindex);
	  previndex = curindex+1;
	  }
	  nh = aspathnhi.substring(previndex); // last AS-NHI in string
	  bindex += nh2bytes(nh, bytes, bindex);

	  } else { // using traditional AS number format (plain integers)
	*/
	String aspathints = aspath.toMinString(' ');
	int previndex = 0, curindex = 0;
	while ((curindex = aspathints.indexOf(" ", previndex)) >= 0) {
	    int asnum = new Integer(aspathints.substring(previndex,curindex)).
		intValue();
	    bindex += BytesUtil.int2bytes(asnum,bytes,bindex);
	    previndex = curindex+1;
	}
	// last AS number in string
	int asnum = new Integer(aspathints.substring(previndex)).intValue();
	bindex += BytesUtil.int2bytes(asnum,bytes,bindex);
	/*
	  }
	*/
    
	return bindex - startindex;
    }

    // ----- Monitor.bytes2aspath -------------------------------------------- //
    /**
     * Converts a series of bytes to a simple AS path (no set or sequence
     * information).  The conversion may result in either traditional AS numbers
     * (integers) or AS-NHI addresses.  If NHI addressing is used, each AS-NHI
     * value must be preceded by one byte which indicates the total number of
     * bytes in that AS-NHI.  The very first byte must represent the total number
     * of ASes in the path list.
     *
     * @param aspath  A StringBuffer into which the results will be placed.
     *                It <em>must</em> be initialized to the empty string.
     * @param bytes   The byte array to convert to a simple AS path.
     * @param bindex  The index into the given byte array from which to begin
     *                converting.
     * @param usenhi  Whether or not to use NHI addressing.
     * @return the total number of bytes used in the conversion (including size
     *         byte)
     */
    public static int bytes2aspath(StringBuffer aspath, byte[] bytes,
				   int bindex/*, boolean usenhi*/) {
	Debug.gaffirm(aspath.length()==0, "invalid StringBuffer (must be \"\")");
	int startindex = bindex;
	int pathlen = (int)bytes[bindex++];


	for (int i=1; i<=pathlen; i++) {
	    aspath.append(BytesUtil.bytes2int(bytes,bindex));
	    bindex += BytesUtil.bytes_per_int;
	    if (i != pathlen) {
		aspath.append(" ");
	    }
	}
	/*
	  }
	*/
    
	return bindex - startindex;
    }

    // ----- ASpath.toBytes() ---------------------------------------------- //
    /**
     *
     */
    public byte [] toBytes()
    {
	byte [] header= super.toBytes();
	byte [] bytes= new byte[bytecount()];
	System.arraycopy(header, 0, bytes, 0, header.length);
	int pos= header.length;
	for (int i= 0; i < segs.size(); i++) {
	    byte [] segment= ((Segment) segs.get(i)).toBytes();
	    System.arraycopy(segment, 0, bytes, pos, segment.length);
	    pos+= segment.length;
	}
	return bytes;
    }

    // ----- ASpath.fromBytes ---------------------------------------------- //
    /**
     *
     */
    public int fromBytes(byte [] bytes)
    {
	int pos= super.fromBytes(bytes);
	while (pos < bytes.length) {

//	    System.out.println("read segment ...");

	    byte segment_type= bytes[pos++];
	    byte segment_length= bytes[pos++];
	    byte [] segment_bytes= new byte[2+segment_length];

//	    System.out.println("segment-length: "+segment_length);

	    segment_bytes[0]= segment_type;
	    segment_bytes[1]= segment_length;
	    System.arraycopy(bytes, pos, segment_bytes, 2, segment_length);
	    
	    Segment segment= new Segment(segment_bytes);
	    if (segs == null)
		segs= new ArrayList<Segment>();
	    segs.add(segment);
	    pos+= segment_length;
	}

	return pos;
    }
	@Override
	public byte getTypeCode() {
		return ASpath.TYPECODE;
	}


} // end class ASpath
