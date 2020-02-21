/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.handlers;

import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * IPI Exit Handler
 *
 * @author Matthew Khouzam
 */
public class IPIExitHandler extends KernelEventHandler {

    /**
     * Constructor
     *
     * @param layout
     *            event layout
     */
    public IPIExitHandler(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    @Override
    public void handleEvent(ITmfStateSystemBuilder ss, ITmfEvent event) throws AttributeNotFoundException {
        Integer cpu = KernelEventHandlerUtils.getCpu(event);
        if (cpu == null) {
            return;
        }
        int currentThreadNode = KernelEventHandlerUtils.getCurrentThreadNode(cpu, ss);
        Integer irqId = ((Long) event.getContent().getField(getLayout().fieldIPIVector()).getValue()).intValue();
        /* Put this IRQ back to inactive in the resource tree */
        int quark = ss.getQuarkRelativeAndAdd(KernelEventHandlerUtils.getNodeIRQs(cpu, ss), irqId.toString());
        long timestamp = KernelEventHandlerUtils.getTimestamp(event);
        ss.modifyAttribute(timestamp, (Object) null, quark);

        /* Set the previous process back to running */
        KernelEventHandlerUtils.setProcessToRunning(timestamp, currentThreadNode, ss);

        /* Set the CPU status back to running or "idle" */
        KernelEventHandlerUtils.updateCpuStatus(timestamp, cpu, ss);

        /* Update the aggregate IRQ entry to set it to a CPU which has this IPI active */
        int aggregateQuark = ss.getQuarkAbsoluteAndAdd(Attributes.IRQS, irqId.toString());
        Integer prevCpu = KernelEventHandlerUtils.getCpuForIrq(ss, irqId);
        ss.modifyAttribute(timestamp, prevCpu, aggregateQuark);
    }
}
