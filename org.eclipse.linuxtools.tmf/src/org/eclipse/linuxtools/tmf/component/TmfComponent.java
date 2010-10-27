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

	private String fName;
	
	// ------------------------------------------------------------------------
	// Constructor
	// ------------------------------------------------------------------------

	public TmfComponent(String name) {
		fName = name;
//		if (Tracer.isComponentTraced()) Tracer.traceComponent(this, "created");
		TmfSignalManager.register(this);
	}
	
	public TmfComponent(TmfComponent oldComponent) {
        this.fName = oldComponent.fName;

        // Should we register? Probably not but I'm not quite sure what this does
        //register();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.component.ITmfComponent#getName()
	 */
	protected void setName(String name) {
		fName = name;
	}

	// ------------------------------------------------------------------------
	// ITmfComponent
	// ------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.component.ITmfComponent#getName()
	 */
	@Override
	public String getName() {
		return fName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.component.ITmfComponent#dispose()
	 */
	@Override
	public void dispose() {
		TmfSignalManager.deregister(this);
//		if (Tracer.isComponentTraced()) Tracer.traceComponent(this, "terminated");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.component.ITmfComponent#broadcast(org.eclipse.linuxtools.tmf.signal.TmfSignal)
	 */
	@Override
	public void broadcast(TmfSignal signal) {
		TmfSignalManager.dispatchSignal(signal);
	}

}
