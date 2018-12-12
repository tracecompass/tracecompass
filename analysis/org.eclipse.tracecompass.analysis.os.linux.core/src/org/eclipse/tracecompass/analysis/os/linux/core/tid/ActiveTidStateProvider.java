/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tid;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.Activator;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * Active TID state provider, this only does one thing: figure out the active
 * TID on any given CPU. This state provider is designed to do one thing and do
 * it fast.
 *
 * Note: this state provider exists as a way to accelerate the TID aspect, but
 * also to start splitting up the kernel analysis into smaller sections.
 *
 * Note 2: this is deliberately only package visible.
 *
 * Attribute tree:
 *
 * <pre>
 * |- <CPU number> -> Active TID number
 * </pre>
 *
 * @author Matthew Khouzam
 */
class ActiveTidStateProvider extends AbstractTmfStateProvider {

    private static final @NonNull String PROVIDER_ID = "activeTidAnalysis.provider"; //$NON-NLS-1$
    private static final int VERSION = 0;

    private final Map<Integer, Integer> fCpuNumToQuark = new TreeMap<>();
    private final @NonNull String fSchedSwitch;
    private final @NonNull String fNextTid;
    private final @NonNull IKernelAnalysisEventLayout fLayout;

    public ActiveTidStateProvider(@NonNull ITmfTrace trace, @NonNull IKernelAnalysisEventLayout layout) {
        super(trace, PROVIDER_ID);
        fSchedSwitch = layout.eventSchedSwitch();
        fNextTid = layout.fieldNextTid();
        fLayout = layout;
    }

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public @NonNull ITmfStateProvider getNewInstance() {
        return new ActiveTidStateProvider(getTrace(), fLayout);
    }

    @Override
    protected void eventHandle(@NonNull ITmfEvent event) {
        if (!event.getName().equals(fSchedSwitch)) {
            return;
        }
        ITmfStateSystemBuilder ssb = getStateSystemBuilder();
        if (ssb == null) {
            return;
        }
        Integer cpu = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event);
        if (cpu == null) {
            return;
        }
        Integer cpuQuark = fCpuNumToQuark.get(cpu);
        if (cpuQuark == null) {
            // this will only happen once
            String cpuAttributeName = NonNullUtils.nullToEmptyString(cpu);
            cpuQuark = ssb.getQuarkAbsoluteAndAdd(cpuAttributeName);
            fCpuNumToQuark.put(cpu, cpuQuark);
        }
        try {
            int nextTid = ((Long) event.getContent().getField(fNextTid).getValue()).intValue();
            ssb.modifyAttribute(event.getTimestamp().toNanos(), nextTid, cpuQuark);
        } catch (StateValueTypeException e) {
            Activator.getDefault().logError(NonNullUtils.nullToEmptyString(e.getMessage()), e);
        }
    }
}