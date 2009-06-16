/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard (fchouinard@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.eventlog;

/**
 * <b><u>ITmfRequestHandler</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public interface ITmfRequestHandler {

    /**
     * Process the request. The client thread can be suspended until the 
     * request is completed (e.g. for a specific range of events) or it
     * can choose to process the events asynchronously (e.g. for streaming).
     * 
     * @param waitForCompletion Suspend the client thread until the request completes or is canceled 
     */
    public void process(TmfEventRequest request, boolean waitForCompletion);

}
