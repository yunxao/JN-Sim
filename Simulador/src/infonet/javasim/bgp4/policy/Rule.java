// =============================================================================== //
// @(#)Rule.java
//
// @author BJ Premore
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
//
// @lastdate 15/04/2002
// =============================================================================== //

package infonet.javasim.bgp4.policy;

import java.util.ArrayList;

import tid.graphic.GraphicBGPEventManager;
import infonet.javasim.bgp4.*;
import infonet.javasim.bgp4.path.Attribute;

// ===== class SSF.OS.BGP4.Policy.Rule ===================================== //
/**
 * This class captures the notion of a policy rule used for BGP route
 * filtering.
 */
public class Rule {
	/**
	 * BGPSession associated with this rule to can write a log
	 */
  public BGPSession bgpSession;
  // ........................ member data .......................... //
  
  /** The clauses that make up the rule. */
  public ArrayList<Clause> clauses;
  // ----- constructor Rule(boolean) --------------------------------------- //
  /**
   * Constructs a policy rule to either deny all routes or permit all routes.
   * The rule will contain no actions.
   * @param permit Whether to permit all routes or deny all routes.
   * @param session BGPSession associated
   */
  public Rule(boolean permit,BGPSession session) {
	bgpSession = session;  
    if (permit) {
      // add just one clause which permits all
      add_clause(new Clause(new Predicate(null), new Action(true, null)));
    } else
	clauses= null;
  }
  /*
   * Constructs a policy rule to either deny all routes or permit all routes.
   * The rule will contain no actions.
   *
   * @param permit  Whether to permit all routes or deny all routes.
   *
  public Rule(boolean permit) {
		bgpSession = null;  
	    if (permit) {
	      // add just one clause which permits all
	      add_clause(new Clause(new Predicate(null), new Action(true, null)));
	    } else
		clauses= null;
	  }*/

  

  // ----- constructor Rule() ---------------------------------------------- //
  /**
   * Constructs a default policy rule which denies all routes.  Because failure
   * to match any clauses in a policy rule implies denial, we need only have
   * zero clauses.
   */
  public Rule() {
    this(false,null);
  }

  // ----- constructor Rule(Clause[]) -------------------------------------- //
  /**
   * Constructs the policy rule with the given clauses.
   *
   * @param cls  An array of clauses with which to compose the policy rule.
   */
  public Rule(ArrayList<Clause> clauselist) {
    clauses = clauselist;
  }

  // ----- Rule.add_clause ------------------------------------------------- //
  /**
   * Adds a clause to the policy rule at the end of the list.
   */
  public void add_clause(Clause c) {
    if (clauses == null) {
      clauses = new ArrayList<Clause>();
    }
    clauses.add(c);
  }

  // ----- Rule.apply_to --------------------------------------------------- //
  /**
   * Applies the policy rule to the given route, determining whether it will be
   * denied or permitted, and applying any desired attribute manipulation on
   * those which are permitted.
   *
   * @param route  The route to which to apply this policy rule.
   * @return whether or not to permit the route
   */
  public boolean apply_to(Route r) {
    if (clauses != null) {
      for (int i=0; i<clauses.size(); i++) {
        boolean[] results = { false, false };
        results = ((Clause)clauses.get(i)).apply_to(r);
        if (results[0]) { // whether or not the clause's predicate matched
//        	if (bgpSession != null){
            	if (results[1]){
            		// there is Something to apply
            	}
               	else 
           	    	bgpSession.logDebug("Rule.apply_to: route "+ r +" has been denied in a Clause. Clause"+clauses.get(i));
//        	}

        	return results[1]; // whether or not the route was permitted
        }
      }
      
      
    }
    if (bgpSession != null)
    	bgpSession.logDebug("Rule.apply_to: route "+ r +" has been permit (by default). Rule: "+toString());

    return true; // no clause's predicate matched, so deny
  }

  // ----- Rule.toString --------------------------------------------------- //
  /**
   * Puts the rule into string form suitable for output.
   *
   * @return the rule in string form
   */
  public String toString() {
    String str = "policy rule:\n";
    if (clauses != null) {
	for (int i=0; i<clauses.size(); i++) {
	    str += ((Clause)clauses.get(i)).toString("  ");
	}
    }
    return str;
  }
  
  @SuppressWarnings("unchecked")
  public Object clone(){
	  Rule newObject = new Rule();
	  if (this.clauses != null){
		  newObject.clauses = (ArrayList<Clause>)clauses.clone();
		  
	  }
	  newObject.bgpSession = this.bgpSession;
	  return newObject;
  }

} // end of class Rule
