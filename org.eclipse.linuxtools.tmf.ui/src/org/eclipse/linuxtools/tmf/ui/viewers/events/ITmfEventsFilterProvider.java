/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.events;

/**
 * A filter/search event provider
 * 
 * @version 1.0
 * @author Patrick Tasse
 */
public interface ITmfEventsFilterProvider {

	public void addEventsFilterListener (ITmfEventsFilterListener listener);
	
	public void removeEventsFilterListener (ITmfEventsFilterListener listener);
	
}
