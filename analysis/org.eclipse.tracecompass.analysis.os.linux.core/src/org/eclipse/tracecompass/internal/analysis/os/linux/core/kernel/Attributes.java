/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.util.Pair;

/**
 * This file defines all the attribute names used in the handler. Both the
 * construction and query steps should use them.
 *
 * These should not be externalized! The values here are used as-is in the
 * history file on disk, so they should be kept the same to keep the file format
 * compatible. If a view shows attribute names directly, the localization should
 * be done on the viewer side.
 *
 * @author Alexandre Montplaisir
 */
@SuppressWarnings({ "nls", "javadoc" })
public interface Attributes {

    /* First-level attributes */
    String CPUS = "CPUs";
    String THREADS = "Threads";

    /* Sub-attributes of the CPU nodes */
    String CURRENT_THREAD = "Current_thread";
    String SOFT_IRQS = "Soft_IRQs";
    String IRQS = "IRQs";

    /* Sub-attributes of the Thread nodes */
    String CURRENT_CPU_RQ = "Current_cpu_rq";
    String PPID = "PPID";
    String EXEC_NAME = "Exec_name";

    String PRIO = "Prio";
    String SYSTEM_CALL = "System_call";

    /* Misc stuff */
    String UNKNOWN = "Unknown";
    String THREAD_0_PREFIX = "0_";
    String THREAD_0_SEPARATOR = "_";

    /**
     * Build the thread attribute name.
     *
     * For all threads except "0" this is the string representation of the
     * threadId. For thread "0" which is the idle thread and can be running
     * concurrently on multiple CPUs, append "_cpuId".
     *
     * @param threadId
     *            the thread id
     * @param cpuId
     *            the cpu id
     * @return the thread attribute name null if the threadId is zero and the
     *         cpuId is null
     */
    public static @Nullable String buildThreadAttributeName(int threadId, @Nullable Integer cpuId) {
        if (threadId == 0) {
            if (cpuId == null) {
                return null;
            }
            return Attributes.THREAD_0_PREFIX + String.valueOf(cpuId);
        }

        return String.valueOf(threadId);
    }

    /**
     * Parse the thread id and CPU id from the thread attribute name string
     *
     * For thread "0" the attribute name is in the form "threadId_cpuId",
     * extract both values from the string.
     *
     * For all other threads, the attribute name is the string representation of
     * the threadId and there is no cpuId.
     *
     * @param threadAttributeName
     *            the thread attribute name
     * @return the thread id and cpu id
     */
    public static Pair<Integer, Integer> parseThreadAttributeName(String threadAttributeName) {
        Integer threadId = -1;
        Integer cpuId = -1;

        try {
            if (threadAttributeName.startsWith(Attributes.THREAD_0_PREFIX)) {
                threadId = 0;
                String[] tokens = threadAttributeName.split(Attributes.THREAD_0_SEPARATOR);
                cpuId = Integer.parseInt(tokens[1]);
            } else {
                threadId = Integer.parseInt(threadAttributeName);
            }
        } catch (NumberFormatException e1) {
            // pass
        }

        return new Pair<>(threadId, cpuId);
    }
}
