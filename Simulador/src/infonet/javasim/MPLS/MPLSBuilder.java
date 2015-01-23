// Author: Louis SWINNEN (lsw@infonet.fundp.ac.be)
//
// August 2002
//
// $Id: MPLSBuilder.java,v 1.2 2003/04/02 12:59:08 lsw Exp $

package infonet.javasim.MPLS;

import drcl.inet.core.*;

import java.util.*;
import drcl.comp.*;
import drcl.comp.queue.ActiveQueueContract;
import drcl.inet.Protocol;
import drcl.inet.contract.*;
import drcl.inet.data.*;
import drcl.net.Module;
import drcl.comp.queue.*;
//import drcl.inet.core.*;
import infonet.javasim.FT.*;

public class MPLSBuilder extends drcl.inet.core.CSLBuilder implements infonet.javasim.FT.FTInetConstants
{
    
	public static final String ID_FT="ft";
	public static final String ID_MPLS="mpls";
	
	private static final boolean DEBUG = true;


	/* this method override the CSLBuilder's build() function */
	/* it adds MPLS components and Forwarding Table inside CSL */
	

	/* Main code is from build() function of CSLBuilder */
	/* Modifications are mentionned */
	
	public synchronized void build(Object[] cc_)
	{
		super.build(cc_);
		
		for(int i = 0; i<cc_.length; ++i)
		{
			Component csl, pd, id;
			
			csl = (Component)cc_[i];
			id = csl.getComponent(ID_IDENTITY);
			pd = csl.getComponent(ID_PKT_DISPATCHER);
			
			addMPLSandFT( csl, pd, id );
		}
	}
	
	public void addMPLSandFT(Component target_, Component pd_, Component id_)
	{
		Port downPorts[] = pd_.getAllPorts(Module.PortGroup_DOWN);
		// Port upPorts[] = pd_.getAllPorts(Module.PortGroup_UP); // only for ID
		
		/*if(DEBUG)
			drcl.Debug.error(this, "addMPLSandFT: downPort is null? " + (downPorts == null) + " upPort is null? " + (upPorts == null), false);*/
		
		/* first: add MPLS and FT components */
		Component mpls_ = addMPLS(target_);
		Component ft_ = addFT(target_);
		
		for(int i=0; i < downPorts.length; ++i) // for all downPorts on PktDispatcher
		{
		 	Port current_ = downPorts[i];
			Port newMPLSDownPort = mpls_.addPort(Module.PortGroup_DOWN, current_.getID());
			Port newMPLSUpPort = mpls_.addPort(Module.PortGroup_UP, current_.getID() + "_" + String.valueOf(i));
			Port outPeersPD[] = current_.getOutPeers(); // should only be ONE packet filter
			Port inPeersPD[] = current_.getInPeers(); // should only be ONE CSL shadow port
				 
			// MPLS component will be connected to the packet filter bank exactly like PktDisp.
			
			current_.disconnect(); // pd is now totally disconnected 

			
			for(int j=0; j < inPeersPD.length; ++j)
			{
				newMPLSDownPort.connectTo(inPeersPD[j]);
				current_.connectTo(inPeersPD[j]);
			}
			
			// Because MPLS component is located between the CSL down port and PktDispatcher down port, we have to:
			//   1. disconnect the input wire from CSL (via detachIn)
			//   2. connect this port (CSL) to the MPLS component.
			
			
			
			for(int j=0;j<outPeersPD.length; ++j)
			{
				outPeersPD[j].connectTo(newMPLSDownPort);
			}
			
			// finally, we must connect the MPLS component to the downport of PktDisp.
			newMPLSUpPort.connectTo(current_);
			
			
			// this hack is used to allow MPLS object to forward information to packetdispatcher according to
			// incoming if.
			
			//((MPLS)mpls_).portMap.put(newMPLSDownPort,current_);
			((MPLS)mpls_).portMap.put(newMPLSDownPort,newMPLSUpPort);
	
			
		} // end of loop on all down port of PktDisp.
		
		// we have to connect MPLS and FT component together
		
		Port pMPLS = mpls_.getPort(SERVICE_FT_PORT_ID);
		Port pFT = ft_.getPort(SERVICE_FT_PORT_ID);
		if(pMPLS != null && pFT != null)
			pMPLS.connect(pFT);
		else
			if(DEBUG)
				drcl.Debug.error(this, "addMPLSandFT(): SERVICE_FT_PORT_ID == null ?  mpls)" + (pMPLS == null) + " ft)" + (pFT == null));
		
		// MPLS component must be connected to the ID component.
		pMPLS = mpls_.getPort(SERVICE_ID_PORT_ID);
		Port pID = id_.getPort(SERVICE_ID_PORT_ID);
		
		if(pID != null && pMPLS != null)
			pMPLS.connect(pID);
		else
			if(DEBUG)
				drcl.Debug.error(this, "addMPLSandFT() : SERVICE_ID_PORT_ID == null ? mpls)" + (pMPLS == null) + " id)" + (pID == null));
	}
	
	public Component addMPLS(Component target_)
	{
		Component mpls_ = target_.getComponent(ID_MPLS);
		if(mpls_ == null)
		{
			mpls_ = new MPLS(ID_MPLS);
			target_.addComponent(mpls_);
		}
		
		return mpls_;
	}
	
	public Component addFT(Component target_)
	{
		Component ft_ = target_.getComponent(ID_FT);
		if(ft_ == null)
		{
			ft_ = new FT(ID_FT);
			target_.addComponent(ft_);
		}
		
		Port p_ = target_.addPort(SERVICE_FT_PORT_ID);
		if(p_ != null)
			ft_.addPort(SERVICE_FT_PORT_ID).connect(p_);
			
		p_ = target_.addPort(EVENT_FT_CHANGED_PORT_ID);
		if(p_ != null)
			ft_.addPort(EVENT_FT_CHANGED_PORT_ID).connectTo(p_);
				
		return ft_;
	}
}
