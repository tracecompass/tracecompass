/*******************************************************************************
 * Copyright (c) 2015, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.markers;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStateStatistics.Attributes;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStatisticsEventTypesModule;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStatisticsModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.IMarkerEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.IMarkerEventSource;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.MarkerEvent;

/**
 * Marker event source for lost events.
 */
public class LostEventsMarkerEventSource implements IMarkerEventSource {

    private static final @NonNull String LOST_EVENTS = checkNotNull(Messages.MarkerEvent_LostEvents);

    private static final RGBA COLOR = new RGBA(255, 0, 0, 50);

    private final @NonNull ITmfTrace fTrace;
    private long[] fLastRequest;
    private @NonNull List<@NonNull IMarkerEvent> fLastMarkers = Collections.emptyList();

    /**
     * Constructor.
     *
     * @param trace
     *            the trace
     */
    public LostEventsMarkerEventSource(@NonNull ITmfTrace trace) {
        fTrace = trace;
    }

    @Override
    public @NonNull List<@NonNull String> getMarkerCategories() {
        return Arrays.asList(LOST_EVENTS);
    }

    @Override
    public synchronized @NonNull List<@NonNull IMarkerEvent> getMarkerList(@NonNull String category, long startTime, long endTime, long resolution, @NonNull IProgressMonitor monitor) {
        if (!category.equals(LOST_EVENTS)) {
            return Collections.emptyList();
        }
        ITmfStateSystem ss = getStateSystem();
        if (ss == null) {
            return Collections.emptyList();
        }
        int lostEventsQuark = getLostEventsQuark(ss);
        if (lostEventsQuark == -1) {
            return Collections.emptyList();
        }
        long[] request = new long[] { startTime, endTime, resolution, ss.getCurrentEndTime() };
        if (Arrays.equals(request, fLastRequest)) {
            return fLastMarkers;
        }
        List<@NonNull IMarkerEvent> markers = new ArrayList<>();
        try {
            long start = Math.max(startTime, ss.getStartTime());
            long end = Math.min(endTime, ss.getCurrentEndTime());
            if (start <= end) {
                /* Update start to ensure that the previous marker is included. */
                start = Math.max(start - 1, ss.getStartTime());
                /* Update end to ensure that the next marker is included. */
                long nextStartTime = ss.querySingleState(end, lostEventsQuark).getEndTime() + 1;
                end = Math.min(nextStartTime, ss.getCurrentEndTime());
                List<ITmfStateInterval> intervals = StateSystemUtils.queryHistoryRange(ss, lostEventsQuark, start, end, resolution, monitor);
                for (ITmfStateInterval interval : intervals) {
                    if (interval.getStateValue().isNull()) {
                        continue;
                    }
                    long lostEventsStartTime = interval.getStartTime();
                    /*
                     * The end time of the lost events range is the value of the
                     * attribute, not the end time of the interval.
                     */
                    long lostEventsEndTime = interval.getStateValue().unboxLong();
                    long duration = lostEventsEndTime - lostEventsStartTime;
                    IMarkerEvent marker = new MarkerEvent(null, lostEventsStartTime, duration, LOST_EVENTS, COLOR, null, false);
                    markers.add(marker);
                }
            }
        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            /* ignored */
        }
        fLastRequest = request;
        fLastMarkers = Collections.unmodifiableList(markers);
        return fLastMarkers;
    }

    private ITmfStateSystem getStateSystem() {
        TmfStatisticsModule module = TmfTraceUtils.getAnalysisModuleOfClass(fTrace, TmfStatisticsModule.class, TmfStatisticsModule.ID);
        if (module == null) {
            return null;
        }
        return module.getStateSystem(TmfStatisticsEventTypesModule.ID);
    }

    private static int getLostEventsQuark(ITmfStateSystem ss) {
        try {
            return ss.getQuarkAbsolute(Attributes.LOST_EVENTS);
        } catch (AttributeNotFoundException e) {
            return -1;
        }
    }
}
