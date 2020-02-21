/*******************************************************************************
 * Copyright (c) 2015, 2018 Ericsson
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
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Raise a soft irq event
 */
public class SoftIrqRaiseHandler extends KernelEventHandler {

    /**
     * Constructor
     *
     * @param layout
     *            event layout
     */
    public SoftIrqRaiseHandler(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    @Override
    public void handleEvent(ITmfStateSystemBuilder ss, ITmfEvent event) throws AttributeNotFoundException {
        Integer softIrqId = ((Long) event.getContent().getField(getLayout().fieldVec()).getValue()).intValue();
        Integer cpu = KernelEventHandlerUtils.getCpu(event);
        if (cpu == null) {
            return;
        }
        /*
         * Mark this SoftIRQ as *raised* in the resource tree.
         */
        int quark = ss.getQuarkRelativeAndAdd(KernelEventHandlerUtils.getNodeSoftIRQs(cpu, ss), softIrqId.toString());

        ITmfStateValue value = KernelEventHandlerUtils.isInSoftIrq(ss, quark) ?
                StateValues.SOFT_IRQ_RAISED_RUNNING_VALUE :
                StateValues.SOFT_IRQ_RAISED_VALUE;
        ss.modifyAttribute(KernelEventHandlerUtils.getTimestamp(event), value.unboxValue(), quark);

        /* Update the aggregate IRQ entry to set it to this CPU */
        int aggregateQuark = ss.getQuarkAbsoluteAndAdd(Attributes.SOFT_IRQS, softIrqId.toString());
        ITmfStateValue aggregateValue = KernelEventHandlerUtils.getAggregate(ss, Attributes.SOFT_IRQS, softIrqId);
        ss.modifyAttribute(KernelEventHandlerUtils.getTimestamp(event), aggregateValue.unboxValue(), aggregateQuark);
    }
}
