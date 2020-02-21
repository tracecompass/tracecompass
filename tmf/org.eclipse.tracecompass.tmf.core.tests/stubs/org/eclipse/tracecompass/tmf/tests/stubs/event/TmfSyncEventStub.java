/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.tests.stubs.event;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.event.TmfEventField;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Event stub used by the synchronization tests
 *
 * @author Geneviève Bastien
 */
public class TmfSyncEventStub extends TmfEvent {

    private static final @NonNull String stub = "stub"; //$NON-NLS-1$

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
                ITmfContext.UNKNOWN_RANK,
                timestamp,
                new TmfEventTypeStub(),
                new TmfEventField(stub, stub, null));
    }
}
