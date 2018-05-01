/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.statistics.AbstractSegmentStatisticsAnalysis;
import org.eclipse.tracecompass.internal.tmf.core.model.tree.TmfTreeCompositeDataProvider;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.segment.interfaces.INamedSegment;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderFactory;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

/**
 * Generalized {@link SegmentStoreStatisticsDataProvider} factory using
 * secondary ID to identify which segment store provider to build it from.
 *
 * @author Loic Prieur-Drevon
 * @since 4.0
 */
public class SegmentStoreStatisticsDataProviderFactory implements IDataProviderFactory {

    private static final class GenericSegmentStatisticsAnalysis extends AbstractSegmentStatisticsAnalysis {
        private final String fSecondaryId;

        private GenericSegmentStatisticsAnalysis(String secondaryId) {
            fSecondaryId = secondaryId;
        }

        @Override
        protected @Nullable String getSegmentType(@NonNull ISegment segment) {
            if (segment instanceof INamedSegment) {
                return ((INamedSegment) segment).getName();
            }
            return null;
        }

        @Override
        protected @Nullable ISegmentStoreProvider getSegmentProviderAnalysis(@NonNull ITmfTrace trace) {
            IAnalysisModule segmentStoreModule = trace.getAnalysisModule(fSecondaryId);
            if (segmentStoreModule instanceof ISegmentStoreProvider) {
                return (ISegmentStoreProvider) segmentStoreModule;
            }
            return null;
        }
    }

    @Override
    public @Nullable ITmfTreeDataProvider<? extends ITmfTreeDataModel> createProvider(ITmfTrace trace) {
        return null;
    }

    @Override
    public @Nullable ITmfTreeDataProvider<? extends ITmfTreeDataModel> createProvider(ITmfTrace trace, String secondaryId) {

        IAnalysisModule m = trace.getAnalysisModule(secondaryId);
        String composedId = SegmentStoreStatisticsDataProvider.ID + ':' + secondaryId;
        // check that this trace has the queried analysis.
        if (!(m instanceof ISegmentStoreProvider)) {
            if (!(trace instanceof TmfExperiment)) {
                return null;
            }
            return TmfTreeCompositeDataProvider.create(TmfTraceManager.getTraceSet(trace), composedId);
        }
        m.schedule();

        AbstractSegmentStatisticsAnalysis module = new GenericSegmentStatisticsAnalysis(secondaryId);
        try {
            module.setTrace(trace);
        } catch (TmfAnalysisException e) {
            module.dispose();
            return null;
        }
        module.schedule();
        return new SegmentStoreStatisticsDataProvider(trace, module, composedId);
    }

}
