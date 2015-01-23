/**
 * EventTimer.java
 *
 * @author BJ Premore
 */

package infonet.javasim.bgp4.timing;

import drcl.comp.*;
import infonet.javasim.bgp4.*;
import infonet.javasim.bgp4.comm.*;
import infonet.javasim.bgp4.util.*;
import infonet.javasim.util.*;


// ===== class SSF.OS.BGP4.Timing.EventTimer =============================== //
/**
 * Used to represent most of BGP's timers, with the exception of the Minimum Route Advertisement Interval Timer, which has its own class (<code>MRAITimer</code>).
 */
public class EventTimer extends Timer {

  /** The BGPSession using this timer. */
  private BGPSession bgp;

  /** The type of event associated with this timer.  Possible types are listed
   *  in class BGPSession. */
  public int event_type;

  /** The entry of the peer to whom this timer applies. */
  public PeerConnection peerConnection;


  // ----- constructor EventTimer(BGPSession,long,int,PeerEntry) ----------- //
  /**
   * A basic constructor to initialize member data.
   *
   * @param bgp The BGPSession with which this timer is associated.
   * @param timerPort (see Timer)
   * @param duration The length of time (in ticks) for which the timer is set.
   * @param event_type The type of timeout associated with this timer.
   * @param peer The entry of the peer to whom this timer applies.
   */
  public EventTimer(BGPSession bgp, double duration,
		    int event_type, PeerConnection peerConnection) {
    super(bgp.timerMaster, duration);
    this.bgp= bgp;
    this.event_type= event_type;
    this.peerConnection= peerConnection;
  }

  // ----- constructor EventTimer(BGPSession,int,int,PeerEntry) ------------ //
  /**
   * A basic constructor to initialize member data.  For convenience, it takes
   * an integer instead of a long.
   *
   * @param bgp The BGPSession with which this timer is associated.
   * @param timerPort (see Timer)
   * @param duration The length of time (in ticks) for which the timer is set.
   * @param timeout_type  The type of timeout associated with this timer.
   * @param peer The entry of the peer to whom this timer applies.
   * @see #EventTimer(BGPSession,long,int,PeerEntry)
   */
  public EventTimer(BGPSession bgp, int duration,
		    int timeout_type, PeerConnection peerConnection) {
    this(bgp, (double) duration, timeout_type, peerConnection);
  }

  // ----- EventTimer.callback --------------------------------------------- //
  /**
   * Sends a timeout message to the owning BGPSession when the timer expires.
   */
  public void callback() {
    is_expired= true;

    // essentially, BGP is calling push() on itself
    if ((event_type == BGPSession.BGPstart) ||
	(event_type == BGPSession.BGPstop)) {
	bgp.push(new StartStopMessage(event_type, peerConnection/*.addr*//*nh*/));
					  } else if (event_type == BGPSession.BGPrun) {
		bgp.push(new BGPMessage(BGPMessage.RUN, peerConnection/*addr*//*nh*/));
					    } else {
			bgp.push(new TimeoutMessage(event_type, peerConnection/*.addr*//*nh*/));
							}
  }

} // end class EventTimer
