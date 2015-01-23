/////////////////////////////////////////////////////////////////////
// @(#)RedistributionCommunity.java
//
// (c) 2002, Infonet Group, University of Namur, Belgium
//
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
// @lastdate 19/07/2002
/////////////////////////////////////////////////////////////////////

package infonet.javasim.bgp4.path;

import infonet.javasim.util.IPaddress;

// ===== infonet.javasim.bgp4.path.RedistributionCommunity ======= //
/**
 * The ExtendedCommunity class implements
 * draft-ietf-ptomaine-bgp-redistribution-00.txt.
 *
 * <br>
 * <center>
 * <table border=1>
 * <tr><th>1 octet</th><th>1 octet</th><th>6 octets</th></tr>
 * <tr><td>01TBDTBD</td><td>Action</td><td>BGP_Speakers_Filter</td></tr>
 * </table>
 * <strong>Figure 1 : Encoding of the redistribution community</strong>
 * </center>
 *
 * <br>
 * The Action octet is encoded as follow:
 * <ul>
 * <li>The high and the second order bits (Bit7 and Bit6) are reserved
 *   and set to zero in this document
 * <li>Bit5-3 are the Action type
 * <li>Bit2-0 are the Action parameters
 * </ul>
 *
 * <br>
 * Action types
 * <ul>
 * <li>000b Prepend.
 * <li>001b No_Export.
 * <li>010b Do not announce.
 * </ul>
 *
 * <center>
 * <table border=1>
 * <tr><th>1 octet</th><th>5 octets</th></tr>
 * <tr><td>Type</td><td>BGP_Speakers_Filter Value</td></tr>
 * </table>
 * <strong>Figure 2 : Encoding of the BGP_Speakers_Filter field</strong>
 * </center>
 *
 */

public class RedistributionCommunity extends ExtendedCommunity
{

    /** Request prepending. */
    public static final byte RED_ACTION_PREPEND  = 0;
    /** Request attachment of NO_EXPORT. */
    public static final byte RED_ACTION_NO_EXPORT= 1;
    /** Request no redistribution. */
    public static final byte RED_ACTION_IGNORE   = 2;

    /** One AS number filter type. */
    public static final byte RED_FTYPE_AS  = 1;
    /** Two AS numbers filter type. */
    public static final byte RED_FTYPE_2AS = 2;
    /** One CIDR prefix filter type. */
    public static final byte RED_FTYPE_CIDR= 3;
    /** One 4-bytes AS number filter type. */
    public static final byte RED_FTYPE_AS4 = 4;

    /** Extended community type code (same as in Zebra). */
    public static final byte TYPE_CODE= 3;
    /** Extended community name. */
    public static final String name= "red";

    private static final int MAX_RED_ACTION= 3;
    private static final String [] RED_ACTION_NAME= {
	"prepend", "no_export", "ignore", "?", "?", "?", "?", "?"
    };

    private static final int MAX_RED_FTYPE= 4;
    private static final String [] RED_FTYPE_NAME= {
	"?", "as", "2as", "cidr", "as4"
    };

    // ----- RedistributionCommunity constructor ----------------- //
    public RedistributionCommunity(byte action, byte actionParam,
				   IPaddress IPTarget)
    {
	super(false, false, TYPE_CODE,
	      new byte[]{(byte) (((action & 0x07) << 3)+(actionParam & 0x07)),
			     RED_FTYPE_CIDR, 0,
			     0, 0,
			     0, 0});
    }

    // ----- RedistributionCommunity constructor ----------------- //
    public RedistributionCommunity(byte action, byte actionParam,
				   int ASTarget)
    {
	super(false, false, TYPE_CODE,
	      new byte[]{(byte) (((action & 0x07) << 3)+(actionParam & 0x07)),
			     RED_FTYPE_AS, 0, 0, 0,
			     (byte) ((ASTarget >> 8)-128),
			     (byte) ((ASTarget & 0xff)-128)});
    }
    
    // ----- RedistributionCommunity constructor ----------------- //
    public RedistributionCommunity(String [] values)
    {
	super(false, false, TYPE_CODE, new byte[7]);

	byte pos= 0;
	byte action= RED_ACTION_PREPEND;
	byte filter= RED_FTYPE_AS;
	if (values[pos++].equals(name)) {
	    if (pos >= values.length)
		throw new Error("ext_community \"red\" not enough arguments !");
	    while ((action < MAX_RED_ACTION) &&
		   (!values[pos].equals(RED_ACTION_NAME[action])))
		action++;
	    if (action >= MAX_RED_ACTION)
		throw new Error("ext_community \"red\" action unknown ("+
				values[pos]+
				") !");
	    pos++;
	    action<<= 3;
	    if (action == RED_ACTION_PREPEND)
		action+=
		    ((byte) Integer.valueOf(values[pos++]).intValue()) & 0x07;
	    if (pos >= values.length)
		throw new Error("ext_community \"red\" not enough arguments !");
	    while ((filter < MAX_RED_FTYPE+1) &&
		   (!values[pos].equals(RED_FTYPE_NAME[filter])))
		filter++;
	    if ((filter > MAX_RED_FTYPE) || (filter == 0))
		throw new Error("ext_community \"red\" filter unknown ("+
				values[pos]+
				") !");
	    pos++;
	    if (pos >= values.length)
		throw new Error("ext_community \"red\" not enough arguments !");
	    switch (filter) {
	    case RED_FTYPE_AS:
		int as= Integer.valueOf(values[pos++]).intValue();
		value[3]= 0;
		value[4]= 0;
		value[5]= (byte) ((as >> 8)-128);
		value[6]= (byte) ((as & 0xff)-128);
		break;
	    }
	    filter= (byte) (filter-128);
	    if (pos < values.length) {
		if (values[pos].equals("exclude"))
		    filter+= 128;
		else
		    throw new Error("ext_community \"red\" syntax error !");
	    }
	    value[1]= action;
	    value[2]= filter;
	} else
	    throw new Error("ext_community name should be \"red\"");
    }

    public RedistributionCommunity(byte [] bytes)
    {
	super(bytes);
    }

    // ----- RedistributionCommunity.getAction ------------------- //
    public byte getAction()
    {
	return (byte) (((((int) value[1])+128) >> 3) & 0x07);
    }

    // ----- RedistributionCommunity.getActionParam -------------- //
    public byte getActionParam()
    {
	return (byte) ((((int) value[1])+128) & 0x07);
    }

    // ----- RedistributionCommunity.getFilterType --------------- //
    public byte getFilterType()
    {
	return (byte) (((int) value[2]) & 0x7f);
    }

    // ----- getFilterInclude()
    public boolean getFilterInclude()
    {
	return ((((int) value[2]) & 0x80) != 0);
    }

    // ----- RedistributionComunity.getFilterAS ------------------- //
    public int getFilterAS()
    {
	return ((((int) value[5])+128) << 8) + ((int) value[6])+128;
    }

    // ----- RedistributionCommunity.toString -------------------- //
    public String toString()
    {
	String str= "red-"+RED_ACTION_NAME[getAction()]+"-"+
	    RED_FTYPE_NAME[getFilterType()];
	return str;
    }

}
