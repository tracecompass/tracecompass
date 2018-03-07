/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.analysis.timing.core.segmentstore;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.statistics.AbstractSegmentStatisticsAnalysis;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.segment.interfaces.INamedSegment;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderFactory;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Generalized {@link SegmentStoreStatisticsDataProvider} factory using
 * secondaty ID to identify which segment store provider to build it from.
 *
 * @author Loic Prieur-Drevon
 */
public class SegmentStoreStatisticsDataProviderFactory implements IDataProviderFactory {

    @Override
    public @Nullable ITmfTreeDataProvider<? extends ITmfTreeDataModel> createProvider(ITmfTrace trace) {
        return null;
    }

    @Override
    public @Nullable ITmfTreeDataProvider<? extends ITmfTreeDataModel> createProvider(ITmfTrace trace, String secondaryId) {

        // check that this trace has the queried analysis.
        IAnalysisModule m = trace.getAnalysisModule(secondaryId);
        if (!(m instanceof ISegmentStoreProvider)) {
            return null;
        }
        m.schedule();

        AbstractSegmentStatisticsAnalysis module = new AbstractSegmentStatisticsAnalysis() {

            @Override
            protected @Nullable String getSegmentType(@NonNull ISegment segment) {
                if (segment instanceof INamedSegment) {
                    return ((INamedSegment) segment).getName();
                }
                return null;
            }

            @Override
            protected @Nullable ISegmentStoreProvider getSegmentProviderAnalysis(@NonNull ITmfTrace trace2) {
                IAnalysisModule segmentStoreModule = trace2.getAnalysisModule(secondaryId);
                if (!(segmentStoreModule instanceof ISegmentStoreProvider)) {
                    return null;
                }
                return (ISegmentStoreProvider) segmentStoreModule;
            }

        };
        try {
            module.setTrace(trace);
        } catch (TmfAnalysisException e) {
            module.dispose();
            return null;
        }
        module.schedule();
        return new SegmentStoreStatisticsDataProvider(trace, module, SegmentStoreStatisticsDataProvider.ID + ':' + secondaryId);
    }

}
