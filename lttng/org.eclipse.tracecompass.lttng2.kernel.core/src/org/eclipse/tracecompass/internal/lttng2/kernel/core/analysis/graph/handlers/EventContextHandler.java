/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.analysis.graph.handlers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

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

    private final Map<String, Consumer<ITmfEvent>> fHandlers = new HashMap<>();
    private final Set<IKernelAnalysisEventLayout> fLayouts = new HashSet<>();

    private final Consumer<ITmfEvent> fDefault = event -> {
        // Do nothing
    };
    private final Consumer<ITmfEvent> fSoftIrqEntryHandler = event -> pushInterruptContext(event, Context.SOFTIRQ);
    private final Consumer<ITmfEvent> fSoftIrqExitHandler = event -> popInterruptContext(event, Context.SOFTIRQ);
    private final Consumer<ITmfEvent> fHRTimerExpireEntry = event -> pushInterruptContext(event, Context.HRTIMER);
    private final Consumer<ITmfEvent> fHRTimerExpireExit = event -> popInterruptContext(event, Context.HRTIMER);
    private final Consumer<ITmfEvent> fIrqHandlerEntry = event -> pushInterruptContext(event, Context.IRQ);
    private final Consumer<ITmfEvent> fIrqHandlerExit = event -> popInterruptContext(event, Context.IRQ);
    private final Consumer<ITmfEvent> fIpiEntry = event -> pushInterruptContext(event, Context.IPI);
    private final Consumer<ITmfEvent> fIpiExit = event -> popInterruptContext(event, Context.IPI);

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
        IKernelAnalysisEventLayout eventLayout = getProvider().getEventLayout(event.getTrace());
        if (!fLayouts.contains(eventLayout)) {
            populateHandlerMap(eventLayout);
            fLayouts.add(eventLayout);
        }
        fHandlers.getOrDefault(event.getName(), fDefault).accept(event);
    }

    private void populateHandlerMap(IKernelAnalysisEventLayout eventLayout) {
        fHandlers.put(eventLayout.eventSoftIrqEntry(), fSoftIrqEntryHandler);
        fHandlers.put(eventLayout.eventSoftIrqExit(), fSoftIrqExitHandler);
        fHandlers.put(eventLayout.eventHRTimerExpireEntry(), fHRTimerExpireEntry);
        fHandlers.put(eventLayout.eventHRTimerExpireExit(), fHRTimerExpireExit);
        fHandlers.put(eventLayout.eventIrqHandlerEntry(), fIrqHandlerEntry);
        fHandlers.put(eventLayout.eventIrqHandlerExit(), fIrqHandlerExit);
        for (String ipiName : eventLayout.getIPIIrqVectorsEntries()) {
            fHandlers.put(ipiName, fIpiEntry);
        }
        for (String ipiName : eventLayout.getIPIIrqVectorsEntries()) {
            fHandlers.put(ipiName, fIpiExit);
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

        /*
         * TODO: add a warning bookmark if the interrupt context is not coherent
         */
        LttngInterruptContext interruptCtx = system.peekContextStack(event.getTrace().getHostId(), cpu);
        if (interruptCtx.getContext() == ctx) {
            system.popContextStack(event.getTrace().getHostId(), cpu);
        }
    }
}
