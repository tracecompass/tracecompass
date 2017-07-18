/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.ui.criticalpath;

import org.eclipse.tracecompass.analysis.graph.core.criticalpath.CriticalPathModule;
import org.eclipse.tracecompass.analysis.os.linux.core.execution.graph.OsWorker;
import org.eclipse.tracecompass.analysis.os.linux.core.model.HostThread;
import org.eclipse.tracecompass.analysis.os.linux.core.signals.TmfThreadSelectedSignal;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisParamProvider;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Class that provides parameters to the critical path analysis for lttng kernel
 * traces
 *
 * @author Geneviève Bastien
 */
public class CriticalPathParameterProvider extends TmfAbstractAnalysisParamProvider {

    private static final String NAME = "Critical Path Lttng kernel parameter provider"; //$NON-NLS-1$

    private HostThread fCurrentHostThread = null;

    /**
     * Constructor
     */
    public CriticalPathParameterProvider() {
        super();
        TmfSignalManager.register(this);
    }

    @Override
    public void dispose() {
        super.dispose();
        TmfSignalManager.deregister(this);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Object getParameter(String name) {
        if (name.equals(CriticalPathModule.PARAM_WORKER)) {
            final HostThread currentHostThread = fCurrentHostThread;
            if (currentHostThread == null) {
                return null;
            }
            /* Try to find the worker for the critical path */
            IAnalysisModule mod = getModule();
            if ((mod != null) && (mod instanceof CriticalPathModule)) {
                OsWorker worker = new OsWorker(currentHostThread, "", 0); //$NON-NLS-1$
                return worker;
            }
        }
        return null;
    }

    @Override
    public boolean appliesToTrace(ITmfTrace trace) {
        return true;
    }

    private void setCurrentHostThread(HostThread hostThread) {
        if (!hostThread.equals(fCurrentHostThread)) {
            fCurrentHostThread = hostThread;
            notifyParameterChanged(CriticalPathModule.PARAM_WORKER);
        }
    }

    /**
     * Signal handler to know that a thread was selected
     *
     * @param signal
     *            the thread was selected
     */
    @TmfSignalHandler
    public void tmfThreadSelectedSignalHander(TmfThreadSelectedSignal signal) {
        final TmfThreadSelectedSignal threadSignal = signal;
        if (threadSignal != null) {
            setCurrentHostThread(new HostThread(threadSignal.getTraceHost(), threadSignal.getThreadId()));
        }
    }

    /**
     * Reset the selection when a new trace is selected
     *
     * @param signal The trace selected signal
     */
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        fCurrentHostThread = null;
    }

}
