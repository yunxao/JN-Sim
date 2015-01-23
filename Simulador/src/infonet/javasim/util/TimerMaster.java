// ========================================================================= //
// @(#)TimerMaster.java
//
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
//
// @lastdate 12/04/2002
// ========================================================================= //

package infonet.javasim.util;

import drcl.comp.*;

public class TimerMaster
{

    private Component parent;
    private Port timerPort;

    // ----- TimerMaster --------------------------------------------------- //
    /**
     *
     */
    public TimerMaster(Port timerPort, Component parent)
    {
	super();
	this.parent= parent;
	this.timerPort= timerPort;
    }

    // ----- TimerMaster.processTimer -------------------------------------- //
    /**
     *
     */
    public boolean processTimer(Object data, Port inPort)
    {
	if ((inPort == timerPort) && (data instanceof Timer)) {
	    Timer timer= (Timer) data;
	    if (!timer.isCanceled())
		timer.callback();
	    return true;
	} else
	    return false;
    }

    // ----- TimerMaster.fork ---------------------------------------------- //
    /**
     *
     */
    public ACATimer fork(Timer timer, double duration)
    {
	return parent.fork(timerPort, timer, duration);
    }

    // ----- TimerMaster.cancel -------------------------------------------- //
    /**
     *
     */
    public void cancel(ACATimer timer)
    {
	parent.cancelFork(timer);
    }

    // ----- TimerMaster.getTime ------------------------------------------- //
    /**
     *
     */
    public double getTime()
    {
	return parent.getTime();
    }

}
