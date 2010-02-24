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
	// Constructor
	// ------------------------------------------------------------------------

	public TmfComponent() {
		register();
	}

	// ------------------------------------------------------------------------
	// ITmfComponent
	// ------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.component.ITmfComponent#register()
	 */
	public void register() {
		TmfSignalManager.register(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.component.ITmfComponent#deregister()
	 */
	public void deregister() {
		TmfSignalManager.deregister(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.component.ITmfComponent#broadcast(org.eclipse.linuxtools.tmf.signal.TmfSignal)
	 */
	public void broadcast(TmfSignal signal) {
		TmfSignalManager.dispatchSignal(signal);
	}

}
