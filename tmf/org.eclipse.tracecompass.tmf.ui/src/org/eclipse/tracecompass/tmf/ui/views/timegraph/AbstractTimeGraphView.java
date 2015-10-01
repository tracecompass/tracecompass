/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson, École Polytechnique de Montréal
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

package org.eclipse.tracecompass.tmf.ui.views.timegraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTimestampFormatUpdateSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfNanoTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.TmfUiRefreshHandler;
import org.eclipse.tracecompass.tmf.ui.signal.TmfTimeViewAlignmentInfo;
import org.eclipse.tracecompass.tmf.ui.views.ITmfTimeAligned;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphBookmarkListener;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphContentProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider2;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphRangeListener;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphSelectionListener;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.ITimeGraphTimeListener;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphBookmarkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphCombo;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphContentProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphRangeUpdateEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphViewer;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ILinkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.IMarkerEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.MarkerEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils.TimeFormat;
import org.eclipse.ui.IActionBars;

/**
 * An abstract view all time graph views can inherit
 *
 * This view contains either a time graph viewer, or a time graph combo which is
 * divided between a tree viewer on the left and a time graph viewer on the right.
 */
public abstract class AbstractTimeGraphView extends TmfView implements ITmfTimeAligned, IResourceChangeListener {

    /** Constant indicating that all levels of the time graph should be expanded */
    protected static final int ALL_LEVELS = AbstractTreeViewer.ALL_LEVELS;

    /** Color marker attribute. The format is the output of RGBA.toString(). */
    private static final String MARKER_COLOR = "color"; //$NON-NLS-1$

    /** Time marker attribute. The format is the output of Long.toString(). */
    private static final String MARKER_TIME = "time"; //$NON-NLS-1$

    /** Duration marker attribute. The format is the output of Long.toString(). */
    private static final String MARKER_DURATION = "duration"; //$NON-NLS-1$

    private static final Pattern RGBA_PATTERN = Pattern.compile("RGBA \\{(\\d+), (\\d+), (\\d+), (\\d+)\\}"); //$NON-NLS-1$

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

    /** The selected trace editor file*/
    private IFile fEditorFile;

    /** The timegraph entry list */
    private List<TimeGraphEntry> fEntryList;

    /** The trace to entry list hash map */
    private final Map<ITmfTrace, List<TimeGraphEntry>> fEntryListMap = new HashMap<>();

    /** The trace to filters hash map */
    private final Map<ITmfTrace, ViewerFilter[]> fFiltersMap = new HashMap<>();

    /** The trace to build thread hash map */
    private final Map<ITmfTrace, BuildThread> fBuildThreadMap = new HashMap<>();

    /** The start time */
    private long fStartTime = SWT.DEFAULT;

    /** The end time */
    private long fEndTime = SWT.DEFAULT;

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

    /** The time graph content provider */
    private @NonNull ITimeGraphContentProvider fTimeGraphContentProvider = new TimeGraphContentProvider();

    /** The relative weight of the sash, ignored if combo is not used */
    private int[] fWeight = { 1, 3 };

    /** The filter column label array, or null if filter is not used */
    private String[] fFilterColumns;

    /** The pack done flag */
    private boolean fPackDone = false;

    /** The filter label provider, or null if filter is not used */
    private TreeLabelProvider fFilterLabelProvider;

    private int fAutoExpandLevel = ALL_LEVELS;

    /** The list of color resources created by this view */
    private final List<Color> fColors = new ArrayList<>();

    // ------------------------------------------------------------------------
    // Classes
    // ------------------------------------------------------------------------

    private interface ITimeGraphWrapper {

        void setTimeGraphContentProvider(ITimeGraphContentProvider timeGraphContentProvider);

        void setTimeGraphPresentationProvider(TimeGraphPresentationProvider timeGraphPresentationProvider);

        TimeGraphViewer getTimeGraphViewer();

        void addSelectionListener(ITimeGraphSelectionListener listener);

        ISelectionProvider getSelectionProvider();

        void setFocus();

        boolean isDisposed();

        void refresh();

        void setInput(Object input);

        Object getInput();

        void setFilters(ViewerFilter[] filters);

        ViewerFilter[] getFilters();

        void redraw();

        void update();

        void setAutoExpandLevel(int level);

        void setFilterColumns(String[] columnNames);

        void setFilterContentProvider(ITreeContentProvider contentProvider);

        void setFilterLabelProvider(ITableLabelProvider labelProvider);

        IAction getShowFilterDialogAction();

        void performAlign(int offset, int width);

        TmfTimeViewAlignmentInfo getTimeViewAlignmentInfo();

        int getAvailableWidth(int requestedOffset);
    }

    private class TimeGraphViewerWrapper implements ITimeGraphWrapper {
        private TimeGraphViewer viewer;

        private TimeGraphViewerWrapper(Composite parent, int style) {
            viewer = new TimeGraphViewer(parent, style);
        }

        @Override
        public void setTimeGraphContentProvider(ITimeGraphContentProvider timeGraphContentProvider) {
            viewer.setTimeGraphContentProvider(timeGraphContentProvider);
        }

        @Override
        public void setTimeGraphPresentationProvider(TimeGraphPresentationProvider timeGraphPresentationProvider) {
            viewer.setTimeGraphProvider(timeGraphPresentationProvider);
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
        public void setFilterColumns(String[] columnNames) {
            viewer.setFilterColumns(columnNames);
        }

        @Override
        public void setFilterContentProvider(ITreeContentProvider contentProvider) {
            viewer.setFilterContentProvider(contentProvider);
        }

        @Override
        public void setFilterLabelProvider(ITableLabelProvider labelProvider) {
            viewer.setFilterLabelProvider(labelProvider);
        }

        @Override
        public void setFilters(ViewerFilter[] filters) {
            viewer.setFilters(filters);
        }

        @Override
        public ViewerFilter[] getFilters() {
            return viewer.getFilters();
        }

        @Override
        public IAction getShowFilterDialogAction() {
            return viewer.getShowFilterDialogAction();
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

        @Override
        public void setAutoExpandLevel(int level) {
            viewer.setAutoExpandLevel(level);
        }

        @Override
        public void performAlign(int offset, int width) {
            viewer.performAlign(offset, width);
        }

        @Override
        public TmfTimeViewAlignmentInfo getTimeViewAlignmentInfo() {
            return viewer.getTimeViewAlignmentInfo();
        }

        @Override
        public int getAvailableWidth(int requestedOffset) {
            return viewer.getAvailableWidth(requestedOffset);
        }
    }

    private class TimeGraphComboWrapper implements ITimeGraphWrapper {
        private TimeGraphCombo combo;

        private TimeGraphComboWrapper(Composite parent, int style) {
            combo = new TimeGraphCombo(parent, style, fWeight);
        }

        @Override
        public void setTimeGraphContentProvider(ITimeGraphContentProvider timeGraphContentProvider) {
            combo.setTimeGraphContentProvider(timeGraphContentProvider);
        }

        @Override
        public void setTimeGraphPresentationProvider(TimeGraphPresentationProvider timeGraphPresentationProvider) {
            combo.setTimeGraphProvider(timeGraphPresentationProvider);
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
        public void setFilterColumns(String[] columnNames) {
            combo.setFilterColumns(columnNames);
        }

        @Override
        public void setFilterContentProvider(ITreeContentProvider contentProvider) {
            combo.setFilterContentProvider(contentProvider);
        }

        @Override
        public void setFilterLabelProvider(ITableLabelProvider labelProvider) {
            combo.setFilterLabelProvider(labelProvider);
        }

        @Override
        public void setFilters(ViewerFilter[] filters) {
            combo.setFilters(filters);
        }

        @Override
        public ViewerFilter[] getFilters() {
            return combo.getFilters();
        }

        @Override
        public IAction getShowFilterDialogAction() {
            return combo.getShowFilterDialogAction();
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

        @Override
        public void setAutoExpandLevel(int level) {
            combo.setAutoExpandLevel(level);
        }

        TimeGraphCombo getTimeGraphCombo() {
            return combo;
        }

        TreeViewer getTreeViewer() {
            return combo.getTreeViewer();
        }

        @Override
        public void performAlign(int offset, int width) {
            combo.performAlign(offset, width);
        }

        @Override
        public TmfTimeViewAlignmentInfo getTimeViewAlignmentInfo() {
            return combo.getTimeViewAlignmentInfo();
        }

        @Override
        public int getAvailableWidth(int requestedOffset) {
            return combo.getAvailableWidth(requestedOffset);
        }
    }

    /**
     * Base class to provide the labels for the tree viewer. Views extending
     * this class typically need to override the getColumnText method if they
     * have more than one column to display
     */
    protected static class TreeLabelProvider implements ITableLabelProvider, ILabelProvider {

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

        @Override
        public Image getImage(Object element) {
            return null;
        }

        @Override
        public String getText(Object element) {
            TimeGraphEntry entry = (TimeGraphEntry) element;
            return entry.getName();
        }

    }

    private class BuildThread extends Thread {
        private final @NonNull ITmfTrace fBuildTrace;
        private final @NonNull ITmfTrace fParentTrace;
        private final @NonNull IProgressMonitor fMonitor;

        public BuildThread(final @NonNull ITmfTrace trace, final @NonNull ITmfTrace parentTrace, final String name) {
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

    /**
     * Zoom thread
     * @since 2.0
     */
    protected abstract class ZoomThread extends Thread {
        private final long fZoomStartTime;
        private final long fZoomEndTime;
        private final long fResolution;
        private final @NonNull  IProgressMonitor fMonitor;

        /**
         * Constructor
         *
         * @param startTime
         *            the start time
         * @param endTime
         *            the end time
         * @param resolution
         *            the resolution
         */
        public ZoomThread(long startTime, long endTime, long resolution) {
            super(AbstractTimeGraphView.this.getName() + " zoom"); //$NON-NLS-1$
            fZoomStartTime = startTime;
            fZoomEndTime = endTime;
            fResolution = resolution;
            fMonitor = new NullProgressMonitor();
        }

        /**
         * @return the zoom start time
         */
        public long getZoomStartTime() {
            return fZoomStartTime;
        }

        /**
         * @return the zoom end time
         */
        public long getZoomEndTime() {
            return fZoomEndTime;
        }

        /**
         * @return the resolution
         */
        public long getResolution() {
            return fResolution;
        }

        /**
         * @return the monitor
         */
        public @NonNull IProgressMonitor getMonitor() {
            return fMonitor;
        }

        /**
         * Cancel the zoom thread
         */
        public void cancel() {
            fMonitor.setCanceled(true);
        }
    }

    private class ZoomThreadByEntry extends ZoomThread {
        private final @NonNull List<TimeGraphEntry> fZoomEntryList;

        public ZoomThreadByEntry(@NonNull List<TimeGraphEntry> entryList, long startTime, long endTime, long resolution) {
            super(startTime, endTime, resolution);
            fZoomEntryList = entryList;
        }

        @Override
        public void run() {
            for (TimeGraphEntry entry : fZoomEntryList) {
                if (getMonitor().isCanceled()) {
                    return;
                }
                if (entry == null) {
                    break;
                }
                zoom(entry, getMonitor());
            }
            /* Refresh the arrows when zooming */
            List<ILinkEvent> events = getLinkList(getZoomStartTime(), getZoomEndTime(), getResolution(), getMonitor());
            if (events != null) {
                fTimeGraphWrapper.getTimeGraphViewer().setLinks(events);
                redraw();
            }
            /* Refresh the markers when zooming */
            List<IMarkerEvent> markers = getMarkerList(getZoomStartTime(), getZoomEndTime(), getResolution(), getMonitor());
            if (markers != null) {
                fTimeGraphWrapper.getTimeGraphViewer().getTimeGraphControl().setMarkers(markers);
                redraw();
            }
        }

        private void zoom(@NonNull TimeGraphEntry entry, @NonNull IProgressMonitor monitor) {
            if (getZoomStartTime() <= fStartTime && getZoomEndTime() >= fEndTime) {
                entry.setZoomedEventList(null);
            } else {
                List<ITimeEvent> zoomedEventList = getEventList(entry, getZoomStartTime(), getZoomEndTime(), getResolution(), monitor);
                if (zoomedEventList != null) {
                    entry.setZoomedEventList(zoomedEventList);
                }
            }
            redraw();
            for (ITimeGraphEntry child : entry.getChildren()) {
                if (monitor.isCanceled()) {
                    return;
                }
                if (child instanceof TimeGraphEntry) {
                    zoom((TimeGraphEntry) child, monitor);
                }
            }
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
     * Getter for the presentation provider
     *
     * @return The time graph presentation provider
     */
    protected ITimeGraphPresentationProvider2 getPresentationProvider() {
        return fPresentation;
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
     * Sets the time graph content provider. This should be called from the
     * constructor.
     *
     * @param tgcp
     *            The time graph content provider
     * @since 1.0
     */
    protected void setTimeGraphContentProvider(final @NonNull ITimeGraphContentProvider tgcp) {
        fTimeGraphContentProvider = tgcp;
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
     * Sets the auto-expand level to be used for the input of the view. The
     * value 0 means that there is no auto-expand; 1 means that top-level
     * elements are expanded, but not their children; 2 means that top-level
     * elements are expanded, and their children, but not grand-children; and so
     * on.
     * <p>
     * The value {@link #ALL_LEVELS} means that all subtrees should be expanded.
     * </p>
     *
     * @param level
     *            non-negative level, or <code>ALL_LEVELS</code> to expand all
     *            levels of the tree
     */
    protected void setAutoExpandLevel(int level) {
        fAutoExpandLevel = level;
        ITimeGraphWrapper tgWrapper = fTimeGraphWrapper;
        if (tgWrapper != null) {
            tgWrapper.setAutoExpandLevel(level);
        }
    }

    /**
     * Gets the entry list for a trace
     *
     * @param trace
     *            the trace
     *
     * @return the entry list map
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
        super.createPartControl(parent);
        if (fColumns == null || fLabelProvider == null) {
            fTimeGraphWrapper = new TimeGraphViewerWrapper(parent, SWT.NONE);
        } else {
            TimeGraphComboWrapper wrapper = new TimeGraphComboWrapper(parent, SWT.NONE);
            fTimeGraphWrapper = wrapper;
            TimeGraphCombo combo = wrapper.getTimeGraphCombo();
            combo.setTreeContentProvider(fTimeGraphContentProvider);
            combo.setTreeLabelProvider(fLabelProvider);
            combo.setTreeColumns(fColumns);
        }
        fTimeGraphWrapper.setTimeGraphContentProvider(fTimeGraphContentProvider);
        fTimeGraphWrapper.setFilterContentProvider(fTimeGraphContentProvider);
        fTimeGraphWrapper.setFilterLabelProvider(fFilterLabelProvider);
        fTimeGraphWrapper.setFilterColumns(fFilterColumns);

        fTimeGraphWrapper.setTimeGraphPresentationProvider(fPresentation);
        fTimeGraphWrapper.setAutoExpandLevel(fAutoExpandLevel);

        fTimeGraphWrapper.getTimeGraphViewer().addRangeListener(new ITimeGraphRangeListener() {
            @Override
            public void timeRangeUpdated(TimeGraphRangeUpdateEvent event) {
                final long startTime = event.getStartTime();
                final long endTime = event.getEndTime();
                TmfTimeRange range = new TmfTimeRange(new TmfNanoTimestamp(startTime), new TmfNanoTimestamp(endTime));
                broadcast(new TmfWindowRangeUpdatedSignal(AbstractTimeGraphView.this, range));
                startZoomThread(startTime, endTime);
            }
        });

        fTimeGraphWrapper.getTimeGraphViewer().addTimeListener(new ITimeGraphTimeListener() {
            @Override
            public void timeSelected(TimeGraphTimeEvent event) {
                TmfNanoTimestamp startTime = new TmfNanoTimestamp(event.getBeginTime());
                TmfNanoTimestamp endTime = new TmfNanoTimestamp(event.getEndTime());
                broadcast(new TmfSelectionRangeUpdatedSignal(AbstractTimeGraphView.this, startTime, endTime));
            }
        });

        fTimeGraphWrapper.getTimeGraphViewer().addBookmarkListener(new ITimeGraphBookmarkListener() {
            @Override
            public void bookmarkAdded(final TimeGraphBookmarkEvent event) {
                try {
                    ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
                        @Override
                        public void run(IProgressMonitor monitor) throws CoreException {
                            IMarkerEvent bookmark = event.getBookmark();
                            IMarker marker = fEditorFile.createMarker(IMarker.BOOKMARK);
                            marker.setAttribute(IMarker.MESSAGE, bookmark.getLabel());
                            marker.setAttribute(MARKER_TIME, Long.toString(bookmark.getTime()));
                            if (bookmark.getDuration() > 0) {
                                marker.setAttribute(MARKER_DURATION, Long.toString(bookmark.getDuration()));
                                marker.setAttribute(IMarker.LOCATION,
                                        String.format("[%d, %d]", bookmark.getTime(), bookmark.getTime() + bookmark.getDuration())); //$NON-NLS-1$
                            } else {
                                marker.setAttribute(IMarker.LOCATION,
                                        String.format("[%d]", bookmark.getTime())); //$NON-NLS-1$
                            }
                            marker.setAttribute(MARKER_COLOR, bookmark.getColor().getRGBA().toString());
                        }
                    }, null);
                } catch (CoreException e) {
                    Activator.getDefault().logError(e.getMessage());
                }
            }

            @Override
            public void bookmarkRemoved(TimeGraphBookmarkEvent event) {
                try {
                    IMarkerEvent bookmark = event.getBookmark();
                    IMarker[] markers = fEditorFile.findMarkers(IMarker.BOOKMARK, false, IResource.DEPTH_ZERO);
                    for (IMarker marker : markers) {
                        if (bookmark.getLabel().equals(marker.getAttribute(IMarker.MESSAGE)) &&
                                Long.toString(bookmark.getTime()).equals(marker.getAttribute(MARKER_TIME, (String) null)) &&
                                Long.toString(bookmark.getDuration()).equals(marker.getAttribute(MARKER_DURATION, Long.toString(0))) &&
                                bookmark.getColor().getRGBA().toString().equals(marker.getAttribute(MARKER_COLOR))) {
                            marker.delete();
                            break;
                        }
                    }
                } catch (CoreException e) {
                    Activator.getDefault().logError(e.getMessage());
                }
            }
        });

        fTimeGraphWrapper.getTimeGraphViewer().setTimeFormat(TimeFormat.CALENDAR);

        IStatusLineManager statusLineManager = getViewSite().getActionBars().getStatusLineManager();
        fTimeGraphWrapper.getTimeGraphViewer().getTimeGraphControl().setStatusLineManager(statusLineManager);

        // View Action Handling
        makeActions();
        contributeToActionBars();

        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        if (trace != null) {
            traceSelected(new TmfTraceSelectedSignal(this, trace));
        }

        // make selection available to other views
        getSite().setSelectionProvider(fTimeGraphWrapper.getSelectionProvider());

        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
    }

    @Override
    public void setFocus() {
        fTimeGraphWrapper.setFocus();
    }

    @Override
    public void dispose() {
        super.dispose();
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
    }

    /**
     * @since 2.0
     */
    @Override
    public void resourceChanged(final IResourceChangeEvent event) {
        for (final IMarkerDelta delta : event.findMarkerDeltas(IMarker.BOOKMARK, false)) {
            if (delta.getResource().equals(fEditorFile)) {
                fTimeGraphWrapper.getTimeGraphViewer().setBookmarks(refreshBookmarks(fEditorFile));
                redraw();
                return;
            }
        }
    }

    private List<IMarkerEvent> refreshBookmarks(final IFile editorFile) {
        List<IMarkerEvent> bookmarks = new ArrayList<>();
        try {
            for (Color color : fColors) {
                color.dispose();
            }
            fColors.clear();
            IMarker[] markers = editorFile.findMarkers(IMarker.BOOKMARK, false, IResource.DEPTH_ZERO);
            for (IMarker marker : markers) {
                String label = marker.getAttribute(IMarker.MESSAGE, (String) null);
                String time = marker.getAttribute(MARKER_TIME, (String) null);
                String duration = marker.getAttribute(MARKER_DURATION, Long.toString(0));
                String rgba = marker.getAttribute(MARKER_COLOR, (String) null);
                if (label != null && time != null && rgba != null) {
                    Matcher matcher = RGBA_PATTERN.matcher(rgba);
                    if (matcher.matches()) {
                        try {
                            int red = Integer.valueOf(matcher.group(1));
                            int green = Integer.valueOf(matcher.group(2));
                            int blue = Integer.valueOf(matcher.group(3));
                            int alpha = Integer.valueOf(matcher.group(4));
                            Color color = new Color(Display.getDefault(), red, green, blue, alpha);
                            fColors.add(color);
                            bookmarks.add(new MarkerEvent(null, Long.valueOf(time), Long.valueOf(duration), color, label, true));
                        } catch (NumberFormatException e) {
                            Activator.getDefault().logError(e.getMessage());
                        }
                    }
                }
            }
        } catch (CoreException e) {
            Activator.getDefault().logError(e.getMessage());
        }
        return bookmarks;
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Handler for the trace opened signal.
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        loadTrace(signal.getTrace());
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
        loadTrace(signal.getTrace());
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
            for (ITmfTrace trace : getTracesToBuild(signal.getTrace())) {
                BuildThread buildThread = fBuildThreadMap.remove(trace);
                if (buildThread != null) {
                    buildThread.cancel();
                }
            }
        }
        synchronized (fEntryListMap) {
            fEntryListMap.remove(signal.getTrace());
        }
        fFiltersMap.remove(signal.getTrace());
        if (signal.getTrace() == fTrace) {
            fTrace = null;
            fStartTime = SWT.DEFAULT;
            fEndTime = SWT.DEFAULT;
            if (fZoomThread != null) {
                fZoomThread.cancel();
                fZoomThread = null;
            }
            refresh();
        }
    }

    /**
     * Handler for the selection range signal.
     *
     * @param signal
     *            The signal that's received
     * @since 1.0
     */
    @TmfSignalHandler
    public void selectionRangeUpdated(final TmfSelectionRangeUpdatedSignal signal) {
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
                synchingToTime(fTimeGraphWrapper.getTimeGraphViewer().getTime0());
            }
        });
    }

    /**
     * Handler for the window range signal.
     *
     * @param signal
     *            The signal that's received
     * @since 1.0
     */
    @TmfSignalHandler
    public void windowRangeUpdated(final TmfWindowRangeUpdatedSignal signal) {
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
     */
    @TmfSignalHandler
    public void updateTimeFormat( final TmfTimestampFormatUpdateSignal signal){
        fTimeGraphWrapper.refresh();
    }

    // ------------------------------------------------------------------------
    // Internal
    // ------------------------------------------------------------------------

    private void loadTrace(final ITmfTrace trace) {
        if (fZoomThread != null) {
            fZoomThread.cancel();
            fZoomThread = null;
        }
        if (fTrace != null) {
            /* save the filters of the previous trace */
            fFiltersMap.put(fTrace, fTimeGraphWrapper.getFilters());
        }
        fTrace = trace;
        fEditorFile = TmfTraceManager.getInstance().getTraceEditorFile(trace);
        synchronized (fEntryListMap) {
            fEntryList = fEntryListMap.get(fTrace);
            if (fEntryList == null) {
                rebuild();
            } else {
                fStartTime = fTrace.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                fEndTime = fTrace.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                refresh();
            }
        }
    }

    /**
     * Forces a rebuild of the entries list, even if entries already exist for this trace
     */
    protected void rebuild() {
        setStartTime(Long.MAX_VALUE);
        setEndTime(Long.MIN_VALUE);
        refresh();
        ITmfTrace viewTrace = fTrace;
        if (viewTrace == null) {
            return;
        }
        synchronized (fBuildThreadMap) {
            for (ITmfTrace trace : getTracesToBuild(viewTrace)) {
                if (trace == null) {
                    break;
                }
                BuildThread buildThread = new BuildThread(trace, viewTrace, getName());
                fBuildThreadMap.put(trace, buildThread);
                buildThread.start();
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
     * Return the list of traces whose data or analysis results will be used to
     * populate the view. By default, if the trace is an experiment, the traces
     * under it will be returned, otherwise, the trace itself is returned.
     *
     * A build thread will be started for each trace returned by this method,
     * some of which may receive events in live streaming mode.
     *
     * @param trace
     *            The trace associated with this view
     * @return List of traces with data to display
     */
    protected @NonNull Iterable<ITmfTrace> getTracesToBuild(@NonNull ITmfTrace trace) {
        return TmfTraceManager.getTraceSet(trace);
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
     */
    protected abstract void buildEventList(@NonNull ITmfTrace trace, @NonNull ITmfTrace parentTrace, @NonNull IProgressMonitor monitor);

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
    protected abstract @Nullable List<ITimeEvent> getEventList(@NonNull TimeGraphEntry entry,
            long startTime, long endTime, long resolution,
            @NonNull IProgressMonitor monitor);

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
     */
    protected @Nullable List<ILinkEvent> getLinkList(long startTime, long endTime,
            long resolution, @NonNull IProgressMonitor monitor) {
        return new ArrayList<>();
    }

    /**
     * Gets the list of markers for a trace in a given time range. Default
     * implementation returns an empty list.
     *
     * @param startTime
     *            Start of the time range
     * @param endTime
     *            End of the time range
     * @param resolution
     *            The resolution
     * @param monitor
     *            The progress monitor object
     * @return The list of marker events
     * @since 2.0
     */
    protected @Nullable List<IMarkerEvent> getMarkerList(long startTime, long endTime,
            long resolution, @NonNull IProgressMonitor monitor) {
        return new ArrayList<>();
    }

    /**
     * Refresh the display
     */
    protected void refresh() {
        final boolean zoomThread = Thread.currentThread() instanceof ZoomThread;
        TmfUiRefreshHandler.getInstance().queueUpdate(this, new Runnable() {
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
                    /* restore the previously saved filters, if any */
                    fTimeGraphWrapper.setFilters(fFiltersMap.get(fTrace));
                    fTimeGraphWrapper.getTimeGraphViewer().setLinks(null);
                    fTimeGraphWrapper.getTimeGraphViewer().setBookmarks(refreshBookmarks(fEditorFile));
                } else {
                    fTimeGraphWrapper.refresh();
                }
                long startBound = (fStartTime == Long.MAX_VALUE ? SWT.DEFAULT : fStartTime);
                long endBound = (fEndTime == Long.MIN_VALUE ? SWT.DEFAULT : fEndTime);
                fTimeGraphWrapper.getTimeGraphViewer().setTimeBounds(startBound, endBound);

                TmfTraceContext ctx = TmfTraceManager.getInstance().getCurrentTraceContext();
                long selectionBeginTime = fTrace == null ? SWT.DEFAULT : ctx.getSelectionRange().getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                long selectionEndTime = fTrace == null ? SWT.DEFAULT : ctx.getSelectionRange().getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                long startTime = fTrace == null ? SWT.DEFAULT : ctx.getWindowRange().getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                long endTime = fTrace == null ? SWT.DEFAULT : ctx.getWindowRange().getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                startTime = (fStartTime == Long.MAX_VALUE ? SWT.DEFAULT : Math.max(startTime, fStartTime));
                endTime = (fEndTime == Long.MIN_VALUE ? SWT.DEFAULT : Math.min(endTime, fEndTime));
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

                if (!zoomThread) {
                    startZoomThread(startTime, endTime);
                }
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
        boolean restart = false;
        if (fZoomThread != null) {
            fZoomThread.cancel();
            if (fZoomThread.fZoomStartTime == startTime && fZoomThread.fZoomEndTime == endTime) {
                restart = true;
            }
        }
        long resolution = Math.max(1, (endTime - startTime) / fDisplayWidth);
        fZoomThread = createZoomThread(startTime, endTime, resolution, restart);
        if (fZoomThread != null) {
            fZoomThread.start();
        }
    }

    /**
     * Create a zoom thread.
     *
     * @param startTime
     *            the zoom start time
     * @param endTime
     *            the zoom end time
     * @param resolution
     *            the resolution
     * @param restart
     *            true if restarting zoom for the same time range
     * @return a zoom thread
     * @since 2.0
     */
    protected @Nullable ZoomThread createZoomThread(long startTime, long endTime, long resolution, boolean restart) {
        final List<TimeGraphEntry> entryList = fEntryList;
        if (entryList == null) {
            return null;
        }
        return new ZoomThreadByEntry(entryList, startTime, endTime, resolution);
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
        if (fFilterColumns != null && fFilterLabelProvider != null && fFilterColumns.length > 0) {
            manager.add(fTimeGraphWrapper.getShowFilterDialogAction());
        }
        manager.add(fTimeGraphWrapper.getTimeGraphViewer().getShowLegendAction());
        manager.add(new Separator());
        manager.add(fTimeGraphWrapper.getTimeGraphViewer().getResetScaleAction());
        manager.add(fTimeGraphWrapper.getTimeGraphViewer().getPreviousEventAction());
        manager.add(fTimeGraphWrapper.getTimeGraphViewer().getNextEventAction());
        manager.add(fTimeGraphWrapper.getTimeGraphViewer().getToggleBookmarkAction());
        manager.add(fTimeGraphWrapper.getTimeGraphViewer().getPreviousBookmarkAction());
        manager.add(fTimeGraphWrapper.getTimeGraphViewer().getNextBookmarkAction());
        manager.add(fPreviousResourceAction);
        manager.add(fNextResourceAction);
        manager.add(fTimeGraphWrapper.getTimeGraphViewer().getZoomInAction());
        manager.add(fTimeGraphWrapper.getTimeGraphViewer().getZoomOutAction());
        manager.add(new Separator());
    }

    /**
     * @since 1.0
     */
    @Override
    public TmfTimeViewAlignmentInfo getTimeViewAlignmentInfo() {
        if (fTimeGraphWrapper == null) {
            return null;
        }
        return fTimeGraphWrapper.getTimeViewAlignmentInfo();
    }

    /**
     * @since 1.0
     */
    @Override
    public int getAvailableWidth(int requestedOffset) {
        if (fTimeGraphWrapper == null) {
            return 0;
        }
        return fTimeGraphWrapper.getAvailableWidth(requestedOffset);
    }

    /**
     * @since 1.0
     */
    @Override
    public void performAlign(int offset, int width) {
        if (fTimeGraphWrapper != null) {
            fTimeGraphWrapper.performAlign(offset, width);
        }
    }
}
