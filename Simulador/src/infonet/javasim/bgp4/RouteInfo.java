/**
 * RouteInfo.java
 *
 * @author BJ Premore
 */


package infonet.javasim.bgp4;


import java.io.*;
import java.util.*;
import infonet.javasim.bgp4.path.*;
import infonet.javasim.util.*;


// ===== class drcl.inet.protocol.bgp4.RouteInfo =========================== //
/**
 * A unit of BGP route information.  It contains all information about a
 * particular route which is used by BGP, including the destination, path
 * attributes, and degree of preference.
 */
public class RouteInfo {
  
  // ......................... constants ........................... //

/** Indicates that this route is not part of any aggregated route. */
  public static final int AGG_NONE = 0;

  /** Indicates that this route is part of an aggregation in which there is a
   *  less specific route than this one. */
  public static final int AGG_CONTAINED = 1;

  /** Indicates that this route is part of an aggregation in which this route
   *  is the least specific. */
  public static final int AGG_CONTAINS = 2;

  /** The minimum value for degree of preference. */
  public static final int MIN_DOP = 0;

  /** The maximum value for degree of preference. */
  public static final int MAX_DOP = 100;

  // ........................ member data .......................... //

  /** The route itself. */
  public Route route;

  /** The BGPSession with which this route info is associated. */
  public BGPSession bgp; // XXX cambiado de private a public

  /** The status of this information with regard to route aggregation (see
   *  descriptions of the three possible states). */
  public int agg_status;

  /** An aggregation of this route with others, if appropriate.  Used during
   *  Phase 3 of the Decision Process. */
  public Route agg_route;

  /** The degree of preference of this route (according to BGP policy). */
  public int dop;

  /** Whether or not the route is feasible.  A feasible route is one that could
   *  be used, according to BGP specifications.  That is, it does not contain
   *  any AS loops have any other "illegal" properties. */
  public boolean feasible;

  /** Whether or not the route is permissible.  A permissible route is one
   *  which was permitted by the input policy rules.  Permissibility is
   *  orthogonal to feasibility. */
  public boolean permissible;

  /** Whether this route is newly installed or not.  It is no longer new if it
   *  has been advertised to peers, or if it need not be advertised. */
  public boolean isnew;

  /** Whether or not this route is in the Loc-RIB. */
  public boolean inlocrib;

  /** The peer who advertised this route. */
  public PeerEntry peer;


  // ----- constructor RouteInfo(Route,int,Route,int,boolean, -------------- //
  // -----                       boolean,boolean,PeerEntry)   -------------- //
  /**
   * Constructs route information given all of the relevant data.
   *
   * @param b     The BGPSession with which this route info is associated.
   * @param r     The route to which this information pertains.
   * @param aggs  The aggregation status of this information.
   * @param aggr  An aggregation of this route with others (may be null).
   * @param d     The degree of preference of the route.
   * @param feas  Whether or not the route is feasible.
   * @param perm  Whether or not the route is permissible.
   * @param isn   Whether or not the route is marked as new.
   * @param pe    The peer who advertised this route.
   */
  public RouteInfo(BGPSession b, Route r, int aggs, Route aggr, int d,
                   boolean feas, boolean perm, boolean isn, PeerEntry pe) {

    bgp         = b;
    route       = r;
    agg_status  = aggs;
    agg_route   = aggr;
    dop         = d;
    feasible    = feas;
    permissible = perm;
    isnew       = isn;
    peer        = pe;
    inlocrib    = false;
  }

  // ----- constructor RouteInfo() ----------------------------------------- //
  /**
   * Constructs new route information with default values.
   *
   * @param b  The BGPSession with which this route info is associated.
   */
  public RouteInfo(BGPSession b) {
    this(b, null, AGG_NONE, null, MIN_DOP, false, false, true, null);
  }

  // ----- constructor RouteInfo(RouteInfo) -------------------------------- //
  /**
   * Constructs new route information from the given route information.
   *
   * @param b     The BGPSession with which this route info is associated.
   * @param info  The route information to use for constructing this new one.
   */
  public RouteInfo(BGPSession b, RouteInfo info) {
    this(b, new Route(info.route), info.agg_status,
         (info.agg_route==null)?null:new Route(info.agg_route), info.dop,
         info.feasible, info.permissible, info.isnew, info.peer);
  }

  // ----- constructor RouteInfo(Route,int,boolean,PeerEntry) -------------- //
  /**
   * Constructs new route information with the given attributes.
   *
   * @param b     The BGPSession with which this route info is associated.
   * @param rte   The route held by this entry.
   * @param dop   The degree of preference of the route.
   * @param feas  Whether or not the route is feasible.
   * @param pe    The entry for the peer who advertised this route.
   */
  public RouteInfo(BGPSession b, Route rte, int dop, boolean feas,
                   PeerEntry pe) {
    this(b, rte, AGG_NONE, null, dop, feas, false, true, pe);
  }

  // ----- RouteInfo.set_permissibility ------------------------------------ //
  /**
   * Sets the permissibility of the route in this entry.
   *
   * @param perm  Whether or not the route is permissible.
   */
  public void set_permissibility(boolean perm) {
    permissible = perm;
  }

  // ----- RouteInfo.compare ----------------------------------------------- //
  /**
   * Compares to route information for another route to determine one is more
   * preferable.<br>
   * Levels of route_compare_level:<br>
   * <ul>
   * <li>level 0: nothing is compared</li>
   * <li>level 1: 1º Compare Local Preference</li>
   * <li>level 2: level 1 + number of distint as of the route</li>
   * <li>level 3: level 2 + med</li>
   * <li>level 4: level 3 + Prefenrence internal vs external</li>
   * <li>level 5: level 4 + random or id (all)</li>
   * </ul>
   *
   * @param rte  The route information to compare to.
   * @return 1 if this route(info) is preferred, 0 if they are identically
   *         preferable, and -1 if the given route(this) is preferred.
  */
  public int compare(RouteInfo info) {
    if (info == null) {
      return 1;
    }
    int level = info.bgp.routes_compare_level;
    if (level == Global.ROUTES_COMPARE_LEVELS.get("level 0"))
    	return 0;
    
    
    // La local preference es preferir un router
    long local_pref = (route.has_localpref())?this.route.localpref():Global.default_local_pref;

    long info_local_pref = (info.route.has_localpref())?info.route.localpref():Global.default_local_pref;
    if (local_pref < info_local_pref){
    	return -1;
    }
    else if (local_pref > info_local_pref){
    	return 1;
    }
    
    if (level == Global.ROUTES_COMPARE_LEVELS.get("level 1"))
    	return 0;

    Debug.gaffirm(route.nlri.equals(info.route.nlri),
                 "Cannot compare routes with different destinations.");
    if (dop < info.dop) {
      return -1;
    } else if (dop > info.dop) {
      return 1;
    }
    if (level == Global.ROUTES_COMPARE_LEVELS.get("level 2"))
    	return 0;

    
    // La med es para la ruta, anunciada
    // Their degrees of preference are equal.  If both routes were received
    // from BGP speakers in the same AS, then the first tiebreaker uses the
    // MULTI_EXIT_DISC path attribute.  If not, we skip to the next tiebreaker.
    if (peer.ASNum == info.peer.ASNum || this.bgp.always_compare_med) {
      // Having a MED is better than not.  (See 9.1.2.1, where it says that the
      // highest value should be assumed when MED is not presest.  Since lower
      // is better for MEDs, no MED is the worst possible.)
      if (route.has_med() && !info.route.has_med()) {
        return 1;
      } else if (!route.has_med() && info.route.has_med()) {
        return -1;
      }
      if (route.has_med() && info.route.has_med()) {
        if (route.med() < info.route.med()) {
          return 1;
        } else if (route.med() > info.route.med()) {
          return -1;
        }
      }
    }
    if (level == Global.ROUTES_COMPARE_LEVELS.get("level 3"))
    	return 0;


    // Their MULTI_EXIT_DISC values are the same (or both routes were
    // not received from BGP speakers in the same AS), so go to the
    // next tiebreaker, which is based on cost (interior distance).
      // here we're supposed to compare interior distance/cost, but
      // that would seem to imply that forwarding tables could be
      // inconsistent across BGP speakers within this same AS, so
      // we'll forego this comparison until I understand it correctly


    // (This next part (comparing the sources of the routes) is apparently not
    // used for internal tie-breakers (section 9.2.1.1).  Note, however, that
    // it is used during route selection in Phase 2 of the Decision process
    // (section 9.1.2.1).

    // Their costs are the same, go to the next tiebreaker, which is
    // the source of the route (External or Internal BGP peer)
    if (peer.typ == PeerEntry.INTERNAL &&
        info.peer.typ == PeerEntry.EXTERNAL) {
      return -1;
    } else if (peer.typ == PeerEntry.EXTERNAL &&
               info.peer.typ == PeerEntry.INTERNAL) {
      return 1;
    }
    if (level == Global.ROUTES_COMPARE_LEVELS.get("level 4"))
    	return 0;



    // Their sources are the same, go to next tiebreaker, which is lowest BGP
    // ID of the BGP speakers that advertised them.  (An alternate
    // implementation is to randomize the choice if it gets to this point.)
    
/*    if (Global.tbid_tiebreaking) {
	if (route.tbid() > info.route.tbid())
	    return 1;
	else if (route.tbid() < info.route.tbid())
	    return -1;
    }*/

    if (bgp.random_tie_breaking) {
      if (BGPSession.rng1.nextDouble() < 0.5) {
        return 1;
      } else {
        return -1;
      }
    } else {
      if (peer.bgp_id.val() < info.peer.bgp_id.val()) {
        return 1;
      } else if (peer.bgp_id.val() > info.peer.bgp_id.val()) {
        return -1;
      }
    }
    if (level == Global.ROUTES_COMPARE_LEVELS.get("level 5"))
    	return 0;


    // They're exactly tied all the way around. 
    return 0;
  }

  // ----- RouteInfo.approxBytes ------------------------------------------- //
  /**
   * Determines the approximate number of bytes that would be required when
   * converting this route info to a series of bytes with <code>toBytes</code>.
   * It is more likely than not to be an overestimate.  Using NHI addressing
   * makes a difference, so it is included as a parameter.
   *
   * @param usenhi  Whether or not to use NHI addressing.
   * @return the approximate number of bytes that would result from conversion
   *         of this route inf to a series of bytes with <code>toBytes</code>
   */
  public static int approxBytes(boolean usenhi) {
    if (usenhi) {
      // 1 for feasible, 1 for inlocrib, ~5 for network, 1 for self, ~5 for
      // next hop, 0 for metric, 1 for haslocalpref, 4 for local pref, 0 for
      // weight, 1 + ~5*4 for AS path, 1 for internal
      return 40;
    } else {
      // 1 for feasible, 1 for inlocrib, 5 for network, 1 for self, 5 for
      // next hop, 0 for metric, 1 for haslocalpref, 4 for local pref, 0 for
      // weight, 1 + ~5*4 for AS path, 1 for internal
      return 40;
    }
  }

  // ----- RouteInfo.toBytes ----------------------------------------------- //
  /**
   * Converts route info into a series of bytes and inserts them into a given
   * byte array.
   *
   * @param bytes   A byte array in which to place the results.
   * @param bindex  The index into the given byte array at which to begin
   *                placing the results.
   * @param usenhi  Whether or not to use NHI addressing.
   * @return the total number of bytes produced by the conversion
   */
  public int toBytes(byte[] bytes, int bindex, boolean usenhi) {
    int startindex = bindex;

    // ---- status codes ----
    bytes[bindex++] = (byte)(feasible?1:0);
    bytes[bindex++] = (byte)(inlocrib?1:0);

    // ---- network ----
    bindex += IPaddress.ipprefix2bytes(route.nlri, bytes, bindex/*, usenhi*/);
      
    // ---- next hop ----
    bytes[bindex++] = (byte)((peer==peer.bgp.self)?1:0);
    bindex += IPaddress.ipprefix2bytes(route.nexthop(), bytes, bindex/*, usenhi*/);

    // ---- metric ----
    // nothing

    // ---- local pref ----
    bytes[bindex++] = (byte)(route.has_localpref()?1:0);
    if (route.has_localpref()) {
      bindex = BytesUtil.intToBytes((int) route.localpref(), bytes, bindex);
    }

    // ---- weight ----
    // nothing

    // ---- AS path ----
    bindex += ASpath.aspath2bytes(route.aspath(), bytes, bindex/*, usenhi*/);

    // ---- internal ----
    bytes[bindex++] = (byte)((peer.typ==PeerEntry.INTERNAL)?1:0);

    return bindex - startindex;
  }

  // ----- RouteInfo.bytes2str --------------------------------------------- //
  /**
   * Converts a series of bytes into route info (in string form).
   *
   * @param infostr  A StringBuffer into which the results will be placed.
   *                 It <em>must</em> be initialized to the empty string.
   * @param bytes    The byte array to convert to route information.
   * @param bindex   The index into the given byte array from which to begin
   *                 converting.
   * @param usenhi   Whether or not to use NHI addressing.
   * @return the total number of bytes produced by the conversion
   */
  public static int bytes2str(StringBuffer infostr, byte[] bytes, int bindex,
                              boolean usenhi) {
    int startindex = bindex;
    StringBuffer str1 = new StringBuffer("");

    // ---- status codes ----
    String feas = "*", best = ">";
    if (bytes[bindex++] == 0) { // infeasible?
      feas = " ";
    }
    if (bytes[bindex++] == 0) { // not in Loc-RIB?
      best = " ";
    }
    infostr.append(feas + best + "   ");

    // ---- network ----
    bindex += IPaddress.bytes2ipprefix(str1, bytes, bindex/*, usenhi*/);
    infostr.append(StringManip.pad(str1.toString(),19,' ',true));
      
    // ---- next hop ----
    boolean isself = (bytes[bindex++] == 1);
    str1 = new StringBuffer("");
    bindex += IPaddress.bytes2ipprefix(str1, bytes, bindex/*, usenhi*/);
    if (isself) {
      infostr.append("self              ");
    } else {
      infostr.append(StringManip.pad(str1.toString(),18,' ',true));
    }

    // ---- metric ----
    infostr.append("    -");

    // ---- local pref ----
    boolean haslocalpref = (bytes[bindex++] == 1);
    if (haslocalpref) {
      infostr.append(StringManip.pad(""+BytesUtil.bytesToInt(bytes,bindex),
                                     7,' ',false));
      bindex += 4;
    } else {
      infostr.append("      -");
    }

    // ---- weight ----
    infostr.append("      -");

    // ---- AS path ----
    str1 = new StringBuffer("");
    bindex += ASpath.bytes2aspath(str1, bytes, bindex/*, usenhi*/);
    infostr.append(" " + StringManip.pad(str1.toString(),9,' ',true));

    // ---- internal ----
    if (bytes[bindex++] == 1) {
      infostr.append(" i");
    }

    return bindex - startindex;
  }

  // ----- RouteInfo.toString() -------------------------------------------- //
  /**
   * Returns route information as a string.
   *
   * @return the route information as a string
   */
    /*
  public String toString() {
    return toString(false);
  }
    */

  // ----- RouteInfo.toString(boolean) ------------------------------------- //
  /**
   * Returns route information as a string.
   *
   * @param usenhi  Whether or not to use NHI addressing.
   * @return the route information as a string
   */
    public String toString(/*boolean usenhi*/) {
    String str = "";

    // ---- status codes ----
    String feas = "*", best = ">";
    if (!feasible) { feas = " "; }
    if (!inlocrib) { best = " "; }
    str += feas + best + "   ";

    // ---- network ----
    str += StringManip.pad(route.nlri.toString(/*usenhi*/), 19, ' ', true);
      
    // ---- next hop ----
    if (peer == peer.bgp.self) {
      str += "self              ";
    } else {
	str += StringManip.pad(route.nexthop().toString(/*usenhi*/), 18, ' ', true);
    }

    // ---- metric ----
    str += "    -";

    // ---- local pref ----
    if (route.has_localpref()) {
      str += StringManip.pad(""+ route.localpref(), 7, ' ', false);
    } else {
      str += "      -";
    }

    // ---- weight ----
    str += "      -";

    // ---- AS path ----
    str += " " + StringManip.pad(route.aspath().toMinString(' '/*,usenhi*/),
                                 9, ' ', true);

    // ---- internal ----
    if (peer.typ == PeerEntry.INTERNAL) {
      str += " i";
    }

    return str;
  }

} // end of class RouteInfo
