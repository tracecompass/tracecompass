/**********************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.histogram;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.core.model.DataProviderDescriptor;
import org.eclipse.tracecompass.internal.tmf.core.model.xy.TmfTreeXYCompositeDataProvider;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor.ProviderType;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderFactory;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStatisticsModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * Factory to create instances of the {@link ITmfTreeXYDataProvider}. Uses the
 * DataProviderFactory endpoint.
 *
 * @author Loic Prieur-Drevon
 * @since 4.0
 */
public class HistogramDataProviderFactory implements IDataProviderFactory {

    private static final IDataProviderDescriptor DESCRIPTOR =
            new DataProviderDescriptor.Builder()
                        .setId(HistogramDataProvider.ID)
                        .setName(NonNullUtils.nullToEmptyString(Messages.HistogramDataProvider_Title))
                        .setDescription(NonNullUtils.nullToEmptyString(Messages.HistogramDataProviderFactory_DescriptionText))
                        .setProviderType(ProviderType.TREE_TIME_XY)
                        .build();

    @Override
    public @Nullable ITmfTreeXYDataProvider<? extends ITmfTreeDataModel> createProvider(ITmfTrace trace) {
        Collection<ITmfTrace> traces = TmfTraceManager.getTraceSet(trace);
        if (traces.size() == 1) {
            TmfStatisticsModule statsMod = TmfTraceUtils.getAnalysisModuleOfClass(trace, TmfStatisticsModule.class, TmfStatisticsModule.ID);
            if (statsMod != null) {
                statsMod.schedule();
                return new HistogramDataProvider(trace, statsMod);
            }
        }
        return TmfTreeXYCompositeDataProvider.create(traces, HistogramDataProvider.TITLE, HistogramDataProvider.ID);
    }

    @Override
    public Collection<IDataProviderDescriptor> getDescriptors(@NonNull ITmfTrace trace) {
        return Collections.singletonList(DESCRIPTOR);
    }

}
