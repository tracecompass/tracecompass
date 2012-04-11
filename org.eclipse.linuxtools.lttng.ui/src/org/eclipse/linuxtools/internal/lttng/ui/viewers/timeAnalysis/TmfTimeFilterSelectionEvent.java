/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng.ui.viewers.timeAnalysis;

import java.util.EventObject;
import java.util.Vector;

import org.eclipse.linuxtools.internal.lttng.ui.viewers.timeAnalysis.model.ITmfTimeAnalysisEntry;

public class TmfTimeFilterSelectionEvent extends EventObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -150960748016449093L;

	Vector<ITmfTimeAnalysisEntry> filteredOut = null;

	public TmfTimeFilterSelectionEvent(Object source) {
		super(source);
	}

	public Vector<ITmfTimeAnalysisEntry> getFilteredOut() {
		return filteredOut;
	}

	public void setFilteredOut(Vector<ITmfTimeAnalysisEntry> rfilteredOut) {
		this.filteredOut = rfilteredOut;
	}
}
