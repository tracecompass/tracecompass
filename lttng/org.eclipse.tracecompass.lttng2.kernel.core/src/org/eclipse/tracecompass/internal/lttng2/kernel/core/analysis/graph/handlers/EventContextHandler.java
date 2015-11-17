/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.handlers;

import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.building.LttngKernelExecGraphProvider;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.building.LttngKernelExecGraphProvider.Context;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.model.LttngInterruptContext;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.model.LttngSystemModel;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * Event Handler to handle the interrupt context stack of the model
 *
 * @author Francis Giraldeau
 * @author Geneviève Bastien
 */
public class EventContextHandler extends BaseHandler {

    /**
     * Constructor
     *
     * @param provider
     *            The parent graph provider
     */
    public EventContextHandler(LttngKernelExecGraphProvider provider) {
        super(provider);
    }

    @Override
    public void handleEvent(ITmfEvent event) {
        String eventName = event.getName();
        IKernelAnalysisEventLayout eventLayout = getProvider().getEventLayout();
        if (eventName.equals(eventLayout.eventSoftIrqEntry())) {
            handleSoftirqEntry(event);
        } else if (eventName.equals(eventLayout.eventSoftIrqExit())) {
            handleSoftirqExit(event);
        } else if (eventName.equals(eventLayout.eventHRTimerExpireEntry())) {
            handleHrtimerExpireEntry(event);
        } else if (eventName.equals(eventLayout.eventHRTimerExpireExit())) {
            handleHrtimerExpireExit(event);
        } else if (eventName.equals(eventLayout.eventIrqHandlerEntry())) {
            handleIrqHandlerEntry(event);
        } else if (eventName.equals(eventLayout.eventIrqHandlerExit())) {
            handleIrqHandlerExit(event);
        }
    }

    private void pushInterruptContext(ITmfEvent event, Context ctx) {
        Integer cpu = NonNullUtils.checkNotNull(TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event));
        LttngSystemModel system = getProvider().getSystem();

        LttngInterruptContext interruptCtx = new LttngInterruptContext(event, ctx);

        system.pushContextStack(event.getTrace().getHostId(), cpu, interruptCtx);
    }

    private void popInterruptContext(ITmfEvent event, Context ctx) {
        Integer cpu = NonNullUtils.checkNotNull(TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event));
        LttngSystemModel system = getProvider().getSystem();

        /* TODO: add a warning bookmark if the interrupt context is not coherent */
        LttngInterruptContext interruptCtx = system.peekContextStack(event.getTrace().getHostId(), cpu);
        if (interruptCtx.getContext() == ctx) {
            system.popContextStack(event.getTrace().getHostId(), cpu);
        }
    }

    private void handleSoftirqEntry(ITmfEvent event) {
        pushInterruptContext(event, Context.SOFTIRQ);
    }

    private void handleSoftirqExit(ITmfEvent event) {
        popInterruptContext(event, Context.SOFTIRQ);
    }

    private void handleIrqHandlerEntry(ITmfEvent event) {
        pushInterruptContext(event, Context.IRQ);
    }

    private void handleIrqHandlerExit(ITmfEvent event) {
        popInterruptContext(event, Context.IRQ);
    }

    private void handleHrtimerExpireEntry(ITmfEvent event) {
        pushInterruptContext(event, Context.HRTIMER);
    }

    private void handleHrtimerExpireExit(ITmfEvent event) {
        popInterruptContext(event, Context.HRTIMER);
    }

}
