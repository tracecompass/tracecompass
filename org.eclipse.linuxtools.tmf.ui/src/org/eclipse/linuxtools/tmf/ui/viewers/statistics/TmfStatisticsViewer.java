/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial API and implementation
 *   Alexandre Montplaisir - Port to ITmfStatistics provider
 *   Patrick Tasse - Support selection range
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.statistics;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.linuxtools.tmf.core.component.TmfComponent;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceRangeUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.statistics.ITmfStatistics;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.eclipse.linuxtools.tmf.ui.viewers.TmfViewer;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.ITmfColumnDataProvider;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfBaseColumnData;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfBaseColumnDataProvider;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeManager;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeNode;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfTreeContentProvider;
import org.eclipse.linuxtools.tmf.ui.views.statistics.TmfStatisticsModule;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * A basic viewer to display statistics in the statistics view.
 *
 * It is linked to a single ITmfTrace until its disposal.
 *
 * @author Mathieu Denis
 * @version 2.0
 * @since 2.0
 */
public class TmfStatisticsViewer extends TmfViewer {

    /**
     * Timestamp scale (nanosecond)
     */
    public static final byte TIME_SCALE = ITmfTimestamp.NANOSECOND_SCALE;

    /**
     * Default PAGE_SIZE for background requests.
     */
    protected static final int PAGE_SIZE = 50000;

    /**
     * Refresh frequency.
     */
    protected final Long STATS_INPUT_CHANGED_REFRESH = 5000L;

    /**
     * The actual tree viewer to display
     */
    protected TreeViewer fTreeViewer;

    /**
     * The statistics tree linked to this viewer
     */
    protected TmfStatisticsTree fStatisticsData;

    /**
     * Update synchronization parameter (used for streaming): Update busy
     * indicator.
     */
    protected boolean fStatisticsUpdateBusy = false;

    /**
     * Update synchronization parameter (used for streaming): Update pending
     * indicator.
     */
    protected boolean fStatisticsUpdatePending = false;

    /**
     * Update synchronization parameter (used for streaming): Pending Update
     * time range.
     */
    protected TmfTimeRange fStatisticsUpdateRange = null;

    /**
     * Update synchronization object.
     */
    protected final Object fStatisticsUpdateSyncObj = new Object();

    /**
     * Update range synchronization object.
     */
    protected final Object fStatisticsRangeUpdateSyncObj = new Object();

    /**
     * The trace that is displayed by this viewer
     */
    protected ITmfTrace fTrace;

    /**
     * Stores the requested time range.
     */
    protected TmfTimeRange fRequestedTimerange;

    /**
     * Indicates to process all events
     */
    private boolean fProcessAll;

    /**
     * View instance counter (for multiple statistics views)
     */
    private static int fCountInstance = 0;

    /**
     * Number of this instance. Used as an instance ID.
     */
    private int fInstanceNb;

    /**
     * Object to store the cursor while waiting for the trace to load
     */
    private Cursor fWaitCursor = null;

    /**
     * Counts the number of times waitCursor() has been called. It avoids
     * removing the waiting cursor, since there may be multiple requests running
     * at the same time.
     */
    private int fWaitCursorCount = 0;

    /**
     * Tells to send a time range request when the trace gets updated.
     */
    private boolean fSendRangeRequest = true;

    /** Reference to the trace manager */
    private final TmfTraceManager fTraceManager;

    /**
     * Empty constructor. To be used in conjunction with
     * {@link TmfStatisticsViewer#init(Composite, String, ITmfTrace)}
     */
    public TmfStatisticsViewer() {
        super();
        fTraceManager = TmfTraceManager.getInstance();
    }

    /**
     * Create a basic statistics viewer. To be used in conjunction with
     * {@link TmfStatisticsViewer#init(Composite, String, ITmfTrace)}
     *
     * @param parent
     *            The parent composite that will hold the viewer
     * @param viewerName
     *            The name that will be assigned to this viewer
     * @param trace
     *            The trace that is displayed by this viewer
     * @see TmfComponent
     */
    public TmfStatisticsViewer(Composite parent, String viewerName, ITmfTrace trace) {
        init(parent, viewerName, trace);
        fTraceManager = TmfTraceManager.getInstance();
    }

    /**
     * Initialize the statistics viewer.
     *
     * @param parent
     *            The parent component of the viewer.
     * @param viewerName
     *            The name to give to the viewer.
     * @param trace
     *            The trace that will be displayed by the viewer.
     */
    public void init(Composite parent, String viewerName, ITmfTrace trace) {
        super.init(parent, viewerName);
        // Increment a counter to make sure the tree ID is unique.
        fCountInstance++;
        fInstanceNb = fCountInstance;
        fTrace = trace;

        // The viewer will process all events if he is assigned to an experiment
        fProcessAll = (trace instanceof TmfExperiment);

        initContent(parent);
        initInput();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (fWaitCursor != null) {
            fWaitCursor.dispose();
        }

        // Clean the model for this viewer
        TmfStatisticsTreeManager.removeStatTreeRoot(getTreeID());
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Handles the signal about new trace range.
     *
     * @param signal
     *            The trace range updated signal
     */
    @TmfSignalHandler
    public void traceRangeUpdated(TmfTraceRangeUpdatedSignal signal) {
        ITmfTrace trace = signal.getTrace();
        // validate
        if (!isListeningTo(trace)) {
            return;
        }

        synchronized (fStatisticsRangeUpdateSyncObj) {
            // Sends the time range request only once from this method.
            if (fSendRangeRequest) {
                fSendRangeRequest = false;
                ITmfTimestamp begin = fTraceManager.getSelectionBeginTime();
                ITmfTimestamp end = fTraceManager.getSelectionEndTime();
                TmfTimeRange timeRange = new TmfTimeRange(begin, end);
                requestTimeRangeData(trace, timeRange);
            }
        }
        requestData(trace, signal.getRange());
    }

    /**
     * Handles the time range updated signal. It updates the time range
     * statistics.
     *
     * @param signal
     *            Contains the information about the new selected time range.
     * @deprecated
     *            As of 2.1, use {@link #timeSynchUpdated(TmfTimeSynchSignal)}
     */
    @Deprecated
    @TmfSignalHandler
    public void timeRangeUpdated(TmfRangeSynchSignal signal) {
    }

    /**
     * Handles the time synch updated signal. It updates the time range
     * statistics.
     *
     * @param signal
     *            Contains the information about the new selected time range.
     * @since 2.1
     */
    @TmfSignalHandler
    public void timeSynchUpdated(TmfTimeSynchSignal signal) {
        if (fTrace == null) {
            return;
        }
        ITmfTimestamp begin = signal.getBeginTime();
        ITmfTimestamp end = signal.getEndTime();
        TmfTimeRange timeRange = new TmfTimeRange(begin, end);
        requestTimeRangeData(fTrace, timeRange);
    }

    // ------------------------------------------------------------------------
    // Class methods
    // ------------------------------------------------------------------------

    /*
     * Returns the primary control associated with this viewer.
     *
     * @return the SWT control which displays this viewer's content
     */
    @Override
    public Control getControl() {
        return fTreeViewer.getControl();
    }

    /**
     * Get the input of the viewer.
     *
     * @return an object representing the input of the statistics viewer.
     */
    public Object getInput() {
        return fTreeViewer.getInput();
    }

    /**
     * Return the size of the request when performing background request.
     *
     * @return the block size for background request.
     */
    public int getPageSize() {
        return PAGE_SIZE;
    }

    /**
     * Return the number of events to receive before a refresh of the viewer is
     * performed.
     *
     * @return the input refresh rate
     */
    public long getRefreshRate() {
        return STATS_INPUT_CHANGED_REFRESH;
    }

    /**
     * This method can be overridden to implement another way of representing
     * the statistics data and to retrieve the information for display.
     *
     * @return a TmfStatisticsData object.
     */
    public TmfStatisticsTree getStatisticData() {
        if (fStatisticsData == null) {
            fStatisticsData = new TmfStatisticsTree();
        }
        return fStatisticsData;
    }

    /**
     * Returns a unique ID based on name to be associated with the statistics
     * tree for this viewer. For a same name, it will always return the same ID.
     *
     * @return a unique statistics tree ID.
     */
    public String getTreeID() {
        return getName() + fInstanceNb;
    }

    @Override
    public void refresh() {
        final Control viewerControl = getControl();
        // Ignore update if disposed
        if (viewerControl.isDisposed()) {
            return;
        }

        viewerControl.getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (!viewerControl.isDisposed()) {
                    fTreeViewer.refresh();
                }
            }
        });
    }

    /**
     * Will force a request on the partial event count if one is needed.
     */
    public void sendPartialRequestOnNextUpdate() {
        synchronized (fStatisticsRangeUpdateSyncObj) {
            fSendRangeRequest = true;
        }
    }

    /**
     * Focus on the statistics tree of the viewer
     */
    public void setFocus() {
        fTreeViewer.getTree().setFocus();
    }

    /**
     * Cancels the request if it is not already completed
     *
     * @param request
     *            The request to be canceled
     * @since 3.0
     */
    protected void cancelOngoingRequest(ITmfEventRequest request) {
        if (request != null && !request.isCompleted()) {
            request.cancel();
        }
    }

    /**
     * This method can be overridden to change the representation of the data in
     * the columns.
     *
     * @return an object implementing ITmfBaseColumnDataProvider.
     */
    protected ITmfColumnDataProvider getColumnDataProvider() {
        return new TmfBaseColumnDataProvider();
    }

    /**
     * Initialize the content that will be drawn in this viewer
     *
     * @param parent
     *            The parent of the control to create
     */
    protected void initContent(Composite parent) {
        final List<TmfBaseColumnData> columnDataList = getColumnDataProvider().getColumnData();

        fTreeViewer = new TreeViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        fTreeViewer.setContentProvider(new TmfTreeContentProvider());
        fTreeViewer.getTree().setHeaderVisible(true);
        fTreeViewer.setUseHashlookup(true);

        // Creates the columns defined by the column data provider
        for (final TmfBaseColumnData columnData : columnDataList) {
            final TreeViewerColumn treeColumn = new TreeViewerColumn(fTreeViewer, columnData.getAlignment());
            treeColumn.getColumn().setText(columnData.getHeader());
            treeColumn.getColumn().setWidth(columnData.getWidth());
            treeColumn.getColumn().setToolTipText(columnData.getTooltip());

            if (columnData.getComparator() != null) { // A comparator is defined.
                // Adds a listener on the columns header for sorting purpose.
                treeColumn.getColumn().addSelectionListener(new SelectionAdapter() {

                    private ViewerComparator reverseComparator;

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        // Initializes the reverse comparator once.
                        if (reverseComparator == null) {
                            reverseComparator = new ViewerComparator() {
                                @Override
                                public int compare(Viewer viewer, Object e1, Object e2) {
                                    return -1 * columnData.getComparator().compare(viewer, e1, e2);
                                }
                            };
                        }

                        if (fTreeViewer.getTree().getSortDirection() == SWT.UP
                                || fTreeViewer.getTree().getSortColumn() != treeColumn.getColumn()) {
                            /*
                             * Puts the descendant order if the old order was
                             * up or if the selected column has changed.
                             */
                            fTreeViewer.setComparator(columnData.getComparator());
                            fTreeViewer.getTree().setSortDirection(SWT.DOWN);
                        } else {
                            /*
                             * Puts the ascendant ordering if the selected
                             * column hasn't changed.
                             */
                            fTreeViewer.setComparator(reverseComparator);
                            fTreeViewer.getTree().setSortDirection(SWT.UP);
                        }
                        fTreeViewer.getTree().setSortColumn(treeColumn.getColumn());
                    }
                });
            }
            treeColumn.setLabelProvider(columnData.getLabelProvider());
        }

        // Handler that will draw the bar charts.
        fTreeViewer.getTree().addListener(SWT.EraseItem, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (columnDataList.get(event.index).getPercentageProvider() != null) {
                    TmfStatisticsTreeNode node = (TmfStatisticsTreeNode) event.item.getData();

                    double percentage = columnDataList.get(event.index).getPercentageProvider().getPercentage(node);
                    if (percentage == 0) {  // No bar to draw
                        return;
                    }

                    if ((event.detail & SWT.SELECTED) > 0) {    // The item is selected.
                        // Draws our own background to avoid overwritten the bar.
                        event.gc.fillRectangle(event.x, event.y, event.width, event.height);
                        event.detail &= ~SWT.SELECTED;
                    }

                    int barWidth = (int) ((fTreeViewer.getTree().getColumn(event.index).getWidth() - 8) * percentage);
                    int oldAlpha = event.gc.getAlpha();
                    Color oldForeground = event.gc.getForeground();
                    Color oldBackground = event.gc.getBackground();
                    /*
                     * Draws a transparent gradient rectangle from the color of
                     * foreground and background.
                     */
                    event.gc.setAlpha(64);
                    event.gc.setForeground(event.item.getDisplay().getSystemColor(SWT.COLOR_BLUE));
                    event.gc.setBackground(event.item.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
                    event.gc.fillGradientRectangle(event.x, event.y, barWidth, event.height, true);
                    event.gc.drawRectangle(event.x, event.y, barWidth, event.height);
                    // Restores old values
                    event.gc.setForeground(oldForeground);
                    event.gc.setBackground(oldBackground);
                    event.gc.setAlpha(oldAlpha);
                    event.detail &= ~SWT.BACKGROUND;
                }
            }
        });

        // Initializes the comparator parameters
        fTreeViewer.setComparator(columnDataList.get(0).getComparator());
        fTreeViewer.getTree().setSortColumn(fTreeViewer.getTree().getColumn(0));
        fTreeViewer.getTree().setSortDirection(SWT.DOWN);
    }

    /**
     * Initializes the input for the tree viewer.
     */
    protected void initInput() {
        String treeID = getTreeID();
        TmfStatisticsTreeNode statisticsTreeNode;
        if (TmfStatisticsTreeManager.containsTreeRoot(treeID)) {
            // The statistics root is already present
            statisticsTreeNode = TmfStatisticsTreeManager.getStatTreeRoot(treeID);

            // Checks if the trace is already in the statistics tree.
            int numNodeTraces = statisticsTreeNode.getNbChildren();

            ITmfTrace[] traces = TmfTraceManager.getTraceSet(fTrace);
            int numTraces = traces.length;

            if (numTraces == numNodeTraces) {
                boolean same = true;
                /*
                 * Checks if the experiment contains the same traces as when
                 * previously selected.
                 */
                for (int i = 0; i < numTraces; i++) {
                    String traceName = traces[i].getName();
                    if (!statisticsTreeNode.containsChild(traceName)) {
                        same = false;
                        break;
                    }
                }

                if (same) {
                    // No need to reload data, all traces are already loaded
                    fTreeViewer.setInput(statisticsTreeNode);
                    return;
                }
                // Clears the old content to start over
                statisticsTreeNode.reset();
            }
        } else {
            // Creates a new tree
            statisticsTreeNode = TmfStatisticsTreeManager.addStatsTreeRoot(treeID, getStatisticData());
        }

        // Sets the input to a clean data model
        fTreeViewer.setInput(statisticsTreeNode);
        resetUpdateSynchronization();
    }

    /**
     * Tells if the viewer is listening to a trace.
     *
     * @param trace
     *            The trace that the viewer may be listening
     * @return true if the viewer is listening to the trace, false otherwise
     */
    protected boolean isListeningTo(ITmfTrace trace) {
        if (fProcessAll || trace == fTrace) {
            return true;
        }
        return false;
    }

    /**
     * Called when an trace request has been completed successfully.
     *
     * @param global
     *            Tells if the request is a global or time range (partial)
     *            request.
     */
    protected void modelComplete(boolean global) {
        refresh();
        waitCursor(false);
        if (global) {
            sendPendingUpdate();
        }
    }

    /**
     * Called when an trace request has failed or has been cancelled.
     *
     * @param isGlobalRequest
     *            Tells if the request is a global or time range (partial)
     *            request.
     */
    protected void modelIncomplete(boolean isGlobalRequest) {
        if (isGlobalRequest) {  // Clean the global statistics
            /*
             * No need to reset the global number of events, since the index of
             * the last requested event is known.
             */
            resetUpdateSynchronization();
            sendPendingUpdate();
        } else {    // Clean the partial statistics
            resetTimeRangeValue();
        }
        refresh();
        waitCursor(false);
    }

    /**
     * Sends the request to the trace for the whole trace
     *
     * @param trace
     *            The trace used to send the request
     * @param timeRange
     *            The range to request to the trace
     */
    protected void requestData(final ITmfTrace trace, final TmfTimeRange timeRange) {
        buildStatisticsTree(trace, timeRange, true);
    }

    /**
     * Sends the time range request from the trace
     *
     * @param trace
     *            The trace used to send the request
     * @param timeRange
     *            The range to request to the trace
     */
    protected void requestTimeRangeData(final ITmfTrace trace, final TmfTimeRange timeRange) {
        fRequestedTimerange = timeRange;
        buildStatisticsTree(trace, timeRange, false);
    }

    /**
     * Requests all the data of the trace to the state system which
     * contains information about the statistics.
     *
     * Since the viewer may be listening to multiple traces, it may receive
     * an experiment rather than a single trace. The filtering is done with the
     * method {@link #isListeningTo(String trace)}.
     *
     * @param trace
     *            The trace for which a request must be done
     * @param timeRange
     *            The time range that will be requested to the state system
     * @param isGlobal
     *            Tells if the request is for the global event count or the
     *            partial one.
     */
    private void buildStatisticsTree(final ITmfTrace trace, final TmfTimeRange timeRange, final boolean isGlobal) {
        final TmfStatisticsTreeNode statTree = TmfStatisticsTreeManager.getStatTreeRoot(getTreeID());
        final TmfStatisticsTree statsData = TmfStatisticsTreeManager.getStatTree(getTreeID());
        if (statsData == null) {
            return;
        }

        synchronized (statsData) {
            if (isGlobal) {
                statTree.resetGlobalValue();
            } else {
                statTree.resetTimeRangeValue();
            }

            for (final ITmfTrace aTrace : TmfTraceManager.getTraceSet(trace)) {
                if (!isListeningTo(aTrace)) {
                    continue;
                }

                /* Retrieve the statistics object */
                final TmfStatisticsModule statsMod = aTrace.getAnalysisModuleOfClass(TmfStatisticsModule.class, TmfStatisticsModule.ID);
                if (statsMod == null) {
                    /* No statistics module available for this trace */
                    continue;
                }

                /* Run the potentially long queries in a separate thread */
                Thread statsThread = new Thread("Statistics update") { //$NON-NLS-1$
                    @Override
                    public void run() {
                        /* Wait until the analysis is ready */
                        if (!statsMod.waitForCompletion(new NullProgressMonitor())) {
                            return;
                        }

                        ITmfStatistics stats = statsMod.getStatistics();
                        if (stats == null) {
                            /* It should have worked, but didn't */
                            return;
                        }

                        /*
                         * The generic statistics are stored in nanoseconds, so
                         * we must make sure the time range is scaled correctly.
                         */
                        long start = timeRange.getStartTime().normalize(0, TIME_SCALE).getValue();
                        long end = timeRange.getEndTime().normalize(0, TIME_SCALE).getValue();

                        Map<String, Long> map = stats.getEventTypesInRange(start, end);
                        updateStats(isGlobal, map);
                    }
                };
                statsThread.start();
                return;
            }
        }
    }

    /**
     * Whenever a trace's statistics back-end finishes computing the statistics
     * for a given interval, it will send the StatsUpdated signal. This method
     * will receive this signal and update the statistics view accordingly.
     *
     * @param sig
     *            The signal that is received
     */
    private void updateStats(boolean isGlobal, Map<String, Long> eventsPerType) {

        final TmfStatisticsTree statsData = TmfStatisticsTreeManager.getStatTree(getTreeID());
        Map<String, Long> map = eventsPerType;
        String name = fTrace.getName();

        /*
         * "Global", "partial", "total", etc., it's all very confusing...
         *
         * The base view shows the total count for the trace and for
         * each even types, organized in columns like this:
         *
         *                   |  Global  |  Time range |
         * trace name        |    A     |      B      |
         *    Event Type     |          |             |
         *       <event 1>   |    C     |      D      |
         *       <event 2>   |   ...    |     ...     |
         *         ...       |          |             |
         *
         * Here, we called the cells like this:
         *  A : GlobalTotal
         *  B : TimeRangeTotal
         *  C : GlobalTypeCount(s)
         *  D : TimeRangeTypeCount(s)
         */

        /* Fill in an the event counts (either cells C or D) */
        for (Map.Entry<String, Long> entry : map.entrySet()) {
            statsData.setTypeCount(name, entry.getKey(), isGlobal, entry.getValue());
        }

        /*
         * Calculate the totals (cell A or B, depending if isGlobal). We will
         * use the results of the previous request instead of sending another
         * one.
         */
        long globalTotal = 0;
        for (long val : map.values()) {
            globalTotal += val;
        }
        statsData.setTotal(name, isGlobal, globalTotal);

        modelComplete(isGlobal);
    }

    /**
     * Resets the number of events within the time range
     */
    protected void resetTimeRangeValue() {
        TmfStatisticsTreeNode treeModelRoot = TmfStatisticsTreeManager.getStatTreeRoot(getTreeID());
        if (treeModelRoot != null && treeModelRoot.hasChildren()) {
            treeModelRoot.resetTimeRangeValue();
        }
    }

    /**
     * When the trace is loading the cursor will be different so the user
     * knows that the processing is not finished yet.
     *
     * Calls to this method are stacked.
     *
     * @param waitRequested
     *            Indicates if we need to show the waiting cursor, or the
     *            default one.
     */
    protected void waitCursor(final boolean waitRequested) {
        if ((fTreeViewer == null) || (fTreeViewer.getTree().isDisposed())) {
            return;
        }

        boolean needsUpdate = false;
        Display display = fTreeViewer.getControl().getDisplay();
        if (waitRequested) {
            fWaitCursorCount++;
            if (fWaitCursor == null) { // The cursor hasn't been initialized yet
                fWaitCursor = new Cursor(display, SWT.CURSOR_WAIT);
            }
            if (fWaitCursorCount == 1) { // The cursor is not in waiting mode
                needsUpdate = true;
            }
        } else {
            if (fWaitCursorCount > 0) { // The cursor is in waiting mode
                fWaitCursorCount--;
                if (fWaitCursorCount == 0) { // No more reason to wait
                    // Put back the default cursor
                    needsUpdate = true;
                }
            }
        }

        if (needsUpdate) {
            // Performs the updates on the UI thread
            display.asyncExec(new Runnable() {
                @Override
                public void run() {
                    if ((fTreeViewer != null)
                            && (!fTreeViewer.getTree().isDisposed())) {
                        Cursor cursor = null; // indicates default
                        if (waitRequested) {
                            cursor = fWaitCursor;
                        }
                        fTreeViewer.getControl().setCursor(cursor);
                    }
                }
            });
        }
    }

    // ------------------------------------------------------------------------
    // Methods reserved for the streaming functionality
    // ------------------------------------------------------------------------

    /**
     * Resets update synchronization information
     */
    protected void resetUpdateSynchronization() {
        synchronized (fStatisticsUpdateSyncObj) {
            fStatisticsUpdateBusy = false;
            fStatisticsUpdatePending = false;
            fStatisticsUpdateRange = null;
        }
    }

    /**
     * Checks if statistics update is ongoing. If it is ongoing, the new time
     * range is stored as pending
     *
     * @param timeRange
     *            - new time range
     * @return true if statistic update is ongoing else false
     */
    protected boolean checkUpdateBusy(TmfTimeRange timeRange) {
        synchronized (fStatisticsUpdateSyncObj) {
            if (fStatisticsUpdateBusy) {
                fStatisticsUpdatePending = true;
                if (fStatisticsUpdateRange == null
                        || timeRange.getEndTime().compareTo(fStatisticsUpdateRange.getEndTime()) > 0) {
                    fStatisticsUpdateRange = timeRange;
                }
                return true;
            }
            fStatisticsUpdateBusy = true;
            return false;
        }
    }

    /**
     * Sends pending request (if any)
     */
    protected void sendPendingUpdate() {
        synchronized (fStatisticsUpdateSyncObj) {
            fStatisticsUpdateBusy = false;
            if (fStatisticsUpdatePending) {
                fStatisticsUpdatePending = false;
                requestData(fTrace, fStatisticsUpdateRange);
                fStatisticsUpdateRange = null;
            }
        }
    }
}
