/*******************************************************************************
 * Copyright (c) 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.handlers;

import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

/**
 * Handles a CPU frequency change event
 *
 * @author Geneviève Bastien
 */
public class CpuFrequencyHandler extends KernelEventHandler {

    private static final String CPU_ID_FIELD = "cpu_id"; //$NON-NLS-1$
    private static final String CPU_STATE = "state"; //$NON-NLS-1$
    private static final long FREQUENCY_MULTIPLIER = 1000;

    /**
     * Constructor
     *
     * @param layout The event layout
     */
    public CpuFrequencyHandler(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    @Override
    public void handleEvent(ITmfStateSystemBuilder ss, ITmfEvent event) throws AttributeNotFoundException {
        ITmfEventField content = event.getContent();
        Integer cpuId = content.getFieldValue(Integer.class, CPU_ID_FIELD);
        Long cpuState = content.getFieldValue(Long.class, CPU_STATE);
        long timestamp = KernelEventHandlerUtils.getTimestamp(event);

        if (cpuId == null || cpuState == null) {
            // Wrong data, nothing to do
            return;
        }

        // Multiply cpu state by frequency multiplier
        cpuState *= FREQUENCY_MULTIPLIER;
        final int cpuQuark = KernelEventHandlerUtils.getCurrentCPUNode(cpuId, ss);
        int quark = ss.getQuarkRelativeAndAdd(cpuQuark, Attributes.CURRENT_FREQUENCY);
        ss.modifyAttribute(timestamp, cpuState, quark);

        // Set the minimum and maximum CPU frequency for this CPU
        // Minimum value
        quark = ss.getQuarkRelativeAndAdd(cpuQuark, Attributes.MIN_FREQUENCY);
        Object currentValue = ss.queryOngoing(quark);
        Long minVal = (!(currentValue instanceof Long)) ? cpuState : Long.min(cpuState, (Long) currentValue);
        ss.updateOngoingState(minVal, quark);

        // Maximum value
        quark = ss.getQuarkRelativeAndAdd(cpuQuark, Attributes.MAX_FREQUENCY);
        currentValue = ss.queryOngoing(quark);
        Long maxVal = (!(currentValue instanceof Long)) ? cpuState : Long.max(cpuState, (Long) currentValue);
        ss.updateOngoingState(maxVal, quark);
    }

}
