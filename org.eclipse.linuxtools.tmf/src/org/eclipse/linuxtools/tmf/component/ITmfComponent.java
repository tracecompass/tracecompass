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

/**
 * <b><u>ITmfComponent</u></b>
 * <p>
 * This is the basic interface of all the TMF components.
 * <p>
 *  Currently, it only addresses the inter-component signaling.
 */
public interface ITmfComponent {

	/**
	 * Get the component ID
	 */
	public String getName();

	/**
	 * Dispose of the component
	 */
	public void dispose();

	/**
	 * Broadcast a signal to all the interested listeners.
	 * 
	 * @param signal
	 */
	public void broadcast(TmfSignal signal);

}
