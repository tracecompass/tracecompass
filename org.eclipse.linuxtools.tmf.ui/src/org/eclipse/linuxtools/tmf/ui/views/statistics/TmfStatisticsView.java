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
 *   Mathieu Denis - Generalization of the view to instantiate a viewer specific to a trace type
 *
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.statistics;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.internal.tmf.ui.Activator;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
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
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceType;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.TmfStatisticsViewer;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeNode;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeRootFactory;
import org.eclipse.linuxtools.tmf.ui.views.TmfView;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.osgi.framework.Bundle;

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
     * Stores the request to the experiment
     */
    protected ITmfEventRequest fRequest = null;

    /**
     * The viewer that builds the columns to show the statistics
     */
    private TmfStatisticsViewer fStatsViewer;

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
     * Stores a reference to the parent composite of this view
     */
    private Composite fParent;

    /**
     * Stores a reference to the experiment
     */
    private TmfExperiment fExperiment;

    /**
     * Flag to force request the data from trace
     */
    protected boolean fRequestData = false;

    /**
     * Default PAGE_SIZE for background requests
     */
    protected static final int PAGE_SIZE = 50000;

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
     * Constructor of a statistics view.
     *
     * @param viewName The name to give to the view.
     */
    public TmfStatisticsView(String viewName) {
        super(viewName);
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
        fParent = parent;
        TmfExperiment currentExperiment = TmfExperiment.getCurrentExperiment();
        // Read current data if any available
        if (currentExperiment != null) {
            fRequestData = true;
            // Insert the statistics data into the tree
            TmfExperimentSelectedSignal signal = new TmfExperimentSelectedSignal(this, currentExperiment);
            experimentSelected(signal);
            return;
        }
        fStatsViewer = createStatisticsViewer();
        /*
         * Updates the experiment field only at the end because
         * experimentSelected signal verifies the old selected experiment to
         * avoid reloading the same trace.
         */
        fExperiment = currentExperiment;
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
         * Make sure there is no request running before removing the statistics
         * tree
         */
        cancelOngoingRequest(fRequestRange);
        cancelOngoingRequest(fRequest);
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
            // Does not reload the same trace if already opened
            if (fExperiment == null
                    || signal.getExperiment().toString().compareTo(fExperiment.toString()) != 0) {
                /*
                 * Dispose the current viewer and adapt the new one to the trace
                 * type of the experiment selected
                 */
                if (fStatsViewer != null) {
                    fStatsViewer.dispose();
                }
                // Update the current experiment
                fExperiment = signal.getExperiment();
                fStatsViewer = createStatisticsViewer();
                fParent.layout();

                String experimentName = fExperiment.getName();
                String treeID = fStatsViewer.getTreeID(experimentName);

                setInput(treeID, fExperiment.getTraces());

                if (fRequestData) {
                    requestData(fExperiment, fExperiment.getTimeRange());
                    fRequestData = false;
                }
            }
        }
    }

    /**
     * Initialize the viewer with the information received.
     *
     * @param treeID
     *            The unique ID of the tree that is returned by
     *            {@link TmfStatisticsViewer#getTreeID(String)}
     * @param traces
     *            The list of the traces to add in the tree.
     * @since 2.0
     */
    public void setInput(String treeID, ITmfTrace[] traces) {
        if (TmfStatisticsTreeRootFactory.containsTreeRoot(treeID)) {
            // The experiment root is already present
            TmfStatisticsTreeNode experimentTreeNode = TmfStatisticsTreeRootFactory.getStatTreeRoot(treeID);

            // check if there is partial data loaded in the experiment
            int numTraces = traces.length;
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
                    fStatsViewer.setInput(experimentTreeNode);

                    resetUpdateSynchronization();

                    return;
                }
                experimentTreeNode.reset();
            }
        } else {
            TmfStatisticsTreeRootFactory.addStatsTreeRoot(treeID, fStatsViewer.getStatisticData());
        }

        resetUpdateSynchronization();

        TmfStatisticsTreeNode treeModelRoot = TmfStatisticsTreeRootFactory.getStatTreeRoot(treeID);

        // if the model has contents, clear to start over
        if (treeModelRoot.hasChildren()) {
            treeModelRoot.reset();
        }

        // set input to a clean data model
        fStatsViewer.setInput(treeModelRoot);
    }

    /**
     * Refresh the view.
     *
     * @param complete Should a pending update be sent afterwards or not
     */
    public void modelInputChanged(boolean complete) {
        Control viewerControl = fStatsViewer.getControl();
        // Ignore update if disposed
        if (viewerControl.isDisposed()) {
            return;
        }

        fStatsViewer.getControl().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (!fStatsViewer.getControl().isDisposed()) {
                    fStatsViewer.refresh();
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
        Object input = fStatsViewer.getInput();
        if (input != null && input instanceof TmfStatisticsTreeNode) {
            /*
             * The data from this experiment is invalid and shall be removed to
             * refresh upon next selection
             */
            TmfStatisticsTreeRootFactory.removeStatTreeRoot(fStatsViewer.getTreeID(name));

            // Reset synchronization information
            resetUpdateSynchronization();
            modelInputChanged(false);
        }
        fStatsViewer.waitCursor(false);
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

        // Calculate the selected timerange to request
        long startTime = signal.getRange().getStartTime().normalize(0, TIME_SCALE).getValue();
        TmfTimestamp startTS  = new TmfTimestamp(startTime, TIME_SCALE);
        TmfTimestamp endTS    = new TmfTimestamp(startTime + INITIAL_WINDOW_SPAN, TIME_SCALE);
        TmfTimeRange timeRange = new TmfTimeRange(startTS, endTS);

        requestTimeRangeData(experiment, timeRange);
        requestData(experiment, signal.getRange());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.linuxtools.tmf.ui.views.TmfView#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        fStatsViewer.dispose();

        /*
         * Make sure there is no request running before removing the statistics
         * tree.
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
        fStatsViewer.setFocus();
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
        for (TmfStatisticsTreeNode node : ((TmfStatisticsTreeNode) fStatsViewer.getInput()).getChildren()) {
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
     * * Handles the time range updated signal. It updates the time range
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

        requestTimeRangeData(TmfExperiment.getCurrentExperiment(), signal.getCurrentRange());
    }

    /**
     * Get the statistics viewer for an experiment. If all traces in the
     * experiment are of the same type, use the extension point specified.
     *
     * @return a statistics viewer of the appropriate type
     * @since 2.0
     */
    protected TmfStatisticsViewer createStatisticsViewer() {
        if (fExperiment == null) {
            return new TmfStatisticsViewer(fParent);
        }
        String commonTraceType = null;
        try {
            /*
             * Determine if the traces of the experiment are of the same type.
             * If not, it uses the most generic one.
             */
            for (ITmfTrace trace : fExperiment.getTraces()) {
                IResource resource = trace.getResource();
                if (resource == null) {
                    return new TmfStatisticsViewer(fParent);
                }
                String traceType = resource.getPersistentProperty(TmfCommonConstants.TRACETYPE);
                if (commonTraceType != null && !commonTraceType.equals(traceType)) {
                    return new TmfStatisticsViewer(fParent);
                }
                commonTraceType = traceType;
            }
            if (commonTraceType == null) {
                return new TmfStatisticsViewer(fParent);
            }
            /*
             * Search in the configuration if there is any viewer specified for
             * this kind of trace type.
             */
            for (IConfigurationElement ce : TmfTraceType.getTypeElements()) {
                if (ce.getAttribute(TmfTraceType.ID_ATTR).equals(commonTraceType)) {
                    IConfigurationElement[] statisticsViewerCE = ce.getChildren(TmfTraceType.STATISTICS_VIEWER_ELEM);
                    if (statisticsViewerCE.length != 1) {
                        break;
                    }
                    String statisticsViewer = statisticsViewerCE[0].getAttribute(TmfTraceType.CLASS_ATTR);
                    if (statisticsViewer == null || statisticsViewer.length() == 0) {
                        break;
                    }
                    Bundle bundle = Platform.getBundle(ce.getContributor().getName());
                    Class<?> c = bundle.loadClass(statisticsViewer);
                    Class<?>[] constructorArgs = new Class[] { Composite.class };
                    Constructor<?> constructor = c.getConstructor(constructorArgs);
                    Object[] args = new Object[] { fParent };
                    return (TmfStatisticsViewer) constructor.newInstance(args);
                }
            }
        } catch (CoreException e) {
            Activator.getDefault().logError("Error creating statistics viewer : cannot find the property TmfCommonConstants.TRACETYPE", e); //$NON-NLS-1$
        } catch (ClassNotFoundException e) {
            Activator.getDefault().logError("Error creating statistics viewer : cannot load the statistics viewer class", e); //$NON-NLS-1$
        } catch (NoSuchMethodException e) {
            Activator.getDefault().logError("Error creating statistics viewer : constructor of the viewer doesn't exist", e); //$NON-NLS-1$
        } catch (InstantiationException e) {
            Activator.getDefault().logError("Error creating statistics viewer : cannot instantiate the statistics viewer", e); //$NON-NLS-1$
        } catch (IllegalAccessException e) {
            Activator.getDefault().logError("Error creating statistics viewer : cannot access the constructor of the viewer", e); //$NON-NLS-1$
        } catch (IllegalArgumentException e) {
            Activator.getDefault().logError("Error creating statistics viewer : argument(s) sent to the constructor are illegal", e); //$NON-NLS-1$
        } catch (InvocationTargetException e) {
            Activator.getDefault().logError("Error creating statistics viewer : the constructor of the viewer sent an exception", e); //$NON-NLS-1$
        }
        return new TmfStatisticsViewer(fParent);
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
            for (TmfStatisticsTreeNode node : ((TmfStatisticsTreeNode) fStatsViewer.getInput()).getChildren()) {
                index += (int) node.getValue().getTotal();
            }

            // Prepare the global event request
            fRequest = new TmfStatisticsRequest(this, fStatsViewer, experiment, timeRange, index, ExecutionType.BACKGROUND, true);

            experiment.sendRequest(fRequest);
            fStatsViewer.waitCursor(true);
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
            resetTimeRangeValue();
            // Prepare the partial event request
            fRequestRange = new TmfStatisticsRequest(this, fStatsViewer, experiment, timeRange, 0, ExecutionType.FOREGROUND, false);
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
        String treeID = fStatsViewer.getTreeID(TmfExperiment.getCurrentExperiment().getName());
        TmfStatisticsTreeNode treeModelRoot = TmfStatisticsTreeRootFactory.getStatTreeRoot(treeID);
        if (treeModelRoot.hasChildren()) {
            treeModelRoot.resetTimeRangeValue();
        }
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
