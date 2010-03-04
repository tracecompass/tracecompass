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

package org.eclipse.linuxtools.tmf.trace;

/**
 * <b><u>TmfLocation</u></b>
 * <p>
 * A generic implementation of ITmfLocation
 */
public class TmfLocation<L> implements ITmfLocation<L> {

	private L fLocation;
	
	public TmfLocation(L location) {
		fLocation = location;
	}

	public void setLocation(L location) {
		fLocation = location;
	}

	public L getLocation() {
		return fLocation;
	}

	@Override
	public String toString() {
		return fLocation.toString();
	}

	@Override
	@SuppressWarnings("unchecked")
	public TmfLocation<L> clone() {
		try {
			return (TmfLocation<L>) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

}
