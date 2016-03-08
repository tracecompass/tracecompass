/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Rail - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.tests.stubs.analysis;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAnalysisRequirement;
import org.eclipse.tracecompass.tmf.core.analysis.requirements.TmfAnalysisRequirement.ValuePriorityLevel;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;

/**
 * Analysis type to test requirements acquisition
 */
@SuppressWarnings({ "javadoc", "nls" })
public class TestRequirementAnalysis extends TmfAbstractAnalysisModule {
    /* Test requirement types */
    public static final String EVENT_TYPE = "event";
    public static final String FIELD_TYPE = "field";

    /* A few event names */
    public static final String EXIT_SYSCALL = "exit_syscall";
    public static final String SCHED_SWITCH = "sched_switch";
    public static final String SCHED_WAKEUP = "sched_wakeup";

    /* A few fields */
    public static final String PID = "pid";
    public static final String TID = "tid";

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
    public Iterable<TmfAnalysisRequirement> getAnalysisRequirements() {
        Map<String, TmfAnalysisRequirement> requirements = new HashMap<>();

        /* Event type requirement and values */
        TmfAnalysisRequirement eventReqs = new TmfAnalysisRequirement(EVENT_TYPE);
        eventReqs.addValue(EXIT_SYSCALL, ValuePriorityLevel.MANDATORY);
        eventReqs.addValue(SCHED_SWITCH, ValuePriorityLevel.MANDATORY);
        eventReqs.addValue(SCHED_WAKEUP, ValuePriorityLevel.MANDATORY);
        requirements.put(EVENT_TYPE, eventReqs);

        /* Field type requirement and values */
        TmfAnalysisRequirement fieldReqs = new TmfAnalysisRequirement(FIELD_TYPE);
        fieldReqs.addValue(PID, ValuePriorityLevel.MANDATORY);
        fieldReqs.addValue(TID, ValuePriorityLevel.MANDATORY);
        requirements.put(FIELD_TYPE, fieldReqs);

        return requirements.values();
    }
}
