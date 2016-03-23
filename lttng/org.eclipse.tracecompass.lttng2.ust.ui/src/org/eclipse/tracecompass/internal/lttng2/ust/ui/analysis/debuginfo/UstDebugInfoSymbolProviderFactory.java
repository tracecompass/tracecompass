/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.ui.analysis.debuginfo;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo.UstDebugInfoAnalysisModule;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.symbols.ISymbolProvider;
import org.eclipse.tracecompass.tmf.ui.symbols.ISymbolProviderFactory;

/**
 * Factory to create {@link UstDebugInfoSymbolProvider}. Provided to TMF via
 * the extension point. Only works with LTTng-UST traces.
 *
 * @author Alexandre Montplaisir
 */
public class UstDebugInfoSymbolProviderFactory implements ISymbolProviderFactory {

    @Override
    public @Nullable ISymbolProvider createProvider(ITmfTrace trace) {
        /*
         * This applies only to UST traces that fulfill the DebugInfo analysis
         * requirements.
         */
        UstDebugInfoAnalysisModule module = TmfTraceUtils.getAnalysisModuleOfClass(trace,
                UstDebugInfoAnalysisModule.class, UstDebugInfoAnalysisModule.ID);

        if (module != null && trace instanceof LttngUstTrace) {
            return new UstDebugInfoSymbolProvider((LttngUstTrace) trace);
        }
        return null;
    }

}
