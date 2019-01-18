/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.trace;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.event.aspect.LinuxTidAspect;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout.LttngEventLayout;
import org.eclipse.tracecompass.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.trace.TmfEventTypeCollectionHelper;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEventType;

import com.google.common.collect.Multimap;

/**
 * A Linux TID event aspect that retrieves the TID from the context of a kernel
 * event.
 *
 * @author Geneviève Bastien
 */
public class ContextTidAspect extends LinuxTidAspect {

    private final LttngEventLayout fLayout;

    /**
     * Get the context TID aspect if the trace contains at least one event with
     * a tid context field
     *
     * @param trace
     *            The LTTng kernel trace for which to get the aspect
     * @return The aspect if it has the proper field, or <code>null</code>
     *         otherwise
     */
    public static @Nullable ContextTidAspect getAspect(LttngKernelTrace trace) {
        IKernelAnalysisEventLayout layout = trace.getKernelEventLayout();
        if (!(layout instanceof LttngEventLayout)) {
            return null;
        }
        LttngEventLayout lttngLayout = (LttngEventLayout) layout;
        Set<@NonNull CtfTmfEventType> eventTypes = trace.getContainedEventTypes();
        Multimap<@NonNull String, @NonNull String> eventFieldNames = TmfEventTypeCollectionHelper.getEventFieldNames(eventTypes);
        return (eventFieldNames.containsValue(lttngLayout.contextTid())) ? new ContextTidAspect(lttngLayout) : null;
    }

    private ContextTidAspect(LttngEventLayout layout) {
        fLayout = layout;
    }

    @Override
    public @Nullable Integer resolve(@NonNull ITmfEvent event) {
        ITmfEventField content = event.getContent();
        Long tid = content.getFieldValue(Long.class, fLayout.contextTid());
        return tid == null ? null : tid.intValue();
    }

}
