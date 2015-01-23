/**
 * AdjRIBIn.java
 *
 * @author BJ Premore
 */


package infonet.javasim.bgp4;


import java.util.Iterator;
import java.util.Vector;


// ===== class drcl.inet.protocol.bgp4.AdjRIBIn ============================ //
/**
 * One element of the Adj-RIBs-In section of BGP's Routing Information Base.
 */
public class AdjRIBIn extends RIBElement {

  // ........................ member data .......................... //

  /** The peer with whom this element of Adj-RIB-In is associated. */
  public PeerEntry peer;
  

  // ----- constructor AdjRIBIn -------------------------------------------- //
  /**
   * Constructs an element of Adj-RIBs-In with a reference to the local BGP
   * protocol session and the peer associated with it.
   *
   * @param b   The local BGP protocol session.
   * @param pe  The peer with which this Adj-RIB-In is associated.
   */
  public AdjRIBIn(BGPSession b, PeerEntry pe) {
    super(b);
    peer = pe;
  }
  /**
   * Removes the route information corresponding to the given route
   * destination.
   * @param route destination
   * @return the route information removed or null if nothing has been removed
   */
  public RouteInfo remove(Route route){
	  this.bgp.logDebug("remove(adjEIBIn): route:"+route);
	  Vector <RouteInfo>node = this.find(route.nlri);
	  if (node == null){
		  return null;
	  }
	  this.bgp.logDebug("Number of nodes"+node.size());
	  for(Iterator<RouteInfo> it = node.iterator(); it.hasNext();){

		  RouteInfo ri = it.next();
		  this.bgp.logDebug("remove(adjEIBIn): iterator route: "+ri.route);
		  if (route.nlri.equals(ri.route.nlri)){
			  this.bgp.logDebug("remove(adjEIBIn) to eliminate"+ri.route);
			  return super.remove(ri); 
		  }
	  }
	  return null; 

  }


} // end of class AdjRIBIn
