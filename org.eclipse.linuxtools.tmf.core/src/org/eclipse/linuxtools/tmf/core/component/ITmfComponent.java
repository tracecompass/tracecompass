/*******************************************************************************
 * Copyright (c) 2009, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.component;

import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;

/**
 * This is the basic interface of all the TMF components.
 * <p>
 * Currently, it only addresses the inter-component signalling.
 *
 * @version 1.0
 * @author Francois Chouinard
 * 
 * @see TmfComponent
 */
public interface ITmfComponent {

	/**
	 * @return the component ID (display name) 
	 */
	public String getName();

	/**
	 * Dispose of the component
	 */
	public void dispose();

	/**
	 * Propagate a signal to all the interested listeners.
	 * 
	 * @param signal the signal to broadcast
	 */
	public void broadcast(TmfSignal signal);

}
