/*******************************************************************************
 * Copyright (c) 2009, 2010, 2012 Ericsson
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
 * Basic abstract TMF view class implementation.
 * 
 * It registers any sub class to the signal manager for receiving and sending
 * TMF signals.  
 * 
 * @version 1.0
 * @author Francois Chouinard
 */
public abstract class TmfView extends ViewPart implements ITmfComponent {

	private final String fName;
	
	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	/**
	 * Constructor. Creates a TMF view and registers to the signal manager.
	 * 
	 * @param viewName A view name
	 */
	public TmfView(String viewName) {
		super();
		fName = viewName;
		TmfSignalManager.register(this);
	}

	/**
	 * Disposes this view and deregisters itself from the signal manager 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		TmfSignalManager.deregister(this);
		super.dispose();
	}

	// ------------------------------------------------------------------------
	// ITmfComponent
	// ------------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.core.component.ITmfComponent#getName()
	 */
	@Override
	public String getName() {
		return fName;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.core.component.ITmfComponent#broadcast(org.eclipse.linuxtools.tmf.core.signal.TmfSignal)
	 */
	@Override
	public void broadcast(TmfSignal signal) {
		TmfSignalManager.dispatchSignal(signal);
	}

}