/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider;

import org.eclipse.jdt.annotation.NonNull;
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
 * {@link XmlPatternLatencyStatisticsAnalysis}.
 *
 * @author Loic Prieur-Drevon
 */
public class XmlPatternLatencyStatisticsDataProviderFactory implements IDataProviderFactory {

    /**
     * Data provider prefix for this extension point / factory
     */
    public static final String ID = "org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternLatencyStatisticsAnalysis.statistics"; //$NON-NLS-1$

    @Override
    public @Nullable ITmfTreeDataProvider<? extends ITmfTreeDataModel> createProvider(@NonNull ITmfTrace trace) {
        return null;
    }

    @Override
    public @Nullable ITmfTreeDataProvider<? extends ITmfTreeDataModel> createProvider(@NonNull ITmfTrace trace, @NonNull String secondaryId) {

        // check that this trace has the queried analysis.
        XmlPatternAnalysis analysis = TmfTraceUtils.getAnalysisModuleOfClass(trace, XmlPatternAnalysis.class, secondaryId);
        if (analysis == null) {
            return null;
        }
        analysis.schedule();

        XmlPatternLatencyStatisticsAnalysis statisticsProvider = new XmlPatternLatencyStatisticsAnalysis(secondaryId);
        try {
            statisticsProvider.setTrace(trace);
        } catch (TmfAnalysisException e) {
            statisticsProvider.dispose();
            return null;
        }
        statisticsProvider.schedule();
        return new SegmentStoreStatisticsDataProvider(trace, statisticsProvider, ID + ':' + secondaryId);
    }

}
