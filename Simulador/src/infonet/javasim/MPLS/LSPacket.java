//@author Cristel Pelsser
//@date 18/04/2002
//
// $Id: LSPacket.java,v 1.2 2003/04/02 12:59:08 lsw Exp $
package infonet.javasim.MPLS;

//voir si ceci est inclu dans drcl.util.ObjectUtil !!!!!!!!!!!!!!
//dans drcl.util.queue il y a un truc qui s'appelle Stack.java mais cela n'a
//pas l'air d'tre la mme chose que le stack de java.util.Stack
import java.util.Stack;
import drcl.inet.*;
import drcl.net.Address;
import drcl.net.Packet;
import drcl.util.StringUtil;
import drcl.util.ObjectUtil;

/* Defines the packet structure for an MPLS packet. */

public class LSPacket extends Packet
{
    /*Name of the MPLS Packet*/
    public static final String NAME = "LSP";

    //stack of labels
    Stack labelStack;

    /* Dans Packet il y a un champ id. Que faut-il en faire ici !!!!!! */

    /* p_ should be an InetPacket for us but other packets may be used later */
    /* bodySize_ is the size of the packet p_ */
    public LSPacket(int bodySize_, Packet p_)
    {
		super(0, bodySize_, p_); /* when labelStack is empty, 
				    the headerSize_ is 0 */
		labelStack = new Stack();
	
    }

    public LSPacket(Label label_, int bodySize_, Packet p_)
    {
		super(Label.getLabelSize(), bodySize_,p_);
		labelStack = new Stack();
		if (label_ != null)
		{
			if(p_ instanceof InetPacket)
			{
				InetPacket ip_ = (InetPacket) p_;
				label_.setHops(ip_.getHops());
				label_.setTTL(ip_.getTTL());

			}

		    labelStack.push(label_);
		}
	
		/* When an InetPacket is encapsultated, the hops and ttl fields from
		   the InetPacket are copied into the MPLS header*/
/*		if (p_.getName() =="INET")
	    {
			this.setHops() = p_.getHops();
			this.setTTL() = p_.getTTL();
	    }*/
		
    }

    /* pushes a label in the labelStack when packet is an LSPacket */
    /* returns false when label_ could not be pushed */
    public synchronized boolean push(Label label_)
    {
		if (/* this.getName() == "LSP" && */ label_ != null)
	    {
			if (labelStack.empty())
			{
				/* look if encapsulated packet is of type
				 INETPacket */
				Packet p = (Packet)this.getBody();
				/* if (p.getName() == "INET") */
				if(p instanceof InetPacket)
				{
					InetPacket ip_ = (InetPacket)p;
					label_.setHops(ip_.getHops());
					label_.setTTL(ip_.getTTL());
			    }
				labelStack.push(label_);
				headerSize += Label.getLabelSize();
		    }
			else
		    {
				Label top = (Label)labelStack.peek();
				label_.setHops(top.getHops());
				label_.setTTL(top.getTTL());
				labelStack.push(label_);
				headerSize += Label.getLabelSize();
		    }
		
			return true;
	    }
		else
		    return false;
	    
    }
	
	public boolean isStackEmpty()
	{
		return labelStack.empty();
	}

	public synchronized Label peek()
	{
		if(labelStack.empty())
			return null;
		else
			return ((Label)labelStack.peek());
	}

    /* il devrait y avoir une option pour dire que l'on garde l'ancien hops
       +1 au lieu du nouveau dans certains cas comme possible dans la 
       ralit */

    /* removes the label at the top of the labelStack when it is not empty */
	
	/* Valeur de op : 0 -> on recopie *btement* la valeur de op et TTL actuelle vers le nouveau sommet 
	                  1 -> on ajoute la valeur 1 au nouveau sommet de la pile                             */
					  
	/* !!!!! Vrifier que Hops <= TTL !!!!! */
	
    public synchronized Label pop(int op)
    {
		Label oldTop, newTop;
		if (!labelStack.empty())
		{
			headerSize -= Label.getLabelSize();
			oldTop = (Label)labelStack.pop();
			if (!labelStack.empty())
			{
				newTop = (Label)labelStack.peek();
				if(op == 0)
				{
					newTop.setHops(oldTop.getHops());
					newTop.setTTL(oldTop.getTTL());
			   	}else
			   	{
			   		newTop.setHops(newTop.getHops()+1);
			   	}
		   	}
	       	else
		   	{
		       Packet p = (Packet)getBody();
			   if(op == 0)
			   {
			 
				   if(p instanceof InetPacket)
				   {
				       InetPacket ip_ = (InetPacket) p;
					   ip_.setHops(oldTop.getHops());
				       ip_.setTTL(oldTop.getTTL());
				   }
				}
				else
				{
				   if(p instanceof InetPacket)
				   {
				       InetPacket ip_ = (InetPacket) p;
					   ip_.setHops(ip_.getHops()+1);
				   }					
				}
		   	}
	       	return oldTop;
		}
		else
	    	return null;
    }

    /* Hops should be incremented when LSPacket is forwarded not when label is 
       swapped */

    /* it should be checked if both old and new ttl are the same !!!!!!!!!! */
/*    public boolean swap(Label newlabel_)
    {
		if (!labelStack.empty())
	    {
			this._pop();
			return this._push(newlabel_);
	    }
		else
		    return false;
    }
*/

	public synchronized boolean swap(Label newlabel_)
	{
		if(labelStack != null && !labelStack.empty())
		{
			Label old_ = _pop();
			newlabel_.setHops(old_.getHops());
			newlabel_.setTTL(old_.getTTL());
			
			return (_push(newlabel_));
			
		}
		
		return false;
	}    
    /* removes the label at the top of the labelStack when it is not empty */
    public synchronized Label _pop()
    {
		if (!labelStack.empty())
		{
	       headerSize -= Label.getLabelSize();
	       return (Label)labelStack.pop();
		}
		else
	    	return null;
    }

    /* pushes a label in the labelStack when packet is an LSPacket */
    /* returns false when label_ could not be pushed */
    public synchronized boolean _push(Label label_)
    {
		if (/* this.getName() == "LSP" &&*/ label_ != null)
	    {
			labelStack.push(label_);
			headerSize += Label.getLabelSize();
			return true;
	    }
		else
	    	return false;
	    
    }

    /* il faut redfinir _toString */
    /* this method is used by the toString method from Packet.java */
    public String _toString(String separator_)
    {
		String stackContent = new String();
		Stack tmp = (Stack)labelStack.clone();
		Label label;
		while (!tmp.empty())
	    {
			label = (Label)tmp.pop();
			stackContent = stackContent + separator_
					+ "[" + label.toString() + "]";
	    }

		return stackContent;
    }

	/*
    public void duplicate(Object source_)
    { 
		super.duplicate(source_);
		LSPacket lsp_ = (LSPacket)source_;
		labelStack = (Stack) labelStack.clone();
    }
	*/

    public boolean equals(Object o_)
    {
		if(o_ == this)
			return true;
		
		if(!(o_ instanceof LSPacket))
			return false;
			
		LSPacket lsp_ = (LSPacket) o_;
		
		if(!(lsp_.labelStack.size() == labelStack.size()))
			return false;
		
		if(!(lsp_.labelStack.equals(labelStack)))
			return false;
			
		return super.equals(o_);
		
    }

    public Object clone()
    {
		LSPacket lsp_ = new LSPacket(size, (Packet)body);
		lsp_.headerSize = headerSize;
		lsp_.labelStack = (Stack)labelStack.clone();
		return lsp_;
    }

    public String getName()
    {return NAME;}
}
