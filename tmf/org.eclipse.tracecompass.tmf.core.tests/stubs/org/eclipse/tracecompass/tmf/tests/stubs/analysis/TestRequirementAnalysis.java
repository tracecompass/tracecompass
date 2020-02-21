/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Mathieu Rail - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.tests.stubs.analysis;

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAbstractAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;

import com.google.common.collect.ImmutableSet;

/**
 * Analysis type to test requirements acquisition
 */
@SuppressWarnings({ "javadoc", "nls" })
public class TestRequirementAnalysis extends TmfAbstractAnalysisModule {
    /* Test requirement types */
    public static final @NonNull String EVENT_TYPE = "event";
    public static final @NonNull String FIELD_TYPE = "field";

    /* A few event names */
    public static final @NonNull String EXIT_SYSCALL = "exit_syscall";
    public static final @NonNull String SCHED_SWITCH = "sched_switch";
    public static final @NonNull String SCHED_WAKEUP = "sched_wakeup";

    /* A few fields */
    public static final @NonNull String PID = "pid";
    public static final @NonNull String TID = "tid";

    @Override
    public boolean canExecute(ITmfTrace trace) {
        /* This just makes sure the trace is a ctf stub trace */
        return (TmfTraceStub.class.isAssignableFrom(trace.getClass()));
    }

    @Override
    protected void canceling() {

    }

    @Override
    protected boolean executeAnalysis(final IProgressMonitor monitor) {
        return true;
    }

    @Override
    public boolean setTrace(ITmfTrace trace) throws TmfAnalysisException {
        return super.setTrace(trace);
    }

    @Override
    public Iterable<TmfAbstractAnalysisRequirement> getAnalysisRequirements() {
        Set<TmfAbstractAnalysisRequirement> requirements = ImmutableSet.of(
                AnalysisRequirementFactory.REQUIREMENT_1,
                AnalysisRequirementFactory.REQUIREMENT_3);

        return requirements;
    }
}
