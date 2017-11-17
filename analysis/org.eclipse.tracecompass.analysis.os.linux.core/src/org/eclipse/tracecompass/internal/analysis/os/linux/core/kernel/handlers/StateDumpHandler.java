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

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.LinuxValues;
import org.eclipse.tracecompass.analysis.os.linux.core.model.ProcessStatus;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

/**
 * LTTng Specific state dump event handler
 */
public class StateDumpHandler extends KernelEventHandler {

    /**
     * Constructor
     *
     * @param layout
     *            event layout
     */
    public StateDumpHandler(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    @Override
    public void handleEvent(ITmfStateSystemBuilder ss, ITmfEvent event) throws AttributeNotFoundException {
        ITmfEventField content = event.getContent();
        Integer eventCpu = KernelEventHandlerUtils.getCpu(event);
        int tid = ((Long) content.getField("tid").getValue()).intValue(); //$NON-NLS-1$
        int pid = ((Long) content.getField("pid").getValue()).intValue(); //$NON-NLS-1$
        int ppid = ((Long) content.getField("ppid").getValue()).intValue(); //$NON-NLS-1$
        long status = ((Long) content.getField("status").getValue()).longValue(); //$NON-NLS-1$
        String name = checkNotNull((String) content.getField("name").getValue()); //$NON-NLS-1$
        /* Only present in LTTng 2.10+ */
        @Nullable Long cpuField = content.getFieldValue(Long.class, "cpu"); //$NON-NLS-1$
        /*
         * "mode" could be interesting too, but it doesn't seem to be populated
         * with anything relevant for now.
         */

        String threadAttributeName = Attributes.buildThreadAttributeName(tid, eventCpu);
        if (threadAttributeName == null) {
            return;
        }

        int curThreadNode = ss.getQuarkRelativeAndAdd(KernelEventHandlerUtils.getNodeThreads(ss), threadAttributeName);
        long timestamp = KernelEventHandlerUtils.getTimestamp(event);
        /* Set the process' name */
        setProcessName(ss, name, curThreadNode, timestamp);

        /* Set the process' PPID */
        setPpid(ss, ppid, curThreadNode, timestamp);

        /* Set the process' PID */
        setPid(ss, tid, pid, curThreadNode, timestamp);

        /* Set the process' status */
        setStatus(ss, status, curThreadNode, cpuField, timestamp);
    }

    private static void setPid(ITmfStateSystemBuilder ss, int tid, int pid, int curThreadNode, long timestamp) {
        if (tid == pid) {
            /* It's a process, no need to set a PID */
            return;
        }
        int quark = ss.getQuarkRelativeAndAdd(curThreadNode, Attributes.PID);
        if (ss.queryOngoingState(quark).isNull()) {
            ss.modifyAttribute(timestamp, pid, quark);
        }
    }

    private static void setStatus(ITmfStateSystemBuilder ss, long status, int curThreadNode, @Nullable Long cpu, long timestamp) {
        ITmfStateValue value;
        if (ss.queryOngoingState(curThreadNode).isNull()) {
            value = ProcessStatus.getStatusFromStatedump(status).getStateValue();
            ss.modifyAttribute(timestamp, value, curThreadNode);
            if (status == LinuxValues.STATEDUMP_PROCESS_STATUS_WAIT_CPU) {
                setRunQueue(ss, curThreadNode, cpu, timestamp);
            }
        }
    }

    private static void setRunQueue(ITmfStateSystemBuilder ss, int curThreadNode, @Nullable Long cpu, long timestamp) {
        if (cpu != null) {
            int quark = ss.getQuarkRelativeAndAdd(curThreadNode, Attributes.CURRENT_CPU_RQ);
            ITmfStateValue value = TmfStateValue.newValueInt(cpu.intValue());
            ss.modifyAttribute(timestamp, value, quark);
        }
    }

    private static void setPpid(ITmfStateSystemBuilder ss, int ppid, int curThreadNode, long timestamp) {
        int quark = ss.getQuarkRelativeAndAdd(curThreadNode, Attributes.PPID);
        if (ss.queryOngoingState(quark).isNull()) {
            ss.modifyAttribute(timestamp, ppid, quark);
        }
    }

    private static void setProcessName(ITmfStateSystemBuilder ss, String name, int curThreadNode, long timestamp) {
        ITmfStateValue value;
        int quark = ss.getQuarkRelativeAndAdd(curThreadNode, Attributes.EXEC_NAME);
        if (ss.queryOngoingState(quark).isNull()) {
            /* If the value didn't exist previously, set it */
            value = TmfStateValue.newValueString(name);
            ss.modifyAttribute(timestamp, value, quark);
        }
    }
}
