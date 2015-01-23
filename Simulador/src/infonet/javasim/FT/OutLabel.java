// Author: Louis SWINNEN (lsw@infonet.fundp.ac.be)
//
// August 2002
//
// $Id: OutLabel.java,v 1.1.1.1 2002/08/09 15:00:52 lsw Exp $
package infonet.javasim.FT;

import drcl.inet.data.*;

public class OutLabel
{
	public int op;
	public int label;
	
	public static final int PUSH = 1;
	public static final int POP = 2;
	public static final int SWAP = 3;
	public static final int STOP = 4;
	
	public OutLabel()
	{	}
	
	public OutLabel(int op_, int label_)
	{
		op = op_;
		label = label_;
	}
	
	public void setLabel(int label_)
	{
		label = label_;
	}
	
	public void setOp (int op_)
	{
		op = op_;
	}
	
	public int getOp()
	{
		return op;
	}
	
	public int getLabel()
	{
		return label;
	}
	
	public boolean equals(Object o_)
	{
		if(o_ == null)
			return false;
		
		if(o_ == this)
			return true;
		
			
		if (!(o_ instanceof OutLabel))
			return false;
			
		OutLabel that_ = (OutLabel) o_;
		
		if(that_.op != op)
			return false;
		
		return (that_.label == label);
	}
	
	public void duplicate(Object source_)
	{
		if (source_ == null)
			return;
		
		OutLabel that_ = (OutLabel) source_;
		op = that_.op;
		label = that_.label;
		
	}
	
	public static void main(String args[])
	{
		// only for test
		
		OutLabel a = new OutLabel(0,2);
		
		System.out.println("Label = " + a.getLabel() + " Operation= " + a.getOp());
	}
		
}
