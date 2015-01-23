/*
 * TclObject.java
 *
 * Copyright (c) 1997 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 * 
 * RCS: @(#) $Id: TclObject.java,v 1.9 2003/01/09 02:15:40 mdejong Exp $
 *
 */

package tcl.lang;

/**
 * This class implements the basic notion of an "object" in Tcl. The
 * fundamental representation of an object is its string value. However,
 * an object can also have an internal representation, which is a "cached"
 * reprsentation of this object in another form. The type of the internal
 * rep of Tcl objects can mutate. This class provides the storage of the
 * string rep and the internal rep, as well as the facilities for mutating
 * the internal rep.
 */

public final class TclObject {
    // Internal representation of the object.

    protected InternalRep internalRep;

    // Reference count of this object. When 0 the object will be deallocated.

    protected int refCount;

    // String  representation of the object.

    protected String stringRep;

    /**
     * Creates a TclObject with the given InternalRep. This method should be
     * called only by an InternalRep implementation.
     *
     * @param rep the initial InternalRep for this object.
     */
    public TclObject(InternalRep rep) {
	if (rep == null) {
	    throw new TclRuntimeError("null InternalRep");
	}
	internalRep = rep;
	stringRep = null;
	refCount = 0;
    }

    /**
     * Creates a TclObject with the given InternalRep and stringRep.
     * This constructor is used by the TclString class only. No other place
     * should call this constructor.
     *
     * @param rep the initial InternalRep for this object.
     * @param s the initial string rep for this object.
     */
    protected TclObject(TclString rep, String s) {
	if (rep == null) {
	    throw new TclRuntimeError("null InternalRep");
	}
	internalRep = rep;
	stringRep = s;
	refCount = 0;
    }

    /**
     * Returns the handle to the current internal rep. This method should be
     * called only by an InternalRep implementation.
     *
     * @return the handle to the current internal rep.
     */
    public final InternalRep getInternalRep() {
	disposedCheck();
	return internalRep;
    }

    /**
     * Change the internal rep of the object. The old internal rep
     * will be deallocated as a result. This method should be
     * called only by an InternalRep implementation.
     *
     * @param rep the new internal rep.
     */
    public final void setInternalRep(InternalRep rep) {
	disposedCheck();
	if (rep == null) {
	    throw new TclRuntimeError("null InternalRep");
	}
	if (rep == internalRep) {
	    return;
	}

	// In the special case where the internal representation is a CObject,
	// we want to call the special interface to convert the underlying
	// native object into a reference to the Java TclObject.  Note that
	// this test will always fail if we are not using the native
	// implementation. Also note that the makeReference method
	// will do nothing in the case where the Tcl_Obj inside the
	// CObject was originally allocated in Java. When converting
	// to a CObject we need to break the link made earlier.

	if ((internalRep instanceof CObject) && !(rep instanceof CObject)) {
	    // We must ensure that the string rep is copied into Java
	    // before we lose the reference to the underlying CObject.
	    // Otherwise we will lose the original string information
	    // when the backpointer is lost.

	    if (stringRep == null) {
		stringRep = internalRep.toString();
	    }
	    ((CObject) internalRep).makeReference(this);
	}

        //System.out.println("TclObject setInternalRep for \"" + stringRep + "\"");
        //System.out.println("from \"" + internalRep.getClass().getName() +
        //    "\" to \"" + rep.getClass().getName() + "\"");
	internalRep.dispose();
	internalRep = rep;
    }

    /**
     * Returns the string representation of the object.
     *
     * @return the string representation of the object.
     */
    public final String toString() {
	disposedCheck();
	if (stringRep == null) {
	    stringRep = internalRep.toString();
	}
	return stringRep;
    }

    /**
     * Sets the string representation of the object to null.  Next
     * time when toString() is called, getInternalRep().toString() will
     * be called. This method should be called ONLY when an InternalRep
     * is about to modify the value of a TclObject.
     *
     * @exception TclRuntimeError if object is not exclusively owned.
     */
    public final void invalidateStringRep() throws TclRuntimeError {
	disposedCheck();
	if (refCount > 1) {
	    throw new TclRuntimeError("string representation of object \"" +
		    toString() + "\" cannot be invalidated: refCount = " +
		    refCount);
	}
	stringRep = null;
    }

    /**
     * Returns true if the TclObject is shared, false otherwise.
     * @return true if the TclObject is shared, false otherwise.
     */
    public final boolean isShared() {
	disposedCheck();
	return (refCount > 1);
    }

    /**
     * Tcl_DuplicateObj -> duplicate
     *
     * Duplicate a TclObject, this method provides the preferred
     * means to deal with modification of a shared TclObject.
     * It should be invoked in conjunction with isShared instead
     * of using the deprecated takeExclusive method.
     *
     * Example:
     *
     *		if (tobj.isShared()) {
     *		    tobj = tobj.duplicate();
     *		}
     *		TclString.append(tobj, "hello");
     *
     * @return an TclObject with a refCount of 0.
     */

    public final TclObject duplicate() {
	disposedCheck();
	if (internalRep instanceof TclString) {
	    if (stringRep == null) {
	        stringRep = internalRep.toString();
	    }
	}
	TclObject newObj = new TclObject(internalRep.duplicate());
	newObj.stringRep = this.stringRep;
	newObj.refCount = 0;
	return newObj;
    }

    /**
     * @deprecated The takeExclusive method has been deprecated
     * in favor of the new duplicate() method. The takeExclusive
     * method would modify the ref count of the original object
     * and return an object with a ref count of 1 instead of 0.
     * These two behaviors lead to lots of useless duplication
     * of objects that could be modified directly.
     */

    public final TclObject takeExclusive() throws TclRuntimeError {
	disposedCheck();
	if (refCount == 1) {
	    return this;
	} else if (refCount > 1) {
	    if (internalRep instanceof TclString) {
		if (stringRep == null) {
		    stringRep = internalRep.toString();
		}
	    }
	    TclObject newObj = new TclObject(internalRep.duplicate());
	    newObj.stringRep = this.stringRep;
	    newObj.refCount = 1;
	    refCount--;
	    return newObj;
	} else {
	    throw new TclRuntimeError("takeExclusive() called on object \"" +
		    toString() + "\" with: refCount = 0");
	}
    }

    /**
     * Tcl_IncrRefCount -> preserve
     *
     * Increments the refCount to indicate the caller's intent to
     * preserve the value of this object. Each preserve() call must be matched
     * by a corresponding release() call.
     *
     * @exception TclRuntimeError if the object has already been deallocated.
     */
    public final void preserve() throws TclRuntimeError {
	disposedCheck();
	/* DRCL: undefined
	if (internalRep instanceof CObject) {
	    ((CObject) internalRep).incrRefCount();
	}
	*/
        _preserve();
    }

    /**
     * _preserve
     *
     * Private implementation of preserve() method.
     * This method will be invoked from Native code
     * to change the TclObject's ref count without
     * effecting the ref count of a CObject.
     */
    private final void _preserve() throws TclRuntimeError {
	refCount++;
    }

    /**
     * Tcl_DecrRefCount -> release
     *
     * Decrements the refCount to indicate that the caller is no longer
     * interested in the value of this object. If the refCount reaches 0,
     * the obejct will be deallocated.
     */
    public final void release() {
	disposedCheck();
	/* DRCL: undefined
	if (internalRep instanceof CObject) {
	    ((CObject) internalRep).decrRefCount();
	}
	*/
	_release();
    }

    /**
     * _release
     *
     * Private implementation of preserve() method.
     * This method will be invoked from Native code
     * to change the TclObject's ref count without
     * effecting the ref count of a CObject.
     */
    private final void _release() {
	refCount--;
	if (refCount <= 0) {
	    internalRep.dispose();

	    // Setting these to null will ensure that any attempt to use
	    // this object will result in a Java NullPointerException.

	    internalRep = null;
	    stringRep = null;
	}
    }

    /**
     * Returns the refCount of this object.
     *
     * @return refCount.
     */
    final int getRefCount() {
	return refCount;
    }

    /**
     * Returns the Tcl_Obj* objPtr member for a CObject or TclList.
     * This method is only called from Tcl Blend.
     */

    final long getCObjectPtr() {
	/* DRCL: undefined
	if (internalRep instanceof CObject) {
	    return ((CObject) internalRep).getCObjectPtr();
	} else {
	    return 0;
	}
	*/
	    return 0;
    }

    /**
     * Returns 2 if the internal rep is a TclList.
     * Returns 1 if the internal rep is a CObject.
     * Otherwise returns 0.
     * This method provides an optimization over
     * invoking getInternalRep() and two instanceof
     * checks via JNI. It is only used by Tcl Blend.
     */

    final int getCObjectInst() {
	if (internalRep instanceof CObject) {
	    if (internalRep instanceof TclList)
	        return 2;
	    else
	        return 1;
	} else {
	    return 0;
	}
    }

    /**
     * Raise a TclRuntimeError if this TclObject has been
     * disposed of because the last ref was released.
     */

    private final void disposedCheck() {
	if (internalRep == null) {
	    throw new TclRuntimeError("TclObject has been deallocated");
	}
    }
}

