/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.tests.stubs.trace;

import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.trace.indexer.ITmfTraceIndexer;

/**
 * <b><u>TmfExperimentStub</u></b>
 * <p>
 * Implement me. Please.
 * <p>
 */
@SuppressWarnings("javadoc")
public class TmfExperimentStub extends TmfExperiment {

    public TmfExperimentStub() {
        super();
    }

    public TmfExperimentStub(String name, ITmfTrace[] traces, int blockSize) {
        super(ITmfEvent.class, name, traces, blockSize);
    }

    @Override
    protected ITmfTraceIndexer createIndexer(int interval) {
        return new TmfIndexerStub(this, interval);
    }

    @Override
    public TmfIndexerStub getIndexer() {
        return (TmfIndexerStub) super.getIndexer();
    }

    @Override
    public void initExperiment(final Class<? extends ITmfEvent> type, final String path, final ITmfTrace[] traces, final int indexPageSize, IResource resource) {
        super.initExperiment(type, path, traces, indexPageSize, resource);
    }
}
