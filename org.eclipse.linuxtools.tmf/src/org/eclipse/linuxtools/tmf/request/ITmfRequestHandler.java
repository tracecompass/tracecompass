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

package org.eclipse.linuxtools.tmf.request;

import org.eclipse.linuxtools.tmf.event.TmfData;

/**
 * <b><u>ITmfRequestHandler</u></b>
 * <p>
 * TODO: Implement me. Please.
 * @param <V>
 */
public interface ITmfRequestHandler<T extends TmfData> {

    /**
     * Process the request. The client thread can be suspended until the 
     * request is completed (e.g. for a specific range of events) or it
     * can choose to process the events asynchronously (e.g. for streaming).
     * 
     * If the request can't be serviced, it will fail (i.e. isFailed() will be set).
     * 
	 * @param request The request to process
	 * @param waitForCompletion Suspend the client thread until the request completes
	 */
	public void processRequest(TmfDataRequest<T> request, boolean waitForCompletion);
}
