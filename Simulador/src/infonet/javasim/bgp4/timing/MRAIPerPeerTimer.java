/**
 * MRAIPerPeerTimer.java
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

import java.util.HashMap;
import java.util.Iterator;

// ===== class SSF.OS.BGP4.Timing.MRAIPerPeerTimer ========================= //
/**
 * BGP's Minimum Route Advertisement Interval Timer as used for per-peer rate
 * limiting. (Class <code>MRAITimer</code> is used for per-peer, per-destination
 * rate limiting.) This Minimum Route Advertisement Interval Timer has its own
 * implementation (instead of using the more generic <code>EventTimer</code>
 * class) because there is a significant amount of specialized work that needs
 * to be done once it expires. The callback method in this class encapsulates
 * that work. The timer is to help ensure that peers do not receive update
 * messages too often.
 */
public class MRAIPerPeerTimer extends Timer {

	/** The BGPSession with which this timer is associated. */
	private BGPSession bgp;

	/** The entry of the peer to whom a message was sent. */
	public PeerEntry peer;

	// ----- constructor MRAIPerPeerTimer ------------------------------------
	// //
	/**
	 * Constructs a per-peer Minimum Route Advertisement Interval Timer with the
	 * given parameters.
	 * 
	 * @param b
	 *            The BGPSession with which this timer is associated.
	 * @param dt
	 *            The length of time (in ticks) that this timer is set for.
	 * @param pe
	 *            The peer to whom this timer applies.
	 */
	public MRAIPerPeerTimer(BGPSession bgp, double duration, PeerEntry peer) {
		super(bgp.timerMaster, duration);
		this.bgp = bgp;
		this.peer = peer;
	}

	// ----- MRAIPerPeerTimer.callback ---------------------------------------
	// //
	/**
	 * This method executes when the timer expires. Updates are composed for any
	 * prefixes that were waiting to be advertised or withdrawn, and a new timer
	 * is started (if any new updates were in fact sent).
	 */
	public void callback() {
		if (peer.connection != null) {
			is_expired = true;
			// bgp.mon.msg(Monitor.MRAI_EXP, 1, peer);

			boolean update_sent = false;

			if (Global.wrate) {
				for (Iterator<Route> it = peer.waiting_wds.values().iterator(); it
						.hasNext();) {
					Route waitingwd = it.next();

					// send the waiting withdrawal
					UpdateMessage upmsg = new UpdateMessage(peer.connection/*
																			 * bgp.
																			 * ip_addr
																			 * .
																			 * intval
																			 * (
																			 * )
																			 */);
					upmsg.addWithdraw(waitingwd);
					// bgp.mon.msg(Monitor.EXT_UPDATE, 4, peer, upmsg);
					bgp.send(upmsg, peer.connection, 1);
					update_sent = true;
					bgp.reset_timer(peer.connection, TimerConstants.KEEPALIVE);
				}
				// rather than removing every element, just make a new table
				peer.waiting_wds = new HashMap();
			}

			for (Iterator it = peer.waiting_adv.values().iterator(); it
					.hasNext();) {
				Pair pair = (Pair) it.next();
				Route waitingrte = (Route) pair.item1;
				if (Global.wrate) {
					Object waitingwd = peer.waiting_wds.get(waitingrte.nlri);
					bgp.debug.affirm(waitingwd == null,
							"unexpected waiting withdrawal");
				}

				// advertise the waiting route
				UpdateMessage upmsg = new UpdateMessage(peer.connection/*
																		 * bgp.ip_addr
																		 * .
																		 * intval
																		 * ()
																		 */,
						waitingrte);
				// bgp.mon.msg(Monitor.EXT_UPDATE, 4, peer, upmsg);
				bgp.send(upmsg, peer.connection, 1);
				bgp.debug.valid(Global.PROPAGATION, 3, upmsg.getAnnounce(0));
				update_sent = true;
				bgp.reset_timer(peer.connection, TimerConstants.KEEPALIVE);
			}
			// rather than removing every element, just make a new table
			peer.waiting_adv = new HashMap();

			if (update_sent) {
				// start a new MRAIPerPeerTimer
				peer.mraiTimer = new MRAIPerPeerTimer(bgp, peer.mrai, peer);
				// bgp.mon.msg(Monitor.SET_MRAI, peer);
				// The two-argument version of set_timer is used instead the
				// one-argument
				// version just in case the continuous_mrai_timers option is in
				// use, in
				// which case the previous timer could have been set for a
				// fraction of
				// the full MRAI.

				peer.mraiTimer.set(peer.mrai);
			} else { // there was no waiting advertisement (or withdrawal)
				// bgp.mon.msg(Monitor.NO_MSG_WAITING);
				if (Global.continuous_mrai_timers && peer.mrai > 0) {
					// The two-argument version of set_timer is used instead of
					// the
					// one-argument version because the previous timer could
					// have been set
					// for a fraction of the full MRAI.
					peer.mraiTimer.set(peer.mrai);
				}
			}
		} else {
			peer.mraiTimer = new MRAIPerPeerTimer(this.bgp, peer.mrai, peer);
			if (bgp.printDebug)
			System.out
					.println("WARNING: MRAIPerPeerTimer => peer.connection is null");
		}

	}
} // end class MRAIPerPeerTimer
