/*******************************************************************************
 * Copyright (c) 2014, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.core.callstack;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackAnalysis;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.lttng2.ust.core.trace.layout.ILttngUstEventLayout;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableSet;

/**
 * Call-stack analysis to populate the TMF CallStack View from UST cyg-profile
 * events.
 *
 * @author Alexandre Montplaisir
 */
public class LttngUstCallStackAnalysis extends CallStackAnalysis {

    /**
     * ID
     */
    public static final String ID = "org.eclipse.linuxtools.lttng2.ust.analysis.callstack"; //$NON-NLS-1$

    private @Nullable Set<@NonNull TmfAbstractAnalysisRequirement> fAnalysisRequirements = null;

    @Override
    public boolean setTrace(ITmfTrace trace) throws TmfAnalysisException {
        if (!(trace instanceof LttngUstTrace)) {
            return false;
        }
        return super.setTrace(trace);
    }

    @Override
    public LttngUstTrace getTrace() {
        return (LttngUstTrace) super.getTrace();
    }

    @Override
    protected ITmfStateProvider createStateProvider() {
        return new LttngUstCallStackProvider(checkNotNull(getTrace()));
    }

    @Override
    public @NonNull Iterable<@NonNull TmfAbstractAnalysisRequirement> getAnalysisRequirements() {

        Set<@NonNull TmfAbstractAnalysisRequirement> requirements = fAnalysisRequirements;
        if (requirements == null) {
            LttngUstTrace trace = getTrace();
            ILttngUstEventLayout layout = ILttngUstEventLayout.DEFAULT_LAYOUT;
            if (trace != null) {
                layout = trace.getEventLayout();
            }
            requirements = ImmutableSet.of(new LttngUstCallStackAnalysisRequirement(layout));
            fAnalysisRequirements = requirements;
        }
        return requirements;
    }
}
