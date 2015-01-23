// ========================================================================= //
// @(#)PeerConnectionConstants.java
//
// Definition of connection constants.
//
// @author Bruno Quoitin
// @lastdate 12/04/2002
// ========================================================================= //

package infonet.javasim.bgp4;

public interface PeerConnectionConstants
{

    // ----- connection states --------------------------------------------- //

    /** Indicates the Idle state in the BGP finite state machine (FSM). */
    public static final int IDLE       = 1;
    /** Indicates the Connect state in the BGP finite state machine (FSM). */
    public static final int CONNECT    = 2;
    /** Indicates the Active state in the BGP finite state machine (FSM). */
    public static final int ACTIVE     = 3;
    /** Indicates the OpenSent state in the BGP finite state machine (FSM). */
    public static final int OPENSENT   = 4;
    /** Indicates the OpenConfirm state in the BGP finite state machine
     * (FSM). */
    public static final int OPENCONFIRM= 5;
    /** Indicates the Established state in the BGP finite state machine
     * (FSM). */
    public static final int ESTABLISHED= 6;

    /** An array of string versions of the state names. */
    public static final String[] statestr = { "",
					      "Idle",
					      "Connect",
					      "Active",
					      "OpenSent",
					      "OpenConfirm",
					      "Established" };

}
