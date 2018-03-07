/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.core.callgraph;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.SegmentStoreStatisticsDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderFactory;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * {@link SegmentStoreStatisticsDataProvider} factory for the
 * {@link CallGraphStatisticsAnalysis}.
 *
 * @author Loic Prieur-Drevon
 */
public class CallGraphStatisticsDataProviderFactory implements IDataProviderFactory {

    @Override
    public @Nullable ITmfTreeDataProvider<? extends ITmfTreeDataModel> createProvider(ITmfTrace trace) {
        CallGraphAnalysis analysis = TmfTraceUtils.getAnalysisModuleOfClass(trace, CallGraphAnalysis.class, CallGraphAnalysis.ID);
        if (analysis == null) {
            return null;
        }
        CallGraphStatisticsAnalysis statisticsAnalysis = new CallGraphStatisticsAnalysis();
        try {
            statisticsAnalysis.setTrace(trace);
        } catch (TmfAnalysisException e) {
            statisticsAnalysis.dispose();
            return null;
        }
        statisticsAnalysis.schedule();
        return new SegmentStoreStatisticsDataProvider(trace, statisticsAnalysis, CallGraphStatisticsAnalysis.ID);
    }

}
