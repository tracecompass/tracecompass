/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.statistics;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.ITmfExtraEventInfo;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.Messages;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.TmfStatisticsViewer;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.AbsTmfStatisticsTree;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeRootFactory;

/**
 * Class for the TMF event requests specific to the statistics view.
 * @version 2.0
 */
class TmfStatisticsRequest extends TmfEventRequest {

    /**
     * Reference to the statistics viewer that sent the request
     */
    private final TmfStatisticsView fSender;

    /**
     * The viewer that displays the statistics data
     */
    private TmfStatisticsViewer fViewer;

    /**
     * The experiment for which to send the request
     */
    private final TmfExperiment fExperiment;

    /**
     * Tells if the request is for the whole trace or for a smaller time range
     */
    private final boolean fGlobal;

    /**
     * The statistics tree that will be updated from the requested data
     */
    private final AbsTmfStatisticsTree fStatisticsData;

    /**
     * Constructor
     *
     * @param sender
     *            Sender of this request
     * @param experiment
     *            Experiment targeted by this request
     * @param range
     *            The target time range
     * @param index
     *            The starting index
     * @param prio
     *            The priority of the request
     * @param global
     *            Is this for a global statistics request (true), or a partial
     *            one (false)?
     */
    TmfStatisticsRequest(TmfStatisticsView sender, TmfStatisticsViewer viewer, TmfExperiment experiment, TmfTimeRange range, long index, ExecutionType prio, boolean global) {
        super(ITmfEvent.class, range, index, TmfDataRequest.ALL_DATA, sender.getIndexPageSize(), prio);
        String treeID = viewer.getTreeID(experiment.getName());

        fSender = sender;
        fViewer = viewer;
        fExperiment = experiment;
        fGlobal = global;
        fStatisticsData = TmfStatisticsTreeRootFactory.getStatTree(treeID);
    }

    @Override
    public void handleData(ITmfEvent data) {
        super.handleData(data);
        if (data != null) {
            final String traceName = data.getTrace().getName();
            ITmfExtraEventInfo extraInfo = new ITmfExtraEventInfo() {
                @Override
                public String getTraceName() {
                    if (traceName == null) {
                        return Messages.TmfStatisticsView_UnknownTraceName;
                    }
                    return traceName;
                }
            };
            if (fGlobal) {
                fStatisticsData.registerEvent(data, extraInfo);
            } else {
                fStatisticsData.registerEventInTimeRange(data, extraInfo);
            }
            fStatisticsData.increase(data, extraInfo, 1);
            // Refresh view
            if ((getNbRead() % fViewer.getInputChangedRefresh()) == 0) {
                fSender.modelInputChanged(false);
            }
        }
    }

    @Override
    public void handleSuccess() {
        super.handleSuccess();
        fSender.modelInputChanged(true);
        if (fGlobal) {
            fViewer.waitCursor(false);
        }
    }

    @Override
    public void handleFailure() {
        super.handleFailure();
        fSender.modelIncomplete(fExperiment.getName());
    }

    @Override
    public void handleCancel() {
        super.handleCancel();
        /*
         * The global request can be cancelled when another experiment is
         * selected, but a time range request can also be cancelled when there is
         * a time range update, which means the model must not be deleted.
         */
        if (fGlobal) {
            fSender.modelIncomplete(fExperiment.getName());
        }
    }
}
