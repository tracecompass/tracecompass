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

package org.eclipse.linuxtools.tmf.signal;

/**
 * <b><u>TmfSignal</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public abstract class TmfSignal {

	// The signal originator
	private final Object fSource;

	public TmfSignal(Object source) {
		fSource = source;
	}

	public Object getSource() {
		return fSource;
	}
}
