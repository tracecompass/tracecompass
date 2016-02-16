/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.actions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.action.Action;
import org.eclipse.tracecompass.analysis.os.linux.core.signals.TmfThreadSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;

/**
 * Follow Thread Action, this action broadcasts a
 * {@link TmfThreadSelectedSignal} when run, it sends a thread id and a trace to
 * the signal.
 *
 * @author Matthew Khouzam
 */
@NonNullByDefault
public class FollowThreadAction extends Action {

    private final int fThreadId;
    private final TmfView fView;
    private final ITmfTrace fTrace;
    private final @Nullable String fThreadName;

    /**
     * Constructor
     *
     * @param source
     *            the view that is generating the signal, but also shall
     *            broadcast it
     * @param threadName
     *            the thread name, can be null
     * @param threadId
     *            the thread id
     * @param trace
     *            the trace containing the thread
     */
    public FollowThreadAction(TmfView source, @Nullable String threadName, int threadId, ITmfTrace trace) {
        fView = source;
        fThreadName = threadName;
        fThreadId = threadId;
        fTrace = trace;
    }

    @Override
    public String getText() {
        if (fThreadName == null) {
            return Messages.FollowThreadAction_follow + ' ' + fThreadId;
        }
        return Messages.FollowThreadAction_follow + ' ' + fThreadName + '/' + fThreadId;
    }

    @Override
    public void run() {
        fView.broadcast(new TmfThreadSelectedSignal(fView, fThreadId, fTrace));
        super.run();
    }

}
