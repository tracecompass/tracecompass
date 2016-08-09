/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.lttng2.common.core.trace;

import java.util.Collection;
import java.util.regex.Pattern;

import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.event.aspect.CounterAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfCounterAspect;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTraceWithPreDefinedEvents;

import com.google.common.collect.ImmutableSet;

/**
 * LTTng trace common interface. Adds helpers common to both kernel and UST
 * traces.
 *
 * @author Matthew Khouzam
 * @since 1.1
 */
public interface ILttngTrace {

    /**
     * The perf context event field prefix
     */
    String CONTEXT_PERF_PREFIX = "context._perf."; //$NON-NLS-1$

    /**
     * Generic perf context
     */
    Pattern CONTEXT_PERF_UNKNOWN = Pattern.compile("^context\\._perf.+$"); //$NON-NLS-1$

    /**
     * Perf CPU context
     */
    Pattern CONTEXT_PERF_CPU = Pattern.compile("^context\\._perf.cpu.+$"); //$NON-NLS-1$

    /**
     * Perf thread context
     */
    Pattern CONTEXT_PERF_THREAD = Pattern.compile("^context\\._perf.thread.+$"); //$NON-NLS-1$

    /**
     * CPU grouping name
     */
    String CPU_GROUPING = "cpu"; //$NON-NLS-1$

    /**
     * Thread grouping name
     */
    String THREAD_GROUPING = "thread"; //$NON-NLS-1$

    /**
     * Make counter aspects for a trace
     *
     * @param trace
     *            The trace
     *
     * @return a Collection of aspects that are related to a given trace
     */
    default Collection<ITmfCounterAspect> createCounterAspects(ITmfTraceWithPreDefinedEvents trace) {
        ImmutableSet.Builder<ITmfCounterAspect> perfBuilder = new ImmutableSet.Builder<>();
        for (ITmfEventType eventType : trace.getContainedEventTypes()) {
            for (String fieldName : eventType.getFieldNames()) {
                if (fieldName != null) {
                    if (CONTEXT_PERF_CPU.matcher(fieldName).matches()) {
                        perfBuilder.add(new CounterAspect(fieldName, fieldName.substring(CONTEXT_PERF_PREFIX.length()), CPU_GROUPING));
                    } else if (CONTEXT_PERF_THREAD.matcher(fieldName).matches()) {
                        perfBuilder.add(new CounterAspect(fieldName, fieldName.substring(CONTEXT_PERF_PREFIX.length()), THREAD_GROUPING));
                    } else if (CONTEXT_PERF_UNKNOWN.matcher(fieldName).matches()) {
                        perfBuilder.add(new CounterAspect(fieldName, fieldName.substring(CONTEXT_PERF_PREFIX.length())));
                    }
                }
            }
        }
        return perfBuilder.build();
    }
}
