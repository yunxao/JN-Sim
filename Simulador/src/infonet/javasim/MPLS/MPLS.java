// Author: Louis SWINNEN (lsw@infonet.fundp.ac.be)
//
// August 2002
//
// $Id: MPLS.java,v 1.2 2003/04/02 12:59:08 lsw Exp $

package infonet.javasim.MPLS;

import drcl.inet.core.*;

import java.util.*;
import drcl.comp.*;
import drcl.net.*;
import drcl.data.*;
import drcl.inet.contract.*;
import drcl.inet.data.*;
import drcl.inet.InetPacket;
import drcl.inet.data.RTKey;
import infonet.javasim.FT.*;
import infonet.javasim.FT.contract.*;
import infonet.javasim.MPLS.contract.*;

public class MPLS extends drcl.net.Module
		implements infonet.javasim.FT.FTInetConstants
{

	public HashMap portMap = new HashMap();
		// downPort -> upPort
		// used when a Inet packet is received and must be forwarded to
		// packet dispatcher.
	static final long FLAG_FRAGMENT_ENABLED = 1L << FLAG_UNDEFINED_START;
	static final long FLAG_TTL_CHECK_SKIP = 1L << (FLAG_UNDEFINED_START +1);
	static final long FLAG_CACHE_ENABLED = 1L << (FLAG_UNDEFINED_START +2);
	static final int POP_OP = 0;
		// see LSPacket.java -> we copy values (TTL & op) from top LSP packet
		// when pop function is called
	
	static final double DEFAULT_FRAGMENT_TTL = 30; // seconds
	
	int seqno = 0;
	Port ftlookup = addPort(SERVICE_FT_PORT_ID, false);
	Port idlookup = addPort(SERVICE_ID_PORT_ID, false);
	
	{
		removeDefaultUpPort();
		removeDefaultDownPort();
	}
		
	FragmentPack fragmentPack = null;
	
	public MPLS()
	{	super();	}
	
	public MPLS(String id_)
	{	super(id_);	}
	
	
	//
	private void ___SCRIPTING___() {}
	//
	
	public boolean isTTLCheckEnabled()
	{
		return getComponentFlag(FLAG_TTL_CHECK_SKIP) == 0;
	}
	
	public final void setTTLCheckEnabled(boolean v_)
	{
		setComponentFlag(FLAG_TTL_CHECK_SKIP, !v_);
	}
	
	public void setFragmentEnabled(boolean enabled_)
	{
		// gestion de la fragmentation
		if(enabled_)
			if(fragmentPack == null)
				fragmentPack = new FragmentPack();
		
		setComponentFlag(FLAG_FRAGMENT_ENABLED, enabled_);
	}
	
	public boolean isFragmentEnabled()
	{
		return getComponentFlag(FLAG_FRAGMENT_ENABLED) != 0;
	}
	
	public void setTableCacheEnabled(boolean enabled_)
	{
		// gestion de la cache
		//setComponentFlag(FLAG_CACHE_ENABLED, enabled_);
	}
	
	public boolean isTableCacheEnabled()
	{
		return getComponentFlag(FLAG_CACHE_ENABLED) !=0;
	}
	
	
	public double getFragmentTTL(double ttl_)
	{
		return fragmentPack == null?
				DEFAULT_FRAGMENT_TTL:fragmentPack.fragmentTTL;
	}
	
	public void setFragmentTTL(double ttl_)
	{
		if(fragmentPack == null) {
			drcl.Debug.error(this,
							"setFragmentTTL(): fragmentation isn't enabled.");
			return;
		}
		fragmentPack.fragmentTTL = ttl_;
	}
	
	public void _setMTU(int index_, int mtu_)
	{
		if( mtu_ == DEFAULT_MTU) return;
		if(fragmentPack == null) setFragmentEnabled(true);
		fragmentPack.mtu = ___createMTUs(fragmentPack.mtu, index_ +1);
		fragmentPack.mtu[index_] = mtu_;
	}
	
	public void _setMTUs(int mtu_)
	{
		if(mtu_ == DEFAULT_MTU) return;
		if(fragmentPack == null) setFragmentEnabled(true);
		fragmentPack.mtu = ___createMTUs(fragmentPack.mtu,0);
		for(int i=0; i<fragmentPack.mtu.length; ++i)
			fragmentPack.mtu[i] = mtu_;			
	}
	
	public int _getMTU(int index_)
	{
		if (fragmentPack == null || fragmentPack.mtu == null
						|| fragmentPack.mtu.length <= index_)
			return DEFAULT_MTU;
		return fragmentPack.mtu[index_];
	}
	
	public void setMTUs(int[] mtu_)
	{
		if(fragmentPack == null) setFragmentEnabled(true);
		fragmentPack.mtu = mtu_;
	}
	
	public int[] getMTUs()
	{
		return ___createMTUs(fragmentPack == null? null:fragmentPack.mtu,0);
	}
	
	int[] ___createMTUs(int[] mtu_, int newSize_)
	{
		Port[] pp_ = getAllPorts(Module.PortGroup_DOWN);
		newSize_ = Math.max(newSize_, pp_.length);
		if(mtu_ == null)
		{
			mtu_ = new int[newSize_];
			for(int i=0; i<mtu_.length;++i)
				mtu_[i] = DEFAULT_MTU;
		}
		else	if(mtu_.length < newSize_)
				{
					int[] tmp_ = new int[newSize_];
					System.arraycopy(mtu_, 0, tmp_, 0, mtu_.length);
					for(int i=mtu_.length;i<tmp_.length; ++i)
						tmp_[i] = DEFAULT_MTU;
					mtu_ = tmp_;
				}
		return mtu_;
	}

	//
	private void ___DISPATCH___() {}
	//
	
	protected synchronized void timeout(Object data_)
	{
		// attente trop longue pour un fragment
	}
	
	/* dataArriveAtUpPort:
	 * We assume that all data should be sent through LSPacket.
	 * So if no route are found inside the forwarding table, 
	 * an event is sent to indictate this error. 
	 *
	 * The incoming data can be :
	 *
	 *    - LSP packet
	 *    - InetPacket
	 *
	 * If the data is an InetPacket, we will check the prefixTable inside
	 * the FT to known the outgoing label, interface, etc
	 * If it is a LSPacket, the labelTable in the FT will give us all
	 * information about the outgoing label and interface.
	 */
	
	protected void dataArriveAtUpPort(Object data_, Port upPort_)
	{

		error(data_, "[MPLS] dataArriveAtUpPort()", infoPort,
				"The MPLS component must NEVER receives data from upper layer");

	}
	
	protected void dataArriveAtDownPort(Object data_, Port downPort_)
	{
		// First: we have to verify the type of the received object

		if(data_ instanceof LSPacket)
		{
			LSPacket pkt_ = (LSPacket) data_;
			if(pkt_.isStackEmpty())
				dataArriveAtDownPort(pkt_.getBody(), downPort_);
			else
				handleLSPacket((LSPacket)pkt_, downPort_);
		}
		else if(data_ instanceof InetPacket)
		{
			InetPacket ip_ = (InetPacket) data_;

			boolean arrived = IDLookup.query(ip_.getDestination(), idlookup);

			// If packet is arrived at destination, forward it to pktdispatcher
			if(arrived) {
				sendUp(ip_, downPort_);
			}
			else {
				//System.err.print(" not yet arrived -");
				// packet not arrived? checking the forwarding table if
				// an entry can be found  ...
				FTEntry fte_ = FTLookup.lookup(
							new RTKey(0, ip_.getDestination(), 0), ftlookup);

				if(fte_ != null) {
					//System.err.println(" LSP packet created and sent !");
					handleInetPacket(ip_, downPort_);
				}
				else {
					sendUp(ip_, downPort_);
				}
			}
		}
		else if(data_ instanceof MPLSending.Message)
		{
			MPLSending.Message request = (MPLSending.Message) data_;
			Packet pkt_ = request.getPacket();
			// error(data_, "[MPLS] dataArriveAtDownPort() - executing ...",
			// 	infoPort, "test");
						               
			// first, we must identify the packet type (LSP or INET ?)
												                       
			switch(request.getType())
			{
				case MPLSending.PACKET_LSP:
					handleLSPacket((LSPacket)pkt_, downPort_);
					break;
					
				
				case MPLSending.PACKET_IN:
					handleInetPacket((InetPacket)pkt_, downPort_);
					break;
					
				case MPLSending.PACKET_UNK:
					if (isGarbageEnabled())
						drop(data_, "dataArriveAtDownPort(): "
										+ "Unknown packet type");			
					break;
			}
		}
		else {
			error("dataArriveAtDownPort()",
					"dont know how to handle: " + data_);
		}
	
	}
	
	protected void handleInetPacket(InetPacket ip_, Port port_)
	{
		// In this case, we must check inside the prefixTable to know the good
		// label and outgoing interface 
		// if no entries match for this prefix, forward it to pktdispatcher
			 				
		// 1. Attempt to obtain information from the prefix table
		
		// For the RTKey, because we are using a RadixMap inside the
		// Forwarding Table, the source and incoming interface isn't needed
		FTEntry fte_ = FTLookup.lookup(new RTKey(0, ip_.getDestination(), 0),
						ftlookup);
				
		/* Now, we have the entry that correspond to this packet */	
		if(fte_ == null) {
			// no route for this destination; have pktdispatcher handle it
			sendUp(ip_, port_);
		}
		else {
			// According to the operation inside the entries, we must
			// construct the LSPacket
			Object pkt = resolvePacket(fte_, ip_);
			
			/* Now we must route this packet */
			if(pkt instanceof LSPacket)
				sendLSPacket((LSPacket)pkt, fte_);
			else if (pkt != null)
				sendUp(pkt, port_);
		}
	}
	
	protected boolean handleLSPacket(LSPacket lsp_, Port port_)
	{
		// check inside the forwarding table to obtain the new label
		// and operation for this entry
		
		// 1. Obtain information from the forwarding label
		
		FTEntry fte_ = FTLookup.lookup(new FTKey(lsp_.peek().getLabel()),
						ftlookup);

		/* Now, we have the entry that correspond to this packet */			
		if(fte_ == null)
		{
			/* no route for this destination */
			/* currently, this error is reported */

			if(isGarbageEnabled())
				drop(lsp_, "No label for the LSP packet");		
			return false;
		}
		else {
			Object pkt =  resolvePacket(fte_, lsp_);

			if(pkt instanceof LSPacket)
				sendLSPacket((LSPacket)pkt, fte_);
			else if (pkt != null)
				sendUp(pkt, port_);
		}

		return true;
	}
	
	// According to operation inside the Forwarding Table,
	// we handle the given packet
	protected Object resolvePacket (FTEntry fte_, Packet pkt_)
	{
		LinkedList labelOpList = fte_.getLabelOp();
		
		Packet result = pkt_;
		
		for(int i=0; i<labelOpList.size();++i)
		{
			boolean isLSPkt = result instanceof LSPacket;
			OutLabel current = (OutLabel)labelOpList.get(i);
			
			switch(current.op)
			{
				case OutLabel.STOP:
					if(isLSPkt)
						return ((LSPacket)pkt_).getBody();
					else
						return pkt_;

				case OutLabel.PUSH:
					if(isLSPkt)
						((LSPacket)result).push(new Label(current.label));
					else
						// The result is a new LSPacket
						result = new LSPacket(new Label(current.label),
									result.getPacketSize(), result);
					break;
				
				case OutLabel.POP:
					if(isLSPkt)
						((LSPacket)result).pop(POP_OP);
					else {
						error("resolvePacket()", "cannot POP on: " + pkt_);
						return null;
					}
					break;
				
				case OutLabel.SWAP:
					if(isLSPkt)
						((LSPacket)result).swap(new Label(current.label));
					else {
						error("resolvePacket()", "cannot SWAP on: " + pkt_);
						return null;
					}
					break;
				
				default:
					/* error: unknown operation */
					error("resolvePacket()", "Unknown operation = "
									+ current.op);
					return null;						
			}
		}
		
		return result;
	}
	
	protected void sendUp(Object ip_, Port port_)
	{
		// If we have to send an InetPacket, we rely on the pktdispatcher to
		// find the correct 'next hop' and outgoing interface. 
		
		Port pktdisp = (Port)portMap.get(port_);
		if(pktdisp != null)
			pktdisp.doLastSending(ip_);
		else
			if (isGarbageEnabled())
				drop(ip_, "sendInetPaccket(): Can't forward the received "
							+ "packet to the pktdispatcher: "
							+ "no upPort found for downPort: " + port_);
	}
	
	protected void sendLSPacket(LSPacket lsp_, FTEntry fte_)
	{
		Port out_;
		int outifs[] = fte_.getOutIf().getSetBitIndices();

		//System.out.println("Sending LSP Packet on interface: " + outifs[0]);
		
		if(outifs.length != 1) {
			/* ERROR: more than one or no outgoing interface */
			if (isGarbageEnabled()) 
				drop(lsp_, "sendLSPacket(): Number of interface (<>1) = "
								+ outifs.length /* outifs[0]*/ );
		}
		else {
			out_ = getPort(PortGroup_DOWN, String.valueOf(outifs[0]));
			if(out_ == null) {
				if (isGarbageEnabled()) 
					drop(lsp_, "sendLSPacket(): interface " + outifs[0]
									+ " does not exist");
			}
			else {
				/* Currently, fragmentation is not used */
				out_.doLastSending(lsp_);
			}
		}
		
	}
	
	
	class FragmentPack
	{
		double fragmentTTL = DEFAULT_FRAGMENT_TTL;
		int[] mtu;
		
		String info()
		{
			/* return "   Fragment TTL= " + fragmentTTL + "\n" +
			    "           MTUs= "
				+ drcl.util.StringUtil.toString(__createMTUs(mtu,0)) + "\n"; */
			return "[MPLS] FragmentPack not yet implemented\n";
		}
		
		FragmentPack _clone()
		{
			FragmentPack new_ = new FragmentPack();
			new_.fragmentTTL = fragmentTTL;
			return new_;
		}
	}
}

