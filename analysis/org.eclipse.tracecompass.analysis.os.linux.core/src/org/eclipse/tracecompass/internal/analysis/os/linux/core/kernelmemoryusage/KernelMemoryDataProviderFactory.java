/**********************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelmemoryusage;

import java.util.Collection;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.model.xy.TmfTreeXYCompositeDataProvider;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderFactory;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;

/**
 * Factory to create instances of the {@link KernelMemoryUsageDataProvider}.
 * Uses the DataProviderFactory extension point.
 *
 * @since 2.4
 * @author Loic Prieur-Drevon
 */
public class KernelMemoryDataProviderFactory implements IDataProviderFactory {

    private static final String TITLE = Objects.requireNonNull(Messages.KernelMemoryUsageDataProvider_title);

    @Override
    public @Nullable ITmfTreeDataProvider<? extends ITmfTreeDataModel> createProvider(@NonNull ITmfTrace trace) {
        Collection<ITmfTrace> traces = TmfTraceManager.getTraceSet(trace);
        if (traces.size() == 1) {
            return KernelMemoryUsageDataProvider.create(trace);

        }
        return TmfTreeXYCompositeDataProvider.create(traces, TITLE, KernelMemoryUsageDataProvider.ID);
    }

}
