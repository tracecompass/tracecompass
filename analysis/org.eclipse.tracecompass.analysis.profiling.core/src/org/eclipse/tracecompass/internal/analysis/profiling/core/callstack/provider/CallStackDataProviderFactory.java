/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.profiling.core.callstack.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackAnalysis;
import org.eclipse.tracecompass.internal.tmf.core.model.DataProviderDescriptor;
import org.eclipse.tracecompass.internal.tmf.core.model.timegraph.TmfTimeGraphCompositeDataProvider;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor.ProviderType;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderFactory;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

import com.google.common.collect.Iterables;

/**
 * {@link CallStackDataProvider} factory, uses the data provider extension
 * point.
 *
 * @author Loic Prieur-Drevon
 */
public class CallStackDataProviderFactory implements IDataProviderFactory {

    private static final IDataProviderDescriptor DESCRIPTOR = new DataProviderDescriptor.Builder()
            .setId(CallStackDataProvider.ID)
            .setName(Objects.requireNonNull(Messages.CallStackDataProviderFactory_title))
            .setDescription(Objects.requireNonNull(Messages.CallStackDataProviderFactory_descriptionText))
            .setProviderType(ProviderType.TIME_GRAPH)
            .build();

    @Override
    public @Nullable ITmfTreeDataProvider<? extends ITmfTreeDataModel> createProvider(ITmfTrace trace) {
        if (trace instanceof TmfExperiment) {
            @NonNull List<@NonNull CallStackDataProvider> providers = new ArrayList<>();
            for (ITmfTrace child : TmfTraceManager.getTraceSet(trace)) {
                CallStackDataProvider provider = createProviderLocal(child);
                if (provider != null) {
                    providers.add(provider);
                }
            }
            if (providers.size() == 1) {
                return providers.get(0);
            }
            if (!providers.isEmpty()) {
                return new TmfTimeGraphCompositeDataProvider<>(providers, CallStackDataProvider.ID);
            }
            return null;
        }
        return createProviderLocal(trace);
    }

    @Override
    public Collection<IDataProviderDescriptor> getDescriptors(@NonNull ITmfTrace trace) {
        Iterable<@NonNull CallStackAnalysis> modules = TmfTraceUtils.getAnalysisModulesOfClass(trace, CallStackAnalysis.class);
        return !Iterables.isEmpty(modules) ? Collections.singletonList(DESCRIPTOR) : Collections.emptyList();
    }

    private static @Nullable CallStackDataProvider createProviderLocal (@NonNull ITmfTrace trace) {
        Iterator<CallStackAnalysis> modules = TmfTraceUtils.getAnalysisModulesOfClass(trace, CallStackAnalysis.class).iterator();
        while (modules.hasNext()) {
            CallStackAnalysis first = modules.next();
            first.schedule();
            return new CallStackDataProvider(trace, first);
        }
        return null;
    }
}
