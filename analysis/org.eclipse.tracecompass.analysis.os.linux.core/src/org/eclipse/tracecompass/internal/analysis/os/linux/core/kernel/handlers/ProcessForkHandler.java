/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.handlers;

import org.eclipse.tracecompass.analysis.os.linux.core.model.ProcessStatus;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

/**
 * Fork Handler
 */
public class ProcessForkHandler extends KernelEventHandler {

    /**
     * Constructor
     *
     * @param layout
     *            event layout
     */
    public ProcessForkHandler(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    @Override
    public void handleEvent(ITmfStateSystemBuilder ss, ITmfEvent event) throws AttributeNotFoundException {
        ITmfEventField content = event.getContent();
        Integer cpu = KernelEventHandlerUtils.getCpu(event);
        String childProcessName = (String) content.getField(getLayout().fieldChildComm()).getValue();

        Integer parentTid = ((Long) content.getField(getLayout().fieldParentTid()).getValue()).intValue();
        Integer childTid = ((Long) content.getField(getLayout().fieldChildTid()).getValue()).intValue();
        Long childPid = content.getFieldValue(Long.class, getLayout().fieldChildPid());

        String parentThreadAttributeName = Attributes.buildThreadAttributeName(parentTid, cpu);
        if (parentThreadAttributeName == null) {
            return;
        }

        String childThreadAttributeName = Attributes.buildThreadAttributeName(childTid, cpu);
        if (childThreadAttributeName == null) {
            return;
        }

        final int threadsNode = KernelEventHandlerUtils.getNodeThreads(ss);
        Integer parentTidNode = ss.getQuarkRelativeAndAdd(threadsNode, parentThreadAttributeName);
        Integer childTidNode = ss.getQuarkRelativeAndAdd(threadsNode, childThreadAttributeName);

        /* Assign the PPID to the new process */
        int quark = ss.getQuarkRelativeAndAdd(childTidNode, Attributes.PPID);
        ITmfStateValue value = TmfStateValue.newValueInt(parentTid);
        long timestamp = KernelEventHandlerUtils.getTimestamp(event);
        ss.modifyAttribute(timestamp, parentTid, quark);

        if (childPid != null && childPid.intValue() != childTid) {
            /* Assign the process ID of the new thread */
            quark = ss.getQuarkRelativeAndAdd(childTidNode, Attributes.PID);
            ss.modifyAttribute(timestamp, childPid.intValue(), quark);
        }

        /* Set the new process' exec_name */
        quark = ss.getQuarkRelativeAndAdd(childTidNode, Attributes.EXEC_NAME);
        value = TmfStateValue.newValueString(childProcessName);
        ss.modifyAttribute(timestamp, childProcessName, quark);

        /* Set the new process' status */
        value = ProcessStatus.WAIT_CPU.getStateValue();
        ss.modifyAttribute(timestamp, ProcessStatus.WAIT_CPU.getStateValue().unboxValue(), childTidNode);

        /*
         * Set the process's run queue to be the same as the parent's, if
         * available.
         */
        quark = ss.optQuarkRelative(parentTidNode, Attributes.CURRENT_CPU_RQ);
        if (quark != ITmfStateSystem.INVALID_ATTRIBUTE) {
            value = ss.queryOngoingState(quark);
            quark = ss.getQuarkRelativeAndAdd(childTidNode, Attributes.CURRENT_CPU_RQ);
            ss.modifyAttribute(timestamp, value.unboxValue(), quark);
        }

        /* Set the process' syscall name, to be the same as the parent's */
        quark = ss.getQuarkRelativeAndAdd(parentTidNode, Attributes.SYSTEM_CALL);
        value = ss.queryOngoingState(quark);
        if (!value.isNull()) {
            quark = ss.getQuarkRelativeAndAdd(childTidNode, Attributes.SYSTEM_CALL);
            ss.modifyAttribute(timestamp, value.unboxValue(), quark);
        }

    }
}
