/**
 * AdjRIBOut.java
 *
 * @author BJ Premore
 */


package infonet.javasim.bgp4;


import java.util.Iterator;
import java.util.Vector;


// ===== class drcl.inet.protocol.bgp4.AdjRIBOut =========================== //
/**
 * One element of the Adj-RIBs-Out section of BGP's Routing Information Base.
 */
public class AdjRIBOut extends RIBElement {

  // ........................ member data .......................... //

  /** The peer with whom this RIB element is associated, if any. */
  public PeerEntry peer;
  

  // ----- constructor AdjRIBOut ------------------------------------------- //
  /**
   * Constructs an element of Adj-RIBs-Out with a reference to the local BGP
   * protocol session and the peer associated with it.
   *
   * @param b   The local BGP protocol session.
   * @param pe  The peer with which this RIB is associated.
   */
  public AdjRIBOut(BGPSession b, PeerEntry pe) {
    super(b);
    peer = pe;
  }
  // ----- AdjRIBOut.remove ------------------------------------------------ //
  /**
   * Removes the route information corresponding to the given route
   * destination.
   * @param route
   * @return Route deleted or null if nothing is deleted
   */
  public RouteInfo remove(Route route){
	  // All the RouteInfo's to network are in a Vector 
	  Vector <RouteInfo>node = this.find(route.nlri);
	  if (node == null){
		  return null;
	  }
	  	
	  
	  // Find de route to deleted
	  for(Iterator<RouteInfo> it = node.iterator(); it.hasNext();){
		  RouteInfo ri = it.next();

		  if (route.equals(ri.route)){
			  return super.remove(ri); 
		  }
	  }
	  return null; 
  }


} // end of class AdjRIBOut
