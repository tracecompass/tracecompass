/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexis Cabana-Loriaux - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.contextswitch;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.Activator;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.StateSystemBuilderUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * Class used to build a state system of the context switches of a trace
 *
 * Attribute tree:
 *
 * <pre>
 * |- CPUs
 * |  |- <CPU number> -> Number of context switches
 * </pre>
 *
 * @author Alexis Cabana-Loriaux
 * @since 2.0
 */
@NonNullByDefault
public class KernelContextSwitchStateProvider extends AbstractTmfStateProvider {

    private static final String ID = "org.eclipse.tracecompass.analysis.os.linux.contextswitch.stateprovider"; //$NON-NLS-1$
    private static final int STARTING_QUARK = -1;
    private int fCpuAttributeQuark = STARTING_QUARK;
    private @Nullable ITmfStateSystemBuilder fStateSystemBuilder;
    private IKernelAnalysisEventLayout fLayout;

    /**
     * Default constructor
     *
     * @param trace
     *            the trace
     * @param layout
     *            the associated layout
     */
    public KernelContextSwitchStateProvider(ITmfTrace trace, IKernelAnalysisEventLayout layout) {
        super(trace, ID);
        fLayout = layout;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public ITmfStateProvider getNewInstance() {
        return new KernelContextSwitchStateProvider(getTrace(), fLayout);
    }

    /*
     * Classify sched_switch events for every CPU
     */
    @Override
    protected void eventHandle(ITmfEvent event) {
        ITmfStateSystemBuilder stateSystemBuilder = fStateSystemBuilder;
        if (stateSystemBuilder == null) {
            stateSystemBuilder = (ITmfStateSystemBuilder) getAssignedStateSystem();
            fStateSystemBuilder = stateSystemBuilder;
        }
        if (stateSystemBuilder == null) {
            return;
        }
        if (fCpuAttributeQuark == STARTING_QUARK) {
            fCpuAttributeQuark = stateSystemBuilder.getQuarkAbsoluteAndAdd(Attributes.CPUS);
        }
        if (event.getName().equals(fLayout.eventSchedSwitch())) {
            Object cpuObj = TmfTraceUtils.resolveEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event);
            if (cpuObj == null) {
                /* We couldn't find any CPU information, ignore this event */
                return;
            }
            int cpuQuark = stateSystemBuilder.getQuarkRelativeAndAdd(fCpuAttributeQuark, cpuObj.toString());
            try {
                StateSystemBuilderUtils.incrementAttributeInt(stateSystemBuilder, event.getTimestamp().getValue(), cpuQuark, 1);
            } catch (StateValueTypeException | AttributeNotFoundException e) {
                Activator.getDefault().logError(NonNullUtils.nullToEmptyString(e.getMessage()), e);
            }
        }

    }

}
