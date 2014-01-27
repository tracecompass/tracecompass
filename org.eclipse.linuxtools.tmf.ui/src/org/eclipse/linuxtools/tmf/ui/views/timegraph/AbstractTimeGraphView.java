/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Bernd Hufmann - Updated signal handling
 *   Geneviève Bastien - Move code to provide base classes for time graph view
 *   Marc-Andre Laperle - Add time zone preference
 *   Geneviève Bastien - Add event links between entries
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.timegraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimestampFormatUpdateSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfNanoTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphContentProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphRangeListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphSelectionListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphTimeListener;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphCombo;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphRangeUpdateEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphSelectionEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphTimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphViewer;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ILinkEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils.TimeFormat;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IActionBars;

/**
 * An abstract view all time graph views can inherit
 *
 * This view contains either a time graph viewer, or a time graph combo which is
 * divided between a tree viewer on the left and a time graph viewer on the right.
 *
 * @since 2.1
 */
public abstract class AbstractTimeGraphView extends TmfView {

    /**
     * Redraw state enum
     */
    private enum State {
        IDLE, BUSY, PENDING
    }

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    /** The timegraph wrapper */
    private ITimeGraphWrapper fTimeGraphWrapper;

    /** The selected trace */
    private ITmfTrace fTrace;

    /** The timegraph entry list */
    private List<TimeGraphEntry> fEntryList;

    /** The trace to entry list hash map */
    private final Map<ITmfTrace, List<TimeGraphEntry>> fEntryListMap = new HashMap<>();

    /** The trace to build thread hash map */
    private final Map<ITmfTrace, BuildThread> fBuildThreadMap = new HashMap<>();

    /** The start time */
    private long fStartTime;

    /** The end time */
    private long fEndTime;

    /** The display width */
    private final int fDisplayWidth;

    /** The zoom thread */
    private ZoomThread fZoomThread;

    /** The next resource action */
    private Action fNextResourceAction;

    /** The previous resource action */
    private Action fPreviousResourceAction;

    /** A comparator class */
    private Comparator<ITimeGraphEntry> fEntryComparator = null;

    /** The redraw state used to prevent unnecessary queuing of display runnables */
    private State fRedrawState = State.IDLE;

    /** The redraw synchronization object */
    private final Object fSyncObj = new Object();

    /** The presentation provider for this view */
    private final TimeGraphPresentationProvider fPresentation;

    /** The tree column label array, or null if combo is not used */
    private String[] fColumns;

    /** The tree label provider, or null if combo is not used */
    private TreeLabelProvider fLabelProvider = null;

    /** The relative weight of the sash, ignored if combo is not used */
    private int[] fWeight = { 1, 1 };

    /** The filter column label array, or null if filter is not used */
    private String[] fFilterColumns;

    /** The pack done flag */
    private boolean fPackDone = false;

    /** The filter label provider, or null if filter is not used */
    private TreeLabelProvider fFilterLabelProvider;

    // ------------------------------------------------------------------------
    // Classes
    // ------------------------------------------------------------------------

    private interface ITimeGraphWrapper {

        void setTimeGraphProvider(TimeGraphPresentationProvider fPresentation);

        TimeGraphViewer getTimeGraphViewer();

        void addSelectionListener(ITimeGraphSelectionListener iTimeGraphSelectionListener);

        ISelectionProvider getSelectionProvider();

        void setFocus();

        boolean isDisposed();

        void refresh();

        void setInput(Object input);

        Object getInput();

        void redraw();

        void update();

    }

    private class TimeGraphViewerWrapper implements ITimeGraphWrapper {
        private TimeGraphViewer viewer;

        private TimeGraphViewerWrapper(Composite parent, int style) {
            viewer = new TimeGraphViewer(parent, style);
        }

        @Override
        public void setTimeGraphProvider(TimeGraphPresentationProvider timeGraphProvider) {
            viewer.setTimeGraphProvider(timeGraphProvider);
        }

        @Override
        public TimeGraphViewer getTimeGraphViewer() {
            return viewer;
        }

        @Override
        public void addSelectionListener(ITimeGraphSelectionListener listener) {
            viewer.addSelectionListener(listener);
        }

        @Override
        public ISelectionProvider getSelectionProvider() {
            return viewer.getSelectionProvider();
        }

        @Override
        public void setFocus() {
            viewer.setFocus();
        }

        @Override
        public boolean isDisposed() {
            return viewer.getControl().isDisposed();
        }

        @Override
        public void setInput(Object input) {
            viewer.setInput(input);
        }

        @Override
        public Object getInput() {
            return viewer.getInput();
        }

        @Override
        public void refresh() {
            viewer.refresh();
        }

        @Override
        public void redraw() {
            viewer.getControl().redraw();
        }

        @Override
        public void update() {
            viewer.getControl().update();
        }
    }

    private class TimeGraphComboWrapper implements ITimeGraphWrapper {
        private TimeGraphCombo combo;

        private TimeGraphComboWrapper(Composite parent, int style) {
            combo = new TimeGraphCombo(parent, style, fWeight);
        }

        @Override
        public void setTimeGraphProvider(TimeGraphPresentationProvider timeGraphProvider) {
            combo.setTimeGraphProvider(timeGraphProvider);
        }

        @Override
        public TimeGraphViewer getTimeGraphViewer() {
            return combo.getTimeGraphViewer();
        }

        @Override
        public void addSelectionListener(ITimeGraphSelectionListener listener) {
            combo.addSelectionListener(listener);
        }

        @Override
        public ISelectionProvider getSelectionProvider() {
            return combo.getTreeViewer();
        }

        @Override
        public void setFocus() {
            combo.setFocus();
        }

        @Override
        public boolean isDisposed() {
            return combo.isDisposed();
        }

        @Override
        public void setInput(Object input) {
            combo.setInput(input);
        }

        @Override
        public Object getInput() {
            return combo.getInput();
        }

        @Override
        public void refresh() {
            combo.refresh();
        }

        @Override
        public void redraw() {
            combo.redraw();
        }

        @Override
        public void update() {
            combo.update();
        }

        TimeGraphCombo getTimeGraphCombo() {
            return combo;
        }

        TreeViewer getTreeViewer() {
            return combo.getTreeViewer();
        }

        IAction getShowFilterAction() {
            return combo.getShowFilterAction();
        }
    }

    private class TreeContentProvider implements ITreeContentProvider {

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public ITimeGraphEntry[] getElements(Object inputElement) {
            if (inputElement != null) {
                try {
                    return ((List<?>) inputElement).toArray(new ITimeGraphEntry[0]);
                } catch (ClassCastException e) {
                }
            }
            return new ITimeGraphEntry[0];
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            ITimeGraphEntry entry = (ITimeGraphEntry) parentElement;
            List<? extends ITimeGraphEntry> children = entry.getChildren();
            return children.toArray(new ITimeGraphEntry[children.size()]);
        }

        @Override
        public Object getParent(Object element) {
            ITimeGraphEntry entry = (ITimeGraphEntry) element;
            return entry.getParent();
        }

        @Override
        public boolean hasChildren(Object element) {
            ITimeGraphEntry entry = (ITimeGraphEntry) element;
            return entry.hasChildren();
        }

    }

    private class TimeGraphContentProvider implements ITimeGraphContentProvider {

        @Override
        public ITimeGraphEntry[] getElements(Object inputElement) {
            if (inputElement != null) {
                try {
                    return ((List<?>) inputElement).toArray(new ITimeGraphEntry[0]);
                } catch (ClassCastException e) {
                }
            }
            return new ITimeGraphEntry[0];
        }

    }

    /**
     * Base class to provide the labels for the tree viewer. Views extending
     * this class typically need to override the getColumnText method if they
     * have more than one column to display
     */
    protected static class TreeLabelProvider implements ITableLabelProvider {

        @Override
        public void addListener(ILabelProviderListener listener) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            TimeGraphEntry entry = (TimeGraphEntry) element;
            if (columnIndex == 0) {
                return entry.getName();
            }
            return new String();
        }

    }

    private class BuildThread extends Thread {
        private final ITmfTrace fBuildTrace;
        private final ITmfTrace fParentTrace;
        private final IProgressMonitor fMonitor;

        public BuildThread(final ITmfTrace trace, final ITmfTrace parentTrace, final String name) {
            super(name + " build"); //$NON-NLS-1$
            fBuildTrace = trace;
            fParentTrace = parentTrace;
            fMonitor = new NullProgressMonitor();
        }

        @Override
        public void run() {
            buildEventList(fBuildTrace, fParentTrace, fMonitor);
            synchronized (fBuildThreadMap) {
                fBuildThreadMap.remove(fBuildTrace);
            }
        }

        public void cancel() {
            fMonitor.setCanceled(true);
        }
    }

    private class ZoomThread extends Thread {
        private final List<TimeGraphEntry> fZoomEntryList;
        private final long fZoomStartTime;
        private final long fZoomEndTime;
        private final long fResolution;
        private final IProgressMonitor fMonitor;

        public ZoomThread(List<TimeGraphEntry> entryList, long startTime, long endTime, String name) {
            super(name + " zoom"); //$NON-NLS-1$
            fZoomEntryList = entryList;
            fZoomStartTime = startTime;
            fZoomEndTime = endTime;
            fResolution = Math.max(1, (fZoomEndTime - fZoomStartTime) / fDisplayWidth);
            fMonitor = new NullProgressMonitor();
        }

        @Override
        public void run() {
            if (fZoomEntryList == null) {
                return;
            }
            for (TimeGraphEntry entry : fZoomEntryList) {
                if (fMonitor.isCanceled()) {
                    return;
                }
                zoom(entry, fMonitor);
            }
            /* Refresh the arrows when zooming */
            List<ILinkEvent> events = getLinkList(fZoomStartTime, fZoomEndTime, fResolution, fMonitor);
            if (events != null) {
                fTimeGraphWrapper.getTimeGraphViewer().setLinks(events);
                redraw();
            }
        }

        private void zoom(TimeGraphEntry entry, IProgressMonitor monitor) {
            if (fZoomStartTime <= fStartTime && fZoomEndTime >= fEndTime) {
                entry.setZoomedEventList(null);
            } else {
                List<ITimeEvent> zoomedEventList = getEventList(entry, fZoomStartTime, fZoomEndTime, fResolution, monitor);
                if (zoomedEventList != null) {
                    entry.setZoomedEventList(zoomedEventList);
                }
            }
            redraw();
            for (TimeGraphEntry child : entry.getChildren()) {
                if (fMonitor.isCanceled()) {
                    return;
                }
                zoom(child, monitor);
            }
        }

        public void cancel() {
            fMonitor.setCanceled(true);
        }
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a time graph view that contains either a time graph viewer or
     * a time graph combo.
     *
     * By default, the view uses a time graph viewer. To use a time graph combo,
     * the subclass constructor must call {@link #setTreeColumns(String[])} and
     * {@link #setTreeLabelProvider(TreeLabelProvider)}.
     *
     * @param id
     *            The id of the view
     * @param pres
     *            The presentation provider
     */
    public AbstractTimeGraphView(String id, TimeGraphPresentationProvider pres) {
        super(id);
        fPresentation = pres;
        fDisplayWidth = Display.getDefault().getBounds().width;
    }

    // ------------------------------------------------------------------------
    // Getters and setters
    // ------------------------------------------------------------------------

    /**
     * Getter for the time graph combo
     *
     * @return The time graph combo, or null if combo is not used
     */
    protected TimeGraphCombo getTimeGraphCombo() {
        if (fTimeGraphWrapper instanceof TimeGraphComboWrapper) {
            return ((TimeGraphComboWrapper) fTimeGraphWrapper).getTimeGraphCombo();
        }
        return null;
    }

    /**
     * Getter for the time graph viewer
     *
     * @return The time graph viewer
     */
    protected TimeGraphViewer getTimeGraphViewer() {
        return fTimeGraphWrapper.getTimeGraphViewer();
    }

    /**
     * Sets the tree column labels.
     * This should be called from the constructor.
     *
     * @param columns
     *            The array of tree column labels
     */
    protected void setTreeColumns(final String[] columns) {
        fColumns = columns;
    }

    /**
     * Sets the tree label provider.
     * This should be called from the constructor.
     *
     * @param tlp
     *            The tree label provider
     */
    protected void setTreeLabelProvider(final TreeLabelProvider tlp) {
        fLabelProvider = tlp;
    }

    /**
     * Sets the relative weight of each part of the time graph combo.
     * This should be called from the constructor.
     *
     * @param weights
     *            The array (length 2) of relative weights of each part of the combo
     */
    protected void setWeight(final int[] weights) {
        fWeight = weights;
    }

    /**
     * Sets the filter column labels.
     * This should be called from the constructor.
     *
     * @param filterColumns
     *            The array of filter column labels
     */
    protected void setFilterColumns(final String[] filterColumns) {
        fFilterColumns = filterColumns;
    }

    /**
     * Sets the filter label provider.
     * This should be called from the constructor.
     *
     * @param labelProvider
     *            The filter label provider
     *
     * @since 3.0
     */
    protected void setFilterLabelProvider(final TreeLabelProvider labelProvider) {
        fFilterLabelProvider = labelProvider;
    }

    /**
     * Gets the display width
     *
     * @return the display width
     */
    protected int getDisplayWidth() {
        return fDisplayWidth;
    }

    /**
     * Gets the comparator for the entries
     *
     * @return The entry comparator
     */
    protected Comparator<ITimeGraphEntry> getEntryComparator() {
        return fEntryComparator;
    }

    /**
     * Sets the comparator class for the entries
     *
     * @param comparator
     *            A comparator object
     */
    protected void setEntryComparator(final Comparator<ITimeGraphEntry> comparator) {
        fEntryComparator = comparator;
    }

    /**
     * Gets the trace displayed in the view
     *
     * @return The trace
     */
    protected ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * Gets the start time
     *
     * @return The start time
     */
    protected long getStartTime() {
        return fStartTime;
    }

    /**
     * Sets the start time
     *
     * @param time
     *            The start time
     */
    protected void setStartTime(long time) {
        fStartTime = time;
    }

    /**
     * Gets the end time
     *
     * @return The end time
     */
    protected long getEndTime() {
        return fEndTime;
    }

    /**
     * Sets the end time
     *
     * @param time
     *            The end time
     */
    protected void setEndTime(long time) {
        fEndTime = time;
    }

    /**
     * Gets the entry list for a trace
     *
     * @param trace
     *            the trace
     *
     * @return the entry list map
     * @since 3.0
     */
    protected List<TimeGraphEntry> getEntryList(ITmfTrace trace) {
        synchronized (fEntryListMap) {
            return fEntryListMap.get(trace);
        }
    }

    /**
     * Adds a trace entry list to the entry list map
     *
     * @param trace
     *            the trace to add
     * @param list
     *            the list of time graph entries
     */
    protected void putEntryList(ITmfTrace trace, List<TimeGraphEntry> list) {
        synchronized (fEntryListMap) {
            fEntryListMap.put(trace, new CopyOnWriteArrayList<>(list));
        }
    }

    /**
     * Adds a list of entries to a trace's entry list
     *
     * @param trace
     *            the trace
     * @param list
     *            the list of time graph entries to add
     * @since 3.0
     */
    protected void addToEntryList(ITmfTrace trace, List<TimeGraphEntry> list) {
        synchronized (fEntryListMap) {
            List<TimeGraphEntry> entryList = fEntryListMap.get(trace);
            if (entryList == null) {
                fEntryListMap.put(trace, new CopyOnWriteArrayList<>(list));
            } else {
                entryList.addAll(list);
            }
        }
    }

    /**
     * Removes a list of entries from a trace's entry list
     *
     * @param trace
     *            the trace
     * @param list
     *            the list of time graph entries to remove
     * @since 3.0
     */
    protected void removeFromEntryList(ITmfTrace trace, List<TimeGraphEntry> list) {
        synchronized (fEntryListMap) {
            List<TimeGraphEntry> entryList = fEntryListMap.get(trace);
            if (entryList != null) {
                entryList.removeAll(list);
            }
        }
    }

    /**
     * Text for the "next" button
     *
     * @return The "next" button text
     */
    protected String getNextText() {
        return Messages.AbstractTimeGraphtView_NextText;
    }

    /**
     * Tooltip for the "next" button
     *
     * @return Tooltip for the "next" button
     */
    protected String getNextTooltip() {
        return Messages.AbstractTimeGraphView_NextTooltip;
    }

    /**
     * Text for the "Previous" button
     *
     * @return The "Previous" button text
     */
    protected String getPrevText() {
        return Messages.AbstractTimeGraphView_PreviousText;
    }

    /**
     * Tooltip for the "previous" button
     *
     * @return Tooltip for the "previous" button
     */
    protected String getPrevTooltip() {
        return Messages.AbstractTimeGraphView_PreviousTooltip;
    }

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------

    @Override
    public void createPartControl(Composite parent) {
        if (fColumns == null || fLabelProvider == null) {
            fTimeGraphWrapper = new TimeGraphViewerWrapper(parent, SWT.NONE);
            TimeGraphViewer viewer = fTimeGraphWrapper.getTimeGraphViewer();
            viewer.setTimeGraphContentProvider(new TimeGraphContentProvider());
        } else {
            TimeGraphComboWrapper wrapper = new TimeGraphComboWrapper(parent, SWT.NONE);
            fTimeGraphWrapper = wrapper;
            TimeGraphCombo combo = wrapper.getTimeGraphCombo();
            combo.setTreeContentProvider(new TreeContentProvider());
            combo.setTreeLabelProvider(fLabelProvider);
            combo.setTreeColumns(fColumns);
            combo.setFilterContentProvider(new TreeContentProvider());
            combo.setFilterLabelProvider(fFilterLabelProvider);
            combo.setFilterColumns(fFilterColumns);
            combo.setTimeGraphContentProvider(new TimeGraphContentProvider());
        }

        fTimeGraphWrapper.setTimeGraphProvider(fPresentation);

        fTimeGraphWrapper.getTimeGraphViewer().addRangeListener(new ITimeGraphRangeListener() {
            @Override
            public void timeRangeUpdated(TimeGraphRangeUpdateEvent event) {
                final long startTime = event.getStartTime();
                final long endTime = event.getEndTime();
                TmfTimeRange range = new TmfTimeRange(new TmfNanoTimestamp(startTime), new TmfNanoTimestamp(endTime));
                broadcast(new TmfRangeSynchSignal(AbstractTimeGraphView.this, range));
                if (fZoomThread != null) {
                    fZoomThread.cancel();
                }
                startZoomThread(startTime, endTime);
            }
        });

        fTimeGraphWrapper.getTimeGraphViewer().addTimeListener(new ITimeGraphTimeListener() {
            @Override
            public void timeSelected(TimeGraphTimeEvent event) {
                TmfNanoTimestamp startTime = new TmfNanoTimestamp(event.getBeginTime());
                TmfNanoTimestamp endTime = new TmfNanoTimestamp(event.getEndTime());
                broadcast(new TmfTimeSynchSignal(AbstractTimeGraphView.this, startTime, endTime));
            }
        });

        fTimeGraphWrapper.addSelectionListener(new ITimeGraphSelectionListener() {
            @Override
            public void selectionChanged(TimeGraphSelectionEvent event) {
                // ITimeGraphEntry selection = event.getSelection();
            }
        });

        fTimeGraphWrapper.getTimeGraphViewer().setTimeFormat(TimeFormat.CALENDAR);

        IStatusLineManager statusLineManager = getViewSite().getActionBars().getStatusLineManager();
        fTimeGraphWrapper.getTimeGraphViewer().getTimeGraphControl().setStatusLineManager(statusLineManager);

        // View Action Handling
        makeActions();
        contributeToActionBars();

        ITmfTrace trace = getActiveTrace();
        if (trace != null) {
            traceSelected(new TmfTraceSelectedSignal(this, trace));
        }

        // make selection available to other views
        getSite().setSelectionProvider(fTimeGraphWrapper.getSelectionProvider());
    }

    @Override
    public void setFocus() {
        fTimeGraphWrapper.setFocus();
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Handler for the trace opened signal.
     *
     * @param signal
     *            The incoming signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        fTrace = signal.getTrace();
        loadTrace();
    }

    /**
     * Handler for the trace selected signal
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public void traceSelected(final TmfTraceSelectedSignal signal) {
        if (signal.getTrace() == fTrace) {
            return;
        }
        fTrace = signal.getTrace();

        loadTrace();
    }

    /**
     * Trace is closed: clear the data structures and the view
     *
     * @param signal
     *            the signal received
     */
    @TmfSignalHandler
    public void traceClosed(final TmfTraceClosedSignal signal) {
        synchronized (fBuildThreadMap) {
            for (ITmfTrace trace : TmfTraceManager.getTraceSet(signal.getTrace())) {
                BuildThread buildThread = fBuildThreadMap.remove(trace);
                if (buildThread != null) {
                    buildThread.cancel();
                }
            }
        }
        synchronized (fEntryListMap) {
            fEntryListMap.remove(signal.getTrace());
        }
        if (signal.getTrace() == fTrace) {
            fTrace = null;
            fStartTime = 0;
            fEndTime = 0;
            if (fZoomThread != null) {
                fZoomThread.cancel();
            }
            refresh();
        }
    }

    /**
     * Handler for the time synch signal
     *
     * @param signal
     *            The signal that's received
     */
    @TmfSignalHandler
    public void synchToTime(final TmfTimeSynchSignal signal) {
        if (signal.getSource() == this || fTrace == null) {
            return;
        }
        final long beginTime = signal.getBeginTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        final long endTime = signal.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();

        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphWrapper.isDisposed()) {
                    return;
                }
                if (beginTime == endTime) {
                    fTimeGraphWrapper.getTimeGraphViewer().setSelectedTime(beginTime, true);
                } else {
                    fTimeGraphWrapper.getTimeGraphViewer().setSelectionRange(beginTime, endTime);
                }
                startZoomThread(fTimeGraphWrapper.getTimeGraphViewer().getTime0(), fTimeGraphWrapper.getTimeGraphViewer().getTime1());

                synchingToTime(beginTime);
            }
        });
    }

    /**
     * Handler for the range synch signal
     *
     * @param signal
     *            The signal that's received
     */
    @TmfSignalHandler
    public void synchToRange(final TmfRangeSynchSignal signal) {
        if (signal.getSource() == this || fTrace == null) {
            return;
        }
        if (signal.getCurrentRange().getIntersection(fTrace.getTimeRange()) == null) {
            return;
        }
        final long startTime = signal.getCurrentRange().getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        final long endTime = signal.getCurrentRange().getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphWrapper.isDisposed()) {
                    return;
                }
                fTimeGraphWrapper.getTimeGraphViewer().setStartFinishTime(startTime, endTime);
                startZoomThread(startTime, endTime);
            }
        });
    }

    /**
     * @param signal the format of the timestamps was updated.
     * @since 2.1
     */
    @TmfSignalHandler
    public void updateTimeFormat( final TmfTimestampFormatUpdateSignal signal){
        fTimeGraphWrapper.refresh();
    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    private void loadTrace() {
        synchronized (fEntryListMap) {
            fEntryList = fEntryListMap.get(fTrace);
            if (fEntryList == null) {
                setStartTime(Long.MAX_VALUE);
                setEndTime(Long.MIN_VALUE);
                synchronized (fBuildThreadMap) {
                    for (ITmfTrace trace : TmfTraceManager.getTraceSet(fTrace)) {
                        BuildThread buildThread = new BuildThread(trace, fTrace, getName());
                        fBuildThreadMap.put(trace, buildThread);
                        buildThread.start();
                    }
                }
            } else {
                fStartTime = fTrace.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                fEndTime = fTrace.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                refresh();
            }
        }
    }

    /**
     * Method called when synching to a given timestamp. Inheriting classes can
     * perform actions here to update the view at the given timestamp.
     *
     * @param time
     *            The currently selected time
     */
    protected void synchingToTime(long time) {

    }

    /**
     * Build the entries list to show in this time graph
     *
     * Called from the BuildThread
     *
     * @param trace
     *            The trace being built
     * @param parentTrace
     *            The parent of the trace set, or the trace itself
     * @param monitor
     *            The progress monitor object
     * @since 3.0
     */
    protected abstract void buildEventList(ITmfTrace trace, ITmfTrace parentTrace, IProgressMonitor monitor);

    /**
     * Gets the list of event for an entry in a given timerange
     *
     * @param entry
     *            The entry to get events for
     * @param startTime
     *            Start of the time range
     * @param endTime
     *            End of the time range
     * @param resolution
     *            The resolution
     * @param monitor
     *            The progress monitor object
     * @return The list of events for the entry
     */
    protected abstract List<ITimeEvent> getEventList(TimeGraphEntry entry,
            long startTime, long endTime, long resolution,
            IProgressMonitor monitor);

    /**
     * Gets the list of links (displayed as arrows) for a trace in a given
     * timerange.  Default implementation returns an empty list.
     *
     * @param startTime
     *            Start of the time range
     * @param endTime
     *            End of the time range
     * @param resolution
     *            The resolution
     * @param monitor
     *            The progress monitor object
     * @return The list of link events
     * @since 2.1
     */
    protected List<ILinkEvent> getLinkList(long startTime, long endTime,
            long resolution, IProgressMonitor monitor) {
        return new ArrayList<>();
    }


    /**
     * Refresh the display
     */
    protected void refresh() {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphWrapper.isDisposed()) {
                    return;
                }
                boolean hasEntries = false;
                synchronized (fEntryListMap) {
                    fEntryList = fEntryListMap.get(fTrace);
                    if (fEntryList == null) {
                        fEntryList = new CopyOnWriteArrayList<>();
                    } else if (fEntryComparator != null) {
                        List<TimeGraphEntry> list = new ArrayList<>(fEntryList);
                        Collections.sort(list, fEntryComparator);
                        fEntryList.clear();
                        fEntryList.addAll(list);
                    }
                    hasEntries = fEntryList.size() != 0;
                }
                if (fEntryList != fTimeGraphWrapper.getInput()) {
                    fTimeGraphWrapper.setInput(fEntryList);
                } else {
                    fTimeGraphWrapper.refresh();
                }
                fTimeGraphWrapper.getTimeGraphViewer().setTimeBounds(fStartTime, fEndTime);

                long selectionBeginTime = fTrace == null ? 0 : fTraceManager.getSelectionBeginTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                long selectionEndTime = fTrace == null ? 0 : fTraceManager.getSelectionEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                long startTime = fTrace == null ? 0 : fTraceManager.getCurrentRange().getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                long endTime = fTrace == null ? 0 : fTraceManager.getCurrentRange().getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                startTime = Math.max(startTime, fStartTime);
                endTime = Math.min(endTime, fEndTime);
                fTimeGraphWrapper.getTimeGraphViewer().setSelectionRange(selectionBeginTime, selectionEndTime);
                fTimeGraphWrapper.getTimeGraphViewer().setStartFinishTime(startTime, endTime);

                if (fTimeGraphWrapper instanceof TimeGraphComboWrapper && !fPackDone) {
                    for (TreeColumn column : ((TimeGraphComboWrapper) fTimeGraphWrapper).getTreeViewer().getTree().getColumns()) {
                        column.pack();
                    }
                    if (hasEntries) {
                        fPackDone = true;
                    }
                }

                startZoomThread(startTime, endTime);
            }
        });
    }

    /**
     * Redraw the canvas
     */
    protected void redraw() {
        synchronized (fSyncObj) {
            if (fRedrawState == State.IDLE) {
                fRedrawState = State.BUSY;
            } else {
                fRedrawState = State.PENDING;
                return;
            }
        }
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (fTimeGraphWrapper.isDisposed()) {
                    return;
                }
                fTimeGraphWrapper.redraw();
                fTimeGraphWrapper.update();
                synchronized (fSyncObj) {
                    if (fRedrawState == State.PENDING) {
                        fRedrawState = State.IDLE;
                        redraw();
                    } else {
                        fRedrawState = State.IDLE;
                    }
                }
            }
        });
    }

    private void startZoomThread(long startTime, long endTime) {
        if (fZoomThread != null) {
            fZoomThread.cancel();
        }
        fZoomThread = new ZoomThread(fEntryList, startTime, endTime, getName());
        fZoomThread.start();
    }

    private void makeActions() {
        fPreviousResourceAction = fTimeGraphWrapper.getTimeGraphViewer().getPreviousItemAction();
        fPreviousResourceAction.setText(getPrevText());
        fPreviousResourceAction.setToolTipText(getPrevTooltip());
        fNextResourceAction = fTimeGraphWrapper.getTimeGraphViewer().getNextItemAction();
        fNextResourceAction.setText(getNextText());
        fNextResourceAction.setToolTipText(getNextTooltip());
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        fillLocalToolBar(bars.getToolBarManager());
    }

    /**
     * Add actions to local tool bar manager
     *
     * @param manager the tool bar manager
     */
    protected void fillLocalToolBar(IToolBarManager manager) {
        if (fTimeGraphWrapper instanceof TimeGraphComboWrapper) {
            if (fFilterColumns != null && fFilterLabelProvider != null && fFilterColumns.length > 0) {
                manager.add(((TimeGraphComboWrapper) fTimeGraphWrapper).getShowFilterAction());
            }
        }
        manager.add(fTimeGraphWrapper.getTimeGraphViewer().getShowLegendAction());
        manager.add(new Separator());
        manager.add(fTimeGraphWrapper.getTimeGraphViewer().getResetScaleAction());
        manager.add(fTimeGraphWrapper.getTimeGraphViewer().getPreviousEventAction());
        manager.add(fTimeGraphWrapper.getTimeGraphViewer().getNextEventAction());
        manager.add(fPreviousResourceAction);
        manager.add(fNextResourceAction);
        manager.add(fTimeGraphWrapper.getTimeGraphViewer().getZoomInAction());
        manager.add(fTimeGraphWrapper.getTimeGraphViewer().getZoomOutAction());
        manager.add(new Separator());
    }
}
