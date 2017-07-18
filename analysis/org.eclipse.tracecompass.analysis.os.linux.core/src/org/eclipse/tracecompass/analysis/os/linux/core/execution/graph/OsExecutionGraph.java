/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.execution.graph;

import org.eclipse.tracecompass.analysis.graph.core.building.ITmfGraphProvider;
import org.eclipse.tracecompass.analysis.graph.core.building.TmfGraphBuilderModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Graph building module for the lttng kernel execution graph
 *
 * @author Francis Giraldeau
 * @author Geneviève Bastien
 * @since 2.4
 */
public class OsExecutionGraph extends TmfGraphBuilderModule {

    /**
     * Analysis id of this module
     */
    public static final String ANALYSIS_ID = "org.eclipse.tracecompass.analysis.os.linux.execgraph"; //$NON-NLS-1$

    @Override
    public boolean canExecute(ITmfTrace trace) {
        /**
         * TODO: Trace must have at least sched_switches and sched_wakeups
         * enabled
         */
        return true;
    }

    @Override
    protected ITmfGraphProvider getGraphProvider() {
        ITmfTrace trace = getTrace();
        if (trace == null) {
            throw new NullPointerException();
        }
        return new OsExecutionGraphProvider(trace);
    }

    @Override
    protected String getFullHelpText() {
        return super.getFullHelpText();
    }

    @Override
    protected String getShortHelpText(ITmfTrace trace) {
        return super.getShortHelpText(trace);
    }

    @Override
    protected String getTraceCannotExecuteHelpText(ITmfTrace trace) {
        return "The trace must have events 'sched_switch' and 'sched_wakeup' enabled"; //$NON-NLS-1$
    }

}
