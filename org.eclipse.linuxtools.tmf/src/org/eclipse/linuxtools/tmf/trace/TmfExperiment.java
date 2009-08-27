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

package org.eclipse.linuxtools.tmf.trace;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.request.ITmfRequestHandler;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;

/**
 * <b><u>TmfExperiment</u></b>
 * <p>
 * TmfExperiment presents a time-ordered, unified view of a set of 
 * TmfTraces that are part of a tracing experiment. 
 * <p>
 * TODO: Implement me. PLease.
 */
public class TmfExperiment implements ITmfRequestHandler<TmfEvent> {

    // ========================================================================
    // Attributes
    // ========================================================================

    // ========================================================================
    // Constructors/Destructors
    // ========================================================================

    public TmfExperiment() {
    }

    // ========================================================================
    // Accessors
    // ========================================================================

    // ========================================================================
    // Operators
    // ========================================================================

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.eventlog.ITmfRequestHandler#processRequest(org.eclipse.linuxtools.tmf.eventlog.TmfDataRequest, boolean)
     */
    public void processRequest(TmfDataRequest<TmfEvent> request, boolean waitForCompletion) {
    }

    // ========================================================================
    // Helper functions
    // ========================================================================

}
