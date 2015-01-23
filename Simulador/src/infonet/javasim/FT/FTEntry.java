// AUTHOR: Louis SWINNEN (lsw@infonet.fundp.ac.be)
// August 2002
//
// $Id: FTEntry.java,v 1.1.1.1 2002/08/09 15:00:52 lsw Exp $
//
// License: This code is provided under FreeBSD-like license.


package infonet.javasim.FT;


import drcl.inet.data.*;

import java.util.LinkedList;
import java.util.Stack;

/**
 * The forwarding table entry class.
 *
 * @author Louis SWINNEN
 * @version 0.1, 04/15/2002
 * @see FTKey
 */
 
// This code is under FreeBSD-like license.

public class FTEntry extends drcl.DrclObj
{
	public drcl.comp.ACATimer handle; // timeout
	
	FTKey inLabel = null;
	RTKey bsKey = null;
	drcl.data.BitSet bsOutIfs=null;
	LinkedList labelOp=null;
	int[] outIf = null;
	double timeout;
	
	public FTEntry()
	{ }
	

	public FTEntry(FTKey labelIn_, drcl.data.BitSet bsOutIfs_, LinkedList labelOp_)
	{
		inLabel = labelIn_;
		bsOutIfs = bsOutIfs_;
		labelOp = labelOp_;
	}	
	
	public FTEntry(RTKey bsKey_, drcl.data.BitSet bsOutIfs_, LinkedList labelOp_)
	{
		bsKey = bsKey_;
		bsOutIfs = bsOutIfs_;
		labelOp = labelOp_;
	}
	
	public FTEntry(drcl.data.BitSet bsOutIfs_)
	{
		bsOutIfs = bsOutIfs_;
	}

	public void duplicate(Object source_)
	{
		FTEntry that_= (FTEntry) source_;
		
		if(that_.inLabel != null)
			inLabel = (FTKey) drcl.util.ObjectUtil.clone(that_.inLabel);
			
		if(that_.bsKey != null)
			bsKey = (RTKey) drcl.util.ObjectUtil.clone(that_.bsKey);
			
		if(that_.outIf != null)
		{
			outIf = new int[that_.outIf.length];
			for(int i=0; i<outIf.length;++i)
				outIf[i] = that_.outIf[i];
		}
		
		bsOutIfs = (drcl.data.BitSet) drcl.util.ObjectUtil.clone(that_.bsOutIfs);
		labelOp = that_.labelOp == null ? null : new LinkedList(that_.labelOp);
		timeout = that_.timeout;
		
	}

	public drcl.data.BitSet getOutIf()
	{
		return bsOutIfs;
	}

	public int[] _getOutIfs()
	{
		if(outIf == null)
			return bsOutIfs ==null ? null : bsOutIfs.getSetBitIndices();
		return outIf;
	}
	
	public void setOutIf(drcl.data.BitSet outIf)
	{
		bsOutIfs = outIf;
		outIf = null;
	}
		
	public void setKey(FTKey labelIn_)
	{
		inLabel = labelIn_;
	}
	
	public void setKey(RTKey bsKey_)
	{
		bsKey = bsKey_;
	
	}
	
	public Object getKey()
	{
		if(bsKey == null)
			if(inLabel == null)
				return null;
			else
				return inLabel;
		else 
			return bsKey;
	}
		
	public void setLabelOp(LinkedList labelop_)
	{
		labelOp = labelop_;
	}
	
	public LinkedList getLabelOp()
	{
		return labelOp;
	}
	
	public double _getTimeout()
	{
		return timeout;
	}
	
	public void _setTimeout(double timeout_)
	{
		timeout = timeout_;
	}
	
	public String toString()
	{
		
		String result = "In:";
		
		if(bsKey == null)
			if(inLabel == null)
				result =  "null";
			else
				result = "[FTKey]" + inLabel;
		else
			result = "[RTKey]" + bsKey;
		
		result = result + " OutIf:" + bsOutIfs;
		
		if(labelOp != null)
		{
			for(int i = 0; i < labelOp.size(); ++i)
				result = result + "[" + i + "] = { operation =" + ((OutLabel)labelOp.get(i)).op + "; label = " + ((OutLabel)labelOp.get(i)).label + "} ;;";
			result = result + "\n";
		}	
		else
			result = result + "LabelOp = null\n";
			
		return result;
		
	}
	
	public void addOp(OutLabel op_)
	{
		if(labelOp == null)
			labelOp = new LinkedList();
		
		labelOp.add(op_);
	}
	
	public OutLabel removeOp(int index)
	{
		if(labelOp == null)
			return null;
		
		if(index <= labelOp.size())
			return (OutLabel)labelOp.remove(index);
		return null;
		
	}
	
	
	public boolean removeOp(OutLabel o_)
	{
		if(o_ == null)
			return false;
		
		return(labelOp.remove(o_));
	}

	public boolean equals(Object o_)
	{
		if(o_ == this) 
			return true;
		if (!(o_ instanceof FTEntry))
			return false;
			
		FTEntry that_ = (FTEntry)o_;

		if(bsKey == null)
			if(inLabel != null)
				if(!drcl.util.ObjectUtil.equals(inLabel, that_.inLabel))
					return false;
			else
				if(that_.inLabel != null && that_.bsKey != null)
					return false;
		else
			if(!drcl.util.ObjectUtil.equals(inLabel, that_.inLabel))
				return false;
			
		if(!drcl.util.ObjectUtil.equals(bsOutIfs, that_.bsOutIfs))
			return false;
		if(!drcl.util.ObjectUtil.equals(labelOp, that_.labelOp))
			return false;
		if(Double.isNaN(timeout) && Double.isNaN(that_.timeout))
			return true;
		return(timeout == that_.timeout);
	}
} 
