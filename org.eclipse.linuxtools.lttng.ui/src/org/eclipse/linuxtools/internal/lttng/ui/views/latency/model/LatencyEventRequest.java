/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng.ui.views.latency.model;

import org.eclipse.linuxtools.internal.lttng.core.LttngConstants;
import org.eclipse.linuxtools.internal.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.internal.lttng.core.latency.analyzer.EventMatcher;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;

/**
 * <b><u>LatencyEventRequest</u></b>
 * <p>
 */
public class LatencyEventRequest extends TmfEventRequest<LttngEvent> {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    final private LatencyController fController;
    
    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    public LatencyEventRequest(LatencyController controller, TmfTimeRange range, int rank, int nbEvents, ITmfDataRequest.ExecutionType execType) {
        super(LttngEvent.class, range, rank, nbEvents, LttngConstants.DEFAULT_BLOCK_SIZE, execType);
        fController = controller;
        EventMatcher.getInstance().clearStack();
    }

    public LatencyEventRequest(LatencyController controller, TmfTimeRange range, ITmfDataRequest.ExecutionType execType) {
        this(controller, range, 0, ALL_DATA, execType);
    }

    public LatencyEventRequest(LatencyController controller, TmfTimeRange range, int rank, ITmfDataRequest.ExecutionType execType) {
        this(controller, range, rank, ALL_DATA, execType);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.request.TmfDataRequest#handleData(org.eclipse.linuxtools.tmf.event.TmfData)
     */
    @Override
    public void handleData(LttngEvent event) {
        super.handleData(event);
        
        LttngEvent startEvent = EventMatcher.getInstance().process(event);

        if (startEvent != null) {
            long latency = event.getTimestamp().getValue() - startEvent.getTimestamp().getValue(); 
            fController.handleData(getNbRead(), startEvent.getTimestamp().getValue(), latency);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.request.TmfDataRequest#handleCompleted()
     */
    @Override
    public void handleCompleted() {
        fController.handleCompleted();
        super.handleCompleted();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.request.TmfDataRequest#handleCancel()
     */
    @Override
    public void handleCancel() {
        EventMatcher.getInstance().clearStack();
        fController.handleCancel();
        super.handleCancel();
    }
}
