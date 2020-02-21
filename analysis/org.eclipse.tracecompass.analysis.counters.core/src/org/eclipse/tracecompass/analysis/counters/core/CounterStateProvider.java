/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.counters.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils;
import org.eclipse.tracecompass.analysis.counters.core.aspects.CounterAspect;
import org.eclipse.tracecompass.analysis.counters.core.aspects.ITmfCounterAspect;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.StateSystemBuilderUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.MultiAspect;
import org.eclipse.tracecompass.tmf.core.statesystem.AbstractTmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * State provider for the counters analyses. <br>
 * <br>
 * Layout of the generated attribute tree:
 *
 * <pre>
 * {root}
 *   +- Grouped counter aspects
 *   |   +- ...
 *   +- Ungrouped counter aspects
 *       +- ...
 * </pre>
 *
 * @author Mikael Ferland
 */
public class CounterStateProvider extends AbstractTmfStateProvider {

    private static final Logger LOGGER = TraceCompassLog.getLogger(CounterStateProvider.class);

    private final Set<ITmfEventAspect<?>> fCounterAspects;

    /*
     * Map linking a class to an implemented object. Used for retrieving the name of
     * the grouping aspect during resolve.
     */
    private final Map<Class<? extends ITmfEventAspect<?>>, ITmfEventAspect<?>> fGroupingAspectImpls;

    /**
     * Factory method to build a new counter state provider.
     *
     * @param trace
     *            Trace directory
     * @return a counter state provider
     */
    public static CounterStateProvider create(ITmfTrace trace) {
        Map<Class<? extends ITmfEventAspect<?>>, ITmfEventAspect<?>> aspectImpls = new HashMap<>();
        Iterable<ITmfEventAspect<?>> counterAspects = TmfTraceUtils.getEventAspects(trace, ITmfCounterAspect.class);
        for (ITmfEventAspect<?> counter : counterAspects) {

            if (counter instanceof CounterAspect) {
                CounterAspect counterAspect = (CounterAspect) counter;
                for (Class<? extends ITmfEventAspect<?>> parentAspectClass : counterAspect.getGroups()) {

                    // Avoid creating the same aggregated aspect multiple times
                    if (parentAspectClass != null && !aspectImpls.containsKey(parentAspectClass)) {
                        /*
                         * Aggregated aspect if more than one are available for a given
                         * ITmfEventAspect<?> class.
                         */
                        ITmfEventAspect<?> goldenAspect = MultiAspect.create(TmfTraceUtils.getEventAspects(trace, parentAspectClass), parentAspectClass.getClass());
                        if (goldenAspect != null) {
                            aspectImpls.put(parentAspectClass, goldenAspect);
                        }
                    }
                }
            }
        }

        return new CounterStateProvider(trace, counterAspects, aspectImpls);
    }

    private CounterStateProvider(ITmfTrace trace, Iterable<ITmfEventAspect<?>> counterAspects, Map<Class<? extends ITmfEventAspect<?>>, ITmfEventAspect<?>> aspectImpls) {
        super(trace, CounterAnalysis.ID);
        fCounterAspects = ImmutableSet.copyOf(counterAspects);
        fGroupingAspectImpls = ImmutableMap.copyOf(aspectImpls);
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public @NonNull ITmfStateProvider getNewInstance() {
        return new CounterStateProvider(getTrace(), fCounterAspects, fGroupingAspectImpls);
    }

    @Override
    protected void eventHandle(@NonNull ITmfEvent event) {
        ITmfStateSystemBuilder ss = getStateSystemBuilder();
        if (ss == null) {
            return;
        }

        for (ITmfEventAspect<?> aspect : fCounterAspects) {
            if (aspect instanceof CounterAspect) {
                CounterAspect counterAspect = (CounterAspect) aspect;
                if (counterAspect.getGroups().length > 0) {
                    int rootQuark = ss.getQuarkAbsoluteAndAdd(CounterAnalysis.GROUPED_COUNTER_ASPECTS_ATTRIB);
                    handleGroupedCounterAspect(event, ss, counterAspect, rootQuark);
                } else {
                    int rootQuark = ss.getQuarkAbsoluteAndAdd(CounterAnalysis.UNGROUPED_COUNTER_ASPECTS_ATTRIB);
                    handleCounterAspect(event, ss, counterAspect, rootQuark);
                }
            }
        }
    }

    /**
     * Add the field value of a grouped counter aspect to the state system (override
     * in specific implementations)
     *
     * @param event
     *            Event to process
     * @param ss
     *            State system object to fill
     * @param aspect
     *            Grouped counter aspect
     * @param rootQuark
     *            Key to a relative root of the state system
     */
    protected void handleGroupedCounterAspect(ITmfEvent event, ITmfStateSystemBuilder ss, CounterAspect aspect, int rootQuark) {
        /*
         * Retrieve the child quark of the counter aspect by going through its attribute
         * tree in the state system. The concatenation of the aspect's groups form a
         * single entry in the state system.
         */
        int quark = rootQuark;
        for (Class<? extends ITmfEventAspect<?>> groupClass : aspect.getGroups()) {
            ITmfEventAspect<?> parentAspect = fGroupingAspectImpls.get(groupClass);
            if (parentAspect != null) {

                // Avoid handling when the parent aspect resolves to null
                Object parentAspectContent = parentAspect.resolve(event);
                if (parentAspectContent != null) {
                    quark = ss.getQuarkRelativeAndAdd(quark, parentAspect.getName());
                    quark = ss.getQuarkRelativeAndAdd(quark, String.valueOf(parentAspectContent));
                } else {
                    return;
                }
            }
        }

        handleCounterAspect(event, ss, aspect, quark);
    }

    private static void handleCounterAspect(ITmfEvent event, ITmfStateSystemBuilder ss, CounterAspect aspect, int rootQuark) {
        int quark = ss.getQuarkRelativeAndAdd(rootQuark, aspect.getName());
        Long eventContent = aspect.resolve(event);
        if (eventContent != null) {
            if (!aspect.isCumulative()) {
                try {
                    StateSystemBuilderUtils.incrementAttributeLong(ss, event.getTimestamp().toNanos(), quark, eventContent);
                } catch (StateValueTypeException | AttributeNotFoundException e) {
                    TraceCompassLogUtils.traceInstant(LOGGER, Level.WARNING, "HandleCounterAspect:Exception", e); //$NON-NLS-1$
                }
            } else {
                ss.modifyAttribute(event.getTimestamp().toNanos(), eventContent, quark);
            }
        }
    }
}