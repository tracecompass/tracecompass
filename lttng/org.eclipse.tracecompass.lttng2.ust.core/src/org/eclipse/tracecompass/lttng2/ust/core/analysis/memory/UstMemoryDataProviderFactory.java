/**********************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.analysis.memory;

import java.util.Collection;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.TmfTreeXYCompositeDataProvider;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderFactory;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;

/**
 * {@link UstMemoryUsageDataProvider} factory using the data provider factory
 * extension point
 *
 * @author Loic Prieur-Drevon
 * @since 3.2
 */
@SuppressWarnings("restriction")
public class UstMemoryDataProviderFactory implements IDataProviderFactory {

    private static final @NonNull String TITLE = Objects.requireNonNull(Messages.MemoryUsageDataProvider_Title);

    @Override
    public @Nullable ITmfTreeXYDataProvider<? extends ITmfTreeDataModel> createProvider(@NonNull ITmfTrace trace) {
        Collection<@NonNull ITmfTrace> traces = TmfTraceManager.getTraceSet(trace);
        if (traces.size() == 1) {
            return UstMemoryUsageDataProvider.create(trace);
        }
        // handle the case where the trace is an experiment
        return TmfTreeXYCompositeDataProvider.create(traces, TITLE, UstMemoryUsageDataProvider.ID);
    }

}
