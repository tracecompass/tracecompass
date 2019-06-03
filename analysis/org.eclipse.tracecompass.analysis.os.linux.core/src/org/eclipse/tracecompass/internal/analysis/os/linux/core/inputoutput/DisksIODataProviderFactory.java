/**********************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.InputOutputAnalysisModule;
import org.eclipse.tracecompass.internal.tmf.core.model.DataProviderDescriptor;
import org.eclipse.tracecompass.internal.tmf.core.model.xy.TmfTreeXYCompositeDataProvider;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor.ProviderType;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderFactory;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * Factory to create instances of the {@link ITmfTreeXYDataProvider}. Uses the
 * DataProviderFactory endpoint.
 *
 * @author Loic Prieur-Drevon
 */
public class DisksIODataProviderFactory implements IDataProviderFactory {
    private static final Predicate<? super ITmfTrace> PREDICATE = t -> TmfTraceUtils.getAnalysisModuleOfClass(t, InputOutputAnalysisModule.class, InputOutputAnalysisModule.ID) != null;
    private static final IDataProviderDescriptor DESCRIPTOR = new DataProviderDescriptor.Builder()
            .setId(DisksIODataProvider.ID)
            .setName(Objects.requireNonNull(Messages.DisksIODataProvider_title))
            .setDescription(Objects.requireNonNull(Messages.DisksIODataProviderFactory_descriptionText))
            .setProviderType(ProviderType.TREE_TIME_XY)
            .build();

    @Override
    public @Nullable ITmfTreeXYDataProvider<? extends ITmfTreeDataModel> createProvider(ITmfTrace trace) {
        Collection<ITmfTrace> traces = TmfTraceManager.getTraceSet(trace);
        if (traces.size() == 1) {
            return DisksIODataProvider.create(trace);
        }
        return TmfTreeXYCompositeDataProvider.create(traces, DisksIODataProvider.PROVIDER_TITLE, DisksIODataProvider.ID);
    }

    @Override
    public Collection<IDataProviderDescriptor> getDescriptors(@NonNull ITmfTrace trace) {
        Collection<ITmfTrace> traces = TmfTraceManager.getTraceSet(trace);
        return Iterables.any(traces, PREDICATE) ? Collections.singletonList(DESCRIPTOR) : Collections.emptyList();
    }

}
