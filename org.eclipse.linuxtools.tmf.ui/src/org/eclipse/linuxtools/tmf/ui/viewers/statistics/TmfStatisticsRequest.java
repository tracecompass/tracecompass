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

package org.eclipse.linuxtools.tmf.ui.viewers.statistics;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.AbsTmfStatisticsTree;

/**
 * Class for the TMF event requests specific to the statistics view.
 *
 * @version 2.0
 * @since 2.0
 */
public class TmfStatisticsRequest extends TmfEventRequest {

    /**
     * Reference to the statistics viewer that sent the request.
     */
    private final TmfStatisticsViewer fSender;

    /**
     * Reference to the statistics tree.
     */
    private final AbsTmfStatisticsTree fStatisticsData;

    /**
     * Tells if the request is for the whole trace or for a smaller time range.
     */
    private final boolean fGlobal;

    /**
     * Index of the last event retrieved.
     */
    private long fLastEventIndex;

    /**
     * Constructor
     *
     * @param sender
     *            Sender of this request
     * @param range
     *            The target time range
     * @param index
     *            The starting index
     * @param global
     *            Is this for a global statistics request (true), or a partial
     *            one (false)?
     */
    public TmfStatisticsRequest(TmfStatisticsViewer sender, TmfTimeRange range, long index, boolean global) {
        super(ITmfEvent.class, range, index, TmfDataRequest.ALL_DATA, sender.getPageSize(), ExecutionType.BACKGROUND);
        fGlobal = global;
        fSender = sender;
        fStatisticsData = fSender.getStatisticData();
        fLastEventIndex = index;
    }

    @Override
    public void handleData(ITmfEvent data) {
        ++fLastEventIndex;

        if (data != null) {
            final String traceName = data.getTrace().getName();
            if (fSender.isListeningTo(traceName)) {
                super.handleData(data);

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

                if (getNbRead() % fSender.getRefreshRate() == 0) {
                    fSender.refresh();
                }
            }
        }
    }

    @Override
    public void handleSuccess() {
        super.handleSuccess();
        handleFinish(true);
    }

    @Override
    public void handleFailure() {
        super.handleFailure();
        handleFinish(false);
    }

    @Override
    public void handleCancel() {
        super.handleCancel();
        handleFinish(false);
    }

    /**
     * @return the index of the last event read in the experiment so far
     */
    protected long getLastEventIndex() {
        return fLastEventIndex;
    }

    /**
     * Handles the end of the request whether it is successful or not.
     *
     * @param isSuccessful
     *            Tells if the request has finished successfully or not and
     *            calls the right method from the sender in each case.
     */
    protected void handleFinish(boolean isSuccessful) {
        if (isSuccessful) {
            fSender.modelComplete(fGlobal);
        } else {
            fSender.modelIncomplete(fGlobal);
        }
    }
}
