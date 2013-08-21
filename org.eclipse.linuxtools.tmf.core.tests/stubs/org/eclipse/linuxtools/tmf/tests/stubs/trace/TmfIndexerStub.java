/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.tests.stubs.trace;

import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpointIndex;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.TmfCheckpointIndexer;

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
