/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Generalized version based on LTTng
 *   Bernd Hufmann - Updated to use trace reference in TmfEvent and streaming
 *   Mathieu Denis - New request added to update the statistics from the selected time range
 *
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.statistics;

import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentDisposedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentRangeUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.AbsTmfStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.ITmfColumnDataProvider;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.TmfBaseColumnData;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.TmfBaseColumnDataProvider;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.TmfBaseStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.TmfStatisticsTreeNode;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.TmfStatisticsTreeRootFactory;
import org.eclipse.linuxtools.tmf.ui.views.statistics.model.TmfTreeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * The generic Statistics View displays statistics for any kind of traces.
 *
 * It is implemented according to the MVC pattern. - The model is a
 * TmfStatisticsTreeNode built by the State Manager. - The view is built with a
 * TreeViewer. - The controller that keeps model and view synchronized is an
 * observer of the model.
 *
 * @version 2.0
 * @author Mathieu Denis
 */
public class TmfStatisticsView extends TmfView {

    /**
     * The ID correspond to the package in which this class is embedded
     */
    public static final String ID = "org.eclipse.linuxtools.tmf.ui.views.statistics"; //$NON-NLS-1$

    /**
     * The view name.
     */
    public static final String TMF_STATISTICS_VIEW = "StatisticsView"; //$NON-NLS-1$

    /**
     * Refresh frequency
     */
    protected static final Long STATS_INPUT_CHANGED_REFRESH = 5000L;

    /**
     * Default PAGE_SIZE for background requests
     */
    protected static final int PAGE_SIZE = 50000;

    /**
     * The initial window span (in nanoseconds)
     *
     * @since 2.0
     */
    public static final long INITIAL_WINDOW_SPAN = (1L * 100 * 1000 * 1000); // .1sec

    /**
     * Timestamp scale (nanosecond)
     *
     * @since 2.0
     */
    public static final byte TIME_SCALE = -9;

    /**
     * The actual tree viewer to display
     */
    protected TreeViewer fTreeViewer;

    /**
     * Stores the global request to the experiment
     */
    protected ITmfEventRequest fRequest = null;

    /**
     * Stores the ranged request to the experiment
     * @since 2.0
     */
    protected ITmfEventRequest fRequestRange = null;

    /**
     * Update synchronization parameter (used for streaming): Update busy
     * indicator
     */
    protected boolean fStatisticsUpdateBusy = false;

    /**
     * Update synchronization parameter (used for streaming): Update pending
     * indicator
     */
    protected boolean fStatisticsUpdatePending = false;

    /**
     * Update synchronization parameter (used for streaming): Pending Update
     * time range
     */
    protected TmfTimeRange fStatisticsUpdateRange = null;

    /**
     * Update synchronization object.
     */
    protected final Object fStatisticsUpdateSyncObj = new Object();

    /**
     * Flag to force request the data from trace
     */
    protected boolean fRequestData = false;

    /**
     * Object to store the cursor while waiting for the experiment to load
     */
    private Cursor fWaitCursor = null;

    /**
     * View instance counter (for multiple statistic views)
     */
    private static int fCountInstance = 0;

    /**
     * Number of this instance. Used as an instance ID.
     */
    private final int fInstanceNb;

    /**
     * Constructor of a statistics view.
     *
     * @param viewName
     *            The name to give to the view.
     */
    public TmfStatisticsView(String viewName) {
        super(viewName);
        fCountInstance++;
        fInstanceNb = fCountInstance;
    }

    /**
     * Default constructor.
     */
    public TmfStatisticsView() {
        this(TMF_STATISTICS_VIEW);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        final List<TmfBaseColumnData> columnDataList = getColumnDataProvider().getColumnData();
        parent.setLayout(new FillLayout());

        fTreeViewer = new TreeViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        fTreeViewer.setContentProvider(new TmfTreeContentProvider());
        fTreeViewer.getTree().setHeaderVisible(true);
        fTreeViewer.setUseHashlookup(true);

        for (final TmfBaseColumnData columnData : columnDataList) {
            final TreeViewerColumn treeColumn = new TreeViewerColumn(fTreeViewer, columnData.getAlignment());
            treeColumn.getColumn().setText(columnData.getHeader());
            treeColumn.getColumn().setWidth(columnData.getWidth());
            treeColumn.getColumn().setToolTipText(columnData.getTooltip());

            if (columnData.getComparator() != null) {
                treeColumn.getColumn().addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        if (fTreeViewer.getTree().getSortDirection() == SWT.UP || fTreeViewer.getTree().getSortColumn() != treeColumn.getColumn()) {
                            fTreeViewer.setComparator(columnData.getComparator());
                            fTreeViewer.getTree().setSortDirection(SWT.DOWN);
                        } else {
                            fTreeViewer.setComparator(new ViewerComparator() {
                                @Override
                                public int compare(Viewer viewer, Object e1, Object e2) {
                                    return -1 * columnData.getComparator().compare(viewer, e1, e2);
                                }
                            });
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
                    if (percentage == 0) {
                        return;
                    }

                    if ((event.detail & SWT.SELECTED) > 0) {
                        Color oldForeground = event.gc.getForeground();
                        event.gc.setForeground(event.item.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
                        event.gc.fillRectangle(event.x, event.y, event.width, event.height);
                        event.gc.setForeground(oldForeground);
                        event.detail &= ~SWT.SELECTED;
                    }

                    int barWidth = (int) ((fTreeViewer.getTree().getColumn(event.index).getWidth() - 8) * percentage);
                    int oldAlpha = event.gc.getAlpha();
                    Color oldForeground = event.gc.getForeground();
                    Color oldBackground = event.gc.getBackground();
                    event.gc.setAlpha(64);
                    event.gc.setForeground(event.item.getDisplay().getSystemColor(SWT.COLOR_BLUE));
                    event.gc.setBackground(event.item.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
                    event.gc.fillGradientRectangle(event.x, event.y, barWidth, event.height, true);
                    event.gc.drawRectangle(event.x, event.y, barWidth, event.height);
                    event.gc.setForeground(oldForeground);
                    event.gc.setBackground(oldBackground);
                    event.gc.setAlpha(oldAlpha);
                    event.detail &= ~SWT.BACKGROUND;
                }
            }
        });

        fTreeViewer.setComparator(columnDataList.get(0).getComparator());
        fTreeViewer.getTree().setSortColumn(fTreeViewer.getTree().getColumn(0));
        fTreeViewer.getTree().setSortDirection(SWT.DOWN);

        // Read current data if any available
        TmfExperiment experiment = TmfExperiment.getCurrentExperiment();
        if (experiment != null) {
            fRequestData = true;
            // Insert the statistics data into the tree
            TmfExperimentSelectedSignal signal = new TmfExperimentSelectedSignal(this, experiment);
            experimentSelected(signal);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.linuxtools.tmf.ui.views.TmfView#dispose()
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
        // clean the model
        TmfStatisticsTreeRootFactory.removeAll();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        fTreeViewer.getTree().setFocus();
    }

    /**
     * Refresh the view.
     *
     * @param complete
     *            Should a pending update be sent afterwards or not
     */
    public void modelInputChanged(boolean complete) {
        // Ignore update if disposed
        if (fTreeViewer.getTree().isDisposed()) {
            return;
        }

        fTreeViewer.getTree().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (!fTreeViewer.getTree().isDisposed()) {
                    fTreeViewer.refresh();
                }
            }
        });

        if (complete) {
            sendPendingUpdate();
        }
    }

    /**
     * Called when an experiment request has failed or has been cancelled.
     * Remove the data retrieved from the experiment from the statistics tree.
     *
     * @param name
     *            The experiment name
     */
    public void modelIncomplete(String name) {
        Object input = fTreeViewer.getInput();
        if (input != null && input instanceof TmfStatisticsTreeNode) {
            /*
             * The data from this experiment is invalid and shall be removed to
             * refresh upon next selection
             */
            TmfStatisticsTreeRootFactory.removeStatTreeRoot(getTreeID(name));

            // Reset synchronization information
            resetUpdateSynchronization();
            modelInputChanged(false);
        }
        waitCursor(false);
    }

    /**
     * Handles the signal about disposal of the current experiment.
     *
     * @param signal
     *            The disposed signal
     */
    @TmfSignalHandler
    public void experimentDisposed(TmfExperimentDisposedSignal signal) {
        if (signal.getExperiment() != TmfExperiment.getCurrentExperiment()) {
            return;
        }
        /*
         * The range request must be cancelled first, since the global one removes
         * the statistics tree
         */
        cancelOngoingRequest(fRequestRange);
        cancelOngoingRequest(fRequest);
        resetTimeRangeValue();
    }

    /**
     * Handler called when an experiment is selected. Checks if the experiment
     * has changed and requests the selected experiment if it has not yet been
     * cached.
     *
     * @param signal
     *            Contains the information about the selection.
     */
    @TmfSignalHandler
    public void experimentSelected(TmfExperimentSelectedSignal signal) {
        if (signal != null) {
            TmfExperiment experiment = signal.getExperiment();
            String experimentName = experiment.getName();

            if (TmfStatisticsTreeRootFactory.containsTreeRoot(getTreeID(experimentName))) {
                // The experiment root is already present
                String treeID = getTreeID(experimentName);
                TmfStatisticsTreeNode experimentTreeNode = TmfStatisticsTreeRootFactory.getStatTreeRoot(treeID);

                ITmfTrace[] traces = experiment.getTraces();

                // check if there is partial data loaded in the experiment
                int numTraces = experiment.getTraces().length;
                int numNodeTraces = experimentTreeNode.getNbChildren();

                if (numTraces == numNodeTraces) {
                    boolean same = true;
                    /*
                     * Detect if the experiment contains the same traces as when
                     * previously selected
                     */
                    for (int i = 0; i < numTraces; i++) {
                        String traceName = traces[i].getName();
                        if (!experimentTreeNode.containsChild(traceName)) {
                            same = false;
                            break;
                        }
                    }

                    if (same) {
                        // no need to reload data, all traces are already loaded
                        fTreeViewer.setInput(experimentTreeNode);

                        resetUpdateSynchronization();

                        return;
                    }
                    experimentTreeNode.reset();
                }
            } else {
                TmfStatisticsTreeRootFactory.addStatsTreeRoot(getTreeID(experimentName), getStatisticData());
            }

            resetUpdateSynchronization();

            TmfStatisticsTreeNode treeModelRoot = TmfStatisticsTreeRootFactory.getStatTreeRoot(getTreeID(experiment.getName()));

            // if the model has contents, clear to start over
            if (treeModelRoot.hasChildren()) {
                treeModelRoot.reset();
            }

            // set input to a clean data model
            fTreeViewer.setInput(treeModelRoot);

            if (fRequestData) {
                requestData(experiment, experiment.getTimeRange());
                fRequestData = false;
            }
        }
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

        // Calculate the selected timerange for the request
        long startTime = signal.getRange().getStartTime().normalize(0, TIME_SCALE).getValue();
        TmfTimestamp startTS  = new TmfTimestamp(startTime, TIME_SCALE);
        TmfTimestamp endTS    = new TmfTimestamp(startTime + INITIAL_WINDOW_SPAN, TIME_SCALE);
        TmfTimeRange timeRange = new TmfTimeRange(startTS, endTS);

        requestTimeRangeData(experiment, timeRange);
        requestData(experiment, signal.getRange());
    }

    /**
     * Handles the experiment updated signal. This will detect new events in
     * case the indexing is not coalesced with a statistics request.
     *
     * @param signal
     *            The experiment updated signal
     *
     * @since 1.1
     */
    @TmfSignalHandler
    public void experimentUpdated(TmfExperimentUpdatedSignal signal) {
        TmfExperiment experiment = signal.getExperiment();
        if (!experiment.equals(TmfExperiment.getCurrentExperiment())) {
            return;
        }

        int nbEvents = 0;
        for (TmfStatisticsTreeNode node : ((TmfStatisticsTreeNode) fTreeViewer.getInput()).getChildren()) {
            nbEvents += (int) node.getValue().getTotal();
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
     * Handles the time range updated signal. It updates the time range
     * statistics.
     *
     * @param signal
     *            Contains the information about the new selected time range.
     * @since 2.0
     */
    @TmfSignalHandler
    public void timeRangeUpdated(TmfRangeSynchSignal signal) {
        /*
         * It is possible that the time range changes while a request is
         * processing
         */
        cancelOngoingRequest(fRequestRange);
        resetTimeRangeValue();

        requestTimeRangeData(TmfExperiment.getCurrentExperiment(), signal.getCurrentRange());
    }

    /**
     * Return the size of the request when performing background request.
     *
     * @return the block size for background request.
     */
    protected int getIndexPageSize() {
        return PAGE_SIZE;
    }

    /**
     * Returns the quantity of data to retrieve before a refresh of the view is
     * performed
     *
     * @return the quantity of data to retrieve before a refresh of the view is
     *         performed.
     */
    protected long getInputChangedRefresh() {
        return STATS_INPUT_CHANGED_REFRESH;
    }

    /**
     * This method can be overridden to implement another way to represent the
     * statistics data and to retrieve the information for display.
     *
     * @return a TmfStatisticsData object.
     */
    protected AbsTmfStatisticsTree getStatisticData() {
        return new TmfBaseStatisticsTree();
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
     * Constructs the ID based on the experiment name and
     * <code>fInstanceNb</code>
     *
     * @param experimentName
     *            the name of the trace name to show in the view
     * @return a view ID
     */
    protected String getTreeID(String experimentName) {
        return experimentName + fInstanceNb;
    }

    /**
     * When the experiment is loading the cursor will be different so the user
     * knows the processing is not finished yet.
     *
     * @param waitInd
     *            Indicates if we need to show the waiting cursor, or the
     *            default one
     */
    protected void waitCursor(final boolean waitInd) {
        if ((fTreeViewer == null) || (fTreeViewer.getTree().isDisposed())) {
            return;
        }

        Display display = fTreeViewer.getControl().getDisplay();
        if (fWaitCursor == null) {
            fWaitCursor = new Cursor(display, SWT.CURSOR_WAIT);
        }

        // Perform the updates on the UI thread
        display.asyncExec(new Runnable() {
            @Override
            public void run() {
                if ((fTreeViewer != null)
                        && (!fTreeViewer.getTree().isDisposed())) {
                    Cursor cursor = null; /* indicates default */
                    if (waitInd) {
                        cursor = fWaitCursor;
                    }
                    fTreeViewer.getControl().setCursor(cursor);
                }
            }
        });
    }

    /**
     * Performs the request for an experiment and populates the statistics tree
     * with events.
     *
     * @param experiment
     *            Experiment for which we need the statistics data.
     * @param timeRange
     *            to request
     */
    protected void requestData(final TmfExperiment experiment, TmfTimeRange timeRange) {
        if (experiment != null) {

            // Check if an update is already ongoing
            if (checkUpdateBusy(timeRange)) {
                return;
            }

            int index = 0;
            for (TmfStatisticsTreeNode node : ((TmfStatisticsTreeNode) fTreeViewer.getInput()).getChildren()) {
                index += (int) node.getValue().getTotal();
            }

            // Prepare the global event request
            fRequest = new TmfStatisticsRequest(this, experiment, timeRange, index, ExecutionType.BACKGROUND, true);

            experiment.sendRequest(fRequest);
            waitCursor(true);
        }
    }

    /**
     * Performs the time range request for an experiment and populates the
     * statistics tree with events.
     *
     * @param experiment
     *            Experiment for which we need the statistics data.
     * @param timeRange
     *            To request
     * @since 2.0
     */
    protected void requestTimeRangeData(final TmfExperiment experiment, TmfTimeRange timeRange) {
        if (experiment != null) {

            // Prepare the partial event request
            fRequestRange = new TmfStatisticsRequest(this, experiment, timeRange, 0, ExecutionType.FOREGROUND, false);
            experiment.sendRequest(fRequestRange);
        }
    }

    /**
     * Reset the number of events within the time range
     *
     * @since 2.0
     */
    protected void resetTimeRangeValue() {
        // Reset the number of events in the time range
        String treeID = getTreeID(TmfExperiment.getCurrentExperiment().getName());
        TmfStatisticsTreeNode treeModelRoot = TmfStatisticsTreeRootFactory.getStatTreeRoot(treeID);
        if (treeModelRoot.hasChildren()) {
            treeModelRoot.resetTimeRangeValue();
        }
    }

    /**
     * Cancels the current ongoing request
     *
     * @param request
     *            The request to be canceled
     * @since 2.0
     */
    protected void cancelOngoingRequest(ITmfEventRequest request) {
        if (request != null && !request.isCompleted()) {
            request.cancel();
        }
    }

    /**
     * Reset update synchronization information
     */
    protected void resetUpdateSynchronization() {
        synchronized (fStatisticsUpdateSyncObj) {
            fStatisticsUpdateBusy = false;
            fStatisticsUpdatePending = false;
            fStatisticsUpdateRange = null;
        }
    }

    /**
     * Checks if statistic update is ongoing. If it is ongoing the new time
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
