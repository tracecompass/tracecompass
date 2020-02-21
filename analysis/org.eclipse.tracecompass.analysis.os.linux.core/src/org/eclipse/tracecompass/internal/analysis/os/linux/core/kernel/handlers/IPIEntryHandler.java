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

import org.eclipse.tracecompass.analysis.os.linux.core.kernel.StateValues;
import org.eclipse.tracecompass.analysis.os.linux.core.model.ProcessStatus;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * IPI Entry Handler
 *
 * @author Matthew Khouzam
 */
public class IPIEntryHandler extends KernelEventHandler {

    /**
     * Constructor
     *
     * @param layout
     *            event layout
     */
    public IPIEntryHandler(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    @Override
    public void handleEvent(ITmfStateSystemBuilder ss, ITmfEvent event) throws AttributeNotFoundException {

        Integer cpu = KernelEventHandlerUtils.getCpu(event);
        if (cpu == null) {
            return;
        }
        Integer irqId = ((Long) event.getContent().getField(getLayout().fieldIPIVector()).getValue()).intValue();

        /*
         * Mark this IRQ as active in the resource tree. The state value = the
         * CPU on which this IRQ is sitting
         */
        int quark = ss.getQuarkRelativeAndAdd(KernelEventHandlerUtils.getNodeIRQs(cpu, ss), irqId.toString());

        long timestamp = KernelEventHandlerUtils.getTimestamp(event);
        ss.modifyAttribute(timestamp, cpu.intValue(), quark);

        /* Change the status of the running process to interrupted */
        quark = KernelEventHandlerUtils.getCurrentThreadNode(cpu, ss);
        ss.modifyAttribute(timestamp, ProcessStatus.INTERRUPTED.getStateValue().unboxValue(), quark);

        /* Change the status of the CPU to interrupted */
        quark = KernelEventHandlerUtils.getCurrentCPUNode(cpu, ss);
        ss.modifyAttribute(timestamp, StateValues.CPU_STATUS_IRQ_VALUE.unboxValue(), quark);

        /* Update the aggregate IRQ entry to set it to this CPU */
        int aggregateQuark = ss.getQuarkAbsoluteAndAdd(Attributes.IRQS, irqId.toString());
        ss.modifyAttribute(timestamp, cpu, aggregateQuark);
    }

}
