// ========================================================================= //
// @(#)Segment.java
//
// @author BJ Premore
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
//
// @lastdate 15/04/2002
// ========================================================================= //

package infonet.javasim.bgp4.path;

import java.util.*;

// ===== class drcl.inet.protocol.bgp4.path.Segment ======================== //
/**
 * An AS path segment. A path segment is a grouping of ASes (indicated by NHI
 * address prefix) which comprises a portion of an AS path. A segment can either
 * be an unordered set of ASes or an ordered sequence of ASes.
 */
public class Segment {

	// ----- constants ----------------------------------------------------- //
	/**
	 * Indicates a segment type of AS_SET, meaning that the ASes in the segment
	 * are not ordered.
	 */
	public static final byte SET = 1;

	/**
	 * Indicates a segment type of AS_SEQUENCE, meaning that the ASes in the
	 * segment are ordered. Those closer to the beginning of the list (lower
	 * indices) have been added more recently.
	 */
	public static final byte SEQ = 2;

	// ----- member data --------------------------------------------------- //
	/**
	 * The type of the path segment. Either an unordered (set) or ordered
	 * (sequence) group of ASes.
	 */
	public byte typ;

	/** The AS NHI prefix addresses which make up this segment of the path. */
	public ArrayList<Integer> asnhs;

	// ----- constructor Segment(int,ArrayList) ---------------------------- //
	/**
	 * Generic constructor for initializing member data.
	 * 
	 * @param ty
	 *            The type of the path segment.
	 * @param asn
	 *            The AS NHI prefix addresses making up the path segment.
	 */
	public Segment(byte typ, ArrayList<Integer> asn) {
		this.typ = typ;
		asnhs = asn;
	}

	// ----- Segment ------------------------------------------------------- //
	/**
     *
     */
	public Segment(byte[] bytes) {
		fromBytes(bytes);
	}

	// ----- constructor Segment(Segment) ---------------------------------- //
	/**
	 * Constructs a path segment based on another one.
	 * 
	 * @param ps
	 *            The path segment on which to base a new one.
	 */
	public Segment(Segment seg) {
		typ = seg.typ;
		asnhs = new ArrayList<Integer>();
		for (int i = 0; i < seg.asnhs.size(); i++) {
			asnhs.add(seg.asnhs.get(i));
		}
	}

	// ----- Segment.size -------------------------------------------------- //
	/**
	 * Returns the number of ASes in this path segment.
	 * 
	 * @return the number of ASes in this path segment
	 */
	public final int size() {
		return asnhs.size();
	}

	// ----- Segment.contains ---------------------------------------------- //
	/**
	 * Determines whether or not this path segment contains a given AS.
	 * 
	 * @param asnh
	 *            The NHI prefix of the AS to look for in this segment.
	 * @return true only if the AS is in this segment
	 */
	public boolean contains(int ASNum/* String asnh */) {
		for (int i = 0; i < asnhs.size(); i++) {
			if (asnhs.get(i).equals(new Integer(ASNum)/* asnh */)) {
				return true;
			}
		}
		return false;
	}

	// --------------------------- Segment.prepend_as ----------------------- //
	/**
	 * Adds an AS NHI prefix address to the beginning of the list.
	 * 
	 * @param asnum
	 *            The AS NHI prefix address to prepend to this segment.
	 */
	public final void prepend_as(int ASNum/* String asnh */) {
		asnhs.add(0, new Integer(ASNum)/* asnh */);
	}

	// --------------------------- Segment.append_as ------------------------ //
	/**
	 * Adds an AS NHI prefix address to the end of the list.
	 * 
	 * @param asnh
	 *            The AS NHI prefix address to append to this segment.
	 */
	public final void append_as(int ASNum/* String asnh */) {
		asnhs.add(new Integer(ASNum)/* asnh */);
	}

	// ----- Segment.equals ------------------------------------------------ //
	/**
	 * Returns true only if the two path segments are equivalent. This means
	 * that if it is a sequence, the AS NHI prefix addresses must be in the same
	 * order, but if it is a set, they need not be in the same order.
	 * 
	 * @param seg
	 *            The path segment to compare with this one.
	 * @return true if either of two cases holds: (1) the segments are both
	 *         sequences and have identical lists of AS NHI prefix addresses or
	 *         (2) the segments are both sets and contain exactly the same AS
	 *         NHI prefix addresses, not necessarily in the same order
	 */
	public boolean equals(Segment seg) {
		if (typ != seg.typ) {
			return false;
		}
		if (asnhs.size() != seg.asnhs.size()) {
			return false;
		}
		if (typ == SEQ) {
			for (int i = 0; i < asnhs.size(); i++) {
				if (!asnhs.get(i).equals(seg.asnhs.get(i))) {
					return false;
				}
			}
		} else {
			boolean found;
			for (int i = 0; i < asnhs.size(); i++) {
				found = false;
				for (int j = 0; j < asnhs.size(); i++) {
					found = found || asnhs.get(i).equals(seg.asnhs.get(j));
				}
				if (!found) {
					return false;
				}
			}
		}
		return true;
	}

	// ----- Segment.toMinString(char,boolean) ----------------------------- //
	/**
	 * Returns this path segment as a string, leaving out set/sequence info.
	 * 
	 * @param sepchar
	 *            The character used to separate ASes in the string.
	 * @param usenhi
	 *            Whether to show ASes as NHI address prefixes or numbers
	 * @return the path segment as a string of ASes
	 */
	public final String toMinString(char sepchar) {
		String str = "";
		for (int i = 0; i < asnhs.size(); i++) {
			if (i != 0) {
				str += sepchar;
			}
			str += /* AS_descriptor.nh2as((String) */asnhs.get(i)/* ) */;
		}
		return str;
	}

	// ----- Segment.toMinString() ----------------------------------------- //
	/**
	 * Returns this path segment as a string, leaving out set/sequence info.
	 * 
	 * @return the path segment as a string of ASes
	 */
	public final String toMinString() {
		return toMinString(' ');
	}

	// ----- Segment.toString ---------------------------------------------- //
	/**
	 * Returns this path segment as a string.
	 * 
	 * @return the path segment as a string
	 */
	public final String toString() {
		String str = "";
		if (typ == SET) {
			str += "{"; // sets use curly braces
			for (int i = 0; i < asnhs.size() - 1; i++) {
				str += asnhs.get(i) + " ";
			}
			str += asnhs.get(asnhs.size() - 1) + "}";
		} else { // SEQ
			str += "("; // sequences use parens
			for (int i = 0; i < asnhs.size() - 1; i++) {
				str += asnhs.get(i) + " ";
			}
			str += asnhs.get(asnhs.size() - 1) + ")";
		}
		return str;
	}

	// ----- Segment.toBytes ----------------------------------------------- //
	/**
     *
     */
	public byte[] toBytes() {
		byte[] bytes = new byte[2 + 2 * asnhs.size()];
		int pos = 0;

		// Segment type
		bytes[pos++] = typ;
		// Segment length
		bytes[pos++] = (byte) (asnhs.size() * 2);
		// List of AS Numbers
		for (int i = 0; i < asnhs.size(); i++) {
			int ASNum = ((Integer) asnhs.get(i)).intValue();
			bytes[pos++] = (byte) ((ASNum >> 8) - 128);
			bytes[pos++] = (byte) ((ASNum & 255) - 128);
		}

		return bytes;
	}

	// ----- Segment.fromBytes --------------------------------------------- //
	/**
     *
     */
	public int fromBytes(byte[] bytes) {
		int pos = 0;

		// Segment type
		typ = bytes[pos++];
		// Length
		byte length = bytes[pos++];
		// List of AS Numbers
		for (int i = 0; i < (length >> 1); i++) {
			if (asnhs == null)
				asnhs = new ArrayList<Integer>();
			int ASNum = ((((int) bytes[pos++]) + 128) << 8)
					+ (((int) bytes[pos++]) + 128);

			// System.out.println("as-number: "+ASNum);

			asnhs.add(new Integer(ASNum));
		}

		return pos;
	}

} // end class Segment
