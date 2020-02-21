/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.ui.analysis.debuginfo;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.debuginfo.UstDebugInfoSymbolProvider;
import org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo.UstDebugInfoAnalysisModule;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.tmf.ui.symbols.ISymbolProvider;
import org.eclipse.tracecompass.tmf.ui.symbols.ISymbolProviderPreferencePage;

/**
 * Symbol provider for UST traces with debug information.
 *
 * @author Alexandre Montplaisir
 * @see UstDebugInfoAnalysisModule
 */
public class UstDebugInfoUiSymbolProvider extends UstDebugInfoSymbolProvider implements ISymbolProvider {

    /**
     * Constructor
     *
     * @param trace
     *            the corresponding trace
     */
    public UstDebugInfoUiSymbolProvider(LttngUstTrace trace) {
        super(trace);
    }

    @Override
    public @NonNull ISymbolProviderPreferencePage createPreferencePage() {
        return new UstDebugInfoSymbolProviderPreferencePage(this);
    }

}
