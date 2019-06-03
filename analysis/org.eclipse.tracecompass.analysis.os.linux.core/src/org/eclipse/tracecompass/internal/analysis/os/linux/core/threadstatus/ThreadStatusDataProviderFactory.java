/**********************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.internal.tmf.core.model.DataProviderDescriptor;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderDescriptor.ProviderType;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderFactory;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * {@link ThreadStatusDataProvider} factory, uses the data provider extension
 * point.
 *
 * @author Loic Prieur-Drevon
 */
public class ThreadStatusDataProviderFactory implements IDataProviderFactory {

    private static final IDataProviderDescriptor DESCRIPTOR = new DataProviderDescriptor.Builder()
            .setId(ThreadStatusDataProvider.ID)
            .setName(Objects.requireNonNull(Messages.ThreadStatusDataProviderFactory_title))
            .setDescription(Objects.requireNonNull(Messages.ThreadStatusDataProviderFactory_descriptionText))
            .setProviderType(ProviderType.TIME_GRAPH)
            .build();

    @Override
    public @Nullable ITmfTreeDataProvider<? extends ITmfTreeDataModel> createProvider(@NonNull ITmfTrace trace) {
        KernelAnalysisModule module = TmfTraceUtils.getAnalysisModuleOfClass(trace, KernelAnalysisModule.class, KernelAnalysisModule.ID);
        if (module != null) {
            module.schedule();
            return new ThreadStatusDataProvider(trace, module);
        }

        return null;
    }

    @Override
    public Collection<IDataProviderDescriptor> getDescriptors(@NonNull ITmfTrace trace) {
        KernelAnalysisModule module = TmfTraceUtils.getAnalysisModuleOfClass(trace, KernelAnalysisModule.class, KernelAnalysisModule.ID);
        return module != null ? Collections.singletonList(DESCRIPTOR) : Collections.emptyList();
    }

}
