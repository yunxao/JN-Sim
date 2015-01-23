//@author Cristel Pelsser
//@date 18/04/2002
//
// $Id: Label.java,v 1.1.1.1 2002/08/09 15:00:52 lsw Exp $
package infonet.javasim.MPLS;

import drcl.net.Packet;

public class Label
{
    /* 0, 1, 2, 3 are reserved label values */

	int label; /* stored inside 20 bits inside an MPLS header */
	int exp; /* reserved for experimental use;
		 stored inside 3 bits inside an MPLS header;
		 by default when no value is given for exp, it is set to 0 */
	
	/* there is one more bit inside an MPLS header to indicate if it is the 
	bottom of the stack */

	/* Two possible ways to handle the hops field :
	Either it starts from the value carried in InetPacket 
	and is incremented at each hop
	and then copied into the InetPacket at decapsulation
	or hops is only incremented by one when leaving the 
	MPLS cloud.*/

	/* In InetPacket, there is a field hops and a field ttl;
	Only hops is incremented at each hop;
	ttl is used to see if too many hops have been crossed */

	int hops; /* used for loop detection */
	int ttl; /* stored inside 8 bits inside an MPLS header */
	
	public Label()
    {label = -1;}

    public Label(int label_)
    {
		label = label_;
		exp = 0;
    }

    public Label(int label_, int ttl_)
    {
		label = label_;
		exp = 0;
		hops = 0;
		ttl = ttl_;
    }
    
    public Label(int label_, int exp_, int ttl_)
    {
		label = label_;
		exp = exp_;
		hops = 0;
		ttl = ttl_;
    }
    
    public Label(int label_, int exp_, int hops_, int ttl_)
    {
		label = label_;
		exp = exp_;
		hops = hops_;
		ttl = ttl_;
    }
    
    public int getLabel()
    {return label;}
    
    public void setLabel(int label_)
    {label = label_;}

    public int getExp()
    {return exp;}

    public void setExp(int exp_)
    {exp = exp_;}
 
    public int getHops()
    {return hops;}

    public void setHops(int hops_)
    {hops = hops_;}
    
    public int getTTL()
    {return ttl;}

    public void setTTL(int ttl_)
    {ttl = ttl_;}

    /* Returns the size in bits of an MPLS label */
    /* It does not correspond to the size of the 
       labels used in the simulations */
    public static int getLabelSize()
    {return 4;} 

    public String toString(String separator_)
    {return "label:" + label + separator_ 
	 + "exp:" + exp + separator_ 
	 + "ttl:" + hops + "/" + ttl;}
	 
	public boolean equals(Object o_)
	{
		if(o_ == this)
			return true;
			
		if(!(o_ instanceof Label))
			return false;
			
		Label l_ = (Label) o_;
		
		return (label == l_.label && hops == l_.hops && ttl == l_.ttl && exp == l_.exp);
	}
	
	public Object clone()
	{
		return new Label(label, exp, hops, ttl);
	}
	
	public void duplicate (Object source_)
	{
		Label l_ = (Label)source_;
		
		label = l_.label;
		hops = l_.hops;
		exp = l_.exp;
		ttl = l_.ttl;
	}

	public String toString()
	{
		return "LB=" + label + ",exp=" + exp
				+ ",ttl=" + ttl + ",hops=" + hops;
	}
}

