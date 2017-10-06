/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.handlers;

import org.eclipse.tracecompass.analysis.os.linux.core.kernel.StateValues;
import org.eclipse.tracecompass.analysis.os.linux.core.model.ProcessStatus;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
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

        ITmfStateValue value = TmfStateValue.newValueInt(cpu.intValue());
        long timestamp = KernelEventHandlerUtils.getTimestamp(event);
        ss.modifyAttribute(timestamp, value, quark);

        /* Change the status of the running process to interrupted */
        quark = KernelEventHandlerUtils.getCurrentThreadNode(cpu, ss);
        value = ProcessStatus.INTERRUPTED.getStateValue();
        ss.modifyAttribute(timestamp, value, quark);

        /* Change the status of the CPU to interrupted */
        quark = KernelEventHandlerUtils.getCurrentCPUNode(cpu, ss);
        value = StateValues.CPU_STATUS_IRQ_VALUE;
        ss.modifyAttribute(timestamp, value, quark);
    }

}
