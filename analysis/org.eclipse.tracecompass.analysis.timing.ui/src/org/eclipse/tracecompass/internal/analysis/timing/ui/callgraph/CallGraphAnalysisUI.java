/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.ui.callgraph;

import java.util.Collections;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.analysis.timing.core.callgraph.CallGraphAnalysis;
import org.eclipse.tracecompass.tmf.core.segment.ISegmentAspect;

/**
 * CallGraph Analysis with aspects
 *
 * @author Sonia Farrah
 */
public class CallGraphAnalysisUI extends CallGraphAnalysis {

    /**
     * ID
     */
    public static final @NonNull String ID = "org.eclipse.tracecompass.internal.analysis.timing.ui.callgraph.callgraphanalysis"; //$NON-NLS-1$

    /**
     * Default constructor
     */
    public CallGraphAnalysisUI() {
        super();
    }

    @Override
    public @NonNull Iterable<@NonNull ISegmentAspect> getSegmentAspects() {
        return Collections.singletonList(SymbolAspect.SYMBOL_ASPECT);
    }
}
