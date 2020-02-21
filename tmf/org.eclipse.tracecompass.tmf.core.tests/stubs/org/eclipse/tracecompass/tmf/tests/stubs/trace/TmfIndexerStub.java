/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.tests.stubs.trace;

import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.ITmfCheckpointIndex;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.TmfCheckpointIndexer;

/**
 * <b><u>TmfIndexerStub</u></b>
 * <p>
 * Implement me. Please.
 * <p>
 */
@SuppressWarnings("javadoc")
public class TmfIndexerStub extends TmfCheckpointIndexer {

    public TmfIndexerStub(ITmfTrace trace, int blockSize) {
        super(trace, blockSize);
    }

    public ITmfCheckpointIndex getCheckpoints() {
        return getTraceIndex();
    }

}
