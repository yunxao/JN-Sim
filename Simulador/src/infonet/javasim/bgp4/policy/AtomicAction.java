///////////////////////////////////////////////////////////////////////
// @(#)AtomicAction.java
//
// @author BJ Premore (SSFNet)
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
// @lastdate 09/08/2002
///////////////////////////////////////////////////////////////////////

package infonet.javasim.bgp4.policy;

import java.util.ArrayList;
import infonet.javasim.bgp4.*;
import infonet.javasim.bgp4.path.*;
import infonet.javasim.bgp4.util.AS_descriptor;

// ===== class SSF.OS.BGP4.Policy.AtomicAction =================== //
/**
 * An atomic action applies to a given type of path attribute (of a
 * route), a typically specifies that the attribute be assigned a
 * given value. In the process of applying BGP policy rules, routes
 * are evaluated against certain predicates.  When such a predicate is
 * satisfied by a route, an action which is associated with the
 * predicate is performed on that route. Such an action can be
 * composed of multiple atomic actions, such as are represented by
 * this class.
 *
 * @see Attribute
 * @see Rule
 * @see Clause
 * @see Predicate
 * @see AtomicPredicate
 * @see Action
 */
public class AtomicAction {

    // ......................... constants ....................... //

    /** Indicates an action which sets a value. */
    private static final int SET     = 0;
    /** Indicates an action which prepends a value. */
    private static final int PREPEND = 1;
    /** Indicates an action which appends a value. */
    private static final int APPEND  = 2;
    /** Strip (communities, extended communities) */
    private static final int STRIP   = 3;
    /** The maximum value of any action constant. */
    private static final int MAX_ACTION_VALUE = 3;

    /** The names, in string form, of each action type. */
    private static final String[] actionnames = { "set", "prepend",
						  "append", "strip" };

    // ........................ member data ...................... //

    /** The type of path attribute to which this atomic action
     * applies. */
    private int attrib_type;

    /** The type of action to be performed. */
    private int action_type;

    /** A set of values whose meaning vary depending on the attribute
     * type. */
    private String[] values;

    // ----- constructor AtomicAction(int,int,String[]) ---------- //
    /**
     * Constructs an atomic action with the given attribute type,
     * action type, and values.
     *
     * @param attribval  An integer indicating the path attribute
     *                   type.
     * @param actionval  An integer indicating the action to be
     *                   taken.
     * @param vals       An array of values to be used in conjunction
     *                   with the given type of action.
     */
    public AtomicAction(int attribval, int actionval, String[] vals) {
	attrib_type= attribval;
	action_type= actionval;
	values= vals;
    }

    // ----- constructor AtomicAction(String,String,String[]) ---- //
    /**
     * Constructs an atomic action with the given path attribute type
     * string, action type string, and values.
     *
     * @param attribstr  A string indicating the path attribute type.
     * @param actionstr  A string indicating the action to be taken.
     * @param vals       An array of string values to be used in
     *                   conjunction with the given type of action.
     */
    public AtomicAction(String attribstr, String actionstr, String[] vals) {
	attrib_type = Attribute.MIN_TYPECODE;
	while (attrib_type <= Attribute.MAX_TYPECODE &&
	       !attribstr.equals(Attribute.names[attrib_type])) {
	    attrib_type++;
	}
	if (attrib_type == Attribute.MAX_TYPECODE+1) { // no match yet
	    Debug.gexcept("unrecognized path attribute while building atomic " +
			  "action: " + attribstr);
	}

	action_type = 0;
	while (action_type <= MAX_ACTION_VALUE &&
	       !actionstr.equals(actionnames[action_type])) {
	    action_type++;
	}
	if (action_type == MAX_ACTION_VALUE+1) {
	    Debug.gexcept("unrecognized action type while building atomic action: " +
			  actionnames);
	}

	values = vals; 
    }

    // ----- constructor AtomicAction(String,String,ArrayList) --- //
    /**
     * Constructs an atomic action with the given attribute type,
     * action type, and values.
     *
     * @param attribstr  A string indicating the path attribute type.
     * @param actionstr  A string indicating the action to be taken.
     * @param vals       An ArrayList of string values to be used in
     *                   conjunction with the given type of action.
     */
    public AtomicAction(String attribstr, String actionstr, ArrayList vals) {
	this(attribstr,actionstr,(String[])null);
	values = new String[vals.size()];
	for (int i=0; i<vals.size(); i++) {
	    values[i] = (String)vals.get(i);
	}
    }

    // ----- AtomicAction.apply_to ------------------------------- //
    /**
     * Applies this atomic action to the given route, modifying one of
     * its path attributes.
     *
     * @param route  The route to which to apply this atomic action.
     */
    public void apply_to(Route r) {
	switch (attrib_type) {
	case Origin.TYPECODE:
	    switch (action_type) {
	    default:
		Debug.gexcept("undefined action for Origin attribute: " +
			      actionnames[action_type]);
	    }
	    break;
	case ASpath.TYPECODE:
	    switch (action_type) {
	    case PREPEND:
		r.aspath().prepend_as(new Integer(values[0]).intValue());
		break;
	    default:
		Debug.gexcept("undefined action for ASpath attribute: " +
			      actionnames[action_type]);
	    }
	    break;
	case NextHop.TYPECODE:
	    switch (action_type) {
	    default:
		Debug.gexcept("undefined action for NextHop attribute: " +
			      actionnames[action_type]);
	    }
	    break;
	case MED.TYPECODE:
	    switch (action_type) {
	    case SET:
		r.set_med(new Integer(values[0]).intValue());
		break;
	    default:
		Debug.gexcept("undefined action for MED attribute: " +
			      actionnames[action_type]);
	    }
	    break;
	case LocalPref.TYPECODE:
	    switch (action_type) {
	    case SET:
		r.set_localpref(new Integer(values[0]).intValue());
		break;
	    default:
		Debug.gexcept("undefined action for LocalPref attribute: " +
			      actionnames[action_type]);
	    }
	    break;
	case AtomicAggregate.TYPECODE:
	    switch (action_type) {
	    default:
		Debug.gexcept("undefined action for AtomicAggregate attribute: " +
			      actionnames[action_type]);
	    }
	    break;
	case Aggregator.TYPECODE:
	    switch (action_type) {
	    default:
		Debug.gexcept("undefined action for Aggregator attribute: " +
			      actionnames[action_type]);
	    }
	    break;
	case Community.TYPECODE:
	    switch (action_type) {
	    case STRIP:
		//System.out.println("COMMUNITY STRIP ["+r+"]");
		r.strip_comm();
		break;
	    case APPEND:
		for (int i= 0; i < values.length; i++) {
		    //System.out.println("COMMUNITY APPEND ("+values[i]+") ["+r+"]");
		    r.append_comm(Community.build(values[i]));
		}
		break;
	    case SET:
	    	r.strip_comm();
	    	for (int i = 0; i < values.length;i++)
	    		r.append_comm(Community.build(values[i]));
	    	
	    	break;
	    default:
		Debug.gexcept("undefined action for Community attribute: " +
			      actionnames[action_type]);
	    }
	    break;
	case OriginatorID.TYPECODE:
	    switch (action_type) {
	    default:
		Debug.gexcept("undefined action for OriginatorID attribute: " +
			      actionnames[action_type]);
	    }
	    break;
	case ClusterList.TYPECODE:
	    switch (action_type) {
	    default:
		Debug.gexcept("undefined action for ClusterList attribute: " +
			      actionnames[action_type]);
	    }
	    break;
	case ExtendedCommunities.TYPECODE:
	    switch (action_type) {
	    case APPEND:
		r.append_extcomm(ExtendedCommunity.build(values));
		break;
	    default:
		Debug.gexcept("undefined action for ClusterList attribute: " +
			      actionnames[action_type]);
	    }
	    break;
	default:
	    Debug.gexcept("unrecognized path attribute type: " + attrib_type);
	}
    }

    // ----- AtomicAction.toString() ----------------------------- //
    /**
     * Puts the atomic action into string form suitable for output.
     *
     * @return the atomic action in string form
     */
    public String toString() {
	return toString("");
    }

    // ----- AtomicAction.toString(String) ----------------------- //
    /**
     * Puts the atomic action into string form suitable for output.
     *
     * @param ind  A string to use as a prefix for each line in the
     * string.
     * @return the atomic action in string form
     */
    public String toString(String ind) {
	String str = ind + "(";

	str += Attribute.names[attrib_type] + "," + actionnames[action_type];

	if (values != null) {
	    for (int i=0; i<values.length; i++) {
		str += "," + values[i];
	    }
	}
	str += ")";
	return str;
    }

} // end of class AtomicAction
