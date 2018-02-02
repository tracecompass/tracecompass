/*******************************************************************************
 * Copyright (c) 2017, 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.DataProviderDescriptor;
import org.eclipse.tracecompass.internal.tmf.core.model.xy.TmfTreeXYCompositeDataProvider;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.component.DataProviderConstants;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor.ProviderType;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderFactory;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

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

    @Override
    public Collection<IDataProviderDescriptor> getDescriptors(ITmfTrace trace) {
        Iterable<ISegmentStoreProvider> modules = TmfTraceUtils.getAnalysisModulesOfClass(trace, ISegmentStoreProvider.class);
        List<IDataProviderDescriptor> descriptors = new ArrayList<>();
        Set<String> existingModules = new HashSet<>();
        for (ISegmentStoreProvider module : modules) {
            IAnalysisModule analysis = (IAnalysisModule) module;
            // Only add analysis once per trace (which could be an experiment)
            if (!existingModules.contains(analysis.getId())) {
                DataProviderDescriptor.Builder builder = new DataProviderDescriptor.Builder();
                builder.setId(SegmentStoreScatterDataProvider.ID + DataProviderConstants.ID_SEPARATOR + analysis.getId())
                    .setName(Objects.requireNonNull(NLS.bind(Messages.SegmentStoreScatterGraphDataProvider_title, analysis.getName())))
                    .setDescription(Objects.requireNonNull(NLS.bind(Messages.SegmentStoreScatterGraphDataProvider_description, analysis.getHelpText())))
                    .setProviderType(ProviderType.TREE_TIME_XY);
                descriptors.add(builder.build());
            }
        }
        return descriptors;
    }
}
