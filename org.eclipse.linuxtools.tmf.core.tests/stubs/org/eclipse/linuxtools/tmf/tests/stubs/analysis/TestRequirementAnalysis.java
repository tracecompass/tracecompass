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

package org.eclipse.linuxtools.tmf.tests.stubs.analysis;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisRequirement;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisRequirement.ValuePriorityLevel;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;

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
    public void setTrace(ITmfTrace trace) throws TmfAnalysisException {
    }

    @Override
    public Iterable<TmfAnalysisRequirement> getAnalysisRequirements() {
        Map<String, TmfAnalysisRequirement> requirements = new HashMap<>();

        /* Event type requirement and values */
        requirements.put(EVENT_TYPE, new TmfAnalysisRequirement(EVENT_TYPE));
        requirements.get(EVENT_TYPE).addValue(EXIT_SYSCALL, ValuePriorityLevel.MANDATORY);
        requirements.get(EVENT_TYPE).addValue(SCHED_SWITCH, ValuePriorityLevel.MANDATORY);
        requirements.get(EVENT_TYPE).addValue(SCHED_WAKEUP, ValuePriorityLevel.MANDATORY);

        /* Field type requirement and values */
        requirements.put(FIELD_TYPE, new TmfAnalysisRequirement(FIELD_TYPE));
        requirements.get(FIELD_TYPE).addValue(PID, ValuePriorityLevel.MANDATORY);
        requirements.get(FIELD_TYPE).addValue(TID, ValuePriorityLevel.MANDATORY);

        return requirements.values();
    }
}
