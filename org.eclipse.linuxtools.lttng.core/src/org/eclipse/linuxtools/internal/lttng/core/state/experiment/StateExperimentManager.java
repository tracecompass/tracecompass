/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *   Marc Dumais (marc.dumais@ericsson.com) - Fix for 316455 (second part)
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.core.state.experiment;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.internal.lttng.core.Activator;
import org.eclipse.linuxtools.internal.lttng.core.LttngConstants;
import org.eclipse.linuxtools.internal.lttng.core.TraceDebug;
import org.eclipse.linuxtools.internal.lttng.core.control.LttngCoreProviderFactory;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngSyntheticEvent;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngSyntheticEvent.SequenceInd;
import org.eclipse.linuxtools.internal.lttng.core.model.LTTngTreeNode;
import org.eclipse.linuxtools.internal.lttng.core.signal.ILttExperimentSelectedListener;
import org.eclipse.linuxtools.internal.lttng.core.signal.StateExperimentListener;
import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngTraceState;
import org.eclipse.linuxtools.internal.lttng.core.state.trace.IStateTraceManager;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentRangeUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * @author alvaro
 * 
 */
public class StateExperimentManager extends LTTngTreeNode implements ILttExperimentSelectedListener,
        IStateExperimentManager {

    // ========================================================================
    // Data
    // =======================================================================
    private LTTngTreeNode fSelectedExperiment = null; // one selected experiment
                                                      // supported
    private final StateExperimentListener fexperimentListener;
    private boolean fwaitForCompletion = false;
    /**
     * Used to route incoming events to proper trace manager, during check point building
     */
    private final Map<ITmfTrace<?>, StateTraceHelper> ftraceToManagerMap = new HashMap<ITmfTrace<?>, StateTraceHelper>();

    private LttngSyntheticEvent syntheticEvent = null;
    private ITmfDataRequest<LttngEvent> fStateCheckPointRequest = null;
    private boolean fCheckPointUpdateBusy = false;
    private boolean fCheckPointUpdatePending = false;
    private int fCheckPointUpdateIndex = 0;
    private TmfTimeRange fCheckPointUpdateRange = null;
    private long fCheckPointNbEventsHandled = 0;
    private final Object fCheckPointUpdateSyncObj = new Object();
    private boolean fInitial = false;

    // ========================================================================
    // Constructors
    // =======================================================================
    public StateExperimentManager(Long id, String name) {
        super(id, null, name, null);
        fexperimentListener = new StateExperimentListener("Experiment Manager", this); //$NON-NLS-1$
        TmfSignalManager.registerVIP(this);
    }

    @TmfSignalHandler
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void experimentSelected(TmfExperimentSelectedSignal signal) {
        experimentSelected_prep(signal.getExperiment());
    }

    // ========================================================================
    // Methods
    // =======================================================================

//	/* (non-Javadoc)
//	 * @see org.eclipse.linuxtools.lttng.state.experiment.IStateExperimentManager#readExperimentTimeWindow(org.eclipse.linuxtools.tmf.event.TmfTimeRange, java.lang.String, org.eclipse.linuxtools.lttng.state.IStateDataRequestListener)
//	 */
//	public ILttngSyntEventRequest readExperimentTimeWindow(TmfTimeRange trange,
//			Object source, IRequestStatusListener listener,
//			ITransEventProcessor processor) {
//
//		ILttngSyntEventRequest request = null;
//
//		// validate
//		if (fSelectedExperiment != null) {
//			// Get all trace manager nodes
//			LTTngTreeNode[] traceMgrs = fSelectedExperiment.getChildren();
//
//			if (traceMgrs != null && traceMgrs.length > 0) {
//				IStateTraceManager traceManager;
//				// Trigger one request per trace
//				for (LTTngTreeNode traceNode : traceMgrs) {
//					traceManager = (IStateTraceManager) traceNode;
//					request = traceManager.executeDataRequest(trange, source,
//							listener,
//							processor);
//				}
//			}
//		} else {
//			if (fSelectedExperiment == null) {
//				TraceDebug.debug("No experiment selected");
//			}
//		}
//
//		return request;
//	}

//	/* (non-Javadoc)
//	 * @see org.eclipse.linuxtools.lttng.state.experiment.IStateExperimentManager#readExperiment(java.lang.String, org.eclipse.linuxtools.lttng.state.IStateDataRequestListener)
//	 */
//	@SuppressWarnings("unchecked")
//	public void readExperiment(Object source, IRequestStatusListener listener,
//			ITransEventProcessor processor) {
//		// validate
//		if (fSelectedExperiment != null) {
//			TmfExperiment<LttngEvent> experiment = (TmfExperiment<LttngEvent>) fSelectedExperiment
//					.getValue();
//			TmfTimeRange trange = experiment.getTimeRange();
//			readExperimentTimeWindow(trange, source, listener, processor);
//		} else {
//			TraceDebug.debug("No selected experiment available");
//		}
//	}

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.linuxtools.lttng.state.experiment.IStateExperimentManager #experimentSelected_prep
     * (org.eclipse.linuxtools.tmf.experiment.TmfExperiment)
     */
    @Override
    public void experimentSelected_prep(TmfExperiment<LttngEvent> experiment) {

        if (fSelectedExperiment != null) {
            clearExperimentNode(fSelectedExperiment);
            fSelectedExperiment = null;
        }

        LTTngTreeNode experimentNode = null;
        if (experiment != null) {
            experimentNode = getChildByName(experiment.getName());
            // keep experiment if already loaded with the same value
            if (experimentNode != null && experimentNode.getValue() != experiment) {
                clearExperimentNode(experimentNode);
                experimentNode = null;
            }

            // Make sure all traces involved have a corresponding state manager
            // and
            // state system to request its initial data
            if (experimentNode == null) {
                // Create the new experiment tree node
                experimentNode = new LTTngTreeNode(getNextUniqueId(), this, experiment.getName(), experiment);
                // add the new experiment to this children list
                addChild(experimentNode);
            }

            // Make sure the traces exists in the tree
            ITmfTrace<?>[] rtraces = experiment.getTraces();
            String traceName;
            LTTngTreeNode traceStateManagerNode;
            // StateStacksHandler
            for (ITmfTrace<?> rtrace : rtraces) {
                traceName = rtrace.getName();
                traceStateManagerNode = experimentNode.getChildByName(traceName);
                // Node does not exist for this experiment, so needs to be
                // created
                if (traceStateManagerNode == null) {
                    traceStateManagerNode = StateManagerFactory.getManager(rtrace, experimentNode);
                    experimentNode.addChild(traceStateManagerNode);
                }
            }

            // Reset event provider to handle requests for the new experiment
            LttngCoreProviderFactory.reset(experimentNode);

            // preserve the selected experiment
            fSelectedExperiment = experimentNode;
        }
    }

    private void clearExperimentNode(LTTngTreeNode experimentNode) {
        // Remove checkpoints
        LTTngTreeNode[] traceNodes = experimentNode.getChildren();

        for (LTTngTreeNode traceStateManagerNode : traceNodes) {
            IStateTraceManager traceManager = null;
            try {
                traceManager = (IStateTraceManager) traceStateManagerNode;
                // Clear all previously created check points as preparation to
                // re-build
                traceManager.clearCheckPoints();
                experimentNode.removeChild(traceStateManagerNode);
            } catch (ClassCastException e) {
                // Nothing to do
            }

            // rebuild the experiment nodes from scratch
            removeChild(experimentNode);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.linuxtools.lttng.signal.ILttExperimentSelectedListener# experimentSelected(java.lang.Object,
     * org.eclipse.linuxtools.tmf.experiment.TmfExperiment)
     */
    @Override
    public void experimentSelected(Object source, TmfExperiment<LttngEvent> experiment) {
        // validate
        if (experiment == null) {
            TraceDebug.debug("Received experiment is null"); //$NON-NLS-1$
            return;
        }

        // If previous request is ongoing, cancel it before requesting a new
        // one.
        if (fStateCheckPointRequest != null && !fStateCheckPointRequest.isCompleted()) {
            fStateCheckPointRequest.cancel();
        }

        synchronized (fCheckPointUpdateSyncObj) {
            fCheckPointUpdateBusy = false;
            fCheckPointUpdatePending = false;
            fCheckPointUpdateIndex = 0;
        }

        fInitial = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.linuxtools.lttng.signal.ILttExperimentSelectedListener# experimentUpdated
     * (org.eclipse.linuxtools.tmf.signal.TmfExperimentUpdatedSignal, boolean)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void experimentRangeUpdated(TmfExperimentRangeUpdatedSignal signal) {
        TmfExperiment<LttngEvent> experiment = (TmfExperiment<LttngEvent>) signal.getExperiment();
        // validate
        if (fSelectedExperiment == null || experiment != fSelectedExperiment.getValue()) {
            return;
        }

        synchronized (fCheckPointUpdateSyncObj) {
            if (fCheckPointUpdateBusy) {
                fCheckPointUpdatePending = true;
                fCheckPointUpdateRange = signal.getRange();
                return;
            } else {
                fCheckPointUpdateBusy = true;
            }
        }

        // If previous request is ongoing, cancel it before requesting a new
        // one.
        if (fStateCheckPointRequest != null && !fStateCheckPointRequest.isCompleted()) {
            fStateCheckPointRequest.cancel();
        }

        // trigger data request to build the state system check points
        fStateCheckPointRequest = buildCheckPoints(experiment, signal.getRange(), fInitial);
        fInitial = false;

        if (fStateCheckPointRequest == null) {
            synchronized (fCheckPointUpdateSyncObj) {
                fCheckPointUpdateBusy = false;
            }
        }
    }

    /**
     * @return the SelectedExperiment tree node
     */
    @Override
    public LTTngTreeNode getSelectedExperiment() {
        return fSelectedExperiment;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.linuxtools.lttng.state.experiment.IStateExperimentManager#getExperimentTimeRange()
     */
    @Override
    @SuppressWarnings("unchecked")
    public TmfTimeRange getExperimentTimeRange() {
        TmfTimeRange timeRangeResult = null;
        if (fSelectedExperiment != null) {
            timeRangeResult = ((TmfExperiment<LttngEvent>) fSelectedExperiment.getValue()).getTimeRange();
        }
        return timeRangeResult;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() {
        fexperimentListener.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.linuxtools.lttng.state.experiment.IStateExperimentManager #waitForComplete(boolean)
     */
    @Override
    public void waitForCompletion(boolean wait) {
        fwaitForCompletion = wait;
    }

    private ITmfDataRequest<LttngEvent> buildCheckPoints(final TmfExperiment<LttngEvent> experiment,
            final TmfTimeRange range, boolean initial) {
        // validate
        if (experiment == null) {
            TraceDebug.debug("Received experiment is null"); //$NON-NLS-1$
            return null;
        }

        LTTngTreeNode experimentNode = getChildByName(experiment.getName());
        if (experimentNode == null) {
            TraceDebug.debug("Experiment Node " + experiment.getName() + " does not exist"); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        }

        final boolean waitForCompletion = fwaitForCompletion;

        // get the trace manager nodes associated to the experiment
        LTTngTreeNode[] traceNodes = experimentNode.getChildren();

        if (initial) {
            synchronized (this) {
                ftraceToManagerMap.clear();
            }

            ITmfTrace<?> trace;
            for (LTTngTreeNode traceStateManagerNode : traceNodes) {
                IStateTraceManager traceManager;
                try {
                    traceManager = (IStateTraceManager) traceStateManagerNode;
                } catch (ClassCastException e) {
                    Activator.getDefault().logError("Unexpected Error", e);  //$NON-NLS-1$
                    return null;
                }

                // Clear all previously created check points as preparation to
                // re-build
                traceManager.clearCheckPoints();

                // build the trace to manager mapping for event dispatching
                trace = traceManager.getStateTrace();
                synchronized (this) {
                    ftraceToManagerMap.put(trace, new StateTraceHelper(traceManager));
                }
            }
        }

        // if no trace mapping
        if (ftraceToManagerMap.size() < 1) {
            TraceDebug.debug("No traces associated to experiment " + experiment.getName()); //$NON-NLS-1$
            return null;
        }

        fCheckPointNbEventsHandled = 0;

        // Prepare event data request to build state model
        ITmfEventRequest<LttngEvent> request = new TmfEventRequest<LttngEvent>(LttngEvent.class, range,
                fCheckPointUpdateIndex, TmfEventRequest.ALL_DATA, LttngConstants.DEFAULT_BLOCK_SIZE,
                ITmfDataRequest.ExecutionType.BACKGROUND) {

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.linuxtools.tmf.request.TmfDataRequest#handleData()
             */
            @Override
            public void handleData(LttngEvent event) {
                super.handleData(event);
                if (event != null) {
//					Tracer.trace("Chk: " + event.getTimestamp());
                    fCheckPointNbEventsHandled++;
                    ITmfTrace<?> trace = event.getTrace();

                    StateTraceHelper helper = ftraceToManagerMap.get(trace);

                    if (helper != null) {
                        helper.incrementNumberRead();

                        // obtain synthetic event
                        LttngSyntheticEvent synEvent = updateSynEvent(event, helper.getTraceModel());

                        // update state system, and save check points as needed
                        helper.getStateManager().handleEvent(synEvent, helper.getNumberRead());
                    } else {
                        TraceDebug.debug("StateTraceManager not found for trace" //$NON-NLS-1$
                                + trace.getName());
                    }
                }
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.linuxtools.tmf.request.TmfDataRequest#handleCompleted()
             */
            @Override
            public void handleCompleted() {
                super.handleCompleted();
                printCompletedMessage();

                if (!waitForCompletion) {
                    synchronized (fCheckPointUpdateSyncObj) {
                        fCheckPointUpdateBusy = false;
                        fCheckPointUpdateIndex += fCheckPointNbEventsHandled;
                        if (fCheckPointUpdatePending) {
                            fCheckPointUpdatePending = false;
                            fCheckPointUpdateBusy = true;
                            buildCheckPoints(experiment, fCheckPointUpdateRange, false);
                        }
                    }
                }
            }

            /*
             * /**
             * 
             * @param header
             */
            private void printCompletedMessage() {
                if (TraceDebug.isDEBUG()) {
                    TraceDebug
                            .debug("Trace check point building completed, number of events handled: " + fCheckPointNbEventsHandled + "\n\t\t"); //$NON-NLS-1$ //$NON-NLS-2$
                    for (StateTraceHelper helper : ftraceToManagerMap.values()) {
                        TraceDebug.debug(helper.getStateManager().toString() + "\n\t\t"); //$NON-NLS-1$
                    }
                }
            }
        };

        // Execute event data request
        experiment.sendRequest(request);

        if (waitForCompletion) {
            try {
                request.waitForCompletion();
                synchronized (fCheckPointUpdateSyncObj) {
                    fCheckPointUpdateBusy = false;
                    fCheckPointUpdateIndex += fCheckPointNbEventsHandled;
                    if (fCheckPointUpdatePending) {
                        fCheckPointUpdatePending = false;
                        fCheckPointUpdateBusy = true;
                        buildCheckPoints(experiment, fCheckPointUpdateRange, false);
                    }
                }
            } catch (InterruptedException e) {
            }
        }

        return request;
    }

    private LttngSyntheticEvent updateSynEvent(LttngEvent e, LttngTraceState stateModel) {
        if (syntheticEvent == null || syntheticEvent.getBaseEvent() != e) {
            syntheticEvent = new LttngSyntheticEvent(e);
        }

        // Trace model needed by application handlers
        syntheticEvent.setTraceModel(stateModel);
        syntheticEvent.setSequenceInd(SequenceInd.UPDATE);

        return syntheticEvent;
    }

    
    /**
     * Helper class that wraps the StateTraceManager, the current LTTngTraceState and the number of read events
     * 
     * @author bHufmann
     * 
     */
    private static class StateTraceHelper {

        IStateTraceManager stateTraceManager = null;
        long numberEventsRead = 0;
        LttngTraceState stateTraceModel = null;

        /**
         * Constructor
         * 
         * @param stateManager
         *            The StateTraceManager the helper is for
         */
        public StateTraceHelper(IStateTraceManager stateManager) {
            this.stateTraceManager = stateManager;
            // Get the TraceState at the beginning of the trace
            this.stateTraceManager.restoreCheckPointByTimestamp(stateManager.getStateTrace().getStartTime());
            this.stateTraceModel = this.stateTraceManager.getStateModel();
        }

        /**
         * Returns the StateTraceManager
         * 
         * @return IStateTraceManager
         */
        public IStateTraceManager getStateManager() {
            return stateTraceManager;
        }

        /**
         * Returns the number of read events
         * 
         * @return long
         */
        public long getNumberRead() {
            return numberEventsRead;
        }

        /**
         * Increments the number of read events
         */
        public void incrementNumberRead() {
            ++numberEventsRead;
        }

        /**
         * Returns the current LTTngTraceState
         * 
         * @return LttngTraceState
         */
        public LttngTraceState getTraceModel() {
            return stateTraceModel;
        }
    }
}
