// ========================================================================= //
// @(#) BGPMessageConstants.java
//
// @author BJ Premore
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
//
// @lastdate 13/04/2002
// ========================================================================= //

package infonet.javasim.bgp4.comm;

public interface BGPMessageConstants
{

    // ----- constants ----------------------------------------------------- //
    /** Indicates that a BGP message is an Open message. */
    public static final byte OPEN =  1;
    /** Indicates that a BGP message is an Update message. */
    public static final byte UPDATE =  2;
    /** Indicates that a BGP message is a Notification message. */
    public static final byte NOTIFICATION = 3;
    /** Indicates that a BGP message is a KeepAlive message. */
    public static final byte KEEPALIVE = 4;
    /** Indicates that a BGP message is a local timer expiration indicator. */
    public static final byte TIMEOUT = 5;
    /** Indicates that a BGP message is a Transport message. */
    public static final byte TRANSPORT = 6;
    /** Indicates that a BGP message is a Start or Stop directive. */
    public static final byte STARTSTOP = 7;
    /** Indicates a 'start BGP process' directive to bring BGP into existence
     * in the simulated network. */
    public static final byte RUN = 8;

    /** String representations of the different message types. */
    public static final String[] typeNames = { null,
					       "Open",
					       "Update",
					       "Notification",
					       "KeepAlive",
					       "Timeout",
					       "Transport",
					       "Start/Stop",
					       "Run" };

    /** The number of octets (bytes) in the standard header. */
    public static final int OCTETS_IN_HEADER = 19;
    
}
