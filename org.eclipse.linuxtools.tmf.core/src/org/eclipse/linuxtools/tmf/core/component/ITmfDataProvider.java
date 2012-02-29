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

package org.eclipse.linuxtools.tmf.core.component;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;

/**
 * <b><u>ITmfDataRequest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public interface ITmfDataProvider<T extends ITmfEvent> {

    /**
     * Queues the request for processing.
     * 
     * If the request can't be serviced, it will fail (i.e. isFailed() will be set).
     * 
	 * @param request The request to process
	 */
	public void sendRequest(ITmfDataRequest<T> request);
	public void fireRequest();
	public void notifyPendingRequest(boolean isIncrement);
}
