/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   François Rajotte - Initial API and implementation
 *   Geneviève Bastien - Revision of the initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.cpuusage;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.Activator;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.Attributes;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.LttngStrings;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Creates a state system with the total time spent on CPU for each thread and
 * for each CPU from a kernel trace.
 *
 * This state system in itself keeps the total time on CPU since last time the
 * process was scheduled out. The state system queries will only be accurate
 * when the process is not in a running state. To have exact CPU usage when
 * running, this state system needs to be used along the LTTng Kernel analysis.
 *
 * It requires only the 'sched_switch' events enabled on the trace.
 *
 * @author François Rajotte
 * @since 3.0
 */
public class LttngKernelCpuStateProvider extends AbstractTmfStateProvider {

    private static final int VERSION = 1;

    /* For each CPU, maps the last time a thread was scheduled in */
    private final Map<String, Long> fLastStartTimes = new HashMap<>();
    private final long fTraceStart;

    /**
     * Constructor
     *
     * @param trace
     *            The trace from which to get the CPU usage
     */
    public LttngKernelCpuStateProvider(ITmfTrace trace) {
        super(trace, ITmfEvent.class, "LTTng Kernel CPU usage"); //$NON-NLS-1$
        fTraceStart = trace.getStartTime().getValue();
    }

    // ------------------------------------------------------------------------
    // ITmfStateProvider
    // ------------------------------------------------------------------------

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public LttngKernelCpuStateProvider getNewInstance() {
        return new LttngKernelCpuStateProvider(this.getTrace());
    }

    @Override
    protected void eventHandle(ITmfEvent event) {
        final String eventName = event.getType().getName();

        if (eventName.equals(LttngStrings.SCHED_SWITCH)) {
            /*
             * Fields: string prev_comm, int32 prev_tid, int32 prev_prio, int64
             * prev_state, string next_comm, int32 next_tid, int32 next_prio
             */

            ITmfEventField content = event.getContent();
            long ts = event.getTimestamp().getValue();
            String cpu = event.getSource();

            Long prevTid = (Long) content.getField(LttngStrings.PREV_TID).getValue();

            try {
                Integer currentCPUNode = ss.getQuarkRelativeAndAdd(getNodeCPUs(), cpu);

                /*
                 * This quark contains the value of the cumulative time spent on
                 * the source CPU by the currently running thread
                 */
                Integer cumulativeTimeQuark = ss.getQuarkRelativeAndAdd(currentCPUNode, prevTid.toString());
                Long startTime = fLastStartTimes.get(cpu);
                /*
                 * If start time is null, we haven't seen the start of the
                 * process, so we assume beginning of the trace
                 */
                if (startTime == null) {
                    startTime = fTraceStart;
                }

                /*
                 * We add the time from startTime until now to the cumulative
                 * time of the thread
                 */
                if (startTime != null) {
                    ITmfStateValue value = ss.queryOngoingState(cumulativeTimeQuark);

                    /*
                     * Modify cumulative time for this CPU/TID combo: The total
                     * time changes when the process is scheduled out. Nothing
                     * happens when the process is scheduled in.
                     */
                    long prevCumulativeTime = value.unboxLong();
                    long newCumulativeTime = prevCumulativeTime + (ts - startTime);

                    value = TmfStateValue.newValueLong(newCumulativeTime);
                    ss.modifyAttribute(ts, value, cumulativeTimeQuark);
                    fLastStartTimes.put(cpu, ts);
                }
            } catch (AttributeNotFoundException e) {
                Activator.getDefault().logError("Attribute not found in LttngKernelCpuStateProvider", e); //$NON-NLS-1$
            }

        }
    }

    /* Shortcut for the "current CPU" attribute node */
    private int getNodeCPUs() {
        return ss.getQuarkAbsoluteAndAdd(Attributes.CPUS);
    }

}
