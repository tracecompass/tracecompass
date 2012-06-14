/*******************************************************************************
 * Copyright (c) 2012 Ericsson
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

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;

/**
 * <b><u>TmfExperimentStub</u></b>
 * <p>
 * Implement me. Please.
 * <p>
 */
public class TmfExperimentStub<T extends ITmfEvent> extends TmfExperiment<TmfEvent> {

    public TmfExperimentStub(String name, ITmfTrace<TmfEvent>[] traces, int blockSize) {
        super(TmfEvent.class, name, traces, blockSize);
        setIndexer(new TmfIndexerStub(this, blockSize));
    }

    @Override
    public TmfIndexerStub getIndexer() {
        return (TmfIndexerStub) super.getIndexer();
    }
}
