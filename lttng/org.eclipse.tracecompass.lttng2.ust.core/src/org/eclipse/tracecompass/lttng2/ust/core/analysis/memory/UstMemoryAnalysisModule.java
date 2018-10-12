/*******************************************************************************
 * Copyright (c) 2014, 2016 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Guilliano Molaire - Provide the requirements of the analysis
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.analysis.memory;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * This analysis build a state system from the libc memory instrumentation on a
 * UST trace
 *
 * @author Geneviève Bastien
 * @deprecated This class does not need to be API. It has been moved to
 *             {@link org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.memory.UstMemoryAnalysisModule}
 */
@Deprecated
public class UstMemoryAnalysisModule extends TmfStateSystemAnalysisModule {


    /**
     * Analysis ID, it should match that in the plugin.xml file
     */
    public static final @NonNull String ID = "org.eclipse.linuxtools.lttng2.ust.analysis.memory"; //$NON-NLS-1$

    private final org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.memory.UstMemoryAnalysisModule fInternalModule =
            new org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.memory.UstMemoryAnalysisModule();

    @Override
    protected ITmfStateProvider createStateProvider() {
        return fInternalModule.createStateProvider();
    }

    /**
     * @since 1.0
     */
    @Override
    public boolean setTrace(ITmfTrace trace) throws TmfAnalysisException {
       return fInternalModule.setTrace(trace);
    }

    /**
     * @since 3.0
     */
    @Override
    public LttngUstTrace getTrace() {
        return fInternalModule.getTrace();
    }

    @Override
    public Iterable<TmfAbstractAnalysisRequirement> getAnalysisRequirements() {
       return fInternalModule.getAnalysisRequirements();
    }
}
