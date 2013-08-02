/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Alexandre Montplaisir - Consolidated constructors
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.request;

import org.eclipse.linuxtools.internal.tmf.core.TmfCoreTracer;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;

/**
 * An extension of TmfDataRequest for timestamped events.
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public abstract class TmfEventRequest extends TmfDataRequest implements ITmfEventRequest {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final TmfTimeRange fRange; // The requested events time range

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Request 'n' events of a given type for the given time range (given
     * priority). Events are returned in blocks of the given size.
     *
     * @param dataType
     *            The requested data type.
     * @param range
     *            The time range of the requested events. You can use
     *            {@link TmfTimeRange#ETERNITY} to indicate you want to cover
     *            the whole trace.
     * @param index
     *            The index of the first event to retrieve. You can use '0' to
     *            start at the beginning of the trace.
     * @param nbRequested
     *            The number of events requested. You can use
     *            {@link TmfEventRequest#ALL_DATA} to indicate you want all
     *            events in the time range.
     * @param priority
     *            The requested execution priority.
     * @since 3.0
     */
    public TmfEventRequest(Class<? extends ITmfEvent> dataType,
            TmfTimeRange range,
            long index,
            int nbRequested,
            ExecutionType priority) {
        super(dataType, index, nbRequested, priority);
        fRange = range;

        if (TmfCoreTracer.isRequestTraced()) {
            String type = getClass().getName();
            type = type.substring(type.lastIndexOf('.') + 1);
            @SuppressWarnings("nls")
            String message = "CREATED "
                    + (getExecType() == ITmfDataRequest.ExecutionType.BACKGROUND ? "(BG)" : "(FG)")
                    + " Type=" + type + " Index=" + getIndex() + " NbReq=" + getNbRequested()
                    + " Range=" + getRange()
                    + " DataType=" + getDataType().getSimpleName();
            TmfCoreTracer.traceRequest(this, message);
        }
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the requested time range
     * @since 2.0
     */
    @Override
    public TmfTimeRange getRange() {
        return fRange;
    }

    // ------------------------------------------------------------------------
    // Setters
    // ------------------------------------------------------------------------

    /**
     * this method is called by the event provider to set the index
     * corresponding to the time range start time once it is known
     *
     * @param index
     *            the start index
     */
    @Override
    public void setStartIndex(int index) {
        setIndex(index);
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    // All requests have a unique id
    public int hashCode() {
        return getRequestId();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TmfEventRequest) {
            TmfEventRequest request = (TmfEventRequest) other;
            return super.equals(other) && request.fRange.equals(fRange);
        }
        return false;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        String name = getClass().getName();
        int dot = name.lastIndexOf('.');
        if (dot >= 0) {
            name = name.substring(dot + 1);
        }
        return "[" + name + "(" + getRequestId() + "," + getDataType().getSimpleName() + "," + getExecType()
                + "," + getRange() + "," + getIndex() + "," + getNbRequested() + ")]";
    }

}
