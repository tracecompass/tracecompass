/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.profiling.ui.callgraph;

import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.tracecompass.analysis.profiling.core.callgraph.ICallGraphProvider;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackAnalysis;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table.AbstractSegmentStoreTableViewer;
import org.eclipse.tracecompass.internal.analysis.profiling.core.callgraph.CallGraphAnalysis;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.Iterables;

/**
 * Displays the Call Stack data in a column table
 *
 * @author Sonia Farrah
 */
public class CallGraphTableViewer extends AbstractSegmentStoreTableViewer {

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param tableViewer
     *            The table viewer
     */
    public CallGraphTableViewer(@NonNull TableViewer tableViewer) {
        super(tableViewer);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected @Nullable ISegmentStoreProvider getSegmentStoreProvider(@NonNull ITmfTrace trace) {
        Iterable<CallStackAnalysis> csModules = TmfTraceUtils.getAnalysisModulesOfClass(trace, CallStackAnalysis.class);
        @Nullable CallStackAnalysis csModule = Iterables.getFirst(csModules, null);
        if (csModule == null) {
            return null;
        }
        csModule.schedule();
        ICallGraphProvider cgModule = csModule.getCallGraph();
        if (!(cgModule instanceof CallGraphAnalysis)) {
            return null;
        }
        CallGraphAnalysis module = (CallGraphAnalysis) cgModule;
        Job job = new Job(Messages.CallGraphAnalysis) {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                // The callgraph module will be scheduled by the callstack analysis, but we need
                // to wait for its specific termination
                module.waitForCompletion(Objects.requireNonNull((monitor)));
                if (monitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                return Status.OK_STATUS;
            }
        };
        job.schedule();
        return csModule;
    }
}