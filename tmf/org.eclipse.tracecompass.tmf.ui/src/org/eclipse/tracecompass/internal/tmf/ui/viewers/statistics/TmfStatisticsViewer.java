/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
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
 *   Bernd Hufmann - Fix range selection updates
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.viewers.statistics;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.tracecompass.tmf.core.component.TmfComponent;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStatisticsModule;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.internal.tmf.ui.viewers.piecharts.TmfPieChartViewer;
import org.eclipse.tracecompass.internal.tmf.ui.viewers.piecharts.model.TmfPieChartStatisticsModel;
import org.eclipse.tracecompass.internal.tmf.ui.viewers.statistics.model.TmfBaseColumnData;
import org.eclipse.tracecompass.internal.tmf.ui.viewers.statistics.model.TmfBaseColumnDataProvider;
import org.eclipse.tracecompass.internal.tmf.ui.viewers.statistics.model.TmfStatisticsFormatter;
import org.eclipse.tracecompass.internal.tmf.ui.viewers.statistics.model.TmfStatisticsTree;
import org.eclipse.tracecompass.internal.tmf.ui.viewers.statistics.model.TmfStatisticsTreeManager;
import org.eclipse.tracecompass.internal.tmf.ui.viewers.statistics.model.TmfStatisticsTreeNode;
import org.eclipse.tracecompass.internal.tmf.ui.viewers.statistics.model.TmfTreeContentProvider;

/**
 * A basic viewer to display statistics in the statistics view.
 *
 * It is linked to a single ITmfTrace until its disposal.
 *
 * @author Mathieu Denis
 */
public class TmfStatisticsViewer extends TmfViewer {

    /** The actual tree viewer to display */
    private TreeViewer fTreeViewer;

    /** Update range synchronization object */
    private final Object fStatisticsRangeUpdateSyncObj = new Object();

    /** The statistics tree linked to this viewer */
    private TmfStatisticsTree fStatisticsData;

    /** The trace that is displayed by this viewer */
    private ITmfTrace fTrace;

    /** Indicates to process all events */
    private boolean fProcessAll;

    /** View instance counter (for multiple statistics views) */
    private static int fCountInstance = 0;

    /** Number of this instance. Used as an instance ID. */
    private int fInstanceNb;

    /** Object to store the cursor while waiting for the trace to load */
    private Cursor fWaitCursor = null;

    /**
     * Counts the number of times waitCursor() has been called. It avoids
     * removing the waiting cursor, since there may be multiple requests running
     * at the same time.
     */
    private int fWaitCursorCount = 0;

    /** Tells to send a time range request when the trace gets updated. */
    private boolean fSendRangeRequest = true;

    private final Map<ITmfTrace, Job> fUpdateJobsPartial = new HashMap<>();
    private final Map<ITmfTrace, Job> fUpdateJobsGlobal = new HashMap<>();

    private TmfPieChartViewer fPieChartViewer;

    private SashForm fSash;

    private TmfPieChartStatisticsModel fPieChartModel;

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

        for (Job j : fUpdateJobsGlobal.values()) {
            j.cancel();
        }

        for (Job j : fUpdateJobsPartial.values()) {
            j.cancel();
        }

        // Clean the model for this viewer
        TmfStatisticsTreeManager.removeStatTreeRoot(getTreeID());
        fPieChartViewer.reinitializeCharts();
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

                TmfTraceContext ctx = TmfTraceManager.getInstance().getCurrentTraceContext();
                TmfTimeRange timeRange = ctx.getSelectionRange();
                requestTimeRangeData(trace, timeRange);
            }
        }
        requestData(trace, signal.getRange());
    }

    /**
     * Handles the time synch updated signal. It updates the time range
     * statistics.
     *
     * @param signal
     *            Contains the information about the new selected time range.
     * @since 1.0
     */
    @TmfSignalHandler
    public void timeSynchUpdated(TmfSelectionRangeUpdatedSignal signal) {
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
        return fSash;
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
     * @return the model of the piecharts in this viewer
     * @since 2.0
     */
    public TmfPieChartStatisticsModel getPieChartModel() {
        if (fPieChartModel == null) {
            fPieChartModel = new TmfPieChartStatisticsModel();
        }
        return fPieChartModel;
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
        refreshTree();
        refreshPieCharts(true, true);
    }

    /**
     * Only refreshes the Tree viewer
     */
    private void refreshTree() {
        final Control viewerControl = getControl();
        // Ignore update if disposed
        if (viewerControl.isDisposed()) {
            return;
        }

        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (!viewerControl.isDisposed()) {
                    fTreeViewer.refresh();
                }
            }
        });
    }

    /**
     * Only refreshes the piecharts depending on the parameters
     *
     * @param refreshGlobal
     *            if we have to refresh the global piechart
     * @param refreshSelection
     *            if we have to refresh the selection piechart
     * @since 2.0
     */
    protected void refreshPieCharts(final boolean refreshGlobal, final boolean refreshSelection) {
        final Control viewerControl = getControl();
        // Ignore update if disposed
        if (viewerControl.isDisposed()) {
            return;
        }

        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (!viewerControl.isDisposed()) {
                    fPieChartViewer.refresh(refreshGlobal, refreshSelection);
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
     * @return An object of type {@link TmfBaseColumnDataProvider}.
     */
    protected TmfBaseColumnDataProvider getColumnDataProvider() {
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

        fSash = new SashForm(parent, SWT.HORIZONTAL);

        fTreeViewer = new TreeViewer(fSash, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        fPieChartViewer = new TmfPieChartViewer(fSash);
        fPieChartViewer.addEventTypeSelectionListener(new Listener() {

            @Override
            public void handleEvent(Event event) {
                String eventTypeName = event.text;
                if (getStatisticData().getRootNode() == null ||
                        fTreeViewer.getTree() == null) {
                    return;
                }
                /* Get all the nodes corresponding to the event name */
                List<TmfStatisticsTreeNode> nodes = (List<TmfStatisticsTreeNode>) getStatisticData().getRootNode().findChildren(eventTypeName, true);
                if (nodes.isEmpty()) {
                    /* Shouldn't happen, except for when selecting "Others" */
                    return;
                }
                /* Only select the first in the collection */
                fTreeViewer.setSelection(new StructuredSelection(nodes.get(0)), true);
            }
        });

        /* Make sure the sash is split in 2 equal parts */
        fSash.setWeights(new int[] { 100, 100 });

        fTreeViewer.setContentProvider(new TmfTreeContentProvider());
        fTreeViewer.getTree().setHeaderVisible(true);
        fTreeViewer.setUseHashlookup(true);

        // Creates the columns defined by the column data provider
        for (final TmfBaseColumnData columnData : columnDataList) {
            final TreeViewerColumn treeColumn = new TreeViewerColumn(fTreeViewer, columnData.getAlignment());
            treeColumn.getColumn().setText(columnData.getHeader());
            treeColumn.getColumn().setWidth(columnData.getWidth());
            treeColumn.getColumn().setToolTipText(columnData.getTooltip());

            // If is dummy column
            if (columnData == columnDataList.get(TmfBaseColumnDataProvider.StatsColumn.DUMMY.getIndex())) {
                treeColumn.getColumn().setResizable(false);
            }

            // A comparator is defined.
            if (columnData.getComparator() != null) {
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
                             * Puts the descendant order if the old order was up
                             * or if the selected column has changed.
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

        // Handler that will draw the percentages and the bar charts.
        fTreeViewer.getTree().addListener(SWT.EraseItem, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (columnDataList.get(event.index).getPercentageProvider() != null) {

                    TmfStatisticsTreeNode node = (TmfStatisticsTreeNode) event.item.getData();

                    // If node is hidden, exit immediately.
                    if (TmfBaseColumnDataProvider.HIDDEN_FOLDER_LEVELS.contains(node.getName())) {
                        return;
                    }

                    // Otherwise, get percentage and draw bar and text if
                    // applicable.
                    double percentage = columnDataList.get(event.index).getPercentageProvider().getPercentage(node);

                    // The item is selected.
                    if ((event.detail & SWT.SELECTED) > 0) {
                        // Draws our own background to avoid overwriting the
                        // bar.
                        event.gc.fillRectangle(event.x, event.y, event.width, event.height);
                        event.detail &= ~SWT.SELECTED;
                    }

                    // Drawing the percentage text
                    // if events are present in top node
                    // and the current node is not the top node
                    // and if is total or partial events column.
                    // If not, exit the method.
                    if (!((event.index == TmfBaseColumnDataProvider.StatsColumn.TOTAL.getIndex() || event.index == TmfBaseColumnDataProvider.StatsColumn.PARTIAL.getIndex())
                    && node != node.getTop())) {
                        return;
                    }

                    long eventValue = event.index == TmfBaseColumnDataProvider.StatsColumn.TOTAL.getIndex() ?
                            node.getTop().getValues().getTotal() : node.getTop().getValues().getPartial();

                    if (eventValue != 0) {

                        int oldAlpha = event.gc.getAlpha();
                        Color oldForeground = event.gc.getForeground();
                        Color oldBackground = event.gc.getBackground();

                        // Bar to draw
                        if (percentage != 0) {
                            /*
                             * Draws a transparent gradient rectangle from the
                             * color of foreground and background.
                             */
                            int barWidth = (int) ((fTreeViewer.getTree().getColumn(event.index).getWidth() - 8) * percentage);
                            event.gc.setAlpha(64);
                            event.gc.setForeground(event.item.getDisplay().getSystemColor(SWT.COLOR_BLUE));
                            event.gc.setBackground(event.item.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
                            event.gc.fillGradientRectangle(event.x, event.y, barWidth, event.height, true);
                            event.gc.drawRectangle(event.x, event.y, barWidth, event.height);

                            // Restore old values
                            event.gc.setBackground(oldBackground);
                            event.gc.setAlpha(oldAlpha);
                            event.detail &= ~SWT.BACKGROUND;

                        }

                        String percentageText = TmfStatisticsFormatter.toPercentageText(percentage);
                        String absoluteNumberText = TmfStatisticsFormatter.toColumnData(node, TmfBaseColumnDataProvider.StatsColumn.getColumn(event.index));

                        if (event.width > event.gc.stringExtent(percentageText).x + event.gc.stringExtent(absoluteNumberText).x) {
                            int textHeight = event.gc.stringExtent(percentageText).y;
                            event.gc.setForeground(event.item.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
                            event.gc.drawText(percentageText, event.x, event.y + (event.height - textHeight) / 2, true);
                        }

                        // Restores old values
                        event.gc.setForeground(oldForeground);

                    }
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

            Collection<ITmfTrace> traces = TmfTraceManager.getTraceSet(fTrace);
            int numTraces = traces.size();

            if (numTraces == numNodeTraces) {
                boolean same = true;
                /*
                 * Checks if the experiment contains the same traces as when
                 * previously selected.
                 */
                for (ITmfTrace trace : traces) {
                    String traceName = trace.getName();
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
        /* Set a new model for the piecharts and keep a reference */
        fPieChartViewer.setInput(getPieChartModel());
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
        refreshTree();
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
        buildStatisticsTree(trace, timeRange, false);
    }

    /**
     * Requests all the data of the trace to the state system which contains
     * information about the statistics.
     *
     * Since the viewer may be listening to multiple traces, it may receive an
     * experiment rather than a single trace. The filtering is done with the
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
        final TmfStatisticsTree statsData = TmfStatisticsTreeManager.getStatTree(getTreeID());
        if (statsData == null) {
            return;
        }

        Map<ITmfTrace, Job> updateJobs;
        if (isGlobal) {
            updateJobs = fUpdateJobsGlobal;
        } else {
            updateJobs = fUpdateJobsPartial;
        }

        for (ITmfTrace aTrace : TmfTraceManager.getTraceSet(trace)) {
            if (!isListeningTo(aTrace)) {
                continue;
            }

            /* Retrieve the statistics object */
            final TmfStatisticsModule statsMod = TmfTraceUtils.getAnalysisModuleOfClass(aTrace, TmfStatisticsModule.class, TmfStatisticsModule.ID);
            if (statsMod == null) {
                /* No statistics module available for this trace */
                continue;
            }

            Job job = updateJobs.get(aTrace);
            if (job == null) {
                job = new StatisticsUpdateJob("Statistics update", aTrace, isGlobal, timeRange, statsMod, this); //$NON-NLS-1$
                updateJobs.put(aTrace, job);
                job.setSystem(true);
                job.schedule();
            }
        }
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
     * When the trace is loading the cursor will be different so the user knows
     * that the processing is not finished yet.
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

    /**
     * @param isGlobal
     *            if the job to remove is global or partial
     * @param jobTrace
     *            The trace
     */
    void removeFromJobs(boolean isGlobal, ITmfTrace jobTrace) {
        Map<ITmfTrace, Job> updateJobs = isGlobal ? fUpdateJobsGlobal : fUpdateJobsPartial;
        Job job = updateJobs.remove(jobTrace);
        if (job != null) {
            job.cancel();
        }
    }
}
