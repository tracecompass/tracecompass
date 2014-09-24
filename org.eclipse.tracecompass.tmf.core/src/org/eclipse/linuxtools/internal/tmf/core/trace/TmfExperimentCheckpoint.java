/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.trace;

import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
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
    private final long[] fRanks;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * @param ts the checkpoint timestamp
     * @param contexts the corresponding set of trace contexts
     */
    public TmfExperimentCheckpoint(final TmfTimestamp ts, final TmfContext[] contexts) {
        fTimestamp = ts;
        fRanks = new long[contexts.length];
        for (int i = 0; i < fRanks.length; i++) {
            fRanks[i] = contexts[i].getRank();
        }
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
     * @return the checkpoint event rank
     */
    public long[] getRanks() {
        return fRanks;
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
    public boolean equals(final Object other) {
        if (!(other instanceof TmfExperimentCheckpoint)) {
            return false;
        }
        final TmfExperimentCheckpoint o = (TmfExperimentCheckpoint) other;
        return fTimestamp.equals(o.fTimestamp);
    }

    // ------------------------------------------------------------------------
    // Comparable
    // ------------------------------------------------------------------------

    @Override
    public int compareTo(final TmfExperimentCheckpoint other) {
        return fTimestamp.compareTo(other.fTimestamp, false);
    }

}
