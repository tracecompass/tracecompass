/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.kernel;

import java.util.Collection;

import org.eclipse.tracecompass.analysis.os.linux.core.event.aspect.ThreadPriorityAspect;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.KernelCallsiteAspect;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.KernelPidAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;

import com.google.common.collect.ImmutableSet;

/**
 * Class containing utility methods for the kernel traces
 *
 * @author Geneviève Bastien
 * @since 2.5
 */
public final class KernelUtils {

    /**
     * Event aspects available for all kernel traces
     */
    private static final Collection<ITmfEventAspect<?>> KERNEL_ASPECTS;

    static {
        ImmutableSet.Builder<ITmfEventAspect<?>> builder = ImmutableSet.builder();
        builder.add(KernelTidAspect.INSTANCE);
        builder.add(ThreadPriorityAspect.INSTANCE);
        builder.add(KernelPidAspect.INSTANCE);
        builder.add(KernelCallsiteAspect.getInstance());
        KERNEL_ASPECTS = builder.build();
    }

    private KernelUtils() {
        // Nothing to do
    }

    /**
     * Get a collection of aspects common for kernel traces
     *
     * @return A collection of kernel aspects
     */
    public static Collection<ITmfEventAspect<?>> getKernelAspects() {
        return KERNEL_ASPECTS;
    }

}
