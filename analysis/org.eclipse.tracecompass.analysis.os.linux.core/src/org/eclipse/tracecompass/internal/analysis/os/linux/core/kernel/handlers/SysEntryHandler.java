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

import org.eclipse.tracecompass.analysis.os.linux.core.kernel.StateValues;
import org.eclipse.tracecompass.analysis.os.linux.core.model.ProcessStatus;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * System call entry handler
 */
public class SysEntryHandler extends KernelEventHandler {

    /**
     * Constructor
     *
     * @param layout
     *            event layout
     */
    public SysEntryHandler(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    @Override
    public void handleEvent(ITmfStateSystemBuilder ss, ITmfEvent event) throws AttributeNotFoundException {
        Integer cpu = KernelEventHandlerUtils.getCpu(event);
        if (cpu == null) {
            return;
        }
        /* Assign the new system call to the process */
        int currentThreadNode = KernelEventHandlerUtils.getCurrentThreadNode(cpu, ss);
        int quark = ss.getQuarkRelativeAndAdd(currentThreadNode, Attributes.SYSTEM_CALL);
        long timestamp = KernelEventHandlerUtils.getTimestamp(event);
        String syscallName = event.getName();
        IKernelAnalysisEventLayout layout = getLayout();
        if (syscallName.startsWith(layout.eventCompatSyscallEntryPrefix())) {
            syscallName = syscallName.substring(layout.eventCompatSyscallEntryPrefix().length());
        } else if (syscallName.startsWith(layout.eventSyscallEntryPrefix())) {
            syscallName = syscallName.substring(layout.eventSyscallEntryPrefix().length());
        }
        ss.modifyAttribute(timestamp, syscallName, quark);

        /* Put the process in system call mode */
        ss.modifyAttribute(timestamp, ProcessStatus.RUN_SYTEMCALL.getStateValue().unboxValue(), currentThreadNode);

        /* Put the CPU in system call (kernel) mode */
        int currentCPUNode = KernelEventHandlerUtils.getCurrentCPUNode(cpu, ss);
        ss.modifyAttribute(timestamp, StateValues.CPU_STATUS_RUN_SYSCALL_VALUE.unboxValue(), currentCPUNode);
    }

}
