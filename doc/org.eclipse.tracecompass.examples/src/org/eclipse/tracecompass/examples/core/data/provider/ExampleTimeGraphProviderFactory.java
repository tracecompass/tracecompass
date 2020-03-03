/**********************************************************************
 * Copyright (c) 2020 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.examples.core.data.provider;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.examples.core.analysis.ExampleStateSystemAnalysisModule;
import org.eclipse.tracecompass.internal.tmf.core.model.DataProviderDescriptor;
import org.eclipse.tracecompass.internal.tmf.core.model.xy.TmfTreeXYCompositeDataProvider;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor.ProviderType;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderFactory;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * An example of a time graph data provider factory.
 *
 * This factory is also in the developer documentation of Trace Compass. If it is
 * modified here, the doc should also be updated.
 *
 * @author Geneviève Bastien
 */
@SuppressWarnings("restriction")
public class ExampleTimeGraphProviderFactory implements IDataProviderFactory {

    private static final IDataProviderDescriptor DESCRIPTOR = new DataProviderDescriptor.Builder()
            .setId(ExampleTimeGraphDataProvider.ID)
            .setName("Example time graph data provider") //$NON-NLS-1$
            .setDescription("This is an example of a time graph data provider using a state system analysis as a source of data") //$NON-NLS-1$
            .setProviderType(ProviderType.TIME_GRAPH)
            .build();

    @Override
    public @Nullable ITmfTreeDataProvider<? extends ITmfTreeDataModel> createProvider(ITmfTrace trace) {
        Collection<@NonNull ITmfTrace> traces = TmfTraceManager.getTraceSet(trace);
        if (traces.size() == 1) {
            return ExampleTimeGraphDataProvider.create(trace);
        }
        return TmfTreeXYCompositeDataProvider.create(traces, "Example time graph data provider", ExampleTimeGraphDataProvider.ID); //$NON-NLS-1$
    }

    @Override
    public Collection<IDataProviderDescriptor> getDescriptors(@NonNull ITmfTrace trace) {
        ExampleStateSystemAnalysisModule module = TmfTraceUtils.getAnalysisModuleOfClass(trace, ExampleStateSystemAnalysisModule.class, ExampleStateSystemAnalysisModule.ID);
        return module != null ? Collections.singletonList(DESCRIPTOR) : Collections.emptyList();
    }

}
