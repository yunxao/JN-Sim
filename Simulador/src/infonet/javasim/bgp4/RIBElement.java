/**
 * RIBElement.java
 *
 * @author BJ Premore
 */


package infonet.javasim.bgp4;


import infonet.javasim.util.BytesUtil;
import infonet.javasim.util.IPaddress;
import infonet.javasim.util.RadixTree;
import infonet.javasim.util.RadixTreeIterator;
import infonet.javasim.util.RadixTreeIteratorAction;
import infonet.javasim.util.RadixTreeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;


// ===== class drcl.inet.protocol.bgp4.RIBElement ========================== //
/**
 * An element of a RIB.  A single RIBElement is either Loc-RIB, or part of
 * Adj-RIBs-In or Adj-RIBs-Out.  An array of RIBElements make up Adj-RIBs-In,
 * and the same is true for Adj-RIBs-Out (since they each have one element per
 * peer).
 */
public class RIBElement extends RadixTree {

  // ........................ member data .......................... //

  /** A reference to the local BGP protocol session. */
  protected BGPSession bgp;

  /** A table mapping NLRI to routes.  It adds no more functionality than the
   *  radix tree already had, but can save time during look-ups. */
  protected HashMap<IPaddress, Vector<RouteInfo>> rtes = new HashMap<IPaddress, Vector<RouteInfo>>();

  /** The version number of the table.  Inspired by Cisco table version
   *  numbering. */
  protected int version = 0;

  // ----- constructor RIBElement ------------------------------------------ //
  /**
   * Constructs a RIB element with a reference to the local BGP protocol
   * session.
   *
   * @param b  The local BGP protocol session.
   */
  RIBElement(BGPSession b) {
    super();
    bgp = b;
  }

  // ----- RIBElement.find ------------------------------------------------- //
  /**
   * Find the routes information for a ipa destination.
   *
   * @param ipa  The destination IP address prefix to search for info about.
   * @return Vector with all routes information
   */
  public Vector <RouteInfo>find(IPaddress ipa) {
    return rtes.get(ipa);
  }
  /**
   * Find the best route information for a ipa destination
   * @param ipa destination 
   * @return the best route information or null if not exists
   */
  public RouteInfo findBest(IPaddress ipa){
	  Vector<RouteInfo> nodes = rtes.get(ipa);
	  RouteInfo best = null;
	  RouteInfo itElement;
	  if (nodes == null)
		  return null;

	  for (Iterator<RouteInfo> it = nodes.iterator();it.hasNext();){
		  itElement = (RouteInfo) it.next();
		  if (best == null || best.compare(itElement) > 0){
			  best = itElement;
		  }
	  }
	  return best;
  }
  /**
   * Find the worst route information for a destination
   * @param ipa destination 
   * @return the worst route information or null ir not exist
   */
  public RouteInfo findWorst(IPaddress ipa){
	  Vector<RouteInfo> nodes = rtes.get(ipa);
	  this.bgp.logDebug("findWorst(RIBElement): ipa "+ipa+"nodes: "+nodes);	  
	  RouteInfo worst = null;
	  RouteInfo itElement;
	  if (nodes == null)
		  return null;
	  for (Iterator<RouteInfo> it = nodes.iterator();it.hasNext();){
		  itElement = (RouteInfo) it.next();
		  if (worst == null || worst.compare(itElement) < 0){
			  worst = itElement;
		  }
	  }
	  return worst;
  }

  // ----- RIBElement.add -------------------------------------------------- //
  /**
   * Adds route information. 
   *
   * @param info  The route information to add.
   */
  public void add(RouteInfo info) {
	  @SuppressWarnings("unchecked")
	  Vector <RouteInfo> node = (Vector<RouteInfo>)super.find(info.route.nlri.prefix_bits());
	  if (node == null){
		  node = new Vector<RouteInfo>();
		  super.add(info.route.nlri.prefix_bits(), node);
		  rtes.put(info.route.nlri,node);
	  }
	  if (node.contains(info)){
		  this.bgp.logDebug("Intentando meter informacion duplicada");
		  return;
	  }
		  
	  node.add(info);
	  this.bgp.logDebug("add(RIBElement): adding info: "+info);
	  version++;
  }

  // ----- RIBElement.remove ----------------------------------------------- //
  /**
   * Removes the route information from the structure
   *
   * @param ipa  RouteInfo to remove
   * @return the removed route information (null if nothing has been removed)
   */
  public RouteInfo remove(RouteInfo info) {
	  Vector<RouteInfo> node = rtes.get(info.route.nlri);
	  if (node == null){
		  return null;
	  }
	  RouteInfo oldinfo = null; 
	  for (int i = 0; i< node.size(); i++){
		  oldinfo = node.get(i);
		  if (oldinfo.equals(info)){
			  node.remove(i);
			  rtes.remove(oldinfo);
			  version++;
			  return oldinfo;
			  
		  }
	  }
	  version++;
	  return null;
  }

  // ----- RIBElement.remove_all ------------------------------------------- //
  /**
   * Removes all route information in the RIB element and returns it as a list.
   *
   * @return a list of removed route information
   */
  public ArrayList <RouteInfo>remove_all() {
    ArrayList<RouteInfo> allrtes = new ArrayList<RouteInfo>();
    for (Iterator<Vector<RouteInfo>> it=rtes.values().iterator(); it.hasNext();) {
      Vector<RouteInfo> v =  it.next();
      // Clear all elements in the vector
      for (Iterator<RouteInfo> it2 = v.iterator();it2.hasNext();){
    	  allrtes.add((RouteInfo)it2.next());
      }
    	  
      v.clear();
    }
    return allrtes;
  }

  // ----- RIBElement.get_all_routes --------------------------------------- //
  /**
   * Constructs and returns a list of all route information in the RIB element.
   *
   * @return a list of all route information in the RIB element
   */
  public ArrayList<RouteInfo> get_all_routes() {
    ArrayList <RouteInfo>allrtes = new ArrayList<RouteInfo>();
    this.bgp.logDebug("Obteniendo todas las rutas");
    for (Iterator<Vector<RouteInfo>> it=(rtes.values()).iterator(); it.hasNext();) {
    	this.bgp.logDebug("#");
    	Vector<RouteInfo> v = (Vector<RouteInfo>)it.next();
    	for (Iterator<RouteInfo> it2 = v.iterator();it2.hasNext();)
	    	  allrtes.add((RouteInfo)it2.next());
    }
    return allrtes;
  }

  // ----- RIBElement.get_less_specifics ----------------------------------- //
  /**
   * Finds any routes with overlapping but less specific NLRI than the given
   * IP address prefix.
   *
   * @param ipa  An IP address prefix to find less specific NLRI than.
   * @return a list of routes with overlapping but less specific NLRI
   */
  public ArrayList get_less_specifics(IPaddress ipa) {
    return get_ancestors(ipa.prefix_bits());
  }

  // ----- RIBElement.get_less_specifics ----------------------------------- //
  /**
   * Finds any routes with overlapping but more specific NLRI than the given
   * IP address prefix.
   *
   * @param ipa  An IP address prefix to find more specific NLRI than.
   * @return a list of routes with overlapping but more specific NLRI
   */
  public ArrayList get_more_specifics(IPaddress ipa) {
    return get_descendants(ipa.prefix_bits());
  }

  // ----- RIBElement.is_less_specific ------------------------------------- //
  /**
   * Determines if there are any routes with more specific NLRI than the given
   * IP address prefix.
   *
   * @param ipa  An IP address prefix to find more specific NLRI than.
   * @return true only if there is at least one route with more specific NLRI
   */
  public boolean is_less_specific(IPaddress ipa) {
    return has_descendants(ipa.prefix_bits());
  }

  // ----- RIBElement.get_dests -------------------------------------------- //
  /**
   * Returns an iterator for enumerating the destinations (IP addresses) of all
   * contained routes.
   *
   * @return an enumeration of the destinations (IP addresses) of all contained
   *         routes
   */
  public Iterator<IPaddress> get_dests() {
    return rtes.keySet().iterator();
  }

  // ----- RIBElement.get_routes ------------------------------------------- //
  /**
   * Returns an iterator for enumerating all contained routes.
   *
   * @return an enumeration of all contained routes
   */
  public Iterator<Vector<RouteInfo>> get_routes() {
    return rtes.values().iterator();
  }

  // ----- RIBElement.approxBytes ------------------------------------------ //
  /**
   * Determines the approximate number of bytes that would be required when
   * converting this RIB element to a series of bytes with
   * <code>toBytes</code>.  It is more likely than not to be an overestimate.
   * Using NHI addressing makes a difference, so it is included as a parameter.
   *
   * @param usenhi  Whether or not to use NHI addressing.
   * @return the approximate number of bytes that would result from conversion
   *         of this RIB element to a series of bytes with <code>toBytes</code>
   */
  public int approxBytes(boolean usenhi) {
    // 8 for version, 4 for RIB-entry-sum
    return (12 + rtes.size()*RouteInfo.approxBytes(usenhi));
  }

  // ----- RIBElement.toBytes ---------------------------------------------- //
  /**
   * Converts this RIB element into a series of bytes and inserts them into a
   * given byte array.
   *
   * @param bytes   A byte array in which to place the results.
   * @param bindex  The index into the given byte array at which to begin
   *                placing the results.
   * @param usenhi  Whether or not to use NHI addressing.
   * @return the total number of bytes produced by the conversion
   */
  /*public int toBytes(byte[] bytes, int bindex, boolean usenhi) {
    int startindex = bindex;
    bindex = BytesUtil.longToBytes((long)version, bytes, bindex);

    bindex += 4; // leave space for total number of entries in the RIB (an int)
    
    // It's silly to have to use an array for bindex (see below), but we need
    // an Object (and yes, an array IS a type of Java Object) because Java uses
    // only call-by-value (or call-by-reference in the case of Objects) and we
    // need to change the value of bindex while iterating over the radix tree.
    // Why not use an Integer, you ask?  The reason is that after creating an
    // Integer, it seems that the value cannot be changed.  Hmm.  (The second
    // element of the array, for convenience, is being used to add up the total
    // number of entries in the RIB.)
    int[] bindexarr = new int[2];
    bindexarr[0] = bindex;
    bindexarr[1] = 0;

    Object parameters[] = new Object[3];
    parameters[0] = bytes;
    parameters[1] = bindexarr;
    parameters[2] = new Boolean(usenhi);
    
    RadixTreeIterator it =
      new RadixTreeIterator(this, new RadixTreeIteratorAction(parameters)
        {
          public void action(RadixTreeNode node, String bitstr) {
            byte[] bytes2    = (byte[])((Object[])params)[0];
            int[] bindexarr2 = (int[])((Object[])params)[1];
            boolean usenhi2  = ((Boolean)((Object[])params)[2]).booleanValue();

            if (node.data != null) {
              bindexarr2[0] +=((RouteInfo)node.data).toBytes(bytes2,
                                                             bindexarr2[0],
                                                             usenhi2);
              bindexarr2[1]++;
            }
          }
        });
    it.iterate();

    // set total number of entries in RIB (start after first 8 bytes (version))
    BytesUtil.intToBytes(bindexarr[1], bytes, startindex+8);

    return bindexarr[0] - startindex;
  }*/

  // ----- RIBElement.bytes2str -------------------------------------------- //
  /**
   * Converts a series of bytes into a string represention of a RIB element.
   *
   * @param ribstr   A StringBuffer into which the results will be placed.
   *                 It <em>must</em> be initialized to the empty string.
   * @param bytes    The byte array to convert to a RIB element.
   * @param bindex   The index into the given byte array from which to begin
   *                 converting.
   * @param ind      The string with which to indent each line.
   * @param usenhi   Whether or not to use NHI addressing.
   * @return the total number of bytes used in the conversion
   */
  public static int bytes2str(StringBuffer ribstr, byte[] bytes, int bindex,
                              String ind, boolean usenhi) {
    int startindex = bindex;

    long version = BytesUtil.bytesToLong(bytes,bindex);
    bindex += 8;

    //ribstr.append(ind + "     table version is " + version + "\n");

    if (usenhi) {
      ribstr.append(ind + "     NetworkNHI         NextHopNHI       " +
                    "Metric LocPrf Weight ASPathNHI\n");
    } else {
      ribstr.append(ind + "     Network            NextHop          " +
                    "Metric LocPrf Weight ASPath\n");
    }
    
    int totalentries = BytesUtil.bytesToInt(bytes, bindex);
    bindex += 4;

    StringBuffer infostr = null;
    for (int i=0; i<totalentries; i++) {
      infostr = new StringBuffer("");
      bindex += RouteInfo.bytes2str(infostr, bytes, bindex, usenhi);
      ribstr.append(ind + infostr + "\n");
    }

    return bindex - startindex;
  }

  // ----- RIBElement.hdr2str ---------------------------------------------- //
  /**
   * Composes into a string of the header/title text used when printing out a
   * RIB element.
   *
   * @param ind     The string with which to indent each line.
   * @param usenhi  Whether or not NHI addressing is being used.
   * @return RIBElement output header text as a string
   */
    protected String hdr2str(String ind) {
    String str = "";
    //str += ind + "     table version is " + version + "\n";
    //str += ind + "table version is " + version + ", " +
    //       "local router ID is " + bgp.bgp_id + "\n";
    //str += ind + "Status codes: s suppressed, d damped, h history, " +
    //       "* valid, > best, i - internal\n";
    //str += ind + "Origin codes: i - IGP, e - EGP, ? - incomplete\n";

      str += ind + "     Network            NextHop          Metric LocPrf " +
             "Weight ASPath\n";

    return str;
  }

  // ----- RIBElement.toString(String,boolean) ----------------------------- //
  /**
   * Returns this RIB element as a string, indenting each line with the given
   * prefix string.
   *
   * @param ind     The string with which to indent each line.
   * @param usenhi  Whether or not to use NHI addressing.
   * @return this RIB element as a string
   */
    public String toString(String ind/*, boolean usenhi*/) {
    String str = hdr2str(ind/*,usenhi*/);
    
    Object parameters[] = new Object[1];
    parameters[0] = new String(ind);
    
    RadixTreeIterator it =
      new RadixTreeIterator(this, new RadixTreeIteratorAction(parameters)
        {
          public void action(RadixTreeNode node, String bitstr) {
            String indent = (String)((Object[])params)[0];
            if (result == null) {
              result = new String("");
            }
            if (node.data != null) {
              result = ((String)result).concat(indent+((RouteInfo)node.data).
					       toString()+"\n");
            }
          }
        });
    it.iterate();
    str += (String)it.result;

    return str;
  }

  // ----- RIBElement.toString() ------------------------------------------- //
  /**
   * Returns this RIB element as a string.
   *
   * @return this RIB element as a string
   */
  public String toString() {
    return toString("");
  }


} // end class RIBElement
