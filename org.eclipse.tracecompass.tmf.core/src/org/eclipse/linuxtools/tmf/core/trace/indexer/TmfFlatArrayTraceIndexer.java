/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace.indexer;

import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpointIndex;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.TmfCheckpointIndexer;

/**
 * An indexer that uses a FlatArray index to store checkpoints
 *
 * @since 3.0
 * @author Marc-Andre Laperle
 */
public class TmfFlatArrayTraceIndexer extends TmfCheckpointIndexer {

    /**
     * Full trace indexer
     *
     * @param trace
     *            the trace to index
     * @param interval
     *            the checkpoints interval
     */
    public TmfFlatArrayTraceIndexer(ITmfTrace trace, int interval) {
        super(trace, interval);
    }

    @Override
    protected ITmfCheckpointIndex createIndex(ITmfTrace trace) {
        return new TmfFlatArrayTraceIndex(trace);
    }
}
