/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;

import java.lang.reflect.Method;

/**
 * <b><u>TmfLocation</u></b>
 * <p>
 * A generic implementation of ITmfLocation
 */
@SuppressWarnings("rawtypes")
public class TmfLocation<L extends Comparable> implements ITmfLocation<L> {

	private L fLocation;
	
	@SuppressWarnings("unused")
	private TmfLocation() {
	}

	public TmfLocation(L location) {
		fLocation = location;
	}

	public TmfLocation(TmfLocation<L> other) {
    	if (other == null)
    		throw new IllegalArgumentException();
    	fLocation = other.fLocation;
	}

	@Override
	public void setLocation(L location) {
		fLocation = location;
	}

	@Override
	public L getLocation() {
		return fLocation;
	}

	// ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

	@Override
    public int hashCode() {
	    if (fLocation == null)
	        return -1;
		return fLocation.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TmfLocation<?>))
        	return false;
        TmfLocation<?> o = (TmfLocation<?>) other;
        if (fLocation == null)
            return (o.fLocation == null);
        return fLocation.equals(o.fLocation);
    }

	@Override
	@SuppressWarnings("nls")
	public String toString() {
	    if (fLocation == null)
	        return "null";
		return fLocation.toString();
	}

	@Override
	@SuppressWarnings({ "nls", "unchecked" })
	public TmfLocation<L> clone() {
		TmfLocation<L> clone = null;
		try {
			clone = (TmfLocation<L>) super.clone();
			if (this.fLocation != null) {
			    Class<?> clazz  = this.fLocation.getClass(); 
			    Method   method = clazz.getMethod("clone", new Class[0]);
			    Object   duplic = method.invoke(this.fLocation, new Object[0]);
			    clone.fLocation = (L) duplic;
			}
		} catch (NoSuchMethodException e) { 
		      // exception suppressed 
		} catch (Exception e) {
			throw new InternalError(e.toString());
		}
		return clone;
	}

}
