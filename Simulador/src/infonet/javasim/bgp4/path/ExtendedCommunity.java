/////////////////////////////////////////////////////////////////////
// @(#)ExtendedCommunity.java
//
// (c) 2002, Infonet Group, University of Namur, Belgium
//
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
// @lastdate 22/07/2002
/////////////////////////////////////////////////////////////////////

package infonet.javasim.bgp4.path;

public class ExtendedCommunity
{

    /** IANA authority bit: FCFS (First Come First Serve) or IETF
	(IETF Consensus). */
    public final boolean EXT_COMM_IANA_FCFS= false;
    public final boolean EXT_COMM_IANA_IETF= true;

    /** Community value. */
    public byte [] value= new byte[8];

    // ----- ExtendedCommunity constructor ----------------------- //
    public ExtendedCommunity()
    {
	super();
    }

    // ----- ExtendedCommunity constructor ----------------------- //
    public ExtendedCommunity(boolean iana,
			     boolean transitive,
			     byte typeField,
			     byte [] value)
    {
	super();
	this.value[0]=
	    (byte) (((iana?0x80:0)+(transitive?0x40:0)+typeField)-128);
	System.arraycopy(value, 0, this.value, 1, 7);
    }

    // ----- ExtendedCommunity constructor ----------------------- //
    public ExtendedCommunity(byte [] value)
    {
	super();
	System.arraycopy(value, 0, this.value, 0, 8);
    }

    // ----- ExtendedCommunity.toString -------------------------- //
    public String toString()
    {
	String str= "{";
	for (int i= 0; i < value.length; i++) {
	    if (i> 0)
		str+= " ";
	    str+= Integer.toHexString(((int) value[i])+128);
	}
	return str+"}";
    }
    
    // ----- ExtendedCommunities.build --------------------------- //
    static public ExtendedCommunity build(String [] values)
    {
	if (values.length > 0) {
	    if (values[0].equals(RedistributionCommunity.name)) {
		return new RedistributionCommunity(values);
	    } else
		throw new Error("ext_communities: unknown/unsupported type ("+
				values[0]+
				") !");
	} else
	    throw new Error("ext_communities: incomplete specification !");
    }

    // ----- ExtendedCommunity.buildFromBytes -------------------- //
    static public ExtendedCommunity buildFromBytes(byte [] bytes)
    {
	byte typeField= (byte) (bytes[0] & 0x3f);
	switch (typeField) {
	case RedistributionCommunity.TYPE_CODE:
	    return new RedistributionCommunity(bytes);
	default:
	    throw new Error("ext_communities: unknown/unsupported type-field ("+typeField+") !");
	}
    }

    // ----- ExtendedCommunity.getTypeField ---------------------- //
    public byte getTypeField()
    {
	return (byte) (value[0] & 0x3f);
    }

    // ----- ExtendedCommunity.getIANA --------------------------- //
    public boolean getIANA()
    {
	return ((value[0] & 0x80) != 0);
    }

    // ----- ExtendedCommunity.getTransitive --------------------- //
    public boolean getTransitive()
    {
	return ((value[0] & 0x40) != 0);
    }

}
