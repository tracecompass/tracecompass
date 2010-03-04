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
 * Implement me. Please.
 */
public class TmfLocation<T> implements ITmfLocation {

	private T fLocation;
	
	public TmfLocation(T location) {
		fLocation = location;
	}

	public void setValue(T location) {
		fLocation = location;
	}

	public T getValue() {
		return fLocation;
	}

	@Override
	@SuppressWarnings("unchecked")
	public TmfLocation<T> clone() {
		try {
			return (TmfLocation<T>) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
}
