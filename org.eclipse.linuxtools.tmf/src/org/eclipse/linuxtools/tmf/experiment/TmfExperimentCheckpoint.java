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

package org.eclipse.linuxtools.tmf.experiment;

import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.trace.TmfTraceContext;

/**
 * <b><u>TmfExperimentCheckpoint</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfExperimentCheckpoint implements Comparable<TmfExperimentCheckpoint> {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    
    private final TmfTimestamp fTimestamp;
    private final TmfTraceContext[] fContexts;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * @param ts
     * @param location
     */
    public TmfExperimentCheckpoint(TmfTimestamp ts, TmfTraceContext[] contexts) {
        fTimestamp = ts;
        fContexts = contexts;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the checkpoint event timestamp
     */
    public TmfTimestamp getTimestamp() {
        return fTimestamp;
    }

    /**
     * @return the checkpoint event stream location
     */
    public TmfTraceContext[] getContexts() {
        return fContexts;
    }

    // ------------------------------------------------------------------------
    // Comparable
    // ------------------------------------------------------------------------

	public int compareTo(TmfExperimentCheckpoint other) {
		return fTimestamp.compareTo(other.fTimestamp, false);
	}

}
