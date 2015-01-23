// ------------------------------------------------------------------------- //
// @(#)NextHop.java
//
// @author BJ Premore
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
//
// @lastdate 18/04/2002
// ------------------------------------------------------------------------- //

package infonet.javasim.bgp4.path;

import infonet.javasim.util.IPaddress;

// =====  drcl.inet.protocol.bgp4.path.NextHop ==================================== //
/**
 * The next hop path attribute.  It describes the next hop in the path, which is an IP address.  It is well-known and mandatory.
 */
public class NextHop extends Attribute {

  // ......................... constants ........................... //
     
  /** The next hop path attribute type code. */
  public static final byte TYPECODE = 3;

  /** The name of the attribute as a string. */
  public static final String name = "next_hop";
     
  // ........................ member data .......................... //

  /** The next hop IP address. */
  public IPaddress ipaddr;


  // ----- constructor NextHop --------------------------------------------- //
  /**
   * Constructs a next hop path attribute with the given IP address.
   *
   * @param ipa  The IP address of the next hop.
   */
  public NextHop(IPaddress ipa) {
    super(TYPECODE, false, true, false);
    ipaddr = ipa;
  }

    // ----- NextHop ------------------------------------------------------- //
    /**
     *
     */
    public NextHop(byte [] bytes)
    {
	super(bytes);
    }

  // ----- NextHop.copy ---------------------------------------------------- //
  /**
   * Constructs and returns a copy of the attribute.
   *
   * @return a copy of the attribute
   */
  public Attribute copy() {
    return new NextHop(new IPaddress(ipaddr));
  }

  // ----- NextHop.bytecount ----------------------------------------------- //
  /**
   * Returns the number of octets (bytes) needed to represent this next hop
   * path attribute in an update message.  The number is the sum of the two
   * octets needed for the attribute type (which contains attribute flags and
   * the attribute type code), the one octets needed for the attribute length,
   * and the four octets needed for the attribute value.
   *
   * @return the number of octets (bytes) needed to represent this next hop
   *         path attribute in an update message
   */
  public int data_bytecount() {
    return 4;
  }

  // ----- NextHop.equals -------------------------------------------------- //
  /**
   * Determines whether or not this path attribute is equivalent to another.
   *
   * @param attrib  A path attribute to compare to this one.
   * @return true only if the two attributes are equivalent
   */
  public boolean equals(Attribute attrib) {
    return (attrib != null &&
            attrib instanceof NextHop &&
            ipaddr.equals(((NextHop)attrib).ipaddr));
  }

  // ----- NextHop.toString ------------------------------------------------ //
  /**
   * Returns this path attribute as a string.
   *
   * @return the attribute as a string
   */
  public final String toString() {
    return ipaddr.toString();
  }

    // ----- NextHop.toBytes ----------------------------------------------- //
    /**
     *
     */
    public byte [] toBytes()
    {
	byte [] header= super.toBytes();
	byte [] bytes= new byte[header.length+4];
	System.arraycopy(header, 0, bytes, 0, header.length);
	int pos= header.length;

	byte [] ipaddr_bytes= ipaddr.bytes();

	/*String s= "< ";
	for (int i= 0; i < 5; i++)
	    s+= ipaddr_bytes[i]+" ";
	    System.out.println(s+">");*/

	System.arraycopy(ipaddr_bytes, 0, bytes, pos, 4);
	pos+= 4;

	return bytes;
    }

    // ----- NextHop.fromBytes --------------------------------------------- //
    /**
     *
     */
    public int fromBytes(byte [] bytes)
    {
	int pos= super.fromBytes(bytes);

	byte [] ipaddr_bytes= new byte[5];
	System.arraycopy(bytes, pos, ipaddr_bytes, 0, 4);
	ipaddr_bytes[4]= 32;

	/*String s= "< ";
	for (int i= 0; i < 5; i++)
	    s+= ipaddr_bytes[i]+" ";
	    System.out.println(s+">");*/

	ipaddr= new IPaddress(ipaddr_bytes);
	pos+= 4;
	
	return pos;
    }

	@Override
	public byte getTypeCode() {
		// TODO Auto-generated method stub
		return this.TYPECODE;
	}

} // end class NextHop
