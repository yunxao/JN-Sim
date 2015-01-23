// ===========================================================================
// @(#) BGPSessionConstants.java
//
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
//
// @lastdate 13/04/2002
// ===========================================================================

package infonet.javasim.bgp4;

public interface BGPSessionConstants
{
	/** Port to check de connectivity */
    // ----- constants ----------------------------------------------------- //
    /** The well-known port number for BGP. */
    public static final int PORT_NUM = 179;
	public static final int PORT_CKECK = PORT_NUM+1;

    // ----- default timer intervals --------------------------------------- //
    /** Default Hold Timer Interval (in seconds) to be used with peers for
     * whom it is not specifically configured. */
    public static final long HOLD_TIMER_DEFAULT = 90;

    /** Default Keep Alive Timer Interval (in seconds) to be used with peers
     * for whom it is not specifically configured. */
    public static final long KEEP_ALIVE_DEFAULT = 30;

    /** The "system" default Minimum Route Advertisement Timer Interval (in
     * seconds) for external neighbors.  It is only used when (a) a timer's
     * value is not specifically configured and (b) no "user" global default
     * timer value for external neighbors is configured. */
    public static final long EBGP_MRAI_DEFAULT = 30;

    /** The "system" default Minimum Route Advertisement Timer Interval (in
     * seconds) for internal neighbors. It is only used when (a) a timer's
     * value is not specifically configured and (b) no "user" global default
     * timer value for internal neighbors is configured. */
    public static final long IBGP_MRAI_DEFAULT = 0;

    // ----- event types --------------------------------------------------- //
    /** Indicates an event that causes the BGP process to start up. */
    public static final int BGPrun            =  0;
    /** Indicates the BGP Start event type. */
    public static final int BGPstart          =  1;
    /** Indicates the BGP Stop event type. */
    public static final int BGPstop           =  2;
    /** Indicates the BGP Transport Connection Open event type. */
    public static final int TransConnOpen     =  3;
    /** Indicates the BGP Transport Connection Closed event type. */
    public static final int TransConnClose    =  4;
    /** Indicates the BGP Transport Connection Open Failed event type. */
    public static final int TransConnOpenFail =  5;
    /** Indicates the BGP Transport Fatal Error event type. */
    public static final int TransFatalError   =  6;
    /** Indicates the ConnectRetry Timer Expired event type. */
    public static final int ConnRetryTimerExp =  7;
    /** Indicates the Hold Timer Expired event type. */
    public static final int HoldTimerExp      =  8;
    /** Indicates the KeepAlive Timer Expired event type. */
    public static final int KeepAliveTimerExp =  9;
    /** Indicates the Receive Open Message event type. */
    public static final int RecvOpen          = 10;
    /** Indicates the Receive KeepAlive Message event type. */
    public static final int RecvKeepAlive     = 11;
    /** Indicates the Receive Update Message event type. */
    public static final int RecvUpdate        = 12;
    /** Indicates the Receive Notification Message event type. */
    public static final int RecvNotification  = 13;

    /** String representations of the different BGP event types. */
    public static final String[] eventNames = { "BGPrun",
						"BGPstart",
						"BGPstop",
						"TransConnOpen",
						"TransConnClose",
						"TransConnOpenFail",
						"TransFatalError",
						"ConnRetryTimerExp",
						"HoldTimerExp",
						"KeepAliveTimerExp",
						"RecvOpen",
						"RecvKeepAlive",
						"RecvUpdate",
						"RecvNotification" };
    
    
    public static int OTHER			 		= 0;
    public static int DEBUG_LEVEL_1 		= 1;
    public static int DEBUG_LEVEL_2 		= 2;
    public static int DEBUG_LEVEL_3 		= 3;
    public static int FSM_MESSAGE 			= 4;
    public static int MSG_MESSAGE 			= 5;
    public static int ROUTE_TABLE_MESSAGE 	= 6;



    public static String[] MESSAGE_TYPES = {
    	"other",						 		// 0
    	"debug message (low relevance)", 		// 1
    	"debug message (medium relevance)", 	// 2
    	"debug message (high relevance)", 		// 3
    	"changes in finete state machine", 		// 4
    	"message between nodes", 				// 5
    	"route table modifications" 			// 6
    	};

}
