/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.counters.core;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.counters.core.aspects.ITmfCounterAspect;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.Iterables;

/**
 * Analysis module for populating the counter state system.
 *
 * @author Mikael Ferland
 */
public class CounterAnalysis extends TmfStateSystemAnalysisModule {

    /**
     * Grouped counter aspects attribute name
     */
    public static final String GROUPED_COUNTER_ASPECTS_ATTRIB = "Grouped"; //$NON-NLS-1$

    /**
     * Ungrouped counter aspects attribute name
     */
    public static final String UNGROUPED_COUNTER_ASPECTS_ATTRIB = "Ungrouped"; //$NON-NLS-1$

    /**
     * ID
     */
    public static final String ID = "org.eclipse.tracecompass.analysis.counters.core.counteranalysis"; //$NON-NLS-1$

    @Override
    public boolean canExecute(@NonNull ITmfTrace trace) {
        return Iterables.any(trace.getEventAspects(), aspect -> aspect instanceof ITmfCounterAspect);
    }

    @Override
    protected @NonNull ITmfStateProvider createStateProvider() {
        return CounterStateProvider.create(Objects.requireNonNull(getTrace()));
    }

}
