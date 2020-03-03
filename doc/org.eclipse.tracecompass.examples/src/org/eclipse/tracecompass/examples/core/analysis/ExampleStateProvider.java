/*******************************************************************************
 * Copyright (c) 2020 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.examples.core.analysis;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * An example of a simple state provider for a simple state system analysis
 *
 * This module is also in the developer documentation of Trace Compass. If it is
 * modified here, the doc should also be updated.
 *
 * @author Alexandre Montplaisir
 * @author Geneviève Bastien
 */
public class ExampleStateProvider extends AbstractTmfStateProvider {

    private static final @NonNull String PROVIDER_ID = "example.state.provider.id"; //$NON-NLS-1$
    private static final int VERSION = 0;

    /**
     * Constructor
     *
     * @param trace
     *            The trace for this state provider
     */
    public ExampleStateProvider(@NonNull ITmfTrace trace) {
        super(trace, PROVIDER_ID);
    }

    @Override
    public int getVersion() {
        return VERSION;
    }

    @Override
    public @NonNull ITmfStateProvider getNewInstance() {
        return new ExampleStateProvider(getTrace());
    }

    @Override
    protected void eventHandle(ITmfEvent event) {

        /**
         * Do what needs to be done with this event, here is an example that
         * updates the CPU state and TID after a sched_switch
         */
        if (event.getName().equals("sched_switch")) { //$NON-NLS-1$

            final long ts = event.getTimestamp().getValue();
            Long nextTid = event.getContent().getFieldValue(Long.class, "next_tid"); //$NON-NLS-1$
            Integer cpu = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event);
            if (cpu == null || nextTid == null) {
                return;
            }

            ITmfStateSystemBuilder ss = Objects.requireNonNull(getStateSystemBuilder());
            int quark = ss.getQuarkAbsoluteAndAdd("CPUs", String.valueOf(cpu)); //$NON-NLS-1$

            // The status attribute has an integer value
            int statusQuark = ss.getQuarkRelativeAndAdd(quark, "Status"); //$NON-NLS-1$
            Integer value = (nextTid > 0 ? 1 : 0);
            ss.modifyAttribute(ts, value, statusQuark);

            // The main quark contains the tid of the running thread
            ss.modifyAttribute(ts, nextTid, quark);
        }
    }

}
