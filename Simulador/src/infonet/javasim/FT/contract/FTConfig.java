// Author: Louis SWINNEN (lsw@infonet.fundp.ac.be)
//
// August 2002
//
// $Id: FTConfig.java,v 1.1.1.1 2002/08/09 15:00:52 lsw Exp $

package infonet.javasim.FT.contract;

import drcl.comp.*;
import drcl.data.*;
import drcl.net.*;
import drcl.inet.data.*;
import drcl.util.ObjectUtil;
import drcl.util.StringUtil;
import drcl.inet.contract.*;
import infonet.javasim.FT.*;

/** 
  The label configuration contract. This contract defines the following services at the reactor:
  <dl>
  <dt> 
  */
  
public class FTConfig extends Contract
{
	public static final FTConfig INSTANCE = new FTConfig();

	public static final int ADD = 0;
	public static final int REMOVE = 3;
	public static final int RETREIVE = 4;
	static final String[] TYPES = {"add", "unused", "unused", "remove", "retreive"};
	public static final int TYPE_FTKEY = 0;
	public static final int TYPE_RTKEY = 1;
	
	public FTConfig()
	{	super();	}
	
	public FTConfig(int role_)
	{	super(role_);	}
	
	public String getName()
	{	return "FTConfiguration Contract"; }
	
	public Object getContractContent()
	{	return null;	}
	
	public static void add(FTKey key_, FTEntry entry_, double timeout_, Port out_)
	{	out_.sendReceive(new Message(ADD, key_, entry_, timeout_)); }

	public static Object remove(FTKey key_, Port out_)
	{	return out_.sendReceive(new Message(REMOVE, key_));	}
	
	public static Object retreive(FTKey key_, Port out_)
	{	return out_.sendReceive(new Message(RETREIVE, key_));	}
	
	public static Object getAllEntries(Port out_)
	{	return out_.sendReceive(new Message(RETREIVE, new FTKey(0)));	}
	
	public static Object createAddRequest(RTKey key_, FTEntry entry_, double timeout_)
	{
		return new Message(ADD, key_, entry_, timeout_);
	}

	public static Object createAddRequest(FTKey key_, FTEntry entry_, double timeout_)
	{
		return new Message(ADD, key_, entry_, timeout_);
	}
	
	public static Object createRemoveRequest(RTKey key_, String match_)
	{
		return new Message(REMOVE, key_, match_);
	}

	public static Object createRemoveRequest(FTKey key_)
	{
		return new Message(REMOVE, key_);
	}

	public static Object createRetreiveRequest(RTKey key_, String match_)
	{
		return new Message(RETREIVE, key_, match_);
	}
	
	public static Object createRetreiveRequest(FTKey key_)
	{
		return new Message(RETREIVE, key_);
	}
	
	public static void main(String args[])
	{
		// for test only
		
		RTKey rtkey = new RTKey(0xc0a40001, -1, 0x8a300102, 0xfffffff0, 0, -1);
		java.util.LinkedList l = new java.util.LinkedList();
		FTEntry ftentry;
		BitSet bs = new BitSet(0);
		
		l.add(new OutLabel(0,2));
		l.add(new OutLabel(1,3));
		bs.set(0,1);
		
		ftentry = new FTEntry(rtkey, bs, l);
		
		Message m;
		
		m = (Message) createAddRequest(rtkey, ftentry, 5.0);
		
		System.out.println("info: " + m.toString());
		
	}

	public static class Message extends drcl.comp.Message
	{
		int type = -1;
		FTKey inLabel = null;
		FTEntry entry = null;
		double timeout = 0.0;
		RTKey rtkey = null;
		String matchType = "";
		public int keyType = -1;
		
		public Message()
		{ }
		
		public Message(int type_, FTKey inLabel_, FTEntry entry_, double timeout_)
		{
			type = type_;
			inLabel = inLabel_;
			entry = entry_;
			timeout = timeout_;
			keyType = 0;
		}
		
		public Message(int type_, RTKey rtkey_, FTEntry entry_, double timeout_)
		{
			type = type_;
			rtkey = rtkey_;
			entry = entry_;
			timeout = timeout_;
			keyType = 1;		
		}
		
		public Message(int type_, RTKey rtkey_, FTEntry entry_, double timeout_, String matchType_)
		{
			type = type_;
			rtkey = rtkey_;
			entry = entry_;
			timeout = timeout_;
			keyType = 1;
			matchType = matchType_;
		}
		
		
		public Message(int type_, FTKey inLabel_)
		{
			type = type_;
			inLabel = inLabel_;
			keyType = 0;
		}
		
		public Message(int type_, RTKey rtkey_, String matchType_)
		{
			type = type_;
			rtkey = rtkey_;
			keyType = 1;
			matchType = matchType_;
		}
		
		public int getKeytype()
		{
			return	keyType;
		}
		
		public String getMatchType()
		{
			return matchType;
		}
		
		public int getType()
		{	return type;	}
		
		public void setType(int type_)
		{	type = type_;	}
		
		public Object getKey()
		{	if(keyType == 0)
				return inLabel;	
			else
				return rtkey;
		}
		
		public void setKey(FTKey inLabel_)
		{
			inLabel = inLabel_;
			keyType = 0;
		}
		public void setKey(RTKey rtkey_)
		{
			rtkey = rtkey_;
			keyType = 1;
		}
		
		public FTEntry getEntry()
		{	return entry;	}
		
		public void setEntry(FTEntry ftentry_)
		{	entry = ftentry_;		}
		
		public double getTimeout()
		{	return timeout;	}
		
		public void setTimeout(double timeout_)
		{	timeout = timeout_;	}
		
		public void duplicate(Object source_)
		{
			Message that_ = (Message) source_;
			type = that_.type;
			keyType = that_.keyType;
			if(keyType == 0)
				inLabel = (FTKey)that_.inLabel.clone();
			else
				rtkey = (RTKey)that_.rtkey.clone();
			entry = that_.entry == null?null:(FTEntry)ObjectUtil.clone(that_.entry);
			timeout = that_.timeout;
		}
		
		public Object clone()
		{	return new Message(type, inLabel, entry, timeout);	}
		
		public Contract getContract()
		{	return INSTANCE;	}
		
		public String toString(String separator_)
		{
			if(type == REMOVE || type == RETREIVE)
				return "FTCONFIG:" + TYPES[type] + separator_ + "key:" + (keyType == 0?inLabel.toString():rtkey.toString());
			else
				return "FTCONFIG:" + TYPES[type] + separator_ + "key:" + (keyType == 0?inLabel.toString():rtkey.toString()) + separator_ + "entry:" + entry + separator_ + "timeout:" + timeout;
		}
	}
}
