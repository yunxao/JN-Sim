/**
 * Debug.java
 *
 * @author BJ Premore
 */


package infonet.javasim.bgp4;


import infonet.javasim.util.*;


// ===== class SSF.OS.BGP4.Debug =========================================== //
/**
 * Encapsulates and manages some debugging information and methods
 * which are specific to BGP.
 */
public class Debug {

  // ......................... constants ........................... //

  /** An IP address used for testing/debugging. */
  public static final IPaddress bogusip = new IPaddress("111.111.222.222/32");

  // ........................ member data .......................... //

  /** The BGPSession object associated with this debugging manager. */
  private BGPSession bgp;


  // ----- constructor Debug ----------------------------------------------- //
  /**
   * Constructs a debugging manager for the given BGP session instance.
   *
   * @param b  The BGPSession with which this debug object is associated.
   */
  Debug(BGPSession b) {
    bgp = b;
  };

  // ----- Debug.hdr() ----------------------------------------------------- //
  /**
   * Constructs a standardized output format prefix.
   *
   * @return the standardized output prefix as a String
   */
  public final String hdr() {
    return hdr(true);
  }

  // ----- Debug.hdr(boolean) ---------------------------------------------- //
  /**
   * Constructs a standardized output format prefix, optionally omitting
   * current simulation time.  Feature for omitting time is useful for messages
   * during configuration or initialization.
   *
   * @return the standardized output prefix as a string
   */
  public final String hdr(boolean showtime) {
    if (showtime) {
      double t = bgp.getTime();
      String wsa = StringManip.ws(14 - (""+t).length());
      //String wsb = StringManip.ws(8 - bgp.nh.length());
      String str = "";
      return (t + wsa + "bgp@" /*+ bgp.nh + wsb*/);
    } else {
	return ("bgp@" /*+ bgp.nh + StringManip.ws(8 - bgp.nh.length())*/);
    }
  }

  // ----- Debug.hdr(boolean) ---------------------------------------------- //
  /**
   * Constructs a standardized output format prefix, optionally omitting
   * current simulation time.  Feature for omitting time is useful for messages
   * during configuration or initialization.
   *
   * @return the standardized output prefix as a string
   */
  public static final String hdr(String nh, double time) {
    String wsa = StringManip.ws(14 - (""+time).length());
    String wsb = StringManip.ws(8 - nh.length());
    return (time + wsa + "bgp@" + nh + wsb);
  }

  // ----- Debug.affirm(boolean,String,boolean) ---------------------------- //
  /**
   * Each of the variations of <code>affirm</code> and <code>gaffirm</code>
   * assert the truth of the given boolean, and print out a message if it is
   * false.  <code>gaffirm</code> is for "generic affirm," since it is static
   * and doesn't print out the associated BGP speaker's info.  This method was
   * called <code>assert</code> in previous versions, but <code>assert</code>
   * became a Java keyword as of Java 1.4.0.
   *
   * @param b         The boolean whose truth is asserted.
   * @param s         The string printed when the boolean is false.
   * @param showtime  Whether or not to report the current simulation time.
   */
  public final void affirm(boolean b, String s, boolean showtime) {
    if (!b) {
      throw new Error(hdr(showtime) + s);
    }
  }

  /** See comments for <code>affirm(boolean,String,boolean)</code>. */
  public final void affirm(boolean b, String s) {
    affirm(b,s,true);
  }

  /** See comments for <code>affirm(boolean,String,boolean)</code>. */
  public final void affirm(boolean b) {
    affirm(b, "an unspecified error occurred", true);
  }

  /** See comments for <code>affirm(boolean,String,boolean)</code>. */
  public static final void gaffirm(boolean b, String s) {
    if (!b) {
      throw new Error("BGP Error: " + s);
    }
  }

  /** See comments for <code>affirm(boolean,String,boolean)</code>. */
  public static final void gaffirm(boolean b) {
    gaffirm(b, "BGP Error: an unspecified error occurred");
  }

  // ----- Debug.err ------------------------------------------------------- //
  /**
   * Reports a BGP-related error.
   *
   * @param str  The string to be printed along with an error message preamble.
   */
  public final void err(String str) {
    throw new Error(hdr() + str);
  }

  // ----- Debug.gerr ------------------------------------------------------ //
  /**
   * A generic function for reporting BGP-related errors which are not
   * associated with a particular BGP speaker.
   *
   * @param str  The string to be printed along with a generic BGP error
   *             message preamble.
   */
  public static final void gerr(String str) {
    throw new Error("BGP: " + str);
  }

  // ----- Debug.except ---------------------------------------------------- //
  /**
   * Reports a BGP-related exception.
   *
   * @param str  The string to be printed along with an exception message
   *             preamble.
   */
  public final void except(String str) {
    new Exception(hdr() + str).printStackTrace();
  }

  // ----- Debug.gexcept --------------------------------------------------- //
  /**
   * Reports a BGP-related exception which is not associated with any
   * particular BGP speaker.
   *
   * @param str  The string to be printed along with a generic BGP exception
   *             preamble.
   */
  public static final void gexcept(String str) {
    new Exception("BGP: " + str).printStackTrace();
  }

  // ----- Debug.warn ------------------------------------------------------ //
  /**
   * Reports a BGP-related warning.
   *
   * @param str  The string to be printed as a warning message with a BGP
   *             warning message preamble.
   */
  public final void warn(String s) {
    System.err.println(hdr(true) + "Warning: " + s);
  }

  // ----- Debug.warn ------------------------------------------------------ //
  /**
   * Reports a BGP-related warning, optionally omitting current simulation
   * time.  Feature for omitting time is useful for warnings during
   * configuration or initialization.
   *
   * @param str       The string to be printed as a warning message with a BGP
   *                  warning message preamble.
   * @param showtime  Whether or not to report the current simulation time.
   */
  public final void warn(String s, boolean showtime) {
    System.err.println(hdr(showtime) + "Warning: " + s);
  }

  // ----- Debug.gwarn ----------------------------------------------------- //
  /**
   * A generic function for reporting BGP-related warnings which are not
   * associated with a particular BGP speaker.
   *
   * @param str  The string to be printed as a warning message with a
   *             generic BGP warning message preamble.
   */
  public static final void gwarn(String str) {
    System.err.println("BGP Warning: " + str);
  }

  // ----- Debug.msg(String) ----------------------------------------------- //
  /**
   * Prints a debugging message in the standardized format.
   */
  public final void msg(String s) {
    System.out.println(hdr() + s);
  }

  // ----- Debug.valid(int,int) -------------------------------------------- //
  /**
   * Each of the variations of <code>valid</cod> handle printing
   * messages associated with specific BGP validation tests.
   *
   * @param testnum  The validation test number.
   * @param msgnum   The message number relative to the validation test.
   */
  public final void valid(int testnum, int msgnum) {
    Global.validation_msg(bgp, testnum, msgnum, null);
  }

  /** See comments for <code>valid(int,int)</code>. */
  public final void valid(int testnum, int msgnum, Object o) {
    Global.validation_msg(bgp, testnum, msgnum, o);
  }


} // end class Debug
