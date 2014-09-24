/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.tests.stubs.event;

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Event stub used by the synchronization tests
 *
 * @author Geneviève Bastien
 */
public class TmfSyncEventStub extends TmfEvent {

    private static final String stub = "stub"; //$NON-NLS-1$

    /**
     * Constructor
     *
     * @param trace
     *            The trace of this event
     * @param timestamp
     *            The timestamp
     */
    public TmfSyncEventStub(final ITmfTrace trace, final ITmfTimestamp timestamp) {
        super(trace,
                timestamp,
                stub,
                new TmfEventTypeStub(),
                new TmfEventField(stub, stub, null),
                stub);
    }
}
