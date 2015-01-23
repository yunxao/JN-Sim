/////////////////////////////////////////////////////////////////////
// @(#)ExtendedCommunities.java
//
// (c) 2002, Infonet Group, University of Namur, Belgium
//
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
// @lastdate 25/09/2002
/////////////////////////////////////////////////////////////////////

package infonet.javasim.bgp4.path;

import java.util.ArrayList;

public class ExtendedCommunities extends Attribute
{

    /** The AS path attribute type code. */
    public static final byte TYPECODE= 16;
     
    /** The name of the attribute as a string. */
    public static final String name= "ext_communities";

    /** The list of communities. */
    public ArrayList<ExtendedCommunity> vals;

    // ----- ExtendedCommunities constructor --------------------- //
    public ExtendedCommunities()
    {
	super(TYPECODE, true, true, false);
    }

    // ----- ExtendedCommunities constructor --------------------- //
    public ExtendedCommunities(ArrayList<ExtendedCommunity> vals)
    {
	super(TYPECODE, true, true, false);
	this.vals= vals;
    }

    // ----- ExtendedCommunities constructor --------------------- //
    public ExtendedCommunities(byte [] bytes)
    {
	super(bytes);
    }

    // ----- ExtendedCommunities.append -------------------------- //
    public void append(ExtendedCommunity community)
    {
	if (vals == null)
	    vals= new ArrayList<ExtendedCommunity>();
	vals.add(community);
    }

    // ----- ExtendedCommunities.remove_non_transitive ----------- //
    public void remove_non_transitive()
    {
	int index= 0;

	while (index < vals.size()) {
	    if (!((ExtendedCommunity) vals.get(index)).getTransitive())
		vals.remove(index);
	    else
		index++;
	}
    }

    // ----- ExtendedCommunities.equals -------------------------- //
    public boolean equals(Attribute attr)
    {
	if ((attr == null) || !(attr instanceof ExtendedCommunities))
	    return false;
	ExtendedCommunities extComm= (ExtendedCommunities) attr;
	if (vals.size() != extComm.vals.size())
	    return false;
	for (int i= 0; i < vals.size(); i++)
	    if (!extComm.vals.contains(vals.get(i)))
		return false;
	return true;
    }

    // ----- ExtendedCommunities.data_bytecount ------------------ //
    public int data_bytecount() {
	// 2 octets for header and 8 octets/community
	if (vals != null)
	    return 8*vals.size();
	else
	    return 0;
    }

    // ----- ExtendedCommunities.copy ---------------------------- //
    public Attribute copy() {
	ArrayList<ExtendedCommunity> v= null;
	if (vals != null) {
	    v= new ArrayList<ExtendedCommunity>();
	    for (int i= 0; i < vals.size(); i++)
		v.add(vals.get(i));
	}
	return new ExtendedCommunities(v);
    }

    // ----- ExtendedCommunities.toString ------------------------ //
    public final String toString()
    {
	String str= "";
	if (vals != null) {
	    for (int i= 0; i < vals.size(); i++) {
		if (i > 0)
		    str+= ",";
		str+= ((ExtendedCommunity) vals.get(i)).toString();
	    }
	}
	return str;
    }

    // ----- ExtendedCommunities.fromBytes ----------------------- //
    public int fromBytes(byte [] bytes)
    {
	int pos= super.fromBytes(bytes);
	if (((bytes.length-pos) % 8) != 0)
	    throw new Error("ext_communities fromBytes error !");
	while (pos < bytes.length) {
	    if (vals == null)
		vals= new ArrayList<ExtendedCommunity>();
	    byte [] value= new byte[8];
	    System.arraycopy(bytes, pos, value, 0, 8);
	    vals.add(ExtendedCommunity.buildFromBytes(value));
	    pos+= 8;
	}

	return pos;
    }

    // ----- ExtendedCommunities.toBytes ------------------------- //
    public byte [] toBytes()
    {
	byte [] header= super.toBytes();
	byte [] bytes= new byte[bytecount()];
	System.arraycopy(header, 0, bytes, 0, header.length);
	int pos= header.length;
	if (vals != null) {
	    for (int i= 0; i < vals.size(); i++) {
		System.arraycopy(((ExtendedCommunity) vals.get(i)).value, 0, bytes, pos, 8);
		pos+=8;
	    }
	}

	return bytes;
    }
	@Override
	public byte getTypeCode() {
		return ExtendedCommunities.TYPECODE;
	}


}
