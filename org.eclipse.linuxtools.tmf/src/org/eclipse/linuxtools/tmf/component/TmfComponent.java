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

package org.eclipse.linuxtools.tmf.component;

import org.eclipse.linuxtools.tmf.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.signal.TmfSignalManager;

/**
 * <b><u>TmfComponent</u></b>
 * <p>
 */
public abstract class TmfComponent implements ITmfComponent {

	public TmfComponent() {
		TmfSignalManager.addListener(this);
	}

	public void dispose() {
		TmfSignalManager.removeListener(this);
	}

	public void broadcastSignal(TmfSignal signal) {
		TmfSignalManager.dispatchSignal(signal);
	}
}
