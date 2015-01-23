/*
 * GlobalCmd.java
 *
 * Copyright (c) 1997 Cornell University.
 * Copyright (c) 1997 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 * 
 * RCS: @(#) $Id: GlobalCmd.java,v 1.2 1999/08/03 02:55:41 mo Exp $
 *
 */

package tcl.lang;

/**
 * This class implements the built-in "global" command in Tcl.
 */

class GlobalCmd implements Command {
    /**
     * See Tcl user documentation for details.
     */

    public void cmdProc(Interp interp, TclObject[] objv)
	    throws TclException
    {
	if (objv.length < 2) {
	    throw new TclNumArgsException(interp, 1, objv, 
		    "varName ?varName ...?");
	}

	//  If we are not executing inside a Tcl procedure, just return.

	if ((interp.varFrame == null)
	    || !interp.varFrame.isProcCallFrame) {
	    return;
	}

	for (int i = 1; i < objv.length; i++) {

	    // Make a local variable linked to its counterpart in the global ::
	    // namespace.

	    TclObject obj = objv[i];
	    String varName = obj.toString();

	    // The variable name might have a scope qualifier, but the name for
	    // the local "link" variable must be the simple name at the tail.

	    int tail = varName.length();
	    
	    tail -= 1; // tail should start on the last index of the string

	    while ((tail > 0) && ((varName.charAt(tail) != ':') ||
				  (varName.charAt(tail-1) != ':'))) {
		tail--;
	    }
	    if (varName.charAt(tail) == ':') {
		tail++;
	    }

	    // Link to the variable "varName" in the global :: namespace.

	    Var.makeUpvar(interp, null,
		varName, null, TCL.GLOBAL_ONLY,
	        varName.substring(tail), 0);
	}
    }

}

