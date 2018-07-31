/*******************************************************************************
 * Copyright (c) 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.profiling.core.callstack;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.profiling.core.base.IProfilingElement;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackHostUtils.IHostIdProvider;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackSeries.IThreadIdProvider;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;

/**
 * Represents the actual callstack for one element. The callstack is a stack of
 * calls, whether function calls, executions, sub-routines that have a certain
 * depth and where durations at each depth is in the form of a reverse pyramid,
 * ie, a call at level n+1 will have start_n+1 >= start_n and end_n+1 <= end_n.
 *
 * TODO: Is that true? the reverse pyramid?
 *
 * @author Geneviève Bastien
 * @since 1.1
 */
public class CallStack {

    private final List<Integer> fQuarks;

    /**
     * Constructor
     *
     * @param ss
     *            The state system containing the callstack
     * @param quarks
     *            The quarks corresponding to each of the depth levels
     * @param element
     *            The element this callstack belongs to
     * @param hostIdProvider
     *            The provider of the host ID for this callstack
     * @param threadIdProvider
     *            The provider of the thread ID for this callstack
     */
    public CallStack(ITmfStateSystem ss, List<Integer> quarks, IProfilingElement element, IHostIdProvider hostIdProvider, @Nullable IThreadIdProvider threadIdProvider) {
        fQuarks = quarks;
    }

    /**
     * Get the maximum depth of this callstack
     *
     * @return The maximum depth of the callstack
     */
    public int getMaxDepth() {
        return fQuarks.size();
    }

    /**
     * Update the quarks list. Only the quarks at positions higher than the size of
     * the quarks will be copied in the list. The ones currently present should not
     * change.
     *
     * @param subAttributes
     *            The new complete list of attributes
     */
    public void updateAttributes(List<Integer> subAttributes) {
        fQuarks.addAll(fQuarks.size(), subAttributes.subList(fQuarks.size(), subAttributes.size()));
    }

}
