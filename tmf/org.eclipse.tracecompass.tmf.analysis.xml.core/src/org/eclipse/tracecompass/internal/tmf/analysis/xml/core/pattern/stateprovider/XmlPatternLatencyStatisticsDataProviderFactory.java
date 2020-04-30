/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.SegmentStoreStatisticsDataProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.DataProviderDescriptor;
import org.eclipse.tracecompass.internal.tmf.core.model.tree.TmfTreeCompositeDataProvider;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor.ProviderType;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderFactory;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;


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
        if (trace instanceof TmfExperiment) {
            return TmfTreeCompositeDataProvider.create(TmfTraceManager.getTraceSet(trace), ID + ':' + secondaryId);
        }

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

    @Override
    public Collection<IDataProviderDescriptor> getDescriptors(ITmfTrace trace) {
        Set<IDataProviderDescriptor> descriptors = new HashSet<>();
        if (trace instanceof TmfExperiment) {
            for (ITmfTrace child : TmfTraceManager.getTraceSet(trace)) {
                descriptors.addAll(getDescriptors(child));
            }
            return descriptors;
        }
        // Single Trace
        Iterable<XmlPatternAnalysis> modules = TmfTraceUtils.getAnalysisModulesOfClass(trace, XmlPatternAnalysis.class);
        for (XmlPatternAnalysis module : modules) {
            descriptors.add(new DataProviderDescriptor.Builder()
                    .setId(ID + ':' + module.getId())
                    .setName(Objects.requireNonNull(module.getName()))
                    .setDescription(Objects.requireNonNull(module.getName()))
                    .setProviderType(ProviderType.DATA_TREE)
                    .build());
        }
        return new ArrayList<>(descriptors);
    }
}
