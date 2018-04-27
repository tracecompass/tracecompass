/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.core.segmentstore;

import java.util.Collection;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.Messages;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.TmfTreeXYCompositeDataProvider;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderFactory;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;

/**
 * @author Geneviève Bastien
 * @since 4.0
 */
public class SegmentStoreScatterDataProviderFactory implements IDataProviderFactory {

    @Override
    public @Nullable ITmfTreeDataProvider<? extends ITmfTreeDataModel> createProvider(ITmfTrace trace) {
        // Need the analysis
        return null;
    }

    @Override
    public @Nullable ITmfTreeDataProvider<? extends ITmfTreeDataModel> createProvider(ITmfTrace trace, String secondaryId) {
        // Create with the trace or experiment first
        ITmfTreeDataProvider<? extends ITmfTreeDataModel> provider = SegmentStoreScatterDataProvider.create(trace, secondaryId);
        if (provider != null) {
            return provider;
        }
        // Otherwise, see if it's an experiment and create a composite if that's the case
        Collection<ITmfTrace> traces = TmfTraceManager.getTraceSet(trace);
        if (traces.size() == 1) {
            return SegmentStoreScatterDataProvider.create(trace, secondaryId);
        }
        return TmfTreeXYCompositeDataProvider.create(traces, Objects.requireNonNull(Messages.SegmentStoreScatterGraphViewer_title), SegmentStoreScatterDataProvider.ID, secondaryId);

    }

}
