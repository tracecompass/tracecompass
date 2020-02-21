/*******************************************************************************
 * Copyright (c) 2015, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Marc-Andre Laperle - Initial API and implementation.
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.ui.editors.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.editors.ITmfTraceEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Refresh the content of a trace in order to see new events.
 */
public class RefreshTraceContentHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {

        IEditorPart activeEditor = HandlerUtil.getActiveEditorChecked(event);
        if (activeEditor instanceof ITmfTraceEditor) {
            ITmfTrace trace = ((ITmfTraceEditor) activeEditor).getTrace();
            refreshTrace(trace);
        }

        return null;
    }

    private static void refreshTrace(ITmfTrace trace) {
        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        TmfTraceRangeUpdatedSignal signal = new TmfTraceRangeUpdatedSignal(trace, trace, range);
        trace.broadcastAsync(signal);
    }

}
