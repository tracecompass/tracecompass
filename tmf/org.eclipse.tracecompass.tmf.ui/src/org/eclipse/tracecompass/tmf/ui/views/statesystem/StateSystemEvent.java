/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.statesystem;

import java.util.Objects;

import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

/**
 * State System time event, composes with an {@link ITmfStateInterval}.
 *
 * @author Loic Prieur-Drevon
 */
class StateSystemEvent extends TimeEvent {
    private final ITmfStateInterval fInterval;

    /**
     * Constructor
     *
     * @param entry
     *            The entry matching this event
     * @param interval
     *            the interval that this time event represents
     */
    public StateSystemEvent(TimeGraphEntry entry, ITmfStateInterval interval) {
        super(entry, interval.getStartTime(), interval.getEndTime() - interval.getStartTime() + 1);
        fInterval = interval;
    }

    /**
     * Getter for the encapsulated interval
     *
     * @return the encapsulated interval
     */
    public ITmfStateInterval getInterval() {
        return fInterval;
    }

    @Override
    public String getLabel() {
        return Objects.toString(fInterval.getValue(), null);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StateSystemEvent) {
            return super.equals(obj) && fInterval.equals(((StateSystemEvent) obj).fInterval);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode() * fInterval.hashCode();
    }
}
