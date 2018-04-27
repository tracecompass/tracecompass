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
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

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
        ITmfEventField content = event.getContent();
        final Integer tid = content.getFieldValue(Integer.class, getLayout().fieldTid());
        if (tid == null) {
            return;
        }
        final Integer prio = content.getFieldValue(Integer.class, getLayout().fieldPrio());
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
        long timestamp = KernelEventHandlerUtils.getTimestamp(event);
        if (status != ProcessStatus.RUN && status != ProcessStatus.RUN_SYTEMCALL) {
            ss.modifyAttribute(timestamp, ProcessStatus.WAIT_CPU.getStateValue().unboxValue(), threadNode);
        }

        /* Set the thread's target run queue */
        int quark = ss.getQuarkRelativeAndAdd(threadNode, Attributes.CURRENT_CPU_RQ);
        ss.modifyAttribute(timestamp, targetCpu.intValue(), quark);

        /*
         * When a user changes a threads prio (e.g. with pthread_setschedparam),
         * it shows in ftrace with a sched_wakeup.
         */
        if (prio != null) {
            quark = ss.getQuarkRelativeAndAdd(threadNode, Attributes.PRIO);
            ss.modifyAttribute(timestamp, prio, quark);
        }
    }
}
