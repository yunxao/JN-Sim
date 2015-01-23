/**
 * LocRIB.java
 *
 * @author BJ Premore
 */


package infonet.javasim.bgp4;


import java.util.ArrayList;
import java.util.Iterator;


// ===== class drcl.inet.protocol.bgp4.LocRIB ============================== //
/**
 * The Loc-RIB section of BGP's Routing Information Base.
 */
public class LocRIB extends RIBElement {

  // ........................ member data .......................... //


  // ----- constructor LocRIB ---------------------------------------------- //
  /**
   * Constructs the Loc-RIB with a reference to the local BGP protocol session.
   *
   * @param b  The local BGP protocal session.
   */
  LocRIB(BGPSession b) {
    super(b);
  }

  // ----- LocRIB.add ------------------------------------------------------ //
  /**
   * Adds route information.  
   *
   * @param info The route information to add.
   */
  public void add(RouteInfo info) {
    super.add(info);
    info.inlocrib = true;
    bgp.ftadd(info);
    version++;
  }
  // ----- LocRIB.remove --------------------------------------------------- //
  /**
   * Removes the route information corresponding to the given route
   * destination from both this Loc-RIB and the local forwarding table.
   *
   * @param ipa  The destination of the route information to remove.
   * @return the removed route information
   */
  public RouteInfo remove(RouteInfo info) {
	this.bgp.logDebug("Removing    : "+info);  
    RouteInfo ri = (RouteInfo)super.remove(info);
    if (ri != null) {
  	  this.bgp.logDebug("Route finded: "+ri);
      ri.inlocrib = false;
      bgp.ftrmv(ri);
      //bqu: bgp.mon.msg(Monitor.LOC_RIB);
    }
    return ri;
  }

  // ----- LocRIB.remove_all ----------------------------------------------- //
  /**
   * Removes all route information from the Loc-RIB element, as well as from
   * the local forwarding table, and returns it as a list.
   *
   * @return a list of removed route information
   */
  public ArrayList<RouteInfo> remove_all() {
    ArrayList<RouteInfo> allrtes = super.remove_all();
    for (Iterator it=allrtes.iterator(); it.hasNext();) {
      RouteInfo oldinfo = (RouteInfo)it.next();
      bgp.ftrmv((RouteInfo)oldinfo);
    }
    return allrtes;
  }


} 
