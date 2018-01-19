/**********************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.cpuusage;

import java.util.Collection;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.cpuusage.Messages;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.TmfTreeXYCompositeDataProvider;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderFactory;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;

/**
 * Extension point factory for the {@link CpuUsageDataProvider}.
 *
 * @since 2.4
 * @author Loic Prieur-Drevon
 */
@SuppressWarnings("restriction")
public class CpuUsageProviderFactory implements IDataProviderFactory {

    @Override
    public @Nullable ITmfTreeDataProvider<? extends ITmfTreeDataModel> createProvider(ITmfTrace trace) {
        Collection<ITmfTrace> traces = TmfTraceManager.getTraceSet(trace);
        if (traces.size() == 1) {
            return CpuUsageDataProvider.create(trace);
        }
        return TmfTreeXYCompositeDataProvider.create(traces, Objects.requireNonNull(Messages.CpuUsageDataProvider_title), CpuUsageDataProvider.ID);
    }

}
