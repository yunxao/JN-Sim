/**
 * MRAITimer.java
 *
 * @author BJ Premore
 */


package infonet.javasim.bgp4.timing;

import infonet.javasim.bgp4.BGPSession;
import infonet.javasim.bgp4.Global;
import infonet.javasim.bgp4.PeerEntry;
import infonet.javasim.bgp4.Route;
import infonet.javasim.bgp4.comm.UpdateMessage;
import infonet.javasim.bgp4.util.Pair;
import infonet.javasim.util.Timer;


// ===== class SSF.OS.BGP4.Timing.MRAITimer ================================ //
/**
 * BGP's Minimum Route Advertisement Interval Timer.  The Minimum Route Advertisement Interval Timer has its own implementation (instead of using the more generic <code>EventTimer</code> class) because there is a significant amount of specialized work that needs to be done once it expires.  The callback method in this class encapsulates that work.  The timer is to help ensure that peers do not receive update messages with routes regarding the same destination too often.
 */
public class MRAITimer extends Timer {

    /** The BGPSession with which this timer is associated. */
    private BGPSession bgp;

    /** The NLRI from the update message which caused this timer to start. */
//    public IPaddress nlri;
    
    public Route route;

    /** The entry of the peer to whom a message was sent. */
    public PeerEntry peer;


    // ----- constructor MRAITimer ----------------------------------------- //
    /**
     * Constructs a Minimum Route Advertisement Interval Timer with the given
     * parameters.
     *
     * @param b    The BGPSession with which this timer is associated.
     * @param dt   The length of time (in ticks) that this timer is set for.
     * @param ipa  The NLRI from the update message which resulted in the
     *             need for this timer to be set.
     * @param pe   The peer to whom this timer applies.
     */
    public MRAITimer(BGPSession bgp, double duration,
		     Route route, PeerEntry pe) {
	super(bgp.timerMaster, duration);
	this.bgp = bgp;
//	nlri= ipa;
	this.route = route;
	peer= pe;
    }
    


    // ----- MRAITimer.callback -------------------------------------------- //
    /**
     * When the timer expires, this method removes the IP address from the list
     * of recently sent updates, sends an update with the advertisement (or
     * possibly withdrawal, if the option to apply MRAI to withdrawals is in
     * use) that was waiting to be sent (if there is one), and restarts a new
     * timer (if a waiting advertisement (or withdrawal) was in fact sent).
     */
    public void callback() {
	is_expired = true;
	//bgp.mon.msg(Monitor.MRAI_EXP, 0, peer, nlri);

	Route adv_nlri = (Route)peer.adv_nlri.remove(route);
	Route wdn_nlri = null;
	if (Global.wrate) {
	    wdn_nlri = (Route)peer.wdn_nlri.remove(route);
	    bgp.debug.affirm(adv_nlri!=null || wdn_nlri!=null, "no matching update "+
			     "for MRAITimer for " + route.toString());
	} else {
	    bgp.debug.affirm(adv_nlri!=null, "no matching update for " +
			     "MRAITimer for " + route.toString());
	}

	Pair pair = (Pair)peer.waiting_adv.remove(route);
	Route waitingrte = null;
	if (pair != null) {
	    waitingrte = (Route)pair.item1;
	}

	Route waitingwd = null;
	if (Global.wrate) {
	    waitingwd = (Route)peer.waiting_wds.remove(route);
	}

	if (waitingrte != null) {
	    bgp.debug.affirm(waitingwd==null, "unexpected waiting withdrawal");
	    // advertise the waiting route
	    UpdateMessage upmsg = new UpdateMessage(peer.connection/*bgp.ip_addr.val()/*nh*/, waitingrte);
	    //bgp.mon.msg(Monitor.EXT_UPDATE, 4, peer, upmsg);
	    bgp.send(upmsg, peer.connection, 1);

	    bgp.reset_timer(peer.connection, TimerConstants.KEEPALIVE);

	    // start a new MRAITimer
	    MRAITimer newtimer = new MRAITimer(bgp, peer.mrai, route, peer);
	    //bgp.mon.msg(Monitor.SET_MRAI, peer);
	    newtimer.set();
	    //bgp.set_timer(newtimer);
	    peer.mrais.put(route, newtimer);
	    // and since we just advertised a route, add it to the adv_nlri table
	    peer.adv_nlri.put(route, route);

	} else if (waitingwd != null) {
	    // send the waiting withdrawal

	    UpdateMessage upmsg = new UpdateMessage(peer.connection/*bgp.ip_addr.intval()/*nh*/);
	    upmsg.addWithdraw(waitingwd);
	    //bgp.mon.msg(Monitor.EXT_UPDATE, 4, peer, upmsg);
	    bgp.send(upmsg, peer.connection, 1);

	    bgp.reset_timer(peer.connection, TimerConstants.KEEPALIVE);

	    // start a new MRAITimer
	    MRAITimer newtimer = new MRAITimer(bgp, peer.mrai, route, peer);
	    //bgp.mon.msg(Monitor.SET_MRAI, peer);
	    newtimer.set();
	    //bgp.set_timer(newtimer);
	    peer.mrais.put(route, newtimer);
	    // and since we just sent as withdrawal, add the NLRI to the wdn_nlri
	    // table
	    peer.wdn_nlri.put(route, route);
	} else { // there was no waiting advertisement (or withdrawal)
	    //bgp.mon.msg(Monitor.NO_MSG_WAITING);
	}
    }

} // end class MRAITimer
