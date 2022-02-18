/*******************************************************************************
 * Copyright (c) 2016, 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.profiling.core.callstack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.profiling.core.base.IProfilingElement;
import org.eclipse.tracecompass.analysis.profiling.core.base.IProfilingGroupDescriptor;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackHostUtils.IHostIdProvider;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackHostUtils.IHostIdResolver;
import org.eclipse.tracecompass.common.core.math.SaturatedArithmetic;
import org.eclipse.tracecompass.internal.analysis.profiling.core.Activator;
import org.eclipse.tracecompass.internal.analysis.profiling.core.callgraph.CalledFunctionFactory;
import org.eclipse.tracecompass.internal.analysis.profiling.core.callgraph.ICalledFunction;
import org.eclipse.tracecompass.internal.analysis.profiling.core.callstack.InstrumentedGroupDescriptor;
import org.eclipse.tracecompass.internal.analysis.profiling.core.callstack.InstrumentedProfilingElement;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

/**
 * A callstack series contain the information necessary to build all the
 * different callstacks from a same pattern.
 *
 * Example: Let's take a trace that registers function entry and exit for
 * threads and where events also provide information on some other stackable
 * application component:
 *
 * The structure of this callstack in the state system could be as follows:
 *
 * <pre>
 *  Per PID
 *    [pid]
 *        [tid]
 *            callstack
 *               1  -> function name
 *               2  -> function name
 *               3  -> function name
 *  Per component
 *    [application component]
 *       [tid]
 *           callstack
 *               1 -> some string
 *               2 -> some string
 * </pre>
 *
 * There are 2 {@link CallStackSeries} in this example, one starting by "Per
 * PID" and another "Per component". For the first series, there could be 3
 * {@link IProfilingGroupDescriptor}: "Per PID/*", "*", "callstack".
 *
 * If the function names happen to be addresses in an executable and the PID is
 * the key to map those symbols to actual function names, then the first group
 * "Per PID/*" would be the symbol key group.
 *
 * Each group descriptor can get the corresponding {@link IProfilingElement}s,
 * ie, for the first group, it would be all the individual pids in the state
 * system, and for the second group, it would be the application components.
 * Each element that is not a leaf element (check with
 * {@link IProfilingElement#isLeaf()}) will have a next group descriptor that
 * can fetch the elements under it. The last group will resolve to leaf elements
 * and each leaf elements has one {@link CallStack} object.
 *
 * This class makes the link between the groups and elements and the state
 * system. It is not included in the CallStackAnalysis because it can be re-used
 * by other types of flame chart providers, for instance, XML generated ones.
 *
 * @author Geneviève Bastien
 * @since 1.1
 */
public class CallStackSeries implements ISegmentStore<ISegment> {

    /**
     * Value for an unknown tid
     */
    public static final int UNKNOWN_TID = -1;

    /**
     * Interface for classes that provide a thread ID at time t for a callstack. The
     * thread ID can be used to calculate extra statistics per thread, for example,
     * the CPU time of each call site.
     */
    public interface IThreadIdProvider {

        /**
         * Get the ID of callstack thread at a given time
         *
         * @param time
         *            The time of request
         * @return The ID of the thread, or {@link #UNKNOWN_TID} if
         *         unavailable
         */
        int getThreadId(long time);

        /**
         * Return whether the value returned by this provider is variable through time
         * (ie, each function of a stack may have a different thread ID), or is fixed
         * (ie, all functions in a stack have the same thread ID)
         *
         * @return If <code>true</code>, the thread ID will be identical for a stack all
         *         throughout its life, it can be therefore be used to provider other
         *         thread-related information on stack even when there are no function
         *         calls.
         */
        boolean variesInTime();

    }

    /**
     * This class uses the value of an attribute as a thread ID.
     */
    private static final class AttributeValueThreadProvider implements IThreadIdProvider {

        private final ITmfStateSystem fSs;
        private final int fQuark;
        private @Nullable ITmfStateInterval fInterval;
        private int fLastThreadId = UNKNOWN_TID; //IHostModel.UNKNOWN_TID;
        private boolean fVariesInTime = true;

        public AttributeValueThreadProvider(ITmfStateSystem ss, int quark) {
            fSs = ss;
            fQuark = quark;
        }

        @Override
        public int getThreadId(long time) {
            ITmfStateInterval interval = fInterval;
            int tid = fLastThreadId;
            // If interval is not null and either the tid does not vary in time or the
            // interval intersects the requested time
            if (interval != null && (!fVariesInTime || interval.intersects(time))) {
                return tid;
            }
            try {
                interval = fSs.querySingleState(time, fQuark);
                switch (interval.getStateValue().getType()) {
                case INTEGER:
                    tid = interval.getStateValue().unboxInt();
                    break;
                case LONG:
                    tid = (int) interval.getStateValue().unboxLong();
                    break;
                case STRING:
                    try {
                        tid = Integer.valueOf(interval.getStateValue().unboxStr());
                    } catch (NumberFormatException e) {
                        tid = UNKNOWN_TID;
                    }
                    break;
                case NULL: /* Fallthrough cases */
                case DOUBLE: /* Fallthrough cases */
                case CUSTOM: /* Fallthrough cases */
                default:
                    break;

                }
                // If the interval spans the whole state system, the tid does not vary in time
                if (fSs.waitUntilBuilt(0)) {
                    if (interval.intersects(fSs.getStartTime()) && interval.intersects(fSs.getCurrentEndTime() - 1)) {
                        fVariesInTime = false;
                    }
                }
            } catch (StateSystemDisposedException e) {
                interval = null;
                tid = UNKNOWN_TID;
            }
            fInterval = interval;
            fLastThreadId = tid;
            return tid;
        }

        @Override
        public boolean variesInTime() {
            return fVariesInTime;
        }

    }

    /**
     * This class uses the value of an attribute as a thread ID.
     */
    private static final class AttributeNameThreadProvider implements IThreadIdProvider {

        private final int fTid;

        public AttributeNameThreadProvider(ITmfStateSystem ss, int quark) {
            int tid = UNKNOWN_TID;
            try {
                String attributeName = ss.getAttributeName(quark);
                tid = Integer.valueOf(attributeName);
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                tid = UNKNOWN_TID;
            }
            fTid = tid;
        }

        @Override
        public int getThreadId(long time) {
            return fTid;
        }

        @Override
        public boolean variesInTime() {
            return false;
        }

    }

    /**
     * Interface for describing how a callstack will get the thread ID
     */
    public interface IThreadIdResolver {

        /**
         * Get the actual thread ID provider from this resolver
         *
         * @param hostProvider
         *            The provider of the host ID for the callstack
         * @param element
         *            The leaf element of the callstack
         * @return The thread ID provider
         */
        @Nullable IThreadIdProvider resolve(IHostIdProvider hostProvider, IProfilingElement element);

    }

    /**
     * This class will resolve the thread ID provider by the value of a attribute at
     * a given depth
     */
    public static final class AttributeValueThreadResolver implements IThreadIdResolver {

        private int fLevel;

        /**
         * Constructor
         *
         * @param level
         *            The depth of the element whose value will be used to retrieve the
         *            thread ID
         */
        public AttributeValueThreadResolver(int level) {
            fLevel = level;
        }

        @Override
        public @Nullable IThreadIdProvider resolve(IHostIdProvider hostProvider, IProfilingElement element) {
            if (!(element instanceof InstrumentedProfilingElement)) {
                throw new IllegalArgumentException();
            }
            InstrumentedProfilingElement insElement = (InstrumentedProfilingElement) element;

            List<InstrumentedProfilingElement> elements = new ArrayList<>();
            InstrumentedProfilingElement el = insElement;
            while (el != null) {
                elements.add(el);
                el = el.getParentElement();
            }
            Collections.reverse(elements);
            if (elements.size() <= fLevel) {
                return null;
            }
            InstrumentedProfilingElement stackElement = elements.get(fLevel);
            return new AttributeValueThreadProvider(stackElement.getStateSystem(), stackElement.getQuark());
        }

    }

    /**
     * This class will resolve the thread ID provider by the value of a attribute at
     * a given depth
     */
    public static final class AttributeNameThreadResolver implements IThreadIdResolver {

        private int fLevel;

        /**
         * Constructor
         *
         * @param level
         *            The depth of the element whose value will be used to retrieve the
         *            thread ID
         */
        public AttributeNameThreadResolver(int level) {
            fLevel = level;
        }

        @Override
        public @Nullable IThreadIdProvider resolve(IHostIdProvider hostProvider, IProfilingElement element) {
            if (!(element instanceof InstrumentedProfilingElement)) {
                throw new IllegalArgumentException();
            }
            InstrumentedProfilingElement insElement = (InstrumentedProfilingElement) element;

            List<InstrumentedProfilingElement> elements = new ArrayList<>();
            InstrumentedProfilingElement el = insElement;
            while (el != null) {
                elements.add(el);
                el = el.getParentElement();
            }
            Collections.reverse(elements);
            if (elements.size() <= fLevel) {
                return null;
            }
            InstrumentedProfilingElement stackElement = elements.get(fLevel);
            return new AttributeNameThreadProvider(stackElement.getStateSystem(), stackElement.getQuark());
        }

    }

    private final InstrumentedGroupDescriptor fRootGroup;
    private final String fName;
    private final @Nullable IThreadIdResolver fResolver;
    private final IHostIdResolver fHostResolver;
    private final Map<Integer, IProfilingElement> fRootElements = new HashMap<>();

    /**
     * Constructor
     *
     * @param ss
     *            The state system containing this call stack
     * @param patternPaths
     *            The patterns for the different levels of the callstack in the
     *            state system. Any further level path is relative to the previous
     *            one.
     * @param symbolKeyLevelIndex
     *            The index in the list of the list to be used as a key to the
     *            symbol provider. The data at this level must be an integer, for
     *            instance a process ID
     * @param name
     *            A name for this callstack
     * @param hostResolver
     *            The host ID resolver for this callstack
     * @param threadResolver
     *            The thread resolver
     */
    public CallStackSeries(ITmfStateSystem ss, List<String[]> patternPaths, int symbolKeyLevelIndex, String name, IHostIdResolver hostResolver, @Nullable IThreadIdResolver threadResolver) {
        // Build the groups from the state system and pattern paths
        if (patternPaths.isEmpty()) {
            throw new IllegalArgumentException("State system callstack: the list of paths should not be empty"); //$NON-NLS-1$
        }
        int startIndex = patternPaths.size() - 1;
        InstrumentedGroupDescriptor prevLevel = new InstrumentedGroupDescriptor(ss, patternPaths.get(startIndex), null, symbolKeyLevelIndex == startIndex ? true : false);
        for (int i = startIndex - 1; i >= 0; i--) {
            InstrumentedGroupDescriptor level = new InstrumentedGroupDescriptor(ss, patternPaths.get(i), prevLevel, symbolKeyLevelIndex == i ? true : false);
            prevLevel = level;
        }
        fRootGroup = prevLevel;
        fName = name;
        fResolver = threadResolver;
        fHostResolver = hostResolver;
    }

    /**
     * Get the root elements of this callstack series
     *
     * @return The root elements of the callstack series
     */
    public Collection<IProfilingElement> getRootElements() {
        return InstrumentedProfilingElement.getRootElements(fRootGroup, fHostResolver, fResolver, fRootElements);
    }

    /**
     * Get the root group of the callstack series
     *
     * @return The root group descriptor
     */
    public IProfilingGroupDescriptor getRootGroup() {
        return fRootGroup;
    }

    /**
     * Get the name of this callstack series
     *
     * @return The name of the callstack series
     */
    public String getName() {
        return fName;
    }

    // ---------------------------------------------------
    // Segment store methods
    // ---------------------------------------------------

    private Collection<IProfilingElement> getLeafElements(IProfilingElement element) {
        if (element.isLeaf()) {
            return Collections.singleton(element);
        }
        List<IProfilingElement> list = new ArrayList<>();
        element.getChildren().forEach(e -> list.addAll(getLeafElements(e)));
        return list;
    }

    @Override
    public int size() {
        return Iterators.size(iterator());
    }

    @Override
    public boolean isEmpty() {
        return !iterator().hasNext();
    }

    @Override
    public boolean contains(@Nullable Object o) {
        // narrow down search when object is a segment
        if (o instanceof ICalledFunction) {
            ICalledFunction seg = (ICalledFunction) o;
            Iterable<@NonNull ISegment> iterable = getIntersectingElements(seg.getStart());
            return Iterables.contains(iterable, seg);
        }
        return false;
    }

    @Override
    public Iterator<ISegment> iterator() {
        ITmfStateSystem stateSystem = fRootGroup.getStateSystem();
        long start = stateSystem.getStartTime();
        long end = stateSystem.getCurrentEndTime();
        return getIntersectingElements(start, end).iterator();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("This segment store can potentially cause OutOfMemoryExceptions"); //$NON-NLS-1$
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException("This segment store can potentially cause OutOfMemoryExceptions"); //$NON-NLS-1$
    }

    @Override
    public boolean add(ISegment e) {
        throw new UnsupportedOperationException("This segment store does not support adding new segments"); //$NON-NLS-1$
    }

    @Override
    public boolean containsAll(@Nullable Collection<?> c) {
        if (c == null) {
            return false;
        }
        /*
         * Check that all elements in the collection are indeed ISegments, and
         * find their min end and max start time
         */
        long minEnd = Long.MAX_VALUE, maxStart = Long.MIN_VALUE;
        for (Object o : c) {
            if (o instanceof ICalledFunction) {
                ICalledFunction seg = (ICalledFunction) o;
                minEnd = Math.min(minEnd, seg.getEnd());
                maxStart = Math.max(maxStart, seg.getStart());
            } else {
                return false;
            }
        }
        if (minEnd > maxStart) {
            /*
             * all segments intersect a common range, we just need to intersect
             * a time stamp in that range
             */
            minEnd = maxStart;
        }

        /* Iterate through possible segments until we have found them all */
        Iterator<@NonNull ISegment> iterator = getIntersectingElements(minEnd, maxStart).iterator();
        int unFound = c.size();
        while (iterator.hasNext() && unFound > 0) {
            ISegment seg = iterator.next();
            for (Object o : c) {
                if (Objects.equals(o, seg)) {
                    unFound--;
                }
            }
        }
        return unFound == 0;
    }

    @Override
    public boolean addAll(@Nullable Collection<? extends ISegment> c) {
        throw new UnsupportedOperationException("This segment store does not support adding new segments"); //$NON-NLS-1$
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("This segment store does not support clearing the data"); //$NON-NLS-1$
    }

    private Map<Integer, CallStack> getCallStackQuarks() {
        Map<Integer, CallStack> quarkToElement = new HashMap<>();
        // Get the leaf elements and their callstacks
        getRootElements().stream().flatMap(e -> getLeafElements(e).stream())
                .filter(e -> e instanceof InstrumentedProfilingElement)
                .map(e -> (InstrumentedProfilingElement) e)
                .forEach(e -> {
                    e.getStackQuarks().forEach(c -> quarkToElement.put(c, e.getCallStack()));
                });
        return quarkToElement;
    }

    @Override
    public Iterable<ISegment> getIntersectingElements(long start, long end) {
        return getIntersectingElements(start, end, x -> true);
    }

    /**
     * Retrieve all elements that inclusively cross another segment and evaluate
     * a predicate to true. We define this target segment by its start and end
     * positions.
     *
     * This effectively means, all elements that respect *all* conditions:
     *
     * <ul>
     * <li>Their end is after the 'start' parameter</li>
     * <li>Their start is before the 'end' parameter</li>
     * <li>They match the predicate</li>
     * </ul>
     *
     * @param start
     *            The target start position
     * @param end
     *            The target end position
     * @param intervalTest
     *            A {@link Predicate} object. The predicate must evaluate to
     *            true in order to consider the interval, otherwise the interval
     *            is ignored
     * @return The elements overlapping with this segment
     * @since 2.2
     */
    protected Iterable<ISegment> getIntersectingElements(long start, long end, Predicate<ITmfStateInterval> intervalTest) {
        ITmfStateSystem stateSystem = fRootGroup.getStateSystem();
        long startTime = Math.max(SaturatedArithmetic.add(start, -1L), stateSystem.getStartTime());
        long endTime = Math.min(end, stateSystem.getCurrentEndTime());
        if (startTime > endTime) {
            return Collections.emptyList();
        }
        Map<Integer, CallStack> quarksToElement = getCallStackQuarks();
        try {
            Iterable<ITmfStateInterval> query2d = stateSystem.query2D(quarksToElement.keySet(), startTime, endTime);
            query2d = Iterables.filter(query2d, interval -> !interval.getStateValue().isNull() && intervalTest.test(interval));
            Function<ITmfStateInterval, ICalledFunction> fct = interval -> {
                CallStack callstack = quarksToElement.get(interval.getAttribute());
                if (callstack == null) {
                    throw new NullPointerException("The quark was in that map in the first place, there must be a callstack to go with it!"); //$NON-NLS-1$
                }
                int pid = callstack.getSymbolKeyAt(interval.getStartTime());
                return CalledFunctionFactory.create(interval.getStartTime(), interval.getEndTime() + 1, Integer.parseInt(stateSystem.getAttributeName(interval.getAttribute())), Objects.requireNonNull(interval.getValue()), pid, null);
            };
            return Iterables.transform(query2d, fct);
        } catch (StateSystemDisposedException e) {
            Activator.getInstance().logError("Error getting intersecting elements: StateSystemDisposed"); //$NON-NLS-1$
        }
        return Collections.emptyList();
    }

    @Override
    public void dispose() {
        // Nothing to do
    }

}
