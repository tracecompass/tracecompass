/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.tests.stubs.request;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;

/**
 * <b><u>TmfEventRequestStub</u></b>
 */
public class TmfEventRequestStub extends TmfEventRequest {

    /**
     * @param dataType the event type
     */
    public TmfEventRequestStub(final Class<? extends ITmfEvent> dataType) {
        super(dataType, TmfTimeRange.ETERNITY, 0, ALL_DATA, ExecutionType.FOREGROUND);
    }

    /**
     * @param dataType the event type
     * @param range the requested time range
     */
    public TmfEventRequestStub(final Class<? extends ITmfEvent> dataType, final TmfTimeRange range) {
        super(dataType, range, 0, ALL_DATA, ExecutionType.FOREGROUND);
    }

    /**
     * @param dataType the event type
     * @param range the requested time range
     * @param nbRequested the number of events requested
     */
    public TmfEventRequestStub(final Class<? extends ITmfEvent> dataType, final TmfTimeRange range, final int nbRequested) {
        super(dataType, range, 0, nbRequested, ExecutionType.FOREGROUND);
    }

    /**
     * @param dataType the event type
     * @param range the requested time range
     * @param nbRequested the number of events requested
     * @param blockSize the event block size
     */
    public TmfEventRequestStub(final Class<? extends ITmfEvent> dataType, final TmfTimeRange range, final int nbRequested, final int blockSize) {
        super(dataType, range, 0, nbRequested, ExecutionType.FOREGROUND);
    }

    /**
     * @param dataType the event type
     * @param range the requested time range
     * @param index the initial event index
     * @param nbRequested the number of events requested
     * @param blockSize the event block size
     */
    public TmfEventRequestStub(final Class<? extends ITmfEvent> dataType, final TmfTimeRange range, final long index, final int nbRequested, final int blockSize) {
        super(dataType, range, index, nbRequested, ExecutionType.FOREGROUND);
    }

    /**
     * @param dataType the event type
     * @param range the requested time range
     * @param nbRequested the number of events requested
     * @param blockSize the event block size
     * @param type the execution type
     * @param dependency the dependency
     */
    public TmfEventRequestStub(final Class<? extends ITmfEvent> dataType, final TmfTimeRange range, final int nbRequested, final int blockSize, ExecutionType type, int dependency) {
        super(dataType, range, 0, nbRequested, type, dependency);
    }

    @Override
    public void handleData(final ITmfEvent data) {
        super.handleData(data);
    }
}
