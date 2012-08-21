/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.statistics;

import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.linuxtools.tmf.core.component.TmfComponent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentRangeUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.viewers.TmfViewer;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.AbsTmfStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.ITmfColumnDataProvider;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfBaseColumnData;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfBaseColumnDataProvider;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfBaseStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeNode;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeRootFactory;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfTreeContentProvider;
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
     * The initial window span (in nanoseconds)
     */
    public static final long INITIAL_WINDOW_SPAN = (1L * 100 * 1000 * 1000); // .1sec

    /**
     * Timestamp scale (nanosecond)
     */
    public static final byte TIME_SCALE = -9;

    /**
     * Default PAGE_SIZE for background requests.
     */
    protected static final int PAGE_SIZE = 50000;

    /**
     * Refresh frequency.
     */
    protected final Long STATS_INPUT_CHANGED_REFRESH = 5000L;

    /**
     * Stores the request to the experiment
     */
    protected TmfStatisticsRequest fRequest = null;

    /**
     * Stores the ranged request to the experiment
     */
    protected TmfStatisticsRequest fRequestRange = null;

    /**
     * The actual tree viewer to display
     */
    protected TreeViewer fTreeViewer;

    /**
     * The statistics tree linked to this viewer
     */
    protected AbsTmfStatisticsTree fStatisticsData;

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
     * The trace that is displayed by this viewer
     */
    private ITmfTrace fTrace;

    /**
     * Object to store the cursor while waiting for the experiment to load
     */
    private Cursor fWaitCursor = null;

    /**
     * Counts the number of times waitCursor() has been called. It avoids
     * removing the waiting cursor, since there may be multiple requests running
     * at the same time.
     */
    private int fWaitCursorCount = 0;

    /**
     * Tells to send a time range request when the experiment gets updated.
     */
    private boolean fSendRangeRequest = true;

    /**
     * Empty constructor. To be used in conjunction with
     * {@link TmfStatisticsViewer#init(Composite, String, ITmfTrace)}
     */
    public TmfStatisticsViewer() {
        super();
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

        // The viewer will process all events if he is assigned to the experiment
        fProcessAll = (trace instanceof TmfExperiment);

        initContent(parent);
        initInput();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.linuxtools.tmf.core.component.TmfComponent#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        if (fWaitCursor != null) {
            fWaitCursor.dispose();
        }
        /*
         * Make sure there is no request running before removing the statistics
         * tree
         */
        cancelOngoingRequest(fRequestRange);
        cancelOngoingRequest(fRequest);

        // Clean the model
        TmfStatisticsTreeRootFactory.removeStatTreeRoot(getTreeID());
    }

    /**
     * Handles the signal about new experiment range.
     *
     * @param signal
     *            The experiment range updated signal
     */
    @TmfSignalHandler
    public void experimentRangeUpdated(TmfExperimentRangeUpdatedSignal signal) {
        TmfExperiment experiment = signal.getExperiment();
        // validate
        if (!experiment.equals(TmfExperiment.getCurrentExperiment())) {
            return;
        }

        // Sends the time range request only once in this method.
        if (fSendRangeRequest) {
            fSendRangeRequest = false;
            // Calculate the selected time range to request
            long startTime = signal.getRange().getStartTime().normalize(0, TIME_SCALE).getValue();
            TmfTimestamp startTS = new TmfTimestamp(startTime, TIME_SCALE);
            TmfTimestamp endTS = new TmfTimestamp(startTime + INITIAL_WINDOW_SPAN, TIME_SCALE);
            TmfTimeRange timeRange = new TmfTimeRange(startTS, endTS);

            requestTimeRangeData(experiment, timeRange);
        }
        requestData(experiment, signal.getRange());
    }

    /**
     * Handles the experiment updated signal. This will detect new events in
     * case the indexing is not coalesced with a statistics request.
     *
     * @param signal
     *            The experiment updated signal
     */
    @TmfSignalHandler
    public void experimentUpdated(TmfExperimentUpdatedSignal signal) {
        TmfExperiment experiment = signal.getExperiment();
        if (!experiment.equals(TmfExperiment.getCurrentExperiment())) {
            return;
        }

        long nbEvents = 0;
        if (fRequest != null) {
            nbEvents = fRequest.getLastEventIndex();
        }
        /*
         * In the normal case, the statistics request is coalesced with indexing
         * and the number of events are the same, there is nothing to do. But if
         * it's not the case, trigger a new request to count the new events.
         */
        if (nbEvents < experiment.getNbEvents()) {
            requestData(experiment, experiment.getTimeRange());
        }
    }

    /**
     * * Handles the time range updated signal. It updates the time range
     * statistics.
     *
     * @param signal
     *            Contains the information about the new selected time range.
     */
    @TmfSignalHandler
    public void timeRangeUpdated(TmfRangeSynchSignal signal) {
        /*
         * It is possible that the time range changes while a request is
         * processing.
         */
        cancelOngoingRequest(fRequestRange);

        requestTimeRangeData(TmfExperiment.getCurrentExperiment(), signal.getCurrentRange());
    }

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
    public AbsTmfStatisticsTree getStatisticData() {
        if (fStatisticsData == null) {
            fStatisticsData = new TmfBaseStatisticsTree();
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
    protected void cancelOngoingRequest(ITmfDataRequest request) {
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
     *
     * @param input
     *            The input of this viewer, or <code>null</code> if none
     */
    protected void initInput() {
        String treeID = getTreeID();
        TmfStatisticsTreeNode experimentTreeNode;
        if (TmfStatisticsTreeRootFactory.containsTreeRoot(treeID)) {
            // The experiment root is already present
            experimentTreeNode = TmfStatisticsTreeRootFactory.getStatTreeRoot(treeID);

            // Checks if the trace is already in the statistics tree.
            int numNodeTraces = experimentTreeNode.getNbChildren();

            int numTraces = 1;
            ITmfTrace[] trace = { fTrace };
            // For experiment, gets all the traces within it
            if (fTrace instanceof TmfExperiment) {
                TmfExperiment experiment = (TmfExperiment) fTrace;
                numTraces = experiment.getTraces().length;
                trace = experiment.getTraces();
            }

            if (numTraces == numNodeTraces) {
                boolean same = true;
                /*
                 * Checks if the experiment contains the same traces as when
                 * previously selected.
                 */
                for (int i = 0; i < numTraces; i++) {
                    String traceName = trace[i].getName();
                    if (!experimentTreeNode.containsChild(traceName)) {
                        same = false;
                        break;
                    }
                }

                if (same) {
                    // No need to reload data, all traces are already loaded
                    fTreeViewer.setInput(experimentTreeNode);
                    return;
                }
                // Clears the old content to start over
                experimentTreeNode.reset();
            }
        } else {
            // Creates a new tree
            experimentTreeNode = TmfStatisticsTreeRootFactory.addStatsTreeRoot(treeID, getStatisticData());
        }

        // Sets the input to a clean data model
        fTreeViewer.setInput(experimentTreeNode);
        resetUpdateSynchronization();
    }

    /**
     * Tells if the viewer is listening to a trace from the selected experiment.
     *
     * @param traceName
     *            The trace that the viewer may be listening
     * @return true if the viewer is listening to the trace, false otherwise
     */
    protected boolean isListeningTo(String traceName) {
        if (fProcessAll || traceName.equals(fTrace.getName())) {
            return true;
        }
        return false;
    }

    /**
     * Called when an experiment request has been completed successfully.
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
     * Called when an experiment request has failed or has been cancelled.
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
     * Sends the request to the experiment for the whole trace
     *
     * @param experiment
     *            The experiment used to send the request
     * @param range
     *            The range to request to the experiment
     */
    protected void requestData(TmfExperiment experiment, TmfTimeRange range) {
        // Check if an update is already ongoing
        if (checkUpdateBusy(range)) {
            return;
        }

        long index = 0;
        /*
         * Sets the index to the last event retrieved from the experiment during
         * the last request.
         */
        if (fRequest != null) {
            index = fRequest.getLastEventIndex();
        }

        fRequest = new TmfStatisticsRequest(this, range, index, true);
        waitCursor(true);
        experiment.sendRequest(fRequest);
    }

    /**
     * Sends the time range request from the experiment
     *
     * @param experiment
     *            The experiment used to send the request
     * @param range
     *            The range to request to the experiment
     */
    protected void requestTimeRangeData(TmfExperiment experiment, TmfTimeRange range) {
        resetTimeRangeValue();
        fRequestRange = new TmfStatisticsRequest(this, range, 0, false);
        waitCursor(true);
        experiment.sendRequest(fRequestRange);
    }

    /**
     * Resets the number of events within the time range
     */
    protected void resetTimeRangeValue() {
        TmfStatisticsTreeNode treeModelRoot = TmfStatisticsTreeRootFactory.getStatTreeRoot(getTreeID());
        if (treeModelRoot != null && treeModelRoot.hasChildren()) {
            treeModelRoot.resetTimeRangeValue();
        }
    }

    /**
     * When the experiment is loading the cursor will be different so the user
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
                requestData(TmfExperiment.getCurrentExperiment(), fStatisticsUpdateRange);
                fStatisticsUpdateRange = null;
            }
        }
    }
}
