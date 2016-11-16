/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.handlers;

import org.eclipse.tracecompass.analysis.os.linux.core.model.ProcessStatus;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Waking/wakeup handler.
 *
 * "sched_waking" and "sched_wakeup" tracepoints contain the same fields, and
 * apply the same state transitions in our model, so they can both use this
 * handler.
 */
public class SchedWakeupHandler extends KernelEventHandler {

    /**
     * Constructor
     * @param layout event layout
     */
    public SchedWakeupHandler(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    @Override
    public void handleEvent(ITmfStateSystemBuilder ss, ITmfEvent event) throws AttributeNotFoundException {
        Integer cpu = KernelEventHandlerUtils.getCpu(event);
        final int tid = ((Long) event.getContent().getField(getLayout().fieldTid()).getValue()).intValue();
        final int prio = ((Long) event.getContent().getField(getLayout().fieldPrio()).getValue()).intValue();
        Long targetCpu = event.getContent().getFieldValue(Long.class, getLayout().fieldTargetCpu());

        String threadAttributeName = Attributes.buildThreadAttributeName(tid, cpu);

        if (cpu == null || targetCpu == null || threadAttributeName == null) {
            return;
        }

        final int threadNode = ss.getQuarkRelativeAndAdd(KernelEventHandlerUtils.getNodeThreads(ss), threadAttributeName);

        /*
         * The process indicated in the event's payload is now ready to run.
         * Assign it to the "wait for cpu" state, but only if it was not already
         * running.
         */
        ProcessStatus status = ProcessStatus.getStatusFromStateValue(ss.queryOngoingState(threadNode));
        ITmfStateValue value = null;
        long timestamp = KernelEventHandlerUtils.getTimestamp(event);
        if (status != ProcessStatus.RUN && status != ProcessStatus.RUN_SYTEMCALL) {
            value = ProcessStatus.WAIT_CPU.getStateValue();
            ss.modifyAttribute(timestamp, value, threadNode);
        }

        /* Set the thread's target run queue */
        int quark = ss.getQuarkRelativeAndAdd(threadNode, Attributes.CURRENT_CPU_RQ);
        value = TmfStateValue.newValueInt(targetCpu.intValue());
        ss.modifyAttribute(timestamp, value, quark);

        /*
         * When a user changes a threads prio (e.g. with pthread_setschedparam),
         * it shows in ftrace with a sched_wakeup.
         */
        quark = ss.getQuarkRelativeAndAdd(threadNode, Attributes.PRIO);
        value = TmfStateValue.newValueInt(prio);
        ss.modifyAttribute(timestamp, value, quark);
    }
}
