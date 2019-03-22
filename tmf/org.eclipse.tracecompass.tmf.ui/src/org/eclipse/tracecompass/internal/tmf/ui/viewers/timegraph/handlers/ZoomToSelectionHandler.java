/*******************************************************************************
* Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.viewers.timegraph.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;

/**
 * Zoom to the current selection
 *
 * @author Matthew Khouzam
 */
public class ZoomToSelectionHandler extends TimeGraphBaseHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        TmfTraceManager manager = TmfTraceManager.getInstance();
        TmfTimeRange selection = manager.getCurrentTraceContext().getSelectionRange();
        ITmfTrace activeTrace = manager.getActiveTrace();
        if (activeTrace != null && !selection.getEndTime().equals(selection.getStartTime())) {
            activeTrace.broadcast(new TmfWindowRangeUpdatedSignal(this, selection));
        }
        return null;
    }
}
