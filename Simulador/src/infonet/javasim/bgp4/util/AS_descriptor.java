/**
 * AS_descriptor.java
 *
 * @author BJ Premore
 */


package infonet.javasim.bgp4.util;

import drcl.inet.*;
import java.util.*;


// ===== class SSF.OS.BGP4.Util.AS_descriptor ============================== //
/**
 * This class manages the assignment of autonomous system identifiers.  It is
 * not intended to have any instantiated objects.  An AS can be identified by
 * its NHI prefix address, which is the NHI prefix address of the
 * <code>Net</code> construct which defines the AS's encompassing border.  A
 * hash is constructed to map each such NHI prefix address to a unique integer
 * for times when an integer AS descriptor (in other words, a traditional AS
 * number) is desired.
 */
public class AS_descriptor {

  // ......................... constants ........................... //

  /** Indicates an undefined AS number.  An node with this value for its AS
   *  number either does not know its true AS number or is not enclosed in a
   *  defined AS. */
  public static final int NO_AS = -1;

  /** The next integer available to be assigned as an AS number. */
  private static int NEXT_FREE_AS_NUM = 1;

  /** A hash table which maps AS NHI address prefixes to AS numbers. */
  private static HashMap nh2as_map = new HashMap();

  /** A hash table which maps AS numbers to AS NHI address prefixes. */
  private static HashMap as2nh_map = new HashMap();


  // ........................ member data .......................... //



  // ----- AS_descriptor.get_as_nh ----------------------------------------- //
  /**
   * Determines the NHI prefix address of the AS in which a host lies and
   * returns it.  Returns an empty string if the host is not contained within
   * an AS.
   *
   * @param thehost  The host whose AS is to be identified.
   * @return the NHI prefix address of the AS in which the host lies
   */
    public static final String get_as_nh(Node node_/*Host thehost*/) {
	/*
    cidrBlock blk = thehost.defined_in_network();
    String str = null;

    try {
      str = (String)blk.networkConfiguration().findSingle("AS_status");
    } catch (configException cfgex) {
      System.err.println("EXCEPTION: error looking for 'AS_status' attribute");
      cfgex.printStackTrace();
    }

    while (str == null && blk.nhi_parent() != null) {
      blk = blk.nhi_parent();
      try {
        str = (String)blk.networkConfiguration().findSingle("AS_status");
      } catch (configException cfgex) {
        System.err.println("EXCEPTION: error looking for " +
                           "'AS_status' attribute");
        cfgex.printStackTrace();
      }
    }

    if (str != null) { // found the nearest enclosing 'AS_status' attribute
      if (str.compareTo("boundary") != 0) {
        // 'boundary' is the only recognized value at this time
        System.err.println("ERROR: unexpected 'AS_status' value in DML: "+str);
      }
      return blk.nhi_prefix;
    } else { // there was no enclosing 'AS_status' attribute
      return "";
    }
	*/
	return "";
  }

  // ----- AS_descriptor.nh2as --------------------------------------------- //
  /**
   * Returns the AS number associated with a given AS NHI prefix address.
   *
   * @param nh  The NHI prefix address to be converted.
   * @return the AS number associated with the NHI prefix address
   */
  public static synchronized int nh2as(String nh) {
    if (nh2as_map.containsKey(nh)) {
      // This NHI prefix is already mapped to an AS number.
      return ((Integer)nh2as_map.get(nh)).intValue();
    } else {
      // This NHI prefix is not yet mapped to an AS number.
      nh2as_map.put(nh, new Integer(NEXT_FREE_AS_NUM));
      as2nh_map.put(new Integer(NEXT_FREE_AS_NUM), nh);
      return NEXT_FREE_AS_NUM++;
    }
  }

  // ----- AS_descriptor.as2nh --------------------------------------------- //
  /**
   * Returns the AS NHI prefix address associated with a given AS number.
   * Returns null if there is no NHI prefix address associated with the given
   * AS number.
   *
   * @param asnum  The AS number to be converted.
   * @return the NHI prefix address associated with the AS number
   */
  public static synchronized String as2nh(int asnum) {
    return (String)as2nh_map.get(new Integer(asnum));
  }


} // end class AS_descriptor
