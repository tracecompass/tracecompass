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

package org.eclipse.linuxtools.tmf.ui.views;

import org.eclipse.linuxtools.tmf.core.component.ITmfComponent;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.ui.part.ViewPart;

/**
 * <b><u>TmfView</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public abstract class TmfView extends ViewPart implements ITmfComponent {

	private final String fName;
	
	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public TmfView(String viewName) {
		super();
		fName = viewName;
		TmfSignalManager.register(this);
	}

	@Override
	public void dispose() {
		TmfSignalManager.deregister(this);
		super.dispose();
	}

	// ------------------------------------------------------------------------
	// ITmfComponent
	// ------------------------------------------------------------------------

	@Override
	public String getName() {
		return fName;
	}
	
	@Override
	public void broadcast(TmfSignal signal) {
		TmfSignalManager.dispatchSignal(signal);
	}

}