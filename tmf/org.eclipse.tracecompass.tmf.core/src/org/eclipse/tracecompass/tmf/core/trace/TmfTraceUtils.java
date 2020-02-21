/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.component.ITmfEventProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Utility methods for ITmfTrace's.
 *
 * @author Alexandre Montplaisir
 */
@NonNullByDefault
public final class TmfTraceUtils {

    private static final int MAX_NB_BINARY_BYTES = 2048;
    private static final Set<ITmfEventAspect<?>> EXTRA_ASPECTS = new HashSet<>();

    private TmfTraceUtils() {
    }

    /**
     * Return the first result of the first analysis module belonging to this trace
     * or its children, with the specified ID and class.
     *
     * @param trace
     *            The trace for which you want the modules
     * @param moduleClass
     *            Returned modules must extend this class
     * @param id
     *            The ID of the analysis module
     * @return The analysis module with specified class and ID, or null if no such
     *         module exists.
     */
    public static @Nullable <T extends IAnalysisModule> T getAnalysisModuleOfClass(ITmfTrace trace,
            Class<T> moduleClass, String id) {
        Iterable<T> modules = getAnalysisModulesOfClass(trace, moduleClass);
        for (T module : modules) {
            if (id.equals(module.getId())) {
                return module;
            }
        }
        return null;
    }

    /**
     * Registers an extra event aspect that may apply to all traces, not directly
     * linked to a trace type. These can be used to retrieve values using the
     * {@link #resolveEventAspectOfClassForEvent(ITmfTrace, Class, ITmfEvent)}
     * method, but will not appear where trace aspects are being displayed.
     *
     * @param aspect
     *            The event aspect to register
     * @since 3.0
     */
    public static void registerEventAspect(ITmfEventAspect<?> aspect) {
        EXTRA_ASPECTS.add(aspect);
    }

    /**
     * Return the analysis modules that are of a given class. The modules will be
     * cast to the requested class. If the trace has children, the childrens modules
     * are also returned.
     *
     * @param trace
     *            The trace for which you want the modules, the children trace
     *            modules are added as well.
     * @param moduleClass
     *            Returned modules must extend this class
     * @return List of modules of class moduleClass
     */
    public static <T> Iterable<@NonNull T> getAnalysisModulesOfClass(ITmfTrace trace, Class<T> moduleClass) {
        Iterable<IAnalysisModule> analysisModules = trace.getAnalysisModules();
        List<@NonNull T> modules = new ArrayList<>();
        for (IAnalysisModule module : analysisModules) {
            if (moduleClass.isAssignableFrom(module.getClass())) {
                modules.add(checkNotNull(moduleClass.cast(module)));
            }
        }
        for (ITmfEventProvider child : trace.getChildren()) {
            if (child instanceof ITmfTrace) {
                ITmfTrace childTrace = (ITmfTrace) child;
                Iterables.addAll(modules, getAnalysisModulesOfClass(childTrace, moduleClass));
            }
        }
        return modules;
    }

    /**
     * Return the analysis modules that are of a given class for a host. The modules
     * will be cast to the requested class. Only the modules for the specific host
     * traces will be returned.
     *
     * @param hostId
     *            The trace for which you want the modules, the children trace
     *            modules are added as well.
     * @param moduleClass
     *            Returned modules must extend this class
     * @return List of modules of class moduleClass
     * @since 3.2
     */
    public static <T> Iterable<@NonNull T> getAnalysisModulesOfClass(String hostId, Class<T> moduleClass) {
        Set<ITmfTrace> traces = TmfTraceManager.getInstance().getTracesForHost(hostId);
        List<@NonNull T> modules = new ArrayList<>();
        for (ITmfTrace trace : traces) {
            Iterable<IAnalysisModule> analysisModules = trace.getAnalysisModules();
            for (IAnalysisModule module : analysisModules) {
                if (moduleClass.isAssignableFrom(module.getClass())) {
                    modules.add(checkNotNull(moduleClass.cast(module)));
                }
            }
        }
        return modules;
    }

    /**
     * Returns all the aspects of a specified type.
     *
     * @param trace
     *            The trace for which you want the event aspects
     * @param aspectClass
     *            The class of the aspect(s) to resolve
     * @return An {@link Iterable} containing all the aspects of the type
     *         aspectClass
     * @since 3.1
     */
    public static Iterable<ITmfEventAspect<?>> getEventAspects(ITmfTrace trace, Class<? extends ITmfEventAspect<?>> aspectClass) {
        return Iterables.filter(Iterables.concat(trace.getEventAspects(), EXTRA_ASPECTS), aspect -> aspectClass.isAssignableFrom(aspect.getClass()));
    }

    /**
     * Return the first result of the first aspect that resolves as non null for the
     * event received in parameter. If the returned value is not null, it can be
     * safely cast to the aspect's class proper return type.
     *
     * @param trace
     *            The trace for which you want the event aspects
     * @param aspectClass
     *            The class of the aspect(s) to resolve
     * @param event
     *            The event for which to get the aspect
     * @return The first result of the {@link ITmfEventAspect#resolve(ITmfEvent)}
     *         that returns non null for the event or {@code null} otherwise
     */
    public static <T extends ITmfEventAspect<?>> @Nullable Object resolveEventAspectOfClassForEvent(
            ITmfTrace trace, Class<T> aspectClass, ITmfEvent event) {
        // First look in the trace aspects
        for (ITmfEventAspect<?> aspect : trace.getEventAspects()) {
            if (aspectClass.isAssignableFrom(aspect.getClass())) {
                Object value = aspect.resolve(event);
                if (value != null) {
                    return value;
                }
            }
        }

        // If the value is not found, look at the global aspects
        for (ITmfEventAspect<?> aspect : EXTRA_ASPECTS) {
            if (aspectClass.isAssignableFrom(aspect.getClass())) {
                Object value = aspect.resolve(event);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    /**
     * Return the first result of the first aspect that resolves as non null for the
     * event received in parameter. If the returned value is not null, it can be
     * safely cast to the aspect's class proper return type.
     *
     * @param trace
     *            The trace for which you want the event aspects
     * @param aspectName
     *            The class of the aspect(s) to resolve
     * @param event
     *            The event for which to get the aspect
     * @return The first result of the {@link ITmfEventAspect#resolve(ITmfEvent)}
     *         that returns non null for the event or {@code null} otherwise
     * @since 3.0
     */
    public static @Nullable Object resolveAspectOfNameForEvent(ITmfTrace trace, String aspectName, ITmfEvent event) {
        // First look in the trace aspects
        for (ITmfEventAspect<?> aspect : trace.getEventAspects()) {
            if (aspectName.equalsIgnoreCase(aspect.getName())) {
                Object value = aspect.resolve(event);
                if (value != null) {
                    return value;
                }
            }
        }

        // If the value is not found, look at the global aspects
        for (ITmfEventAspect<?> aspect : EXTRA_ASPECTS) {
            if (aspectName.equalsIgnoreCase(aspect.getName())) {
                Object value = aspect.resolve(event);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    /**
     * Return the first result of the first aspect that resolves as a non-null
     * Integer for the event received in parameter. If no matching aspects are found
     * then null is returned.
     *
     * @param trace
     *            The trace for which you want the event aspects
     * @param aspectClass
     *            The class of the aspect(s) to resolve
     * @param event
     *            The event for which to get the aspect
     * @return Integer of the first result of the
     *         {@link ITmfEventAspect#resolve(ITmfEvent)} that returns non null for
     *         the event or {@code null} otherwise
     * @since 2.0
     */
    public static <T extends ITmfEventAspect<Integer>> @Nullable Integer resolveIntEventAspectOfClassForEvent(
            ITmfTrace trace, Class<T> aspectClass, ITmfEvent event) {
        // First look in the trace aspects
        for (ITmfEventAspect<?> aspect : trace.getEventAspects()) {
            if (aspectClass.isAssignableFrom(aspect.getClass())) {
                Object value = aspect.resolve(event);
                if (value instanceof Integer) {
                    return (Integer) value;
                }
            }
        }

        // If the value is not found, look at the global aspects
        for (ITmfEventAspect<?> aspect : EXTRA_ASPECTS) {
            if (aspectClass.isAssignableFrom(aspect.getClass())) {
                Object value = aspect.resolve(event);
                if (value instanceof Integer) {
                    return (Integer) value;
                }
            }
        }
        return null;
    }

    /**
     * Checks for text file.
     *
     * Note that it checks for binary value 0 in the first MAX_NB_BINARY_BYTES bytes
     * to determine if the file is text.
     *
     * @param file
     *            the file to check. Caller has to make sure that file exists.
     * @return true if it is binary else false
     * @throws IOException
     *             if IOException occurs
     * @since 1.2
     */
    public static boolean isText(File file) throws IOException {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
            int count = 0;
            int val = bufferedInputStream.read();
            while ((count < MAX_NB_BINARY_BYTES) && (val >= 0)) {
                if (val == 0) {
                    return false;
                }
                count++;
                val = bufferedInputStream.read();
            }
        }
        return true;
    }

    // ------------------------------------------------------------------------
    // Event matching methods
    // ------------------------------------------------------------------------

    /**
     * Retrieve from a trace the next event, from a starting rank, matching the
     * given predicate.
     *
     * @param trace
     *            The trace
     * @param startRank
     *            The rank of the event at which to start searching. Use
     *            <code>0</code> to search from the start of the trace.
     * @param predicate
     *            The predicate to test events against
     * @param monitor
     *            Optional progress monitor that can be used to cancel the operation
     * @return The first event matching the predicate, or null if the end of the
     *         trace was reached and no event was found
     * @since 2.1
     */
    public static @Nullable ITmfEvent getNextEventMatching(ITmfTrace trace, long startRank,
            Predicate<ITmfEvent> predicate, @Nullable IProgressMonitor monitor) {
        if (monitor != null && monitor.isCanceled()) {
            return null;
        }

        /*
         * rank + 1 because we do not want to include the start event itself in the
         * search
         */
        EventMatchingRequest req = new EventMatchingRequest(startRank + 1, predicate, false);
        trace.sendRequest(req);
        try {
            /* Check periodically if the job was cancelled */
            req.waitForStart();
            while (req.isRunning()) {
                Thread.sleep(200);
                if (monitor != null && monitor.isCanceled()) {
                    req.cancel();
                    return null;
                }
            }
            req.waitForCompletion();
        } catch (InterruptedException e) {
            return null;
        }

        return req.getFoundEvent();
    }

    /**
     * Retrieve from a trace the previous event, from a given rank, matching the
     * given predicate.
     *
     * @param trace
     *            The trace
     * @param startRank
     *            The rank of the event at which to start searching backwards.
     * @param predicate
     *            The predicate to test events against
     * @param monitor
     *            Optional progress monitor that can be used to cancel the operation
     * @return The first event found matching the predicate, or null if the
     *         beginning of the trace was reached and no event was found
     * @since 2.1
     */
    public static @Nullable ITmfEvent getPreviousEventMatching(ITmfTrace trace, long startRank,
            Predicate<ITmfEvent> predicate, @Nullable IProgressMonitor monitor) {
        if (monitor != null && monitor.isCanceled()) {
            return null;
        }
        /*
         * We are going to do a series of queries matching the trace's cache size in
         * length (which should minimize on-disk seeks), then iterate on the found
         * events in reverse order until we find a match.
         */
        int step = trace.getCacheSize();

        /*
         * If we are close to the beginning of the trace, make sure we only look for the
         * events before the startRank.
         */
        if (startRank < step) {
            step = (int) startRank;
        }

        long currentRank = startRank;
        try {
            while (currentRank > 0) {
                currentRank = Math.max(currentRank - step, 0);

                List<ITmfEvent> list = new ArrayList<>(step);
                ArrayFillingRequest req = new ArrayFillingRequest(currentRank, step, list);
                trace.sendRequest(req);

                /* Check periodically if the job was cancelled */
                req.waitForStart();
                while (req.isRunning()) {
                    Thread.sleep(200);
                    if (monitor != null && monitor.isCanceled()) {
                        req.cancel();
                        return null;
                    }
                }
                req.waitForCompletion();

                Optional<ITmfEvent> matchingEvent = Lists.reverse(list).stream()
                        .filter(predicate)
                        .findFirst();

                if (matchingEvent.isPresent()) {
                    /* We found an event matching, return it! */
                    return matchingEvent.get();
                }
                /* Keep searching, next loop */

            }
        } catch (InterruptedException e) {
            return null;
        }

        /*
         * We searched up to the beginning of the trace and didn't find anything.
         */
        return null;

    }

    /**
     * Event request looking for an event matching a Predicate.
     */
    private static class EventMatchingRequest extends TmfEventRequest {

        private final Predicate<ITmfEvent> fPredicate;
        private final boolean fReturnLast;

        private @Nullable ITmfEvent fFoundEvent = null;

        /**
         * Basic constructor, will query the trace until the end.
         *
         * @param startRank
         *            The rank at which to start, use 0 for the beginning
         * @param predicate
         *            The predicate to test against each event
         * @param returnLast
         *            Should we return the last or first event found. If false, the
         *            request ends as soon as a matching event is found. If false, we
         *            will go through all events to find a possible last-match.
         */
        public EventMatchingRequest(long startRank, Predicate<ITmfEvent> predicate, boolean returnLast) {
            super(ITmfEvent.class, startRank, ALL_DATA, ExecutionType.FOREGROUND);
            fPredicate = predicate;
            fReturnLast = returnLast;
        }

        /**
         * Basic constructor, will query the trace the limit is reached.
         *
         * @param startRank
         *            The rank at which to start, use 0 for the beginning
         * @param limit
         *            The limit on the number of events
         * @param predicate
         *            The predicate to test against each event
         * @param returnLast
         *            Should we return the last or first event found. If false, the
         *            request ends as soon as a matching event is found. If false, we
         *            will go through all events to find a possible last-match.
         */
        public EventMatchingRequest(long startRank, int limit, Predicate<ITmfEvent> predicate, boolean returnLast) {
            super(ITmfEvent.class, startRank, limit, ExecutionType.FOREGROUND);
            fPredicate = predicate;
            fReturnLast = returnLast;
        }

        public @Nullable ITmfEvent getFoundEvent() {
            return fFoundEvent;
        }

        @Override
        public void handleData(ITmfEvent event) {
            super.handleData(event);

            if (fPredicate.test(event)) {
                fFoundEvent = event;
                if (!fReturnLast) {
                    this.done();
                }
            }
        }
    }

    /**
     * Event request that simply puts all returned events into a list passed in
     * parameter.
     */
    private static class ArrayFillingRequest extends TmfEventRequest {

        private final List<ITmfEvent> fList;

        public ArrayFillingRequest(long startRank, int limit, List<ITmfEvent> listToFill) {
            super(ITmfEvent.class, startRank, limit, ExecutionType.FOREGROUND);
            fList = listToFill;
        }

        @Override
        public void handleData(ITmfEvent event) {
            super.handleData(event);
            fList.add(event);
        }

    }
}
