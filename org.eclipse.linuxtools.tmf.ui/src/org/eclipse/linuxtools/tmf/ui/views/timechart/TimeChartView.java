/*******************************************************************************
 * Copyright (c) 2010, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.timechart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.signal.TmfEventFilterAppliedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfEventSearchAppliedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.editors.ITmfTraceEditor;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.linuxtools.tmf.ui.views.colors.ColorSetting;
import org.eclipse.linuxtools.tmf.ui.views.colors.ColorSettingsManager;
import org.eclipse.linuxtools.tmf.ui.views.colors.IColorSettingsListener;
import org.eclipse.linuxtools.tmf.ui.views.timechart.TimeChartEvent.RankRange;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphRangeListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphSelectionListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphTimeListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphRangeUpdateEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphSelectionEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphTimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphViewer;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils.TimeFormat;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;

/**
 * Generic Time Chart view, which is similar to a Gantt chart for trace analysis
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TimeChartView extends TmfView implements ITimeGraphRangeListener, ITimeGraphSelectionListener, ITimeGraphTimeListener, IColorSettingsListener, IResourceChangeListener {

    /** TimeChartView's ID */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.timechart"; //$NON-NLS-1$

    private static final int TIMESTAMP_SCALE = -9;

    private final int fDisplayWidth;
    private TimeGraphViewer fViewer;
    private final ArrayList<TimeChartAnalysisEntry> fTimeAnalysisEntries = new ArrayList<TimeChartAnalysisEntry>();
    private final Map<ITmfTrace, TimeChartDecorationProvider> fDecorationProviders = new HashMap<ITmfTrace, TimeChartDecorationProvider>();
    private final ArrayList<DecorateThread> fDecorateThreads = new ArrayList<DecorateThread>();
    private long fStartTime = 0;
    private long fStopTime = Long.MAX_VALUE;
    private boolean fRefreshBusy = false;
    private boolean fRefreshPending = false;
    private boolean fRedrawBusy = false;
    private boolean fRedrawPending = false;
    private final Object fSyncObj = new Object();
    private ITimeGraphPresentationProvider fPresentationProvider;

    /**
     * Default constructor
     */
    public TimeChartView() {
        super("Time Chart"); //$NON-NLS-1$
        fDisplayWidth = Display.getDefault().getBounds().width;
    }

    @Override
    public void createPartControl(Composite parent) {
        fViewer = new TimeGraphViewer(parent, SWT.NONE);
        fPresentationProvider = new TimeChartAnalysisProvider();
        fViewer.setTimeGraphProvider(fPresentationProvider);
        fViewer.setTimeFormat(TimeFormat.CALENDAR);
        fViewer.addTimeListener(this);
        fViewer.addRangeListener(this);
        fViewer.addSelectionListener(this);
        fViewer.setMinimumItemWidth(1);

        IEditorReference[] editorReferences = getSite().getPage().getEditorReferences();
        for (IEditorReference editorReference : editorReferences) {
            IEditorPart editor = editorReference.getEditor(false);
            if (editor instanceof ITmfTraceEditor) {
                ITmfTrace trace = ((ITmfTraceEditor) editor).getTrace();
                if (trace != null) {
                    IFile bookmarksFile = ((ITmfTraceEditor) editor).getBookmarksFile();
                    TimeChartAnalysisEntry timeAnalysisEntry = new TimeChartAnalysisEntry(trace, fDisplayWidth * 2);
                    fTimeAnalysisEntries.add(timeAnalysisEntry);
                    fDecorationProviders.put(trace, new TimeChartDecorationProvider(bookmarksFile));
                    Thread thread = new ProcessTraceThread(timeAnalysisEntry);
                    thread.start();
                }
            }
        }
        fViewer.setInput(fTimeAnalysisEntries.toArray(new TimeChartAnalysisEntry[0]));

        ColorSettingsManager.addColorSettingsListener(this);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
    }

    @Override
    public void dispose() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        for (DecorateThread thread : fDecorateThreads) {
            thread.cancel();
        }
        ColorSettingsManager.removeColorSettingsListener(this);
        super.dispose();
    }

    @Override
    public void setFocus() {
        fViewer.setFocus();
    }

    private class ProcessTraceThread extends Thread {

        private final TimeChartAnalysisEntry fTimeAnalysisEntry;

        public ProcessTraceThread(TimeChartAnalysisEntry timeAnalysisEntry) {
            super("ProcessTraceJob:" + timeAnalysisEntry.getName()); //$NON-NLS-1$
            fTimeAnalysisEntry = timeAnalysisEntry;
        }

        @Override
        public void run() {
            updateTraceEntry(fTimeAnalysisEntry, Long.MAX_VALUE, 0, Long.MAX_VALUE);
        }
    }

    private void updateTraceEntry(TimeChartAnalysisEntry timeAnalysisEntry, long stopRank, long startTime, long stopTime) {
        ITmfTrace trace = timeAnalysisEntry.getTrace();
        TimeChartDecorationProvider decorationProvider = fDecorationProviders.get(trace);
        if (decorationProvider == null) {
            return; // the trace has been closed
        }
        ITmfContext context = null;
        // TmfTimestamp lastTimestamp = null;
        boolean done = false;
        while (!done) {
            synchronized (timeAnalysisEntry) {
                if (timeAnalysisEntry.getLastRank() >= trace.getNbEvents()) {
                    done = true;
                    break;
                }
                if (context == null || context.getRank() != timeAnalysisEntry.getLastRank()) {
                    if (context != null) {
                        context.dispose();
                    }
                    if (timeAnalysisEntry.getLastRank() != -1) {
                        context = trace.seekEvent(timeAnalysisEntry.getLastRank());
                    } else {
                        // context = trace.seekLocation(null);
                        context = trace.seekEvent(0);
                    }
                }
                while (true) {
                    long rank = context.getRank();
                    ITmfEvent event = trace.getNext(context);
                    if (event == null) {
                        done = true;
                        break;
                    }
                    // if (!event.getTimestamp().equals(lastTimestamp)) {
                    TimeChartEvent timeEvent = new TimeChartEvent(timeAnalysisEntry, event, rank, decorationProvider);
                    if (timeEvent.getTime() >= startTime && timeEvent.getTime() <= stopTime) {
                        timeAnalysisEntry.addTraceEvent(timeEvent);
                    }
                    // lastTimestamp = event.getTimestamp();
                    // } *** commented out so that color setting priority gets
                    // set even if the event has same time
                    if (context.getRank() == trace.getNbEvents() || context.getRank() == stopRank) {
                        done = true;
                        break;
                    }
                    if (context.getRank() % trace.getCacheSize() == 1) {
                        // break for UI refresh
                        break;
                    }
                }
                // timeAnalysisEntry.setLastRank(Math.min(trace.getNbEvents(),
                // stopRank));
                timeAnalysisEntry.setLastRank(context.getRank());
            }
            redrawViewer(true);
        }
        if (context != null) {
            context.dispose();
        }
    }

    private void refreshViewer() {
        synchronized (fSyncObj) {
            if (fRefreshBusy) {
                fRefreshPending = true;
                return;
            }
            fRefreshBusy = true;
        }
        // Perform the refresh on the UI thread
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fViewer.getControl().isDisposed()) {
                    return;
                }
                fViewer.setInput(fTimeAnalysisEntries.toArray(new TimeChartAnalysisEntry[0]));
                fViewer.resetStartFinishTime();
                synchronized (fSyncObj) {
                    fRefreshBusy = false;
                    if (fRefreshPending) {
                        fRefreshPending = false;
                        refreshViewer();
                    }
                }
            }
        });
    }

    private void redrawViewer(boolean resetTimeIntervals) {
        synchronized (fSyncObj) {
            if (fRedrawBusy) {
                fRedrawPending = true;
                return;
            }
            fRedrawBusy = true;
        }
        final boolean reset = resetTimeIntervals;
        // Perform the refresh on the UI thread
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fViewer.getControl().isDisposed()) {
                    return;
                }
                if (reset) {
                    fViewer.setTimeRange(fTimeAnalysisEntries.toArray(new TimeChartAnalysisEntry[0]));
                    fViewer.setTimeBounds();
                }
                fViewer.getControl().redraw();
                fViewer.getControl().update();
                synchronized (fSyncObj) {
                    fRedrawBusy = false;
                    if (fRedrawPending) {
                        fRedrawPending = false;
                        redrawViewer(reset);
                    }
                }
            }
        });
    }

    private void itemize(long startTime, long stopTime) {
        for (int i = 0; i < fTimeAnalysisEntries.size(); i++) {
            Thread thread = new ItemizeThread(fTimeAnalysisEntries.get(i), startTime, stopTime);
            thread.start();
        }
    }

    private class ItemizeThread extends Thread {

        private final TimeChartAnalysisEntry fTimeAnalysisEntry;
        private final long startTime;
        private final long stopTime;
        private final long fMaxDuration;

        private ItemizeThread(TimeChartAnalysisEntry timeAnalysisEntry, long startTime, long stopTime) {
            super("Itemize Thread:" + timeAnalysisEntry.getName()); //$NON-NLS-1$
            fTimeAnalysisEntry = timeAnalysisEntry;
            this.startTime = startTime;
            this.stopTime = stopTime;
            fMaxDuration = 3 * (stopTime - startTime) / fDisplayWidth;
        }

        @Override
        public void run() {
            itemizeTraceEntry(fTimeAnalysisEntry);
        }

        public void itemizeTraceEntry(TimeChartAnalysisEntry timeAnalysisEntry) {
            Iterator<ITimeEvent> iterator = timeAnalysisEntry.getTimeEventsIterator();
            TimeChartEvent event = null;
            boolean hasNext = true;
            while (hasNext) {
                synchronized (timeAnalysisEntry) {
                    while ((hasNext = iterator.hasNext()) == true) {
                        event = (TimeChartEvent) iterator.next();
                        if (event.getTime() + event.getDuration() > startTime && event.getTime() < stopTime && event.getDuration() > fMaxDuration
                                && event.getNbEvents() > 1) {
                            break;
                        }
                    }
                }
                if (hasNext && event != null) {
                    if (event.getItemizedEntry() == null) {
                        itemizeEvent(event);
                    } else {
                        itemizeTraceEntry(event.getItemizedEntry());
                    }
                }
            }
        }

        public void itemizeEvent(TimeChartEvent event) {
            synchronized (event) {
                if (event.isItemizing()) {
                    return;
                }
                event.setItemizing(true);
            }
            TimeChartAnalysisEntry timeAnalysisEntry = new TimeChartAnalysisEntry(fTimeAnalysisEntry.getTrace(), (int) Math.min(
                    event.getNbEvents() + 1, fDisplayWidth * 2));
            synchronized (event.getRankRangeList()) {
                for (RankRange range : event.getRankRangeList()) {
                    timeAnalysisEntry.setLastRank(range.getFirstRank());
                    updateTraceEntry(timeAnalysisEntry, range.getLastRank() + 1, event.getTime(), event.getTime() + event.getDuration());
                }
            }
            event.setItemizedEntry(timeAnalysisEntry);
            redrawViewer(false);
            itemizeTraceEntry(timeAnalysisEntry);
            synchronized (event) {
                event.setItemizing(false);
            }
        }
    }

    private void redecorate() {
        synchronized (fDecorateThreads) {
            for (DecorateThread thread : fDecorateThreads) {
                thread.cancel();
            }
            fDecorateThreads.clear();
            for (int i = 0; i < fTimeAnalysisEntries.size(); i++) {
                DecorateThread thread = new DecorateThread(fTimeAnalysisEntries.get(i));
                thread.start();
                fDecorateThreads.add(thread);
            }
        }
    }

    private class DecorateThread extends Thread {
        private volatile boolean interrupted = false;
        private final TimeChartAnalysisEntry fTimeAnalysisEntry;
        private final TimeChartDecorationProvider fDecorationProvider;
        private ITmfContext fContext;
        private int fCount = 0;

        private DecorateThread(TimeChartAnalysisEntry timeAnalysisEntry) {
            super("Decorate Thread:" + timeAnalysisEntry.getName()); //$NON-NLS-1$
            fTimeAnalysisEntry = timeAnalysisEntry;
            fDecorationProvider = fDecorationProviders.get(timeAnalysisEntry.getTrace());
        }

        @Override
        public void run() {
            resetTraceEntry(fTimeAnalysisEntry);
            redrawViewer(false);
            decorateTraceEntry(fTimeAnalysisEntry, null);
            redrawViewer(false);
            synchronized (fDecorateThreads) {
                fDecorateThreads.remove(this);
            }
            if (fContext != null) {
                fContext.dispose();
            }
        }

        public void resetTraceEntry(TimeChartAnalysisEntry timeAnalysisEntry) {
            Iterator<ITimeEvent> iterator = timeAnalysisEntry.getTimeEventsIterator();
            TimeChartEvent event = null;
            boolean hasNext = true;
            while (!interrupted && hasNext) {
                synchronized (timeAnalysisEntry) {
                    while ((hasNext = iterator.hasNext()) == true) {
                        event = (TimeChartEvent) iterator.next();
                        break;
                    }
                }
                if (hasNext && event != null) {
                    // TODO possible concurrency problem here with ItemizeJob
                    event.setColorSettingPriority(ColorSettingsManager.PRIORITY_NONE);
                    if (event.getItemizedEntry() != null) {
                        resetTraceEntry(event.getItemizedEntry());
                    }
                }
            }
        }

        public void decorateTraceEntry(TimeChartAnalysisEntry timeAnalysisEntry, TimeChartEvent parentEvent) {
            // Set max duration high to ensure iterator does not consider
            // itemized events
            Iterator<ITimeEvent> iterator = timeAnalysisEntry.getTimeEventsIterator(0, Long.MAX_VALUE, Long.MAX_VALUE);
            TimeChartEvent event = null;
            int entryPriority = ColorSettingsManager.PRIORITY_NONE;
            boolean entryIsBookmarked = false;
            boolean entryIsVisible = false;
            boolean entryIsSearchMatch = false;
            boolean hasNext = true;
            while (!interrupted && hasNext) {
                synchronized (timeAnalysisEntry) {
                    while ((hasNext = iterator.hasNext()) == true) {
                        event = (TimeChartEvent) iterator.next();
                        break;
                    }
                }
                if (hasNext && event != null) {
                    // TODO possible concurrency problem here with ItemizeJob
                    if (event.getItemizedEntry() == null) {
                        decorateEvent(event);
                    } else {
                        decorateTraceEntry(event.getItemizedEntry(), event);
                    }
                    entryPriority = Math.min(entryPriority, event.getColorSettingPriority());
                    entryIsBookmarked |= event.isBookmarked();
                    entryIsVisible |= event.isVisible();
                    entryIsSearchMatch |= event.isSearchMatch();
                    if (++fCount % timeAnalysisEntry.getTrace().getCacheSize() == 0) {
                        redrawViewer(false);
                    }
                }
            }
            if (parentEvent != null) {
                parentEvent.setColorSettingPriority(entryPriority);
                parentEvent.setIsBookmarked(entryIsBookmarked);
                parentEvent.setIsVisible(entryIsVisible);
                parentEvent.setIsSearchMatch(entryIsSearchMatch);
            }
        }

        public void decorateEvent(TimeChartEvent timeChartEvent) {
            // TODO possible concurrency problem here with ItemizeJob
            TimeChartAnalysisEntry timeAnalysisEntry = (TimeChartAnalysisEntry) timeChartEvent.getEntry();
            ITmfTrace trace = timeAnalysisEntry.getTrace();
            int priority = ColorSettingsManager.PRIORITY_NONE;
            boolean isBookmarked = false;
            boolean isVisible = false;
            boolean isSearchMatch = false;
            synchronized (timeChartEvent.getRankRangeList()) {
                for (RankRange range : timeChartEvent.getRankRangeList()) {
                    if (interrupted) {
                        return;
                    }
                    if (fContext == null || fContext.getRank() != range.getFirstRank()) {
                        if (fContext != null) {
                            fContext.dispose();
                        }
                        fContext = trace.seekEvent(range.getFirstRank());
                        fContext.setRank(range.getFirstRank());
                    }
                    while (true) {
                        if (interrupted) {
                            return;
                        }
                        long rank = fContext.getRank();
                        ITmfEvent event = trace.getNext(fContext);
                        if (event == null) {
                            break;
                        }
                        long eventTime = event.getTimestamp().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                        if (eventTime >= timeChartEvent.getTime() && eventTime <= timeChartEvent.getTime() + timeChartEvent.getDuration()) {
                            priority = Math.min(priority, ColorSettingsManager.getColorSettingPriority(event));
                        }
                        isBookmarked |= fDecorationProvider.isBookmark(rank);
                        isVisible |= fDecorationProvider.isVisible(event);
                        isSearchMatch |= fDecorationProvider.isSearchMatch(event);
                        if (fContext.getRank() > range.getLastRank()) {
                            break;
                        }
                    }
                }
            }
            timeChartEvent.setColorSettingPriority(priority);
            timeChartEvent.setIsBookmarked(isBookmarked);
            timeChartEvent.setIsVisible(isVisible);
            timeChartEvent.setIsSearchMatch(isSearchMatch);
        }

        public void cancel() {
            interrupted = true;
        }
    }

    // ------------------------------------------------------------------------
    // Listeners
    // ------------------------------------------------------------------------

    @Override
    public void timeRangeUpdated(TimeGraphRangeUpdateEvent event) {
        fStartTime = event.getStartTime();
        fStopTime = event.getEndTime();
        itemize(fStartTime, fStopTime);
        final ITmfTimestamp startTimestamp = new TmfTimestamp(event.getStartTime(), ITmfTimestamp.NANOSECOND_SCALE);
        final ITmfTimestamp endTimestamp = new TmfTimestamp(event.getEndTime(), ITmfTimestamp.NANOSECOND_SCALE);
        TmfTimeRange range = new TmfTimeRange(startTimestamp, endTimestamp);
        TmfTimestamp timestamp = new TmfTimestamp(fViewer.getSelectedTime(), ITmfTimestamp.NANOSECOND_SCALE);
        broadcast(new TmfRangeSynchSignal(this, range, timestamp));
    }

    @Override
    public void selectionChanged(TimeGraphSelectionEvent event) {
        ITimeGraphEntry timeAnalysisEntry = null;
        if (event.getSelection() instanceof TimeChartAnalysisEntry) {
            timeAnalysisEntry = event.getSelection();
        } else if (event.getSelection() instanceof TimeChartEvent) {
            timeAnalysisEntry = ((TimeChartEvent) event.getSelection()).getEntry();
        }
        if (timeAnalysisEntry instanceof TimeChartAnalysisEntry) {
            broadcast(new TmfTraceSelectedSignal(this, ((TimeChartAnalysisEntry) timeAnalysisEntry).getTrace()));
        }
    }

    @Override
    public void timeSelected(TimeGraphTimeEvent event) {
        broadcast(new TmfTimeSynchSignal(this, new TmfTimestamp(event.getTime(), TIMESTAMP_SCALE)));
    }

    @Override
    public void colorSettingsChanged(ColorSetting[] colorSettings) {
        // Set presentation provider again to trigger re-creation of new color settings which are stored
        // in the TimeGraphControl class
        fViewer.setTimeGraphProvider(fPresentationProvider);
        redecorate();
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        for (IMarkerDelta delta : event.findMarkerDeltas(IMarker.BOOKMARK, false)) {
            for (TimeChartDecorationProvider provider : fDecorationProviders.values()) {
                if (delta.getResource().equals(provider.getBookmarksFile())) {
                    if (delta.getKind() == IResourceDelta.CHANGED && delta.getMarker().getAttribute(IMarker.LOCATION, -1) != -1) {
                        provider.refreshBookmarks();
                    } else if (delta.getKind() == IResourceDelta.REMOVED) {
                        provider.refreshBookmarks();
                    }
                }
            }
        }
        redecorate();
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Handler for the Trace Opened signal
     *
     * @param signal
     *            The incoming signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        final ITmfTrace trace = signal.getTrace();
        final IFile bookmarksFile = signal.getBookmarksFile();
        TimeChartAnalysisEntry timeAnalysisEntry = null;
        for (int i = 0; i < fTimeAnalysisEntries.size(); i++) {
            if (fTimeAnalysisEntries.get(i).getTrace().equals(trace)) {
                timeAnalysisEntry = fTimeAnalysisEntries.get(i);
                break;
            }
        }
        if (timeAnalysisEntry == null) {
            timeAnalysisEntry = new TimeChartAnalysisEntry(trace, fDisplayWidth * 2);
            fTimeAnalysisEntries.add(timeAnalysisEntry);
            fDecorationProviders.put(trace, new TimeChartDecorationProvider(bookmarksFile));
            Thread thread = new ProcessTraceThread(timeAnalysisEntry);
            thread.start();
        }
        refreshViewer();
    }

    /**
     * Handler for the Trace Closed signal
     *
     * @param signal
     *            The incoming signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void traceClosed(TmfTraceClosedSignal signal) {
        final ITmfTrace trace = signal.getTrace();
        for (int i = 0; i < fTimeAnalysisEntries.size(); i++) {
            if (fTimeAnalysisEntries.get(i).getTrace().equals(trace)) {
                fTimeAnalysisEntries.remove(i);
                fDecorationProviders.remove(trace);
                synchronized (fDecorateThreads) {
                    for (DecorateThread thread : fDecorateThreads) {
                        if (thread.fTimeAnalysisEntry.getTrace() == trace) {
                            thread.cancel();
                            fDecorateThreads.remove(thread);
                            break;
                        }
                    }
                }
                refreshViewer();
                break;
            }
        }
    }

    /**
     * Handler for the Trace Selected signal
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        if (signal.getSource() != this) {
            ITmfTrace trace = signal.getTrace();
            for (int i = 0; i < fTimeAnalysisEntries.size(); i++) {
                if (fTimeAnalysisEntries.get(i).getTrace().equals(trace)) {
                    fViewer.setSelection(fTimeAnalysisEntries.get(i));
                    break;
                }
            }
            fViewer.setSelectedTime(fTraceManager.getCurrentTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue(), false);
        }
    }

    /**
     * Handler for the Trace Updated signal
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public void traceUpdated(TmfTraceUpdatedSignal signal) {
        final ITmfTrace trace = signal.getTrace();
        for (int i = 0; i < fTimeAnalysisEntries.size(); i++) {
            TimeChartAnalysisEntry timeAnalysisEntry = fTimeAnalysisEntries.get(i);
            if (timeAnalysisEntry.getTrace().equals(trace)) {
                updateTraceEntry(timeAnalysisEntry, Long.MAX_VALUE, 0, Long.MAX_VALUE);
                break;
            }
        }
    }

    /**
     * Handler for the Time Synch signal
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public void currentTimeUpdated(TmfTimeSynchSignal signal) {
        final long time = signal.getCurrentTime().normalize(0, TIMESTAMP_SCALE).getValue();
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                fViewer.setSelectedTime(time, true);
            }
        });
    }

    /**
     * Handler for the Time Range Synch signal
     *
     * @param signal
     *            The incoming signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void synchToRange(final TmfRangeSynchSignal signal) {
        if (signal.getSource() == this) {
            return;
        }
        final long startTime = signal.getCurrentRange().getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        final long endTime = signal.getCurrentRange().getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        final long time = signal.getCurrentTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                fStartTime = startTime;
                fStopTime = endTime;
                itemize(fStartTime, fStopTime);
                fViewer.setStartFinishTime(startTime, endTime);
                fViewer.setSelectedTime(time, false);
            }
        });
    }

    /**
     * Handler for the Event Filter Applied signal
     *
     * @param signal
     *            The incoming signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void filterApplied(TmfEventFilterAppliedSignal signal) {
        TimeChartDecorationProvider decorationProvider = fDecorationProviders.get(signal.getTrace());
        if (decorationProvider == null) {
            return;
        }
        decorationProvider.filterApplied(signal.getEventFilter());
        redecorate();
    }

    /**
     * Handler for the Event Search Applied signal
     *
     * @param signal
     *            The incoming signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void searchApplied(TmfEventSearchAppliedSignal signal) {
        TimeChartDecorationProvider decorationProvider = fDecorationProviders.get(signal.getTrace());
        if (decorationProvider == null) {
            return;
        }
        decorationProvider.searchApplied(signal.getSearchFilter());
        redecorate();
    }

}
