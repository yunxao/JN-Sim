// ========================================================================= //
// @(#) Timer.java
//
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
//
// @lastdate 12/04/2002
// ========================================================================= //

package infonet.javasim.util;

import drcl.comp.*;
//import drcl.inet.protocol.bgp4.*;

public abstract class Timer {

    // ----- member data --------------------------------------------------- //

    /** The time (in logical clock ticks) at which this timer was set. */
    private double set_at;

    /** Whether or not the timer is expired.  It is also true if timer is not
     *  set.  It is the opposite of whether or not the timer is ticking.  That
     *  is, if the timer is not expired, then it is ticking.  */
    protected boolean is_expired;

    private boolean canceled;

    private TimerMaster master;
    private double duration;
    private ACATimer timer= null;

    // ----- Timer --------------------------------------------------------- //
    /**
     *
     */
    public Timer(TimerMaster master, double duration) {
	super();
	this.duration= duration;
	this.master= master;
	set_at = -1;
	is_expired = true;
	canceled= false;
    }

    // ----- Timer.set ----------------------------------------------------- //
    /**
     *
     */
    public void set() {
	set(duration);
    }

    // ----- Timer.set ----------------------------------------------------- //
    /**
     *
     */
    public void set(double duration) {
	if (timer != null)
	    master.cancel(timer);
	timer= master.fork(this, duration);
	set_at= master.getTime();
	//System.out.println("["+master.getTime()+"] timer_set_at("+master.getTime()+")");
	is_expired= false;
	canceled= false;
    }

    // ----- Timer.cancel -------------------------------------------------- //
    /**
     *
     */
    public final void cancel() {
	//System.out.println("["+master.getTime()+"] cancel_timer("+set_at+")");
	canceled= true;
	if (timer != null) {
	    master.cancel(timer);
	    timer= null;
	}
    }

    // ----- Timer.callback ----------------------------------------------- //
    /**
     *
     */
    public abstract void callback();

    // ----- Timer.getMaster ----------------------------------------------- //
    /**
     *
     */
    public TimerMaster getMaster()
    {
	return master;
    }

    // ----- Timer.isCanceled --------------------------------------------- //
    /**
     *
     */
    public boolean isCanceled()
    {
	return canceled;
    }

    // ----- Timer.isExpired --------------------------------------------- //
    /**
     *
     */
    public boolean isExpired()
    {
	return is_expired;
    }

}
