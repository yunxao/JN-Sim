/////////////////////////////////////////////////////////////////////
// @(#)AtomicPredicate.java
//
// @author BJ Premore (SSFNet)
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
// @lastdate 09/08/2002
/////////////////////////////////////////////////////////////////////

package infonet.javasim.bgp4.policy;

//import gnu.regexp.*;
import java.util.regex.*;
import infonet.javasim.bgp4.*;
import infonet.javasim.bgp4.path.*;
import infonet.javasim.bgp4.util.Parsing;

// ===== class infonet.javasim.bgp4.policy.AtomicPredicate ======= //
/**
 * An atomic predicate applies to a given type of route attribute
 * (typically the route destination or a BGP path attribute).  In the
 * process of applying BGP policy rules, attribute instances are
 * evaluated against such predicates to determine if they satisfy them.
 *
 * @see Attribute
 * @see Rule
 * @see Clause
 * @see Action
 * @see Predicate
 * @see AtomicPredicate
 */
public class AtomicPredicate {

    // ........................ member data ...................... //

    /** The type of route attribute to which this atomic predicate
     * applies. */
    private int attrib_type;

    /** A string used when attempting to match a route attribute. It
     *  may be a regular expression, depending on the attribute. */
    private String matchstr;

    /** A regular expression used when attempting to match certain
     *  types of attributes, including AS paths and NLRI. */
    //private RE regexp;
    private Matcher regexp;


    // ----- constructor AtomicPredicate ------------------------- //
    /**
     * Constructs and atomic predicate given a route attribute type as
     * an integer and a string used for matching certain values of
     * that type of route attribute.
     *
     * @param attribval  A string indicating the route attribute type.
     * @param matcher    A string for matching route attribute values.
     */
    public AtomicPredicate(int attribval, String matcher) {
	attrib_type = attribval;
	if (attrib_type == Community.TYPECODE)
		matcher = ""+Community.build(matcher);

	if (attrib_type == Route.NLRI_TYPECODE ||
	    attrib_type == ASpath.TYPECODE ||
	    attrib_type == NextHop.TYPECODE || 
	    attrib_type == OriginatorID.TYPECODE ||
	    attrib_type == ClusterList.TYPECODE ||
	    attrib_type == Community.TYPECODE) {
		/*
	    try {
		regexp = new RE(matcher);
	    } catch (REException ree) {
		Debug.gexcept("bad regular expression: " + matcher);
	    }
		*/
	    try {
		regexp = Pattern.compile(matcher).matcher("");
	    } catch (PatternSyntaxException e) {
		Debug.gexcept("bad regular expression: " + matcher);
	    }
	    Debug.gaffirm(regexp!=null, "bad regular expression: " + matcher);
	}
	   
	// To support communities xx:yy
	matchstr= matcher;
    }

    // ----- constructor AtomicPredicate ------------------------- //
    /**
     * Constructs an atomic predicate given a route attribute type as
     * a string and a string used for matching certain values of that
     * type of route attribute.
     *
     * @param attribstr  A string indicating the route attribute type.
     * @param matcher    A string for matching route attribute values.
     */
    public AtomicPredicate(String attribstr, String matcher) {
	// first check standard/IP versions of route attributes which are not
	// path attributes
	attrib_type = Route.MAX_TYPECODE;
	while (attrib_type >= Route.MIN_TYPECODE &&
	       !attribstr.equals(Route.attrib_names[(-1)*attrib_type])) {
	    attrib_type--;
	}
	// next check standard/IP versions of path attributes
	if (attrib_type == Route.MIN_TYPECODE-1) { // no match yet
	    attrib_type = Attribute.MIN_TYPECODE;
	    while (attrib_type <= Attribute.MAX_TYPECODE &&
		   !attribstr.equals(Attribute.names[attrib_type])) {
		attrib_type++;
	    }
	    // no match
	    if (attrib_type == Attribute.MAX_TYPECODE+1) {
		Debug.gexcept("unrecognized route attribute while building " +
			      "atomic predicate: " + attribstr);
	    }
	}
	if (attrib_type == Community.TYPECODE)
		matcher = ""+Community.build(matcher);

	if (attrib_type == Route.NLRI_TYPECODE ||
	    attrib_type == ASpath.TYPECODE ||
	    attrib_type == NextHop.TYPECODE || 
	    attrib_type == OriginatorID.TYPECODE ||
	    attrib_type == ClusterList.TYPECODE ||
	    attrib_type == Community.TYPECODE) {
		/*
	    try {
		regexp = new RE(matcher);
	    } catch (REException ree) {
		Debug.gexcept("bad regular expression: " + matcher);
	    }
		*/
	    try {
		regexp = Pattern.compile(matcher).matcher("");
	    } catch (PatternSyntaxException e) {
		Debug.gexcept("bad regular expression: " + matcher);
	    }
	    Debug.gaffirm(regexp!=null, "bad regular expression: " + matcher);
	}
	matchstr= matcher;

    }

    // ----- AtomicPredicate.apply_to ---------------------------- //
    /**
     * Applies this atomic predicate to the given route and returns
     * true only if it matches.
     *
     * @param route  The route to which to apply this atomic predicate.
     * @return whether or not the atomic predicate matches the route
     */
    public boolean apply_to(Route r) {
	switch (attrib_type) {
	case Route.NLRI_TYPECODE:
	    String nlri_string = r.nlri.toString();
	    //return (regexp.getMatch(nlri_string) != null);
	    return (regexp.reset(nlri_string).find());
	case Origin.TYPECODE:
	    int origin = r.origin();
	    Debug.gaffirm(matchstr.equals("igp") || matchstr.equals("egp") ||
			  matchstr.equals("inc"), "illegal 'matcher' value for " +
			  "'origin': " + matchstr + " (must be egp, igp, or inc)");
	    if (origin == Origin.IGP && matchstr.equals("igp") ||
		origin == Origin.EGP && matchstr.equals("egp") ||
		origin == Origin.INC && matchstr.equals("inc")) {
		return true;
	    } else {
		return false;
	    }
	case ASpath.TYPECODE:
	    // We pad the AS path string so that it begins and ends
	    // with a space. This greatly simplifies the regular
	    // expressions that need to be specified for most
	    // predicates.
	    String aspathstr = " " + r.aspath().toMinString(' ') + " ";
	    //return (regexp.getMatch(aspathstr) != null);
	    return regexp.reset(aspathstr).find();
	case NextHop.TYPECODE:
	    String nexthopstr = r.nexthop().toString();
	    //return (regexp.getMatch(nexthopstr) != null);
	    return regexp.reset(nexthopstr).find();
	case MED.TYPECODE:
	    Debug.gwarn("the MED path attribute is not currently implemented");
	    return false;
	    //boolean has_med = r.has_med();
	    //if (!has_med) { // no MED
	    //  if (matchstr.equals("")) {
	    //    return true;
	    //  } else {
	    //    return false;
	    //  }
	    //} else { // has MED
	    //  return Parsing.matchInt(matchstr,r.med());
	    //}
	case LocalPref.TYPECODE:
	    boolean has_localpref = r.has_localpref();
	    if (!has_localpref) { // no local pref
		return matchstr.equals("");
	    } else { // has local pref
		return Parsing.matchInt(matchstr, (int) r.localpref());
	    }
	case AtomicAggregate.TYPECODE:
	    Debug.gwarn("matching based on the Atomic Aggregate path attribute " +
			"is not currently implemented");
	    return false;
	case Aggregator.TYPECODE:
	    Debug.gwarn("matching based on the Aggregator path attribute " +
			"is not currently implemented");
	    return false;
	case Community.TYPECODE:
	    if (r.has_comm()) {
		String communityStr= r.comm().toString();

		//REMatch m= regexp.getMatch(communityStr);
		/*if (m != null)
		    System.out.println("COMMUNITY MATCH ("+matchstr+") "+r);
		else
		System.out.println("COMMUNITY DOES NOT MATCH ("+matchstr+") "+r);*/
		//return (m != null);
		return regexp.reset(communityStr).find();
	    } else {
		return matchstr.equals("");
	    }
	case OriginatorID.TYPECODE:
	    if (r.has_orig_id()) {
		String originatorStr= r.orig_id().toString();
		//return (regexp.getMatch(originatorStr) != null);
		return regexp.reset(originatorStr).find();
	    } else { // no originator attribute
		return false;
	    }
	case ClusterList.TYPECODE:
	    if (!r.has_cluster_list()) {
		// route has no cluster list path attribute
		return false;
	    }
	    // We pad the cluster list string so that it begins and
	    // ends with a space.  This greatly simplifies the regular
	    // expressions that need to be specified for most
	    // predicates.
	    String clusterlist_str = " " + r.cluster_list() + " ";
	    //return (regexp.getMatch(clusterlist_str) != null);
	    return regexp.reset(clusterlist_str).find();
	default:
	    Debug.gexcept("unrecognized path attribute type: " + attrib_type);
	    return false;
	}
    }

    // ----- AtomicPredicate.toString() -------------------------- //
    /**
     * Puts the atomic predicate into string form suitable for output.
     *
     * @return the atomic predicate in string form
     */
    public String toString() {
	return toString("");
    }

    // ----- AtomicPredicate.toString(String) -------------------- //
    /**
     * Puts the atomic predicate into string form suitable for output.
     *
     * @param ind  A string to use as a prefix for each line in the
     * string.
     * @return the atomic predicate in string form
     */
    public String toString(String ind) {
    	if (attrib_type == Route.NLRI_TYPECODE){
    		return ind + "nlri has matcher \"" +
    	    matchstr + "\"";
    	}
	return ind + Attribute.names[attrib_type] + " has matcher \"" +
	    matchstr + "\"";
    }

} // end of class AtomicPredicate
