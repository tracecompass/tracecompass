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

package org.eclipse.linuxtools.tmf.core.experiment;

import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;

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
    private final TmfContext[] fContexts;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * @param ts the checkpoint timestamp
     * @param contexts the corresponding set of trace contexts
     */
    public TmfExperimentCheckpoint(TmfTimestamp ts, TmfContext[] contexts) {
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
    public TmfContext[] getContexts() {
        return fContexts;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
    	int result = 37;
    	result = 17 * result + fTimestamp.hashCode();
    	return result;
    }
 
    @Override
    public boolean equals(Object other) {
    	if (!(other instanceof TmfExperimentCheckpoint)) {
    		return false;
    	}
    	TmfExperimentCheckpoint o = (TmfExperimentCheckpoint) other;
    	return fTimestamp.equals(o.fTimestamp);
    }
 
    // ------------------------------------------------------------------------
    // Comparable
    // ------------------------------------------------------------------------

	@Override
	public int compareTo(TmfExperimentCheckpoint other) {
		return fTimestamp.compareTo(other.fTimestamp, false);
	}

}
