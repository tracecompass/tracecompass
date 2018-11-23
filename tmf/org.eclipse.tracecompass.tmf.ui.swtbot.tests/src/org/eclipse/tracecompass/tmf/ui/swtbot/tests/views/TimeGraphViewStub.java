/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.tracecompass.tmf.core.model.timegraph.IFilterProperty;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ILinkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.IMarkerEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.MarkerEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeLinkEvent;

import com.google.common.collect.Multimap;

/**
 * Time graph stub.
 *
 * @author Matthew Khouzam
 */
public class TimeGraphViewStub extends AbstractTimeGraphView {

    /**
     * Id
     */
    public static final String ID = "org.eclipse.tracecompass.tmf.ui.swtbot.tests.views.timegraph.stub";
    /**
     * Hat id
     */
    public static final int HAT = 0;
    /**
     * Skin id
     */
    public static final int SKIN = 1;
    /**
     * Hair id
     */
    public static final int HAIR = 2;
    /**
     * Eye id
     */
    public static final int EYE = 3;
    /**
     * Pie chart id
     */
    public static final int PIE = 4;
    /**
     * The marker group
     */
    private static final String MARKER_GROUP = "sample";

    private final List<@NonNull TimeGraphEntry> fEntries = new ArrayList<>();
    private final Map<String, List<@NonNull ITimeEvent>> fEvents = new HashMap<>();
    private final @NonNull TimeGraphEntry fRow2;
    private final @NonNull TimeGraphEntry fHead1;
    private final @NonNull TimeGraphEntry fRow4;
    private StubPresentationProvider fPresentationProvider;

    private String fFilterRegex;

    /**
     * Constructor
     */
    public TimeGraphViewStub() {
        super(ID, new StubPresentationProvider());
        fPresentationProvider = (StubPresentationProvider) super.getPresentationProvider();
        TimeGraphEntry hero1 = new TimeGraphEntry("Plumber guy", 0, 120);
        TimeGraphEntry hat1 = new TimeGraphEntry("Hat1", 0, 120);
        TimeGraphEntry hat2 = new TimeGraphEntry("Hat2", 0, 120);
        fHead1 = new TimeGraphEntry("Head1", 0, 120);
        TimeGraphEntry head2 = new TimeGraphEntry("Head2", 0, 120);
        TimeGraphEntry head3 = new TimeGraphEntry("Head3", 0, 120);
        TimeGraphEntry head4 = new TimeGraphEntry("Head4", 0, 120);
        TimeGraphEntry neck = new TimeGraphEntry("Neck", 0, 120);

        TimeGraphEntry hero2 = new TimeGraphEntry("Hungry pie chart", 80, 160);
        TimeGraphEntry row1 = new TimeGraphEntry("row1", 80, 160);
        fRow2 = new TimeGraphEntry("row2", 80, 160);
        TimeGraphEntry row3 = new TimeGraphEntry("row3", 80, 160);
        fRow4 = new TimeGraphEntry("row4", 80, 160);
        TimeGraphEntry row5 = new TimeGraphEntry("row5", 80, 160);
        TimeGraphEntry row6 = new TimeGraphEntry("row6", 80, 160);
        TimeGraphEntry row7 = new TimeGraphEntry("row7", 80, 160);

        fEntries.add(hero1);
        hero1.addChild(hat1);
        hero1.addChild(hat2);
        hero1.addChild(fHead1);
        hero1.addChild(head2);
        hero1.addChild(head3);
        hero1.addChild(head4);
        hero1.addChild(neck);
        fEntries.add(hero2);
        hero2.addChild(row1);
        hero2.addChild(fRow2);
        hero2.addChild(row3);
        hero2.addChild(fRow4);
        hero2.addChild(row5);
        hero2.addChild(row6);
        hero2.addChild(row7);

        fEvents.put(hero1.getName(), Arrays.asList(new NullTimeEvent(hero1, 0, 120)));
        // hat
        fEvents.put(hat1.getName(), Arrays.asList(new NullTimeEvent(hat1, 0, 30), new TimeEvent(hat1, 30, 50, HAT), new NullTimeEvent(hat1, 80, 40)));
        fEvents.put(hat2.getName(), Arrays.asList(new NullTimeEvent(hat2, 0, 20), new TimeEvent(hat2, 20, 90, HAT), new NullTimeEvent(hat2, 110, 10)));
        // head
        fEvents.put(fHead1.getName(),
                Arrays.asList(new NullTimeEvent(fHead1, 0, 20), new TimeEvent(fHead1, 20, 30, HAIR), new TimeEvent(fHead1, 50, 20, SKIN), new TimeEvent(fHead1, 70, 10, EYE), new TimeEvent(fHead1, 80, 10, SKIN), new NullTimeEvent(fHead1, 90, 30)));
        fEvents.put(head2.getName(), Arrays.asList(new NullTimeEvent(head2, 0, 10), new TimeEvent(head2, 10, 10, HAIR), new TimeEvent(head2, 20, 10, SKIN), new TimeEvent(head2, 30, 10, HAIR), new TimeEvent(head2, 40, 30, SKIN),
                new TimeEvent(head2, 70, 10, EYE), new TimeEvent(head2, 80, 30, SKIN), new NullTimeEvent(head2, 110, 10)));
        fEvents.put(head3.getName(), Arrays.asList(new NullTimeEvent(head3, 0, 10), new TimeEvent(head3, 10, 10, HAIR), new TimeEvent(head3, 20, 10, SKIN), new TimeEvent(head3, 30, 20, HAIR), new TimeEvent(head3, 50, 30, SKIN),
                new TimeEvent(head3, 80, 10, HAIR), new TimeEvent(head3, 90, 30, SKIN)));
        fEvents.put(head4.getName(), Arrays.asList(new NullTimeEvent(head4, 0, 10), new TimeEvent(head4, 10, 20, HAIR), new TimeEvent(head4, 30, 40, SKIN), new TimeEvent(head4, 70, 40, HAIR), new TimeEvent(head4, 110, 10)));
        // neck
        fEvents.put(neck.getName(), Arrays.asList(new NullTimeEvent(neck, 0, 30), new TimeEvent(neck, 30, 70, SKIN), new NullTimeEvent(neck, 100, 20)));

        fEvents.put(row1.getName(), Arrays.asList(new TimeEvent(row1, 110, 30, PIE)));
        fEvents.put(fRow2.getName(), Arrays.asList(new TimeEvent(fRow2, 90, 60, PIE)));
        fEvents.put(row3.getName(), Arrays.asList(new TimeEvent(row3, 80, 50, PIE)));
        fEvents.put(fRow4.getName(), Arrays.asList(new TimeEvent(fRow4, 80, 30, PIE)));
        fEvents.put(row5.getName(), Arrays.asList(new TimeEvent(row5, 80, 50, PIE)));
        fEvents.put(row6.getName(), Arrays.asList(new TimeEvent(row6, 90, 60, PIE)));
        fEvents.put(row7.getName(), Arrays.asList(new TimeEvent(row7, 110, 30, PIE)));
    }

    @Override
    protected @NonNull Multimap<@NonNull Integer, @NonNull String> getRegexes() {
        Multimap<@NonNull Integer, @NonNull String> regexes = super.getRegexes();
        if (regexes.containsKey(IFilterProperty.BOUND) && (fFilterRegex == null || fFilterRegex.isEmpty())) {
            regexes.removeAll(IFilterProperty.BOUND);
        }
        if (!regexes.containsKey(IFilterProperty.BOUND) && fFilterRegex != null && !fFilterRegex.isEmpty()) {
            regexes.put(IFilterProperty.BOUND, fFilterRegex);
        }
        return regexes;
    }

    /**
     * Set the regex to highlight on
     */
    public void setFilterRegex(String filterRegex) {
        fFilterRegex = filterRegex;
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
    protected StubPresentationProvider getPresentationProvider() {
        StubPresentationProvider presentationProvider = fPresentationProvider;
        if (presentationProvider == null && (super.getPresentationProvider() instanceof StubPresentationProvider)) {
            return (StubPresentationProvider) super.getPresentationProvider();
        }
        return presentationProvider;
    }

    /**
     * Set the presentation provider
     *
     * @param presentaitonProvider
     *            the presentation provider
     */
    public void setPresentationProvider(StubPresentationProvider presentaitonProvider) {
        fPresentationProvider = presentaitonProvider;
        getTimeGraphViewer().setTimeGraphProvider(presentaitonProvider);
        refresh();
    }

    @Override
    protected @Nullable List<@NonNull ITimeEvent> getEventList(@NonNull TimeGraphEntry entry, long startTime, long endTime, long resolution, @NonNull IProgressMonitor monitor) {
        ITmfTrace trace = getTrace();
        if (trace == null) {
            return Collections.emptyList();
        }
        List<@NonNull ITimeEvent> references = fEvents.get(entry.getName());
        List<@NonNull ITimeEvent> ret = new ArrayList<>();
        if (references != null) {
            for (ITimeEvent ref : references) {
                if (ref instanceof NullTimeEvent) {
                    ret.add(new NullTimeEvent(ref.getEntry(), ref.getTime() + trace.getStartTime().toNanos(), ref.getDuration()));
                } else if (ref instanceof TimeEvent) {
                    ret.add(new TimeEvent(ref.getEntry(), ref.getTime() + trace.getStartTime().toNanos(), ref.getDuration(), ((TimeEvent) ref).getValue()));
                }
            }
        }
        entry.setEventList(ret);
        return ret;
    }

    @Override
    protected @NonNull List<String> getMarkerCategories() {
        return Collections.singletonList(MARKER_GROUP);
    }

    @Override
    protected @Nullable List<@NonNull ILinkEvent> getLinkList(long startTime, long endTime, long resolution, @NonNull IProgressMonitor monitor) {
        ITmfTrace trace = getTrace();
        if (trace == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new TimeLinkEvent(fHead1, fRow2, 75 + trace.getStartTime().toNanos(), 15));
    }

    @Override
    protected @NonNull List<IMarkerEvent> getViewMarkerList(long startTime, long endTime, long resolution, @NonNull IProgressMonitor monitor) {
        ITmfTrace trace = getTrace();
        if (trace == null) {
            return Collections.emptyList();
        }
        long start = trace.getStartTime().toNanos();
        return Arrays.<IMarkerEvent> asList(
                (IMarkerEvent) new MarkerEvent(fHead1, 120 + start, 50, MARKER_GROUP, new RGBA(33, 33, 33, 33), "Wind", true),
                (IMarkerEvent) new MarkerEvent(fRow4, 20 + start, 60, MARKER_GROUP, new RGBA(22, 33, 44, 22), "Speed", false));
    }

    /**
     * Get window range, for testing
     *
     * @return the window range
     */
    public TmfTimeRange getWindowRange() {
        return new TmfTimeRange(TmfTimestamp.fromNanos(getTimeGraphViewer().getTime0()), TmfTimestamp.fromNanos(getTimeGraphViewer().getTime1()));
    }

}
