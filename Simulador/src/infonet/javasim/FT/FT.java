// FT.JAVA
// Copyright (c) 2002, Infonet research group (http://www.infonet.fundp.ac.be)
//
// This code is provided under FreeBSD-like license.
//
// Authors: Cristel PELSSER and Louis SWINNEN

// ! Label 0 is used for specific case:
//
// get() with label 0: the function returns a dump of the labelTable
// This label can be used with the pop operation.

// $Id: FT.java,v 1.2 2003/04/02 12:59:08 lsw Exp $ 

package infonet.javasim.FT;

import drcl.inet.core.*;

import java.util.*;
import drcl.data.*;
import drcl.comp.*;
import infonet.javasim.FT.contract.*; 
import drcl.inet.contract.*;
import drcl.inet.data.*;
import drcl.net.Address;
import infonet.javasim.util.*;

import java.io.*;

/**
 * The component that manage the forwarding table (used in a MPLS network).
 *
 * Two differents tables are maintained by this component to provide this service. Indeed, the first table is used by edge routers only
 * to place a label inside the packet and forward this packet on a given outgoing port (this component must provide all useful
 * information to allow 
 */
 
public class FT extends drcl.comp.Component implements infonet.javasim.FT.FTInetConstants
{
	//drcl.data.Map prefTable = new drcl.data.RadixMap();
	IPRadixTree prefTable = new IPRadixTree();
	java.util.Map labelTable = Collections.synchronizedMap(new HashMap());
	boolean DEBUG = false;
	
	static {
				Contract c1_ = new FTLookup(Contract.Role_REACTOR);
				Contract c2_ = new FTConfig(Contract.Role_REACTOR);
				setContract(FT.class, SERVICE_FT_PORT_ID + "@" + PortGroup_SERVICE, new ContractMultiple(c1_, c2_));
			}
	Port timerPort = addPort(".timer");
	{
		addServerPort(SERVICE_FT_PORT_ID);
	}
	Port ftchange = addEventPort(EVENT_FT_CHANGED_PORT_ID);
	
	public FT()
	{	super();	}
	
	public FT(String id_)
	{	super(id_);	}
	
	public synchronized void reset()
	{
		super.reset();
		FTEntry[] all_ = _getAllPrefixTable();
		// remove entries that are dynamically created
		if(all_ != null)
		{	
			for(int i=0; i < all_.length;++i)
			{
				if(all_[i]._getTimeout() >= 0.0)
					_remove((RTKey)all_[i].getKey(), drcl.data.Map.MATCH_EXACT);
			}
		}
		all_ = _getAllLabelTable();
		if(all_ == null)
			return;
		else
		{
			for(int i=0;i<all_.length;++i)
				if(all_[i]._getTimeout() >= 0.0)
					_remove((FTKey)all_[i].getKey());
		}
			
	}
	
	/*synchronized Object _remove(RTKey key_, String matchType_)
	{
		Object o_ = prefTable.remove(key_, matchType_);
		//cancel timeouts if necessary
		if(o_ == null)
			return null;
			
		else if (o_ instanceof FTEntry)
			 {
			 	FTEntry e_ = (FTEntry)o_;
				if(e_.handle != null)
				{
					cancelFork(e_.handle);
					e_.handle = null;
				}
				return o_;
			 }
		else
		{
			Object[] oo_ = (Object[]) o_;
			if(oo_.length == 0) return null;
			FTEntry[] ee_ = new FTEntry[oo_.length];
			System.arraycopy(oo_, 0, ee_, 0, oo_.length);
			for(int i=0; i<ee_.length;++i)
				if(ee_[i].handle != null)
				{
					cancelFork(ee_[i].handle);
					ee_[i].handle = null;
				}
			return ee_;
		}
	}*/
	
	synchronized Object _remove(RTKey key_, String matchType_) // new version compatible with ssfnet' RadixTree
	{
		Object o_ = null;
		
		if(matchType_.equals(drcl.data.Map.MATCH_EXACT))
		{
			o_ = prefTable.remove(constructBitString(key_));
		}else
		{
			error(this, "FT::_remove(RTKey, matchType_)", infoPort, "Unsupported match type specified: " + matchType_);
		}
		return o_;
		
	}


	synchronized Object _remove(FTKey key_)
	{
		Object o_ = labelTable.remove(key_);
		
		if(o_ == null)
			return null;
			
		else 	if (o_ instanceof FTEntry)
				{
					FTEntry e_ = (FTEntry) o_;
					if(e_.handle != null)
					{
						cancelFork(e_.handle);
						e_.handle = null;
					}
					return o_;
				}
		return o_;
	}
	
	boolean _isEventExportEnabled()
	{
		return ftchange.anyPeer();
	}
	
	/*public void duplicate(Object source_)
	{
		super.duplicate(source_);
		
		FT that_ = (FT) source_;
		// duplicate prefixTable
		if(that_.prefTable != null)
			prefTable = (infonet.javasim.bgp4.util.RadixTree)that_.prefTable.clone();
		// duplicate labelTable
		if(that_.labelTable != null)
			//labelTable = (HashMap)that_.labelTable.clone();
			labelTable = (java.util.Map) ((java.util.HashMap)that_.labelTable).clone();
		
	}*/
	
	public String info()
	{
		
		String info = new String("");
		int i;
		FTEntry[] ftePrefix = _getAllPrefixTable();
		FTEntry[] fteLabel = _getAllLabelTable();

		info = "--- Prefix table ---\n";
		if(ftePrefix != null ) {
			for(i=0; i<ftePrefix.length; ++i)
			{
				info = info + ftePrefix[i].toString();
				info = info + '\n';
			}
		} else {
			info = info + "*null*\n";
		}
		

		info = info + "\n\n--- Label table ---\n";

		if(fteLabel != null )
		{
			for (i=0; i<fteLabel.length; ++i)
			{
				info = info + fteLabel[i].toString();
				info = info + '\n';
			}
		} else {
			info = info + "*null*\n";
		} 

		return(info);
	}
	
	void _exportEvent(String eventName_, Object target_, String description_)
	{
		if(ftchange._isEventExportEnabled())
			ftchange.exportEvent(eventName_, target_, description_);
			
	}
	
	protected void process (Object data_, Port inPort_)
	{
		
		if(inPort_ == timerPort)
		{
			FTEntry entry_ = null;
			int table = 0;
			// handle timout
			if(data_ instanceof RTKey)
			{
				entry_ = (FTEntry) get((RTKey) data_, RTConfig.MATCH_LONGEST);
				table = 1;
			}

			else if(data_ instanceof FTKey)
				 {
					entry_ = (FTEntry) get((FTKey) data_);
					table = 2;
				 }
				 else 
				 	return;
				  	
			if(entry_ == null)
				return;
			
			double timeout_ = entry_._getTimeout();
			if(timeout_ > 0.0)
				if(timeout_ <= getTime())
					if(table == 1) /* prefTable */
					{
						_remove((RTKey)data_, RTConfig.MATCH_EXACT);
						
						if(_isEventExportEnabled())
							_exportEvent(EVENT_FT_ENTRY_REMOVED, entry_, "due to timeout");
					}else /* labelTable */
					{
						_remove((FTKey)data_);
						
						if(_isEventExportEnabled())
							_exportEvent(EVENT_FT_ENTRY_REMOVED, entry_, "due to timeout");
					}							

		}
		
		//look up
		
		if(data_ instanceof RTKey)
		{
			FTEntry e_ = (FTEntry) get((RTKey)data_, RTConfig.MATCH_LONGEST);
			inPort_.doLastSending(e_);
			return;
		}
		
		if(data_ instanceof FTKey)
		{
			FTEntry e_ = (FTEntry) get((FTKey) data_);
			inPort_.doLastSending(e_);
			return;
		}
		
		if(!(data_ instanceof FTConfig.Message))
		{
			error(data_, "process()", inPort_, "unrecognized data");
			inPort_.doLastSending(null);
			return;
		}
		
		//config
		
		FTConfig.Message req = (FTConfig.Message)data_;
		int type_ = req.getType();
		
		int keyType = req.getKeytype();
		
		// remove
		
		if(type_ == FTConfig.REMOVE)
		{
			if (keyType ==FTConfig.TYPE_FTKEY)
				inPort_.doLastSending(_remove((FTKey)req.getKey()));
			else 
				inPort_.doLastSending(_remove((RTKey)req.getKey(), req.getMatchType()));
			return;
		}
		
		if(type_ == FTConfig.RETREIVE)
		{
			if(keyType == FTConfig.TYPE_FTKEY)
				inPort_.doLastSending(get((FTKey)req.getKey()));
			else
				inPort_.doLastSending(get((RTKey)req.getKey(), req.getMatchType()));

			return;
		}
		
		FTEntry entry_ = req.getEntry();
		double timeout_ = req.getTimeout();
		
		// add/modify

		if (type_ == FTConfig.ADD)
		{
			if(keyType == FTConfig.TYPE_FTKEY)
				add((FTKey)req.getKey(), entry_, timeout_);
			else
				add((RTKey)req.getKey(), entry_, timeout_);
			
			inPort_.doLastSending(null);
			return;
		}
		
		error(data_, "process()", inPort_, "unrecognized FT config message request");
		inPort_.doLastSending(null);
	}
	
	public void add(FTKey key_, FTEntry entry_, double timeout_)
	{
		if(timeout_ > 0.0)
			entry_._setTimeout(getTime() + timeout_);
		else
			entry_._setTimeout(Double.NaN);
		_add(key_, entry_);
	}
	
	public void add(RTKey key_, FTEntry entry_, double timeout_)
	{
		if(timeout_ > 0.0)
			entry_._setTimeout(getTime() + timeout_);
		else
			entry_._setTimeout(Double.NaN);
		_add(key_, entry_);
	}
	
	public void add(RTKey key_, FTEntry entry_)
	{
		entry_.setKey(key_);
		entry_._setTimeout(Double.NaN);
		_add(key_, entry_);
	}
	
	public void add(FTKey key_, FTEntry entry_)
	{
		entry_.setKey(key_);
		entry_._setTimeout(Double.NaN);
		_add(key_, entry_);
	}

	public void addLabelEntry(int label_, int bitSet_, double timeout_, String operations_)
	{
		FTKey label = new FTKey(label_);
		drcl.data.BitSet bs = new drcl.data.BitSet();
		bs.set(bitSet_);
		LinkedList list = new LinkedList();

		StringTokenizer st = new StringTokenizer(operations_, ":");
		while(st.hasMoreTokens())
		{
			String token = st.nextToken();
			if( (token.substring(0,4)).equals("PUSH") )
			{
				int val = Integer.parseInt(token.substring(5));
				OutLabel op_ = new OutLabel(OutLabel.PUSH, val);
				list.add(op_);
			}else if( (token.substring(0,4)).equals("POP ") )
			{
				int val = Integer.parseInt(token.substring(4));
				OutLabel op_ = new OutLabel(OutLabel.POP, val);
				list.add(op_);
			}
			else if( (token.substring(0,4)).equals("SWAP") ) {
				int val = Integer.parseInt(token.substring(5));
				OutLabel op_ = new OutLabel(OutLabel.SWAP, val);
				list.add(op_);
			}
			else if( (token.substring(0,4)).equals("STOP") ) {
				int val = Integer.parseInt(token.substring(5));
				OutLabel op_ = new OutLabel(OutLabel.STOP, val);
				list.add(op_);
			}
		}

		FTEntry fte = new FTEntry(label, bs, list);
		add(label, fte,timeout_);
	}
	

	public void addPrefixEntry(long ip_, int mask_, int bitSet_, double timeout_, String operations_)
	{
		RTKey rtkey = new RTKey();
		drcl.data.BitSet bs = new drcl.data.BitSet();
		bs.set(bitSet_);
		LinkedList list = new LinkedList();

		rtkey.setDestination(ip_);
		rtkey.setDestinationMask(mask_);

		StringTokenizer st = new StringTokenizer(operations_, ":");
		while(st.hasMoreTokens())
		{
			String token = st.nextToken();
			if( (token.substring(0,4)).equals("PUSH") )
			{
				int val = Integer.parseInt(token.substring(5));
				OutLabel op_ = new OutLabel(OutLabel.PUSH, val);
				list.add(op_);
			}else if( (token.substring(0,4)).equals("POP ") )
			{
				int val = Integer.parseInt(token.substring(4));
				OutLabel op_ = new OutLabel(OutLabel.POP, val);
				list.add(op_);
			}else if( (token.substring(0,4)).equals("SWAP") )
			{
				int val = Integer.parseInt(token.substring(5));
				OutLabel op_ = new OutLabel(OutLabel.SWAP, val);
				list.add(op_);
			}
		}

		FTEntry fte = new FTEntry(rtkey, bs, list);
		add(rtkey, fte, timeout_);
		
	}
	
	
	synchronized void _add(RTKey key_, FTEntry entry_)
	{
		if(entry_ == null)
			return;
		
		//FTEntry old_ = (FTEntry) prefTable.get(key_, drcl.data.Map.MATCH_EXACT);
		FTEntry old_ = (FTEntry) get(key_, drcl.data.Map.MATCH_EXACT); // new version compatible with ssfnet RadixTree
		boolean eventEnabled_ = _isEventExportEnabled();
		if(old_ != null)
		{
			// modifuy: compare each field of the two entries
			FTEntry copy_ = eventEnabled_?(FTEntry)old_.clone():null;
			boolean changed_ = false;
			// outgoing interfaces
			drcl.data.BitSet outIf_ = entry_.getOutIf();
			if (!drcl.util.ObjectUtil.equals(outIf_, old_.getOutIf()))
			{
				old_.setOutIf(outIf_);
				changed_ = true;
			}
			
			LinkedList labelop = entry_.getLabelOp();
			
			if(!drcl.util.ObjectUtil.equals(labelop, entry_.getLabelOp()))
			{
				old_.setLabelOp(labelop);
				changed_ = true;
			}
			
			double newTimeout_ = entry_._getTimeout();
			double oldTimeout_ = old_._getTimeout();
			
			if(newTimeout_ != oldTimeout_)
			{
			
				if(newTimeout_ > 0.0 && newTimeout_ < oldTimeout_)
				{
					cancelFork(old_.handle);
					old_.handle= forkAt(timerPort, key_, newTimeout_);
				}
				else 	if(newTimeout_ <= 0.0)
							newTimeout_ = Double.NaN;
				old_._setTimeout(newTimeout_);
				changed_ = true;
			}
			
			if(changed_ && eventEnabled_)
				_exportEvent(EVENT_FT_ENTRY_MODIFIED, new FTEntry[]{copy_,old_},"");
			
		}
		else
		{
			// add
			
			double newTimeout_ = entry_._getTimeout();
			
			if(newTimeout_ > 0.0)
				entry_.handle = forkAt(timerPort, key_, newTimeout_);
			if (key_ != null)
				//prefTable.addEntry(key_, entry_);
				addEntry(key_, entry_); // new version compatible with ssfnet'RadixTree
			if(eventEnabled_)
				_exportEvent(EVENT_FT_ENTRY_ADDED, entry_, "");
		}
		
		if(DEBUG)
			error(this, "_add(RTKEY, ENTRY)", infoPort, "Entry added into FT: key=" + key_ + " Entry=" + entry_);

	}
	
	synchronized private void addEntry(RTKey key_, FTEntry entry_)
	{				
		BitString bs = constructBitString(key_);
		prefTable.add(bs, entry_);
	}
	
	private BitString constructBitString(RTKey key_) // function to convert RTKey destination field to BitString
	{
		int nst = getnbset((int)key_.getDestinationMask());
		long msk = key_.getMaskedDestination();
		if(msk < 0) // when TCL is used, it sends signed integer value to RTKey constructor. If the IP address is more than 127.255.255.255, TCL will return a NEGATIVE value !  
		{
			System.err.println("Negative IP address. Assume that your are using TCL script: conversion from TCL to Java");
			msk = msk + 4294967296L;
		}
		
		BitString debug = new BitString(msk >> (32-nst), nst);
		// System.err.println("[constructBitString] RTKey =" + key_  +" nst = " + nst + " msk=" + msk + " BitString: "+ debug);
		return debug;
	}
	
	
	synchronized void _add(FTKey key_, FTEntry entry_)
	{
		if(entry_ == null)
			return;
		boolean eventEnabled_ = _isEventExportEnabled();
		FTEntry old_ = (FTEntry) labelTable.get(key_);
		if(old_ != null)
		{
			// the two entries must be compared
			FTEntry copy_ = eventEnabled_?(FTEntry)old_.clone():null;
			boolean changed_ = false;
			
			drcl.data.BitSet outIf_ = entry_.getOutIf();
			if(!drcl.util.ObjectUtil.equals(outIf_, old_.getOutIf()))
			{
				old_.setOutIf(outIf_);
				changed_ = true;
			}
			
			LinkedList labelop_ = entry_.getLabelOp();
			if(drcl.util.ObjectUtil.equals(labelop_, old_.getLabelOp()))
			{
				changed_ = true;
				old_.setLabelOp(labelop_);
			}
			
			double newTimeout_ = entry_._getTimeout();
			double oldTimeout_ = old_._getTimeout();
			
			if(newTimeout_ != oldTimeout_)
			{
				if(newTimeout_ > 0.0 && newTimeout_ < oldTimeout_)
				{
					cancelFork(old_.handle);
					old_.handle = forkAt(timerPort, key_, newTimeout_);
				}else	if(newTimeout_<= 0.0)
							newTimeout_ = Double.NaN;
				old_._setTimeout(newTimeout_);
				changed_ = true;
			}
			if(changed_ && eventEnabled_)
				_exportEvent(EVENT_FT_ENTRY_MODIFIED, new FTEntry[]{copy_, old_},"");
			
					
			
		}
		else
		{
			double newTimeout_ = entry_._getTimeout();
			
			if(newTimeout_ > 0.0)
				entry_.handle = forkAt(timerPort, key_, newTimeout_);
			if(key_ != null)
				labelTable.put(key_, entry_);
			if(eventEnabled_)
				_exportEvent(EVENT_FT_ENTRY_ADDED, entry_, "");				
		}
	}
	


/*	public synchronized Object get(RTKey key_, String matchType_)
	{
		Object o_ = prefTable.get(key_, matchType_);
		if(o_ == null || o_ instanceof FTEntry)
		{
			if(DEBUG)
				error(this, "get(RTKEY, MATCHTYPE)", infoPort, "Searching " + key_ + " result:" + (FTEntry)o_);
			
			return o_;
		}
		else
		{
			Object[] oo_ = (Object[]) o_;
			FTEntry[] ee_ = new FTEntry[oo_.length];
			System.arraycopy(oo_,0,ee_,0,oo_.length);
			return ee_;
		}
	}*/
	
	private int getnbset(int ip_prefix)
	{
		int i=0, nset = 32;
		while ( (nset >=0) && ((ip_prefix & (1 << i)) == 0) )
		{
			nset --;
			i ++;
		}
		return nset;
	}
	
	/* This version is compliant with the RadixTree from ssfnet */
	public synchronized Object get(RTKey key_, String matchType_)
	{
		Object o_ = null;

		if(key_ == null) {
			if(matchType_.equals(drcl.data.Map.MATCH_WILDCARD)) {
				o_ = (prefTable.getAllEntries()).toArray();
			} else {
				error(this, "get(RTKey, machType)", infoPort, "get with null key  !");
			}
		}else if(matchType_.equals(drcl.data.Map.MATCH_EXACT)) {
			o_ = prefTable.find(constructBitString(key_));
		}else if(matchType_.equals(drcl.data.Map.MATCH_LONGEST)) {
			o_ = prefTable.longest_match(constructBitString(key_));
		}else if(matchType_.equals(drcl.data.Map.MATCH_WILDCARD)) {
			o_ = (prefTable.get_descendants(constructBitString(key_))).toArray();
		}else {
			error(this, "get(RTKey, matchType)", infoPort, "matchType not implemented :" + matchType_);
		}
		return o_;
	}
	
	
	public synchronized Object get(FTKey key_)
	{
		if(key_.get() == 0)
			return _getAllLabelTable();
		
		return labelTable.get(key_);
	}


	public FTEntry[] _getAllPrefixTable()
	{
		if(prefTable == null) 
			return null;
			
		Object[] oo_ = (Object[])get(null, drcl.data.Map.MATCH_WILDCARD);
		FTEntry[] ee_ = new FTEntry[oo_.length];
		System.arraycopy(oo_, 0, ee_, 0, oo_.length);
		return ee_;
	}
	
	public FTEntry[] _getAllLabelTable()
	{
		if(labelTable.size() == 0)	
			return null;
		
		Object[] oo_ = (Object[]) labelTable.values().toArray();
		FTEntry[] ee_ = new FTEntry[oo_.length];
		System.arraycopy(oo_, 0, ee_, 0, oo_.length);
		return ee_; 
	}
	
	
	public boolean loadFromFile(String FileName) throws FileNotFoundException
	{
		RandomAccessFile raf = new RandomAccessFile(FileName,"r");
		String line;

		try {
				while((line = raf.readLine()) != null)
				{
					double timeout;
					StringTokenizer st;
					LinkedList labelOp = new LinkedList(); 
					drcl.data.BitSet outif = new drcl.data.BitSet(192);
					switch(line.charAt(0))
					{
						case 'L':
						{
							int label;						
							st = new StringTokenizer(line.substring(2));
							
							// label
							if(st.hasMoreTokens())
								label = Integer.parseInt(st.nextToken());
							else return false;
							
							if(st.hasMoreTokens())
								outif.set(Integer.parseInt(st.nextToken()));
							else return false;
							
							if(st.hasMoreTokens())
								timeout = Double.parseDouble(st.nextToken());
							else return false;
								
							
							while(st.hasMoreTokens())
							{
								OutLabel ol = new OutLabel(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
								labelOp.addLast(ol);
							}
							

							FTKey ftk = new FTKey(label);

							FTEntry fte = new FTEntry(ftk, outif, labelOp);

							add(ftk, fte, timeout);
							
							break;
						}
					
						case 'I':
						{	
							// IP prefix
							long pref, mask;
							st = new StringTokenizer(line.substring(2));
							
							if(st.hasMoreTokens())
								pref = Long.parseLong(st.nextToken(), 16);
							else return false;
							
							if(st.hasMoreTokens())
								mask = Long.parseLong(st.nextToken(), 16);
							else return false;
							
							if(st.hasMoreTokens())
								outif.set(Integer.parseInt(st.nextToken()));
							else return false;
													
							if(st.hasMoreTokens())
								timeout = Double.parseDouble(st.nextToken());
							else return false;						
							
	
							while(st.hasMoreTokens())
							{
								OutLabel ol = new OutLabel(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
								labelOp.addLast(ol);
							}
							
							RTKey rtk = new RTKey();
							rtk.setDestination(pref);
							rtk.setDestinationMask(mask);
							
							FTEntry fte = new FTEntry(rtk, outif, labelOp);
							
							add(rtk, fte, timeout);
							

							break;
						}
						
						case '#':
							break;
							
						default:
							break;
					}
					
				} 
	
		}catch(Exception ex)
		{
			System.out.println("*** ERROR ***");
			ex.printStackTrace();
		}
	
		return true;
	
	}
}
