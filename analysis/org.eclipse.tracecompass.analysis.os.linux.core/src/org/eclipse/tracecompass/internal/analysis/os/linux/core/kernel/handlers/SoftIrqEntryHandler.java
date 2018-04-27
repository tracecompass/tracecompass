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

import org.eclipse.tracecompass.analysis.os.linux.core.kernel.StateValues;
import org.eclipse.tracecompass.analysis.os.linux.core.model.ProcessStatus;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Soft Irq Entry handler
 */
public class SoftIrqEntryHandler extends KernelEventHandler {

    /**
     * Constructor
     *
     * @param layout
     *            event layout
     */
    public SoftIrqEntryHandler(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    @Override
    public void handleEvent(ITmfStateSystemBuilder ss, ITmfEvent event) throws AttributeNotFoundException {
        Integer cpu = KernelEventHandlerUtils.getCpu(event);
        if (cpu == null) {
            return;
        }

        long timestamp = KernelEventHandlerUtils.getTimestamp(event);
        Integer softIrqId = ((Long) event.getContent().getField(getLayout().fieldVec()).getValue()).intValue();
        int currentThreadNode = KernelEventHandlerUtils.getCurrentThreadNode(cpu, ss);

        /*
         * Mark this SoftIRQ as active in the resource tree.
         */
        int quark = ss.getQuarkRelativeAndAdd(KernelEventHandlerUtils.getNodeSoftIRQs(cpu, ss), softIrqId.toString());
        ss.modifyAttribute(timestamp, StateValues.CPU_STATUS_SOFTIRQ_VALUE.unboxValue(), quark);

        /* Update the aggregate IRQ entry to set it to the highest raised Soft IRQ */
        int aggregateQuark = ss.getQuarkAbsoluteAndAdd(Attributes.SOFT_IRQS, softIrqId.toString());
        ITmfStateValue aggregateValue = KernelEventHandlerUtils.getAggregate(ss, Attributes.SOFT_IRQS, softIrqId);
        ss.modifyAttribute(timestamp, aggregateValue.unboxValue(), aggregateQuark);

        /* Change the status of the running process to interrupted */
        ss.modifyAttribute(timestamp, ProcessStatus.INTERRUPTED.getStateValue().unboxValue(), currentThreadNode);

        /* Change the status of the CPU to interrupted */
        KernelEventHandlerUtils.updateCpuStatus(timestamp, cpu, ss);
    }
}
