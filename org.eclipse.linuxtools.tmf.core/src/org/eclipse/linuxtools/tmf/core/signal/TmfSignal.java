/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.signal;

/**
 * Base class for TMF signals
 * 
 * @version 1.0
 * @author Francois Chouinard
 */
public abstract class TmfSignal {

	// The signal originator
	private final Object fSource;

	private int fReference;

	public TmfSignal(Object source) {
		this(source, 0);
	}

	public TmfSignal(Object source, int reference) {
		fSource = source;
		fReference = reference;
	}

	public Object getSource() {
		return fSource;
	}

	public void setReference(int reference) {
		fReference = reference;
	}

	public int getReference() {
		return fReference;
	}

}
