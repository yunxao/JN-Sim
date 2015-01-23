// Author: Louis SWINNEN (lsw@infonet.fundp.ac.be)
//
// August 2002
//
// $Id: FTKey.java,v 1.1.1.1 2002/08/09 15:00:52 lsw Exp $

package infonet.javasim.FT;

import drcl.inet.data.*;

import drcl.data.*;

/**
 * The key class to the forwarding table entry.
 *
 * @author Louis SWINNEN
 * @version 0.5, 07/30/2002
 * @see FTEntry
 */

// license: FreeBSD-like.


public class FTKey extends drcl.DrclObj /* To avoid problems, this key doesn't inherit from drcl.data.MapKey */
{
	/**
	 * Creates an <code>FTKey</code> given a label. If this label is not specified, a negative label will be used by default
	 * @param label_ label.
	 * @return an <code>FTKey</code> instance.
	 */

	int Keylabel=-1;

	public FTKey(int label_)
	{
		set(label_);
	}
	
	public FTKey()
	{	}
	
	public void set(int label_)
	{
		Keylabel = label_;
	}
	
	public int get()
	{
		return Keylabel;
	}
	
	//
	private void ___MISC___() {}
	//
	
	public int hashCode()
	{
		return Keylabel;
	}
	
	public boolean equals(Object o_)
	{
		if(o_ instanceof FTKey)
			return( ((FTKey)o_).get() == Keylabel);
		else return false;
	}
	
	public String toString()
	{
		return "(" + Keylabel+")";
	}
	
}
