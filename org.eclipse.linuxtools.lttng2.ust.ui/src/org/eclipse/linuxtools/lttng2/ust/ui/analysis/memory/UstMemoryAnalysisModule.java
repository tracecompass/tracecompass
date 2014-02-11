/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.ust.ui.analysis.memory;

import org.eclipse.linuxtools.internal.lttng2.ust.core.memoryusage.MemoryUsageStateProvider;
import org.eclipse.linuxtools.internal.lttng2.ust.ui.views.memusage.MemoryUsageView;
import org.eclipse.linuxtools.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.analysis.TmfAnalysisViewOutput;

/**
 * This analysis build a state system from the libc memory instrumentation on a
 * ust trace
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class UstMemoryAnalysisModule extends TmfStateSystemAnalysisModule {

    /**
     * Analysis ID, it should match that in the plugin.xml file
     */
    public static String ID = "org.eclipse.linuxtools.lttng2.ust.analysis.memory"; //$NON-NLS-1$

    /**
     * Constructor
     */
    public UstMemoryAnalysisModule() {
        super();
        registerOutput(new TmfAnalysisViewOutput(MemoryUsageView.ID));
    }

    @Override
    protected
    ITmfStateProvider createStateProvider() {
        return new MemoryUsageStateProvider(getTrace());
    }

    @Override
    public void setTrace(ITmfTrace trace) throws TmfAnalysisException {
        if (!(trace instanceof LttngUstTrace)) {
            throw new IllegalStateException("UstMemoryAnalysisModule: trace should be of type LttngUstTrace"); //$NON-NLS-1$
        }
        super.setTrace(trace);
    }

    @Override
    protected LttngUstTrace getTrace() {
        return (LttngUstTrace) super.getTrace();
    }

}
