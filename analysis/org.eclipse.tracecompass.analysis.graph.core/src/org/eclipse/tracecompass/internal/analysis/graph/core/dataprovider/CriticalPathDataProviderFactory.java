/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.graph.core.dataprovider;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.graph.core.criticalpath.CriticalPathModule;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderFactory;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfStartAnalysisSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * {@link IDataProviderFactory} for the {@link CriticalPathDataProvider}
 *
 * @author Loic Prieur-Drevon
 */
public class CriticalPathDataProviderFactory implements IDataProviderFactory {

    private final Map<ITmfTrace, CriticalPathModule> map = new HashMap<>();

    /**
     * Constructor, registers the module with the {@link TmfSignalManager}
     */
    public CriticalPathDataProviderFactory() {
        TmfSignalManager.register(this);
    }

    @Override
    public @Nullable ITmfTreeDataProvider<? extends ITmfTreeDataModel> createProvider(@NonNull ITmfTrace trace) {
        CriticalPathModule module = map.remove(trace);
        if (module == null) {
            // the DataProviderManager does not negative cache
            return null;
        }
        return new CriticalPathDataProvider(trace, module);
    }

    /**
     * {@link TmfSignalHandler} for when {@link CriticalPathModule} is started, as
     * the analysis is not registered with the trace, we use this to know to
     * associate a {@link CriticalPathModule} to a trace.
     *
     * @param startAnalysisSignal
     *            analysis started signal
     */
    @TmfSignalHandler
    public synchronized void analysisStarted(TmfStartAnalysisSignal startAnalysisSignal) {
        IAnalysisModule analysis = startAnalysisSignal.getAnalysisModule();
        if (analysis instanceof CriticalPathModule) {
            CriticalPathModule criticalPath = (CriticalPathModule) analysis;
            map.put(criticalPath.getTrace(), criticalPath);
        }
    }

    /**
     * Remove the closed traces' Critical Path Module to avoid resource leaks.
     *
     * @param traceClosedSignal
     *            the TMF trace closed signal
     */
    @TmfSignalHandler
    public synchronized void traceClosed(TmfTraceClosedSignal traceClosedSignal) {
        map.remove(traceClosedSignal.getTrace());
    }

}
