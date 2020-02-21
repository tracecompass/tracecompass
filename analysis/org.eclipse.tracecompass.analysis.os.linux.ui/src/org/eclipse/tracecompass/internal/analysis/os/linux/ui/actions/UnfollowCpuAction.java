/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.actions;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.action.Action;
import org.eclipse.tracecompass.analysis.os.linux.core.signals.TmfCpuSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;

/**
 * CPU Selection Action
 *
 * @author Matthew Khouzam
 */
public class UnfollowCpuAction extends Action {

    private final @NonNull TmfView fView;
    private final int fCpu;
    private final @NonNull ITmfTrace fTrace;

    /**
     * Contructor
     *
     * @param view
     *            the view to send a signal
     * @param cpu
     *            the cpu number
     * @param trace
     *            the trace
     */
    public UnfollowCpuAction(@NonNull TmfView view, int cpu, @NonNull ITmfTrace trace) {
        fView = view;
        fCpu = cpu;
        fTrace = trace;
    }

    @Override
    public String getText() {
        return Messages.CpuSelectionAction_unfollowCpu;
    }

    @Override
    public void run() {
        fView.broadcast(new TmfCpuSelectedSignal(fView, -fCpu - 1, fTrace));
        super.run();
    }

}
