/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.SyscallLookup;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCallsiteAspect;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfCallsite;
import org.eclipse.tracecompass.tmf.core.event.lookup.TmfCallsite;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Get the source location of known trace points.
 *
 * @author Matthew Khouzam
 */
public final class KernelCallsiteAspect extends TmfCallsiteAspect {

    private static @Nullable KernelCallsiteAspect sInstance = null;

    /**
     * Get the singleton instance
     *
     * @return the instance
     */
    public static KernelCallsiteAspect getInstance() {
        KernelCallsiteAspect instance = sInstance;
        if (instance == null) {
            instance = new KernelCallsiteAspect();
            sInstance = instance;
        }
        return instance;
    }

    private KernelCallsiteAspect() {
        // do nothing
    }

    @Override
    public @Nullable List<ITmfCallsite> resolve(ITmfEvent event) {
        ITmfTrace trace = event.getTrace();
        if (trace instanceof IKernelTrace) {
            IKernelAnalysisEventLayout layout = ((IKernelTrace) trace).getKernelEventLayout();
            String syscallName = truncateEntry(layout, event.getName());
            if (syscallName == null) {
                syscallName = truncateExit(layout, event.getName());
            }
            if (syscallName != null) {
                String file = SyscallLookup.getInstance().getFile(syscallName);
                if (!file.isEmpty()) {
                    return Collections.<ITmfCallsite> singletonList(new TmfCallsite(file, Long.valueOf(0)));
                }
            }
        }
        return null;
    }

    private static @Nullable String truncateEntry(IKernelAnalysisEventLayout layout, String syscallName) {
        if (syscallName.startsWith(layout.eventCompatSyscallEntryPrefix())) {
            return syscallName.substring(layout.eventCompatSyscallEntryPrefix().length());
        } else if (syscallName.startsWith(layout.eventSyscallEntryPrefix())) {
            return syscallName.substring(layout.eventSyscallEntryPrefix().length());
        }
        return null;
    }

    private static @Nullable String truncateExit(IKernelAnalysisEventLayout layout, String syscallName) {
        if (syscallName.startsWith(layout.eventCompatSyscallExitPrefix())) {
            return syscallName.substring(layout.eventCompatSyscallExitPrefix().length());
        } else if (syscallName.startsWith(layout.eventSyscallExitPrefix())) {
            return syscallName.substring(layout.eventSyscallExitPrefix().length());
        }
        return null;
    }
}
