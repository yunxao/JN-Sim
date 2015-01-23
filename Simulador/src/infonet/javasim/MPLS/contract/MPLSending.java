// Author: Louis SWINNEN (lsw@infonet.fundp.ac.be)
//
// August 2002
//
// $Id: MPLSending.java,v 1.2 2003/04/02 12:59:08 lsw Exp $

package infonet.javasim.MPLS.contract;

import drcl.inet.contract.*;

import drcl.comp.*;
import drcl.net.Packet;
import drcl.inet.InetPacket;
import infonet.javasim.MPLS.LSPacket;
import drcl.util.StringUtil;

public class MPLSending extends Contract
{
	public static final MPLSending INSTANCE= new MPLSending();
	public static final int PACKET_LSP = 1;
	public static final int PACKET_IN = 2;
	public static final int PACKET_UNK = -1;	
	public MPLSending()
	{
		super();
	}
	
	public MPLSending(int role_)
	{
		super(role_);
	}
	
	public String getName()
	{
		return("MPLSending contract");
	}
	
	public Object getContractContent()
	{
		return null;
	}
	
	
	/* The message can contains an Inet Packet or a LSP packet */
	public static class Message extends drcl.comp.Message
	{
		int type = PACKET_UNK;
		InetPacket pkt;
		LSPacket lsp;
		
		public Message()
		{
		}
		
		public Message(InetPacket ip_)
		{
			type = PACKET_IN;
			pkt = ip_;
			lsp = null;
		}
		
		public Message(LSPacket lsp_)
		{
				type = PACKET_LSP;
				lsp = lsp_;
				pkt = null;
		}
		
		public Packet getPacket()
		{
			switch(type)
			{
				case PACKET_LSP:
					return lsp;
				case PACKET_IN:
					return pkt;
				case PACKET_UNK:
				default:
					return null;
			}
			
		}
		
		public void setPacket(InetPacket ip_)
		{
			type = PACKET_IN;
			pkt = ip_;
			lsp = null;
		}
		
		public void setPacket(LSPacket lsp_)
		{
			type = PACKET_LSP;
			lsp = lsp_;
			pkt = null;
		}
		
		public void duplicate(Object source_)
		{
			Message that_ = (Message)source_;
			type = that_.type;
			lsp = that_.lsp;
			pkt = that_.pkt;
		}

		public Object clone()
		{
			switch(type)
			{
				case PACKET_LSP:
					return new Message(lsp);
				case PACKET_IN:
					return new Message(pkt);
				case PACKET_UNK:
				default:
					return new Message();
			}			
		}
		
		public int getType()
		{
			return type;
		}
		
		public String toString(String seperator_)
		{
		    return "MPLSending: toString not yet implemented !";
		}
		
		public Contract getContract()
		{	return INSTANCE;	}
		
	}
}
