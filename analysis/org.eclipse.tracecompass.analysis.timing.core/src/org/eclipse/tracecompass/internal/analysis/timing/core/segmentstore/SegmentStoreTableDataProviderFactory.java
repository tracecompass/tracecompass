/*******************************************************************************
 * Copyright (c) 2022 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Kyrollos Bekhet - Initial API and implementation
 ******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.DataProviderDescriptor;
import org.eclipse.tracecompass.internal.tmf.core.model.tree.TmfTreeCompositeDataProvider;
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
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

/**
 * Segment store table data provider factory to create
 * {@link SegmentStoreTableDataProvider} using secondary ID to identify which
 * segment store provider to build it from.
 *
 * @author Kyrollos Bekhet
 */
public class SegmentStoreTableDataProviderFactory implements IDataProviderFactory {

    @Override
    public @Nullable ITmfTreeDataProvider<? extends ITmfTreeDataModel> createProvider(ITmfTrace trace) {
        return null;
    }

    @Override
    public @Nullable ITmfTreeDataProvider<? extends ITmfTreeDataModel> createProvider(@NonNull ITmfTrace trace, @NonNull String secondaryId) {
        IAnalysisModule m = trace.getAnalysisModule(secondaryId);
        String composedId = SegmentStoreTableDataProvider.ID + ':' + secondaryId;
        if (!(m instanceof ISegmentStoreProvider)) {
            if (!(trace instanceof TmfExperiment)) {
                return null;
            }
            return TmfTreeCompositeDataProvider.create(TmfTraceManager.getTraceSet(trace), composedId);
        }
        m.schedule();
        return new SegmentStoreTableDataProvider(trace, (ISegmentStoreProvider) m, composedId);

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
                builder.setId(SegmentStoreTableDataProvider.ID + DataProviderConstants.ID_SEPARATOR + analysis.getId())
                        .setName(Objects.requireNonNull(NLS.bind(Messages.SegmentStoreTableDataProvider_title, analysis.getName())))
                        .setDescription(Objects.requireNonNull(NLS.bind(Messages.SegmentStoreTableDataProvider_description, analysis.getHelpText())))
                        .setProviderType(ProviderType.TABLE);
                descriptors.add(builder.build());
                existingModules.add(analysis.getId());
            }
        }
        return descriptors;
    }
}
