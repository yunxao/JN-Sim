// ========================================================================= //
// @(#)ClusterList.java
//
// @author BJ Premore
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
//
// @lastdate 18/04/2002
// ========================================================================= //

package infonet.javasim.bgp4.path;

import java.util.ArrayList;

// ===== class drcl.inet.protocol.bgp4.path.ClusterList ==================== //
/**
 * The cluster list path attribute.  It is used to record the path that a route
 * has taken through the route reflection hierarchy.  It is optional and
 * non-transitive.
 */
public class ClusterList extends Attribute {

    // ......................... constants ........................... //
     
    /** The cluster list path attribute type code. */
    public static final byte TYPECODE = 10;

    /** The name of the attribute as a string. */
    public static final String name = "cluster_list";
     
    // ........................ member data .......................... //

    /** The list of cluster numbers which represents the path that a route has
     *  taken through the route reflection hierarchy. */
    public ArrayList<Long> list;


    // ----- constructor ClusterList --------------------------------------- //
    /**
     * Constructs a cluster list path attribute given a list of cluster numbers.
     *
     * @param l  A list of cluster numbers.
     */
    public ClusterList(ArrayList<Long> l) {
	super(TYPECODE, true, false, false);
	list = l;
    }

    // ----- ClusterList --------------------------------------------------- //
    /**
     *
     */
    public ClusterList(byte [] bytes)
    {
	super(bytes);
    }

    // ----- ClusterList.length -------------------------------------------- //
    /**
     * Returns the length of the cluster list (number of cluster numbers that it
     * contains).
     *
     * @return  the length of the cluster list
     */
    public int length() {
	if (list == null) {
	    return 0;
	} else {
	    return list.size();
	}
    }

    // ----- ClusterList.copy ---------------------------------------------- //
    /**
     * Constructs and returns a copy of the attribute.
     *
     * @return a copy of the attribute
     */
    public Attribute copy() {
	ArrayList<Long> l = new ArrayList<Long>();
	for (int i=0; i<list.size(); i++) {
	    l.add(list.get(i));
	}
	return new ClusterList(l);
    }

    // ----- ClusterList.contains ------------------------------------------ //
    /**
     * Determines whether or not the cluster list contains a given cluster
     * number.
     *
     * @param cnum  The cluster number to look for in the cluster list.
     * @return true only if the cluster number is in the cluster list
     */
    public final boolean contains(long cnum) {
	for (int i=0; i<list.size(); i++) {
	    if (((Long)list.get(i)).longValue() == cnum) {
		return true;
	    }
	}
	return false;
    }

    // ----- ClusterList.append -------------------------------------------- //
    /**
     * Appends a cluster number to the cluster list.
     *
     * @param cnum  The cluster number to append.
     */
    public final void append(long cnum) {
	list.add(new Long(cnum));
    }

    // ----- ClusterList.bytecount ----------------------------------------- //
    /**
     * Returns the number of octets (bytes) needed to represent this cluster
     * list path attribute in an update message.  The number is the sum of
     * the two octets needed for the attribute type (which contains attribute
     * flags and the attribute type code), the one or two octets needed for
     * the attribute length, and the variable number of octets needed for the
     * attribute value.
     *
     * @return the number of octets (bytes) needed to represent this cluster list
     *         path attribute in an update message
     */
    public int bytecount() {
	int octets = 2; // 2 octets for the attribute type
	
	octets += sizeOfAElementOfAList()*list.size(); // 8 octets per cluster number
	if (octets > 255) { // 1 or 2 octets for the attribute length field
	    octets += 2;
	} else {
	    octets++;
	}
	return octets;
    }

    // ----- ClusterList.equals -------------------------------------------- //
    /**
     * Determines whether or not this path attribute is equivalent to another.
     *
     * @param attrib  A path attribute to compare to this one.
     * @return true only if the two attributes are equivalent
     */
    public boolean equals(Attribute attrib) {
	if (attrib == null || !(attrib instanceof ClusterList)) {
	    return false;
	}
	ClusterList cl = (ClusterList)attrib;
	if (list.size() != cl.list.size()) {
	    return false;
	}
	for (int i=0; i<list.size(); i++) {
	    if (!list.get(i).equals(cl.list.get(i))) {
		return false;
	    }
	}
	return true;
    }

    // ----- ClusterList.toString ------------------------------------------ //
    /**
     * Returns the cluster list as a string.  The string is a list of integers
     * separated by spaces.  There is no space following the last integer.
     *
     * @return the cluster list as a string
     */
    public final String toString() {
	String str = "";
	for (int i=0; i<list.size(); i++) {
	    if (i != 0) {
		str += " ";
	    }
	    str += ((Long)list.get(i)).longValue();
	}
	return str;
    }

    // ----- ClusterList.data_bytecount ------------------------------------ //
    /**
     */
    public int data_bytecount()
    {
	return sizeOfAElementOfAList()*list.size();
    }
    /**
     * Size of a element (1 element of the list of segments numbers)
     * @returnm 8; (Long)
     */
    private int sizeOfAElementOfAList(){
    	return Long.SIZE/8;
    }

    // ----- ClusterList.toBytes ------------------------------------------- //
    /**
     *
     */
    public byte [] toBytes()
    {
		byte [] header= super.toBytes();
		byte [] bytes= new byte[header.length+data_bytecount()];
		System.arraycopy(header, 0, bytes, 0, header.length);
		int pos = header.length;
		for (int i = 0; i < list.size(); i++){
			byte []temp = tid.utils.BytesUtils.toBytes(list.get(i));
			System.arraycopy(temp, 0, bytes, pos, temp.length);
			pos += temp.length;
		}
		//int pos= header.length;

	return bytes;
    }
    
    public int fromBytes(byte [] bytes){
    	int pos = super.fromBytes(bytes);
    	if ((bytes.length-pos)%8 != 0){
    		throw new Error("ClusterList.toBytes: Size of the attribute is incorrect");
    	}
    	list = new ArrayList<Long>();
    	for(;pos<bytes.length;pos= pos+sizeOfAElementOfAList()){
    		list.add(tid.utils.BytesUtils.getU32(bytes, pos));
    	}
    	return pos;
    }
	@Override
	public byte getTypeCode() {
		return ClusterList.TYPECODE;
	}


} // end class ClusterList
