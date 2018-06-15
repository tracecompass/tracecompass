/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.profiling.core.callstack;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;

/**
 * Interface that can be implemented by components who provide call stacks as
 * part of their data.
 *
 * @author Geneviève Bastien
 * TODO: Bring methods from the incubator to this interface
 */
public interface IFlameChartProvider extends IAnalysisModule, ISegmentStoreProvider {

    /**
     * Get the callstacks series provided by this analysis.
     *
     * @return The callstack series or null if it is not available yet
     */
    @Nullable CallStackSeries getCallStackSeries();
}
