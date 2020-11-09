/**********************************************************************
 * Copyright (c) 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.IMarkerEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.MarkerEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

/**
 * Time graph stub. This view is intended to be used as a "torture test" to see
 * the performance impact of many many markers.
 *
 * @author Matthew Khouzam
 */
public class TimeGraphViewWithTooManyMarkersStub extends AbstractTimeGraphView {

    private static final int NB_MARKERS = 1000;
    private static final int MARKER_STEP = 10;
    private static final int NB_ENTRIES = 1000;
    /**
     * Id
     */
    public static final String ID = "org.eclipse.tracecompass.tmf.ui.swtbot.tests.views.timegraph.stub2";
    private static final String MARKER_GROUP = "sample";

    private final List<@NonNull TimeGraphEntry> fEntries = new ArrayList<>();

    /**
     * Constructor
     */
    public TimeGraphViewWithTooManyMarkersStub() {
        super(ID, new StubPresentationProvider());
        for (int i = 0; i < NB_ENTRIES; i++) {
            fEntries.add(new TimeGraphEntry(String.format("entry %d", i), 0, NB_MARKERS));
        }
    }

    @Override
    protected void buildEntryList(@NonNull ITmfTrace trace, @NonNull ITmfTrace parentTrace, @NonNull IProgressMonitor monitor) {
        List<@NonNull TimeGraphEntry> entryList = getEntryList(trace);
        if (entryList == null || entryList.isEmpty()) {
            addToEntryList(trace, fEntries);
        }
        refresh();
    }

    @Override
    protected @NonNull List<String> getMarkerCategories() {
        return Collections.singletonList(MARKER_GROUP);
    }

    @Override
    protected @NonNull List<IMarkerEvent> getViewMarkerList(long startTime, long endTime, long resolution, @NonNull IProgressMonitor monitor) {
        ITmfTrace trace = getTrace();
        if (trace == null) {
            return Collections.emptyList();
        }
        RGBA fromInt = new RGBA(0, 0, 0, 255);
        long start = trace.getStartTime().toNanos();
        List<IMarkerEvent> markers = new ArrayList<>();
        for (int rowId = 0; rowId < fEntries.size(); rowId++) {
            TimeGraphEntry row = fEntries.get(rowId);
            int offset = (int) (MARKER_STEP / 2 * Math.sin(rowId * Math.PI / MARKER_STEP)) + MARKER_STEP / 2;
            for (long i = 0; i < NB_MARKERS; i += MARKER_STEP) {
                markers.add(new MarkerEvent(row, start + i + offset, 0L, "", fromInt, "", true, (rowId / MARKER_STEP) % 7));
            }
        }
        return markers;
    }

}
