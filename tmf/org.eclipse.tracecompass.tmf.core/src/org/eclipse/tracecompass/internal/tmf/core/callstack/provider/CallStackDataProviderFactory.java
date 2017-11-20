/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.callstack.provider;

import java.util.Iterator;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.callstack.CallStackAnalysis;
import org.eclipse.tracecompass.tmf.core.dataprovider.IDataProviderFactory;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * {@link CallStackDataProvider} factory, uses the data provider extension
 * point.
 *
 * @author Loic Prieur-Drevon
 */
public class CallStackDataProviderFactory implements IDataProviderFactory {

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

}
