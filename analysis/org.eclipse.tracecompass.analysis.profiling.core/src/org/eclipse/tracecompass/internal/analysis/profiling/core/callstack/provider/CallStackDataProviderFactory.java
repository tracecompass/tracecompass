/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.profiling.core.callstack.provider;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackAnalysis;
import org.eclipse.tracecompass.internal.tmf.core.model.DataProviderDescriptor;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor.ProviderType;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderFactory;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

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
        Iterator<CallStackAnalysis> modules = TmfTraceUtils.getAnalysisModulesOfClass(trace, CallStackAnalysis.class).iterator();
        if (modules.hasNext()) {
            CallStackAnalysis first = modules.next();
            first.schedule();
            return new CallStackDataProvider(trace, first);
        }
        return null;
    }

    @Override
    public Collection<IDataProviderDescriptor> getDescriptors(@NonNull ITmfTrace trace) {
        Iterable<@NonNull CallStackAnalysis> modules = TmfTraceUtils.getAnalysisModulesOfClass(trace, CallStackAnalysis.class);
        return !Iterables.isEmpty(modules) ? Collections.singletonList(DESCRIPTOR) : Collections.emptyList();
    }
}
