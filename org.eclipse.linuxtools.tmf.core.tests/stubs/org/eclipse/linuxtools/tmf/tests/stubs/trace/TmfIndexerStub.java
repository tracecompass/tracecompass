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

import java.util.List;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.TmfCheckpointIndexer;

/**
 * <b><u>TmfIndexerStub</u></b>
 * <p>
 * Implement me. Please.
 * <p>
 */
public class TmfIndexerStub extends TmfCheckpointIndexer<ITmfTrace<ITmfEvent>> {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public TmfIndexerStub(ITmfTrace trace, int blockSize) {
        super(trace, blockSize);
    }

    public List<TmfCheckpoint> getCheckpoints() {
        return getTraceIndex();
    }

}
