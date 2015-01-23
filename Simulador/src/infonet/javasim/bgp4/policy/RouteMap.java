/////////////////////////////////////////////////////////////////////
// @(#)RouteMap.java
//
// @author Bruno Quoitin (bqu@infonet.fundp.ac.be)
// @date 18/07/2002
// @lastdate 10/08/2002
/////////////////////////////////////////////////////////////////////

package infonet.javasim.bgp4.policy;

import java.io.*;
import java.lang.*;
import java.util.ArrayList;
import java.util.Stack;

// ===== infonet.javasim.bgp4.policy.RouteMap ==================== //
/**
 * The RouteMap class is a helper to build complex filtering rules using Rule, Clause, Predicate, AtomicPredicate, Action and AtomicAction. <br><br> <strong>RouteMap syntax:</strong><br> <p> rule ::= clause | ( clause "\n" )* rule<br> clause ::= ( predicate )? "|" ( action )?<br> predicate ::= atomic-predicate | ( atomic-predicate "," ) predicate<br> atomic-predicate ::= attribute predicate-matcher<br> action ::= atomic-action | (atomic-action "," ) action<br> atomic-action ::= attribute action-type (action-values)* | deny<br> </p> The following comments delimiters are allowed: C-style, C++-style and bash-style.<br><br> <strong>RouteMap example:</strong><br> <p> <i><b>|as_path prepend 3,as_path prepend 3</b></i> </p> This route-map statement will prepend 2 times AS number 3 to each route (predicate statement is empty).
 * @see Rule
 * @see Clause
 * @see Predicate
 * @see AtomicPredicate
 * @see Action
 * @see  AtomicPredicate
 */

public class RouteMap extends Rule
{

    private String script;

    // Special tokens
    private final char ST_CLAUSE_DELIMITER= '|';
    private final char ST_ATOM_DELIMITER= ',';
    private final char ST_QUOTE_DELIMITER= '"';
    
    // Syntactic analyzer states
    private final int SA_RULE= 0;
    private final int SA_PREDICATE= 100;
    private final int SA_ATOMIC_PREDICATE= 101;
    private final int SA_PREDICATE1= 102;
    private final int SA_ACTION= 200;
    private final int SA_ATOMIC_ACTION1= 201;
    private final int SA_ATOMIC_ACTION2= 202;
    private final int SA_ACTION1= 203;
    private final int SA_ACTION2= 204;

    // State machine variables
    private int token= 0;
    private int saState= SA_RULE;
    private StreamTokenizer st= null;

    // Current atomic predicates
    private String atomicPredicateAttribute;
    private String atomicPredicateMatcher;
    private ArrayList atomicPredicates= null;

    // Current atomic actions
    private String atomicActionAttribute;
    private String atomicActionAction;
    private ArrayList atomicActionValues= null;
    private ArrayList atomicActions= null;
    private boolean actionDeny= false;

    // ----- RouteMap constructor ------------------------------- //
    /** Build a new RouteMap (Rule), parse the given script, build
     *	filtering clauses and add these to the RouteMap (Rule).
     */
    public RouteMap(String script) throws SyntacticException
    {
	super(false,null);
	this.script= script;
	parseScript(script);
    }

    // ----- RouteMap.getScript ----------------------------------- //
    /**
	 * Return the route-map script.
	 * @uml.property  name="script"
	 */
    public String getScript()
    {
	return script;
    }

    // ----- RouteMap.parseScript ------------------------------- //
    /** This method parses the route-map script and build filtering
     *	clauses that are added to this RouteMap (Rule).
     */
    public void parseScript(String script) throws SyntacticException
    {
	// Set up lexical analyzer
	StringReader sr= new StringReader(script);
	st= new StreamTokenizer(sr);
	st.eolIsSignificant(true);   // report end-of-line
	st.lowerCaseMode(true);      // convert everything to lower
				     // case
	st.slashStarComments(true);  // allow C-style comments
	st.slashSlashComments(true); // allow C++-style comments
	st.commentChar('#');         // allow bash-style comments
	st.quoteChar(ST_QUOTE_DELIMITER);
	                             // quote delimiter
	st.wordChars('_', '_');      // underscore is allowed in a
				     // word

	// Set up syntactic and semantic analyzers
	saState= SA_RULE;
	token= 0;

	atomicPredicates= null;
	atomicActions= null;

	try {
	    do {
		token= st.nextToken();
		switch (saState) {
		case SA_RULE: handle_RULE(); break;
		case SA_PREDICATE: handle_PREDICATE(); break;
		case SA_ATOMIC_PREDICATE: handle_ATOMIC_PREDICATE(); break;
		case SA_PREDICATE1: handle_PREDICATE1(); break;
		case SA_ACTION: handle_ACTION(); break;
		case SA_ATOMIC_ACTION1: handle_ATOMIC_ACTION1(); break;
		case SA_ATOMIC_ACTION2: handle_ATOMIC_ACTION2(); break;
		case SA_ACTION1: handle_ACTION1(); break;
		case SA_ACTION2: handle_ACTION2(); break;
		default:
		    throwSyntacticException();
		}
	    } while (token != StreamTokenizer.TT_EOF);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    // ----- RouteMap.handle_RULE -------------------------------- //
    /** Private method to handle state RULE.
     */
    private void handle_RULE() throws SyntacticException
    {
	switch (token) {
	case StreamTokenizer.TT_EOL: break;
	case StreamTokenizer.TT_EOF: break;
	case ST_CLAUSE_DELIMITER:
	    saState= SA_ACTION;
	    break;
	default:
	    saState= SA_PREDICATE;
	    handle_PREDICATE();
	}
    }

    // ----- RouteMap.handle_PREDICATE --------------------------- //
    /** Private method to handle state PREDICATE.
     */
    private void handle_PREDICATE() throws SyntacticException
    {
	switch (token) {
	case StreamTokenizer.TT_WORD:
	    atomicPredicateAttribute= st.sval;
	    saState= SA_ATOMIC_PREDICATE;
	    break;
	default:
	    throwSyntacticException();
	}
    }

    // ----- RouteMap.handle_ATOMIC_PREDICATE -------------------- //
    /** Private method to handle state ATOMIC_PREDICATE.
     */
    private void handle_ATOMIC_PREDICATE() throws SyntacticException
    {
	switch (token) {
	case StreamTokenizer.TT_NUMBER:
	    atomicPredicateMatcher= ""+((int) st.nval);
	    addAtomicPredicate();
	    saState= SA_PREDICATE1;
	    break;
	case ST_QUOTE_DELIMITER:
	    atomicPredicateMatcher= st.sval;
	    addAtomicPredicate();
	    saState= SA_PREDICATE1;
	    break;
	default:
	    throwSyntacticException();
	}
    }

    // ----- RouteMap.handle_PREDICATE1 -------------------------- //
    /** Private method to handle state PREDICATE1.
     */
    private void handle_PREDICATE1() throws SyntacticException
    {
	switch (token) {
	case ST_CLAUSE_DELIMITER:
	    saState= SA_ACTION;
	    break;
	case ST_ATOM_DELIMITER:
	    saState= SA_PREDICATE;
	    break;
	default:
	    throwSyntacticException();
	}
    }

    // ----- RouteMap.handle_ACTION ------------------------------ //
    /** Private method to handle state ACTION.
     */
    private void handle_ACTION() throws SyntacticException
    {
	switch (token) {
	case StreamTokenizer.TT_WORD:
	    atomicActionAttribute= st.sval;
	    if (atomicActionAttribute.equals("deny")) {
		actionDeny= true;
		saState= SA_ACTION2;
	    } else
		saState= SA_ATOMIC_ACTION1;
	    break;
	case StreamTokenizer.TT_EOF:
	case StreamTokenizer.TT_EOL:
	    addClause();
	    saState= SA_RULE;
	    break;
	default:
	    throwSyntacticException();
	}
    }

    // ----- RouteMap.handle_ATOMIC_ACTION1 ---------------------- //
    /** Private method to handle state ATOMIC_ACTION1.
     */
    private void handle_ATOMIC_ACTION1() throws SyntacticException
    {
	switch (token) {
	case StreamTokenizer.TT_WORD:
	    atomicActionAction= st.sval;
	    saState= SA_ATOMIC_ACTION2;
	    break;
	default:
	    throwSyntacticException();
	}
    }

    // ----- RouteMap.handle_ATOMIC_ACTION2 ---------------------- //
    /** Private method to handle state ATOMIC_ACTION2.
     */
    private void handle_ATOMIC_ACTION2() throws SyntacticException
    {
	switch (token) {
	case StreamTokenizer.TT_NUMBER:
	    addAtomicActionValue(""+((int) st.nval));
	    // Many values are accepted, parser state must not be
	    // changed.
	    break;
	case StreamTokenizer.TT_WORD:
	case ST_QUOTE_DELIMITER:
	    addAtomicActionValue(st.sval);
	    // Many values are accepted, parser state must not be
	    // changed.
	    break;
	case StreamTokenizer.TT_EOF:
	case StreamTokenizer.TT_EOL:
	    addAtomicAction();
	    addClause();
	    saState= SA_RULE;
	    break;
	case ST_ATOM_DELIMITER:
	    addAtomicAction();
	    saState= SA_ACTION1;
	    break;
	default:
	    throwSyntacticException();
	}
    }

    // ----- RouteMap.handle_ACTION1 ----------------------------- //
    /** Private method to handle state ACTION1.
     */
    private void handle_ACTION1() throws SyntacticException
    {
	switch (token) {
	case StreamTokenizer.TT_WORD:
	    atomicActionAttribute= st.sval;
	    saState= SA_ATOMIC_ACTION1;
	    break;
	default:
	    throwSyntacticException();
	}
    }

    // ----- RouteMap.handle_ACTION2 ----------------------------- //
    /** Private method to handle state ACTION2.
     */
    private void handle_ACTION2() throws SyntacticException
    {
	switch (token) {
	case StreamTokenizer.TT_EOF:
	case StreamTokenizer.TT_EOL:
	    addClause();
	    saState= SA_RULE;
	    break;
	default:
	    throwSyntacticException();
	}
    }

    // ----- RouteMap.throwSyntacticException -------------------- //
    /** Throw a SyntacticException with the current script line
     *  number, the last token that has been read and the current state of
     *  the parser.
     *
     * @see SyntacticException
     */
    private void throwSyntacticException() throws SyntacticException
    {
	String msg= ""+st.lineno()+": token=";
	switch (token) {
	case StreamTokenizer.TT_EOF: msg+= "EOF"; break;
	case StreamTokenizer.TT_EOL: msg+= "EOL"; break;
	case StreamTokenizer.TT_NUMBER: msg+= "NUMBER("+st.nval+")"; break;
	case StreamTokenizer.TT_WORD: msg+= "WORD("+st.sval+")"; break;
	default: msg+= "UNKNOWN("+((char) token)+")";
	}
	msg+= ",state=";
	switch (saState) {
	case SA_RULE: msg+= "RULE"; break;
	case SA_PREDICATE: msg+= "PREDICATE"; break;
	case SA_ATOMIC_PREDICATE: msg+= "ATOMIC_PREDICATE"; break;
	case SA_ACTION: msg+= "ACTION"; break;
	case SA_ATOMIC_ACTION1: msg+= "ATOMIC_ACTION1"; break;
	case SA_ATOMIC_ACTION2: msg+= "ATOMIC_ACTION2"; break;
	case SA_ACTION1: msg+= "ACTION1"; break;
	default: msg+= "?";
	}
	throw new SyntacticException(msg);
    }

    // ----- RouteMap.addAtomicPredicate ------------------------- //
    /** Private method used to add a new atomic predicate to the
     *	current clause.
     */
    private void addAtomicPredicate()
    {
	if (atomicPredicates == null)
	    atomicPredicates= new ArrayList();
	atomicPredicates.add(new AtomicPredicate(atomicPredicateAttribute,
						 atomicPredicateMatcher));
	atomicPredicateAttribute= null;
	atomicPredicateMatcher= null;
    }

    // ----- RouteMap.addAtomicAction ---------------------------- //
    /** Private method used to add a new atomic action to the current
     *  clause.
     */
    private void addAtomicAction()
    {
	if (!actionDeny) {
	    if (atomicActions == null)
		atomicActions= new ArrayList();
	    if (atomicActionValues == null)
		atomicActionValues= new ArrayList();
	    atomicActions.add(new AtomicAction(atomicActionAttribute,
					       atomicActionAction,
					       atomicActionValues));
	}
	atomicActionAttribute= null;
	atomicActionAction= null;
	atomicActionValues= null;
    }

    // ----- RouteMap.addAtomicActionValue ----------------------- //
    /** Private method used to add an atomic action value to the
     *  current atomic action.
     */
    private void addAtomicActionValue(String value)
    {
	if (atomicActionValues == null)
	    atomicActionValues= new ArrayList();
	atomicActionValues.add(value);
    }

    // ----- RouteMap.addClause ---------------------------------- //
    /** Private method used to build a new clause with current
     *	predicates and actions. This new clause is added to the
     *	RouteMap (Rule).
     */
    private void addClause()
    {
	if (atomicPredicates == null)
	    atomicPredicates= new ArrayList();
	if (atomicActions == null)
	    atomicActions= new ArrayList();

	// Build a new clause and add the new clause to the rule
	if (actionDeny)
	    add_clause(new Clause(new Predicate(atomicPredicates),
				  new Action(false)));
	else
	    add_clause(new Clause(new Predicate(atomicPredicates),
				  new Action(true, atomicActions)));

	// Clear list of predicates and list of actions
	atomicPredicates= null;
	atomicActions= null;
	actionDeny= false;
    }

    // ----- RouteMap.SyntacticException ------------------------- //
    /** Exception thrown when a syntactic error has been detected. The
     *  exception message contains the line number, the token that
     *  caused the error and the state of the parser.
     */
    public class SyntacticException extends Exception
    {

	public SyntacticException(String msg)
	{
	    super(msg);
	    System.out.println("syntactic error: "+msg);
	}

    }

}
