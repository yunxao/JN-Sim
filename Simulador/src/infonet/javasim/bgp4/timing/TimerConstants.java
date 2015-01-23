// ========================================================================= //
// @(#) Timer.java
//
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
//
// @lastdate 12/04/2002
// ========================================================================= //

package infonet.javasim.bgp4.timing;

import drcl.comp.*;

public interface TimerConstants {

    // ......................... constants ........................... //

    /** Indicates the Connect Retry Timer. */
    public static final int CONNRETRY  = 0;
    /** Indicates the Hold Timer. */
    public static final int HOLD       = 1;
    /** Indicates the Keep Alive Timer. */
    public static final int KEEPALIVE  = 2;
    /** Indicates the Minimum AS Origination Timer. */
    public static final int MASO       = 3;
    /** Indicates the Minimum Route Advertisement Interval Timer. */
    public static final int MRAI       = 4;

}
