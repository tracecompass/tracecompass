/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace.indexer;

import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.ITmfCheckpointIndex;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.TmfCheckpointIndexer;

/**
 * An indexer that uses a FlatArray index to store checkpoints
 *
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
