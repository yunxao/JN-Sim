// Author: Louis SWINNEN (lsw@infonet.fundp.ac.be)
//
// August 2002
//
// $Id: FTLookup.java,v 1.1.1.1 2002/08/09 15:00:52 lsw Exp $
package infonet.javasim.FT.contract;

import drcl.inet.contract.*;

import drcl.comp.*;
import drcl.data.*;
import drcl.net.*;
import infonet.javasim.FT.FTKey;
import infonet.javasim.FT.FTEntry;
import drcl.inet.data.RTKey;

/**
The ForwardTable Lookup contract.

This contract defines the following service at the reactor:
<dl>
<dt>  <code>ForwardTableLookup</code>
<dd>  The initiator sends a key (this key can be a network prefix {@link drcl.inet.data.RTKey} or a label {@link drcl.inet.data.FTKey})
        and the reactor returns the outgoing interfaces, the outgoing label and the operation list (<code>FTEntry</code> {@link drcl.inet.data.FTEntry}). The reactor looks for the <em>longest match</em> (if a prefix is used) or the <em>exact match</em> (if a label is used).
</dl>

This class also provides two static methods
({@link #lookup(drcl.inet.data.RTKey, drcl.comp.Port) lookup(RTKey, Port)}) and
({@link #lookup(drcl.inet.data.FTKey, drcl.comp.Port) lookup(FTKey, Port)})
to facilitate conductiong the above service from the specified port.

These methods are particulary useful in implementing a protocol that needs to look up the forwarding entry.
@Author Louis SWINNEN
@version 0.1, 04/15/2002
@see FTConfig 	for the description of configuration contract
@see drcl.inet.data.RTKey
@see drcl.inet.data FTKey
@see drcl.inet.data.FTEntry

This code is under FreeBSD like license.
*/

public class FTLookup extends Contract
{
	public FTLookup()
	{	super();	}
	
	public FTLookup(int role_)
	{	super(role_);	}
	
	public Object getContractContent()
	{	return null;	}
	
	public static FTEntry lookup(FTKey ft_, Port out_)
	{
		return (FTEntry) out_.sendReceive(ft_);
	}
	
	public static FTEntry lookup(RTKey rt_, Port out_)
	{
		return (FTEntry) out_.sendReceive(rt_);
	}
	
	public String getName()
	{
		return("FTLookup contract");
	}
}
