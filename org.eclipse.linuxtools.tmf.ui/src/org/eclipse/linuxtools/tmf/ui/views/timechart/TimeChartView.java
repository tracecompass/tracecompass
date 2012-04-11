/*******************************************************************************
 * Copyright (c) 2010 Ericsson
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
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.filter.ITmfFilter;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.editors.ITmfTraceEditor;
import org.eclipse.linuxtools.tmf.ui.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.ui.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.ui.viewers.events.ITmfEventsFilterListener;
import org.eclipse.linuxtools.tmf.ui.viewers.events.ITmfEventsFilterProvider;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.linuxtools.tmf.ui.views.colors.ColorSetting;
import org.eclipse.linuxtools.tmf.ui.views.colors.ColorSettingsManager;
import org.eclipse.linuxtools.tmf.ui.views.colors.IColorSettingsListener;
import org.eclipse.linuxtools.tmf.ui.views.timechart.TimeChartEvent.RankRange;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeAnalysisViewer;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITmfTimeScaleSelectionListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITmfTimeSelectionListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TmfTimeScaleSelectionEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TmfTimeSelectionEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TmfViewerFactory;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITmfTimeAnalysisEntry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;

public class TimeChartView extends TmfView implements ITmfTimeScaleSelectionListener, ITmfTimeSelectionListener, IColorSettingsListener,
        IResourceChangeListener, ITmfEventsFilterListener {

    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.timechart"; //$NON-NLS-1$

    private static final int TIMESTAMP_SCALE = -9;

    private final int fDisplayWidth;
    private Composite fComposite;
    private ITimeAnalysisViewer fViewer;
    private final ArrayList<TimeChartAnalysisEntry> fTimeAnalysisEntries = new ArrayList<TimeChartAnalysisEntry>();
    private final Map<ITmfTrace<?>, TimeChartDecorationProvider> fDecorationProviders = new HashMap<ITmfTrace<?>, TimeChartDecorationProvider>();
    private ArrayList<DecorateThread> fDecorateThreads;
    private long fStartTime = 0;
    private long fStopTime = Long.MAX_VALUE;
    private boolean fRefreshBusy = false;
    private boolean fRefreshPending = false;
    private final Object fSyncObj = new Object();

    public TimeChartView() {
        super("Time Chart"); //$NON-NLS-1$
        fDisplayWidth = Display.getDefault().getBounds().width;
    }

    @Override
    public void createPartControl(Composite parent) {
        fComposite = new Composite(parent, SWT.NONE);
        GridLayout gl = new GridLayout();
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        fComposite.setLayout(gl);

        fViewer = TmfViewerFactory.createViewer(fComposite, new TimeChartAnalysisProvider());
        fViewer.groupTraces(false);
        fViewer.setTimeCalendarFormat(true);
        fViewer.setAcceptSelectionAPIcalls(true);
        fViewer.addWidgetTimeScaleSelectionListner(this);
        fViewer.addWidgetSelectionListner(this);
        fViewer.setMinimumItemWidth(1);

        IEditorReference[] editorReferences = getSite().getPage().getEditorReferences();
        for (IEditorReference editorReference : editorReferences) {
            IEditorPart editor = editorReference.getEditor(false);
            if (editor instanceof ITmfTraceEditor) {
                ITmfTrace<?> trace = ((ITmfTraceEditor) editor).getTrace();
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
        fViewer.display(fTimeAnalysisEntries.toArray(new TimeChartAnalysisEntry[0]));

        fDecorateThreads = new ArrayList<DecorateThread>();
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
        super.setFocus();
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
        ITmfTrace<?> trace = timeAnalysisEntry.getTrace();
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
                    ITmfEvent event = trace.readNextEvent(context);
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
            refreshViewer(false);
        }
        if (context != null) {
            context.dispose();
        }
    }

    private void refreshViewer(boolean resetTimeIntervals) {
        if (fComposite == null) {
            return;
        }
        synchronized (fSyncObj) {
            if (fRefreshBusy) {
                fRefreshPending = true;
                return;
            } else {
                fRefreshBusy = true;
            }
        }
        final boolean reset = resetTimeIntervals;
        // Perform the refresh on the UI thread
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fComposite.isDisposed())
                    return;
                fViewer.display(fTimeAnalysisEntries.toArray(new TimeChartAnalysisEntry[0]));
                if (reset) {
                    fViewer.resetStartFinishTime();
                }
                synchronized (fSyncObj) {
                    fRefreshBusy = false;
                    if (fRefreshPending) {
                        fRefreshPending = false;
                        refreshViewer(reset);
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
        private final long fStartTime;
        private final long fStopTime;
        private final long fMaxDuration;

        private ItemizeThread(TimeChartAnalysisEntry timeAnalysisEntry, long startTime, long stopTime) {
            super("Itemize Thread:" + timeAnalysisEntry.getName()); //$NON-NLS-1$
            fTimeAnalysisEntry = timeAnalysisEntry;
            fStartTime = startTime;
            fStopTime = stopTime;
            fMaxDuration = 3 * (fStopTime - fStartTime) / fDisplayWidth;
        }

        @Override
        public void run() {
            itemizeTraceEntry(fTimeAnalysisEntry);
        }

        public void itemizeTraceEntry(TimeChartAnalysisEntry timeAnalysisEntry) {
            Iterator<ITimeEvent> iterator = timeAnalysisEntry.getTraceEventsIterator();
            TimeChartEvent event = null;
            boolean hasNext = true;
            while (hasNext) {
                synchronized (timeAnalysisEntry) {
                    while (hasNext = iterator.hasNext()) {
                        event = (TimeChartEvent) iterator.next();
                        if (event.getTime() + event.getDuration() > fStartTime && event.getTime() < fStopTime && event.getDuration() > fMaxDuration
                                && event.getNbEvents() > 1) {
                            break;
                        }
                    }
                }
                if (hasNext) {
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
            refreshViewer(false);
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
            refreshViewer(false);
            decorateTraceEntry(fTimeAnalysisEntry, null);
            refreshViewer(false);
            synchronized (fDecorateThreads) {
                fDecorateThreads.remove(this);
            }
            if (fContext != null) {
            	fContext.dispose();
            }
        }

        public void resetTraceEntry(TimeChartAnalysisEntry timeAnalysisEntry) {
            Iterator<ITimeEvent> iterator = timeAnalysisEntry.getTraceEventsIterator();
            TimeChartEvent event = null;
            boolean hasNext = true;
            while (!interrupted && hasNext) {
                synchronized (timeAnalysisEntry) {
                    while (hasNext = iterator.hasNext()) {
                        event = (TimeChartEvent) iterator.next();
                        break;
                    }
                }
                if (hasNext) {
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
            Iterator<ITimeEvent> iterator = timeAnalysisEntry.getTraceEventsIterator(0, Long.MAX_VALUE, Long.MAX_VALUE);
            TimeChartEvent event = null;
            int entryPriority = ColorSettingsManager.PRIORITY_NONE;
            boolean entryIsBookmarked = false;
            boolean entryIsVisible = false;
            boolean entryIsSearchMatch = false;
            boolean hasNext = true;
            while (!interrupted && hasNext) {
                synchronized (timeAnalysisEntry) {
                    while (hasNext = iterator.hasNext()) {
                        event = (TimeChartEvent) iterator.next();
                        break;
                    }
                }
                if (hasNext) {
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
                        refreshViewer(false);
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
            ITmfTrace<?> trace = timeAnalysisEntry.getTrace();
            int priority = ColorSettingsManager.PRIORITY_NONE;
            boolean isBookmarked = false;
            boolean isVisible = false;
            boolean isSearchMatch = false;
            synchronized (timeChartEvent.getRankRangeList()) {
                for (RankRange range : timeChartEvent.getRankRangeList()) {
                    if (interrupted)
                        return;
                    if (fContext == null || fContext.getRank() != range.getFirstRank()) {
                        if (fContext != null) {
                        	fContext.dispose();
                        }
                        fContext = trace.seekEvent(range.getFirstRank());
                        fContext.setRank(range.getFirstRank());
                    }
                    while (true) {
                        if (interrupted)
                            return;
                        long rank = fContext.getRank();
                        ITmfEvent event = trace.readNextEvent(fContext);
                        if (event == null) {
                            break;
                        }
                        long eventTime = event.getTimestamp().normalize(0, -9).getValue();
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
    public void tsfTmProcessTimeScaleEvent(TmfTimeScaleSelectionEvent event) {
        fStartTime = event.getTime0();
        fStopTime = event.getTime1();
        itemize(fStartTime, fStopTime);
    }

    @Override
    public void tsfTmProcessSelEvent(TmfTimeSelectionEvent event) {
        ITmfTimeAnalysisEntry timeAnalysisEntry = null;
        if (event.getSelection() instanceof TimeChartAnalysisEntry) {
            timeAnalysisEntry = (TimeChartAnalysisEntry) event.getSelection();
        } else if (event.getSelection() instanceof TimeChartEvent) {
            timeAnalysisEntry = ((TimeChartEvent) event.getSelection()).getEntry();
        }
        if (timeAnalysisEntry instanceof TimeChartAnalysisEntry) {
            broadcast(new TmfTraceSelectedSignal(this, ((TimeChartAnalysisEntry) timeAnalysisEntry).getTrace()));
        }
        broadcast(new TmfTimeSynchSignal(this, new TmfTimestamp(event.getSelectedTime(), TIMESTAMP_SCALE)));
    }

    @Override
    public void colorSettingsChanged(ColorSetting[] colorSettings) {
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

    @Override
    public void filterApplied(ITmfFilter filter, ITmfTrace<?> trace) {
        TimeChartDecorationProvider decorationProvider = fDecorationProviders.get(trace);
        decorationProvider.filterApplied(filter);
        redecorate();
    }

    @Override
    public void searchApplied(ITmfFilter filter, ITmfTrace<?> trace) {
        TimeChartDecorationProvider decorationProvider = fDecorationProviders.get(trace);
        decorationProvider.searchApplied(filter);
        redecorate();
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        if (fTimeAnalysisEntries == null)
            return;
        final ITmfTrace<?> trace = signal.getTrace();
        final IFile bookmarksFile = signal.getBookmarksFile();
        final ITmfEventsFilterProvider eventsFilterProvider = signal.getEventsFilterProvider();
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
        refreshViewer(true);
        if (eventsFilterProvider != null) {
            eventsFilterProvider.addEventsFilterListener(this);
        }
    }

    @TmfSignalHandler
    public void traceClosed(TmfTraceClosedSignal signal) {
        if (fTimeAnalysisEntries == null)
            return;
        final ITmfTrace<?> trace = signal.getTrace();
        for (int i = 0; i < fTimeAnalysisEntries.size(); i++) {
            if (fTimeAnalysisEntries.get(i).getTrace().equals(trace)) {
                fTimeAnalysisEntries.remove(i);
                fDecorationProviders.remove(trace);
                refreshViewer(true);
                break;
            }
        }
    }

    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        if (signal.getSource() != this && fTimeAnalysisEntries != null) {
            ITmfTrace<?> trace = signal.getTrace();
            for (int i = 0; i < fTimeAnalysisEntries.size(); i++) {
                if (fTimeAnalysisEntries.get(i).getTrace().equals(trace)) {
                    fViewer.setSelectedTrace(fTimeAnalysisEntries.get(i));
                    break;
                }
            }
        }
    }

    @TmfSignalHandler
    public void traceUpdated(TmfTraceUpdatedSignal signal) {
        if (fTimeAnalysisEntries == null)
            return;
        final ITmfTrace<?> trace = signal.getTrace();
        for (int i = 0; i < fTimeAnalysisEntries.size(); i++) {
            TimeChartAnalysisEntry timeAnalysisEntry = fTimeAnalysisEntries.get(i);
            if (timeAnalysisEntry.getTrace().equals(trace)) {
                updateTraceEntry(timeAnalysisEntry, Long.MAX_VALUE, 0, Long.MAX_VALUE);
                break;
            }
        }
    }

    @TmfSignalHandler
    public void currentTimeUpdated(TmfTimeSynchSignal signal) {
        long time = signal.getCurrentTime().normalize(0, TIMESTAMP_SCALE).getValue();
        fViewer.setSelectedTime(time, true, this);
    }

}
