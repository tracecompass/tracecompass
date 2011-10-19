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

package org.eclipse.linuxtools.tmf.component;

import org.eclipse.linuxtools.tmf.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.signal.TmfSignalManager;

/**
 * <b><u>TmfComponent</u></b>
 * <p>
 * This is the base class of the TMF components.
 * <p>
 *  Currently, it only addresses the inter-component signaling.
 */
public abstract class TmfComponent implements ITmfComponent {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

	private String fName;
	
	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

    public TmfComponent() {
	    this(""); //$NON-NLS-1$
    }

    public void init(String name) {
        fName = name;
        TmfSignalManager.register(this);
    }

	public TmfComponent(String name) {
		init(name);
	}
	
	public TmfComponent(TmfComponent other) {
        init(other.fName);
	}
	
    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

	protected void setName(String name) {
		fName = name;
	}

	// ------------------------------------------------------------------------
	// ITmfComponent
	// ------------------------------------------------------------------------

	@Override
	public String getName() {
		return fName;
	}

	@Override
	public void dispose() {
		TmfSignalManager.deregister(this);
//		if (Tracer.isComponentTraced()) Tracer.traceComponent(this, "terminated");
	}

	@Override
	public void broadcast(TmfSignal signal) {
		TmfSignalManager.dispatchSignal(signal);
	}

}
