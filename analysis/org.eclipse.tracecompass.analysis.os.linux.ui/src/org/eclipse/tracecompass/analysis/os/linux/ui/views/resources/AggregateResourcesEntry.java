/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.ui.views.resources;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;

/**
 * Internal entry of the aggregate kind, it is one without a state system value,
 * it uses other entries and aggregates their values.
 *
 * @author Matthew Khouzam
 */
class AggregateResourcesEntry extends ResourcesEntry {

    private final @NonNull List<ITimeGraphEntry> fContributors = new ArrayList<>();

    private static final Comparator<ITimeEvent> COMPARATOR = new Comparator<ITimeEvent>() {
        @Override
        public int compare(ITimeEvent o1, ITimeEvent o2) {
            // largest value
            return Integer.compare(getValue(o2), getValue(o1));
        }

        private int getValue(ITimeEvent element) {
            return (element instanceof TimeEvent) ? ((TimeEvent) element).getValue() : Integer.MIN_VALUE;
        }
    };

    /**
     * AggregateResourcesEntry Constructor
     *
     * @param trace
     *            the parent trace
     * @param startTime
     *            the start time
     * @param endTime
     *            the end time
     * @param type
     *            the type
     * @param id
     *            the id
     */
    public AggregateResourcesEntry(@NonNull ITmfTrace trace,
            long startTime, long endTime, Type type, int id) {
        super(ITmfStateSystem.INVALID_ATTRIBUTE, trace, startTime, endTime, type, id);
    }

    @Override
    public void addEvent(ITimeEvent event) {
    }

    @Override
    public void addZoomedEvent(ITimeEvent event) {
    }

    @Override
    public Iterator<@NonNull ITimeEvent> getTimeEventsIterator() {
        return new AggregateEventIterator(fContributors, COMPARATOR);
    }

    @Override
    public Iterator<@NonNull ITimeEvent> getTimeEventsIterator(long startTime, long stopTime, long visibleDuration) {
        return new AggregateEventIterator(fContributors, startTime, stopTime, visibleDuration, COMPARATOR);
    }

    public void addContributor(ITimeGraphEntry entry) {
        fContributors.add(entry);
    }
}
