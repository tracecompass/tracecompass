/*******************************************************************************
 * Copyright (c) 2014, 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Add message to exceptions
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;

/**
 * Provide utility methods for the state system
 *
 * @author Geneviève Bastien
 * @author Loïc Prieur-Drevon
 */
@NonNullByDefault
public final class StateSystemUtils {

    private StateSystemUtils() {
    }

    /**
     * Convenience method to query attribute stacks (created with
     * pushAttribute()/popAttribute()). This will return the interval that is
     * currently at the top of the stack, or 'null' if that stack is currently
     * empty. It works similarly to querySingleState().
     *
     * To retrieve the other values in a stack, you can query the sub-attributes
     * manually.
     *
     * @param ss
     *            The state system to query
     * @param t
     *            The timestamp of the query
     * @param stackAttributeQuark
     *            The top-level stack-attribute (that was the target of
     *            pushAttribute() at creation time)
     * @return The interval that was at the top of the stack, or 'null' if the
     *         stack was empty.
     * @throws StateValueTypeException
     *             If the target attribute is not a valid stack attribute (if it
     *             has a string value for example)
     * @throws AttributeNotFoundException
     *             If the attribute was simply not found
     * @throws TimeRangeException
     *             If the given timestamp is invalid
     * @throws StateSystemDisposedException
     *             If the query is sent after the state system has been disposed
     */
    public static @Nullable ITmfStateInterval querySingleStackTop(ITmfStateSystem ss,
            long t, int stackAttributeQuark)
            throws AttributeNotFoundException, StateSystemDisposedException {
        @Nullable Object curStackStateValue = ss.querySingleState(t, stackAttributeQuark).getValue();

        if (curStackStateValue == null) {
            /* There is nothing stored in this stack at this moment */
            return null;
        }
        if (!(curStackStateValue instanceof Integer)) {
            throw new IllegalStateException(ss.getSSID() + "Quark: " + stackAttributeQuark + ", expected Integer.class, value was " + curStackStateValue.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        int curStackDepth = (int) curStackStateValue;
        if (curStackDepth <= 0) {
            /*
             * This attribute is an integer attribute, but it doesn't seem like
             * it's used as a stack-attribute...
             */
            throw new StateValueTypeException(ss.getSSID() + " Quark:" + stackAttributeQuark + ", Stack depth:" + curStackDepth);  //$NON-NLS-1$//$NON-NLS-2$
        }

        int subAttribQuark = ss.getQuarkRelative(stackAttributeQuark, String.valueOf(curStackDepth));
        return ss.querySingleState(t, subAttribQuark);
    }

    /**
     * Convenience method to query the ongoing value (in the Transient state) of
     * an attribute stack (created with pushAttribute()/popAttribute()). This
     * will return the interval value that is currently at the top of the stack,
     * or 'null' if that stack is currently empty. It works similarly to
     * queryOngoing().
     *
     * To retrieve the other values in a stack, you can query the sub-attributes
     * manually.
     *
     * @param ss
     *            The state system to query
     * @param stackAttributeQuark
     *            The top-level stack-attribute (that was the target of
     *            pushAttribute() at creation time)
     * @return The value of the interval that was at the top of the stack, or
     *         'null' if the stack was empty.
     * @throws IndexOutOfBoundsException
     *             If the stack-attribute quark is out of range
     * @throws IllegalStateException
     *             If the stack-attribute value is not valid (null or positive
     *             integer) or does not have a corresponding sub-attribute
     * @since 4.1
     */
    public static @Nullable Object queryOngoingStackTop(ITmfStateSystem ss, int stackAttributeQuark) {
        @Nullable Object curStackStateValue = ss.queryOngoing(stackAttributeQuark);

        if (curStackStateValue == null) {
            /* There is nothing stored in this stack at this moment */
            return null;
        }
        if (!(curStackStateValue instanceof Integer)) {
            throw new IllegalStateException(ss.getSSID() + " Quark:" + stackAttributeQuark + ", expected Integer.class, value was " + curStackStateValue.getClass()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        int curStackDepth = (int) curStackStateValue;
        if (curStackDepth <= 0) {
            /*
             * This attribute is an integer attribute, but it doesn't seem like
             * it's used as a stack-attribute...
             */
            throw new IllegalStateException(ss.getSSID() + " Quark:" + stackAttributeQuark + ", invalid Stack depth:" + curStackDepth);  //$NON-NLS-1$//$NON-NLS-2$
        }

        try {
            int subAttribQuark = ss.getQuarkRelative(stackAttributeQuark, String.valueOf(curStackDepth));
            return ss.queryOngoing(subAttribQuark);
        } catch (AttributeNotFoundException e) {
            throw new IllegalStateException(ss.getSSID() + " Quark:" + stackAttributeQuark + ", expected subAttribute '" + curStackDepth + "' not found"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    /**
     * Return a list of state intervals, containing the "history" of a given
     * attribute between timestamps t1 and t2. The list will be ordered by
     * ascending time.
     *
     * Note that contrary to queryFullState(), the returned list here is in the
     * "direction" of time (and not in the direction of attributes, as is the
     * case with queryFullState()).
     *
     * @param ss
     *            The state system to query
     * @param attributeQuark
     *            Which attribute this query is interested in
     * @param t1
     *            Start time of the range query
     * @param t2
     *            Target end time of the query. If t2 is greater than the end of
     *            the trace, we will return what we have up to the end of the
     *            history.
     * @return The List of state intervals that happened between t1 and t2
     * @throws TimeRangeException
     *             If t1 is invalid, or if t2 <= t1
     * @throws AttributeNotFoundException
     *             If the requested quark does not exist in the model.
     * @throws StateSystemDisposedException
     *             If the query is sent after the state system has been disposed
     */
    public static List<ITmfStateInterval> queryHistoryRange(ITmfStateSystem ss,
            int attributeQuark, long t1, long t2)
            throws AttributeNotFoundException, StateSystemDisposedException {

        List<ITmfStateInterval> intervals;
        ITmfStateInterval currentInterval;
        long ts, tEnd;

        /* Make sure the time range makes sense */
        if (t2 < t1) {
            throw new TimeRangeException(ss.getSSID() + " Start:" + t1 + ", End:" + t2); //$NON-NLS-1$ //$NON-NLS-2$
        }

        /* Set the actual, valid end time of the range query */
        if (t2 > ss.getCurrentEndTime()) {
            tEnd = ss.getCurrentEndTime();
        } else {
            tEnd = t2;
        }

        /* Get the initial state at time T1 */
        intervals = new ArrayList<>();
        currentInterval = ss.querySingleState(t1, attributeQuark);
        intervals.add(currentInterval);

        /* Get the following state changes */
        ts = currentInterval.getEndTime();
        while (ts != -1 && ts < tEnd) {
            ts++; /* To "jump over" to the next state in the history */
            currentInterval = ss.querySingleState(ts, attributeQuark);
            intervals.add(currentInterval);
            ts = currentInterval.getEndTime();
        }
        return intervals;
    }

    /**
     * Return the state history of a given attribute, but with at most one
     * update per "resolution". This can be useful for populating views (where
     * it's useless to have more than one query per pixel, for example). A
     * progress monitor can be used to cancel the query before completion.
     *
     * @param ss
     *            The state system to query
     * @param attributeQuark
     *            Which attribute this query is interested in
     * @param t1
     *            Start time of the range query
     * @param t2
     *            Target end time of the query. If t2 is greater than the end of
     *            the trace, we will return what we have up to the end of the
     *            history.
     * @param resolution
     *            The "step" of this query
     * @param monitor
     *            A progress monitor. If the monitor is canceled during a query,
     *            we will return what has been found up to that point. You can
     *            use "null" if you do not want to use one.
     * @return The List of states that happened between t1 and t2
     * @throws TimeRangeException
     *             If t1 is invalid, if t2 <= t1, or if the resolution isn't
     *             greater than zero.
     * @throws AttributeNotFoundException
     *             If the attribute doesn't exist
     * @throws StateSystemDisposedException
     *             If the query is sent after the state system has been disposed
     */
    public static List<ITmfStateInterval> queryHistoryRange(ITmfStateSystem ss,
            int attributeQuark, long t1, long t2, long resolution,
            @Nullable IProgressMonitor monitor)
            throws AttributeNotFoundException, StateSystemDisposedException {
        List<ITmfStateInterval> intervals = new LinkedList<>();
        ITmfStateInterval currentInterval = null;
        long ts, tEnd;

        /* Make sure the time range makes sense */
        if (t2 < t1 || resolution <= 0) {
            throw new TimeRangeException(ss.getSSID() + " Start:" + t1 + ", End:" + t2 + ", Resolution:" + resolution); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        /* Set the actual, valid end time of the range query */
        if (t2 > ss.getCurrentEndTime()) {
            tEnd = ss.getCurrentEndTime();
        } else {
            tEnd = t2;
        }

        IProgressMonitor mon = monitor;
        if (mon == null) {
            mon = new NullProgressMonitor();
        }

        /*
         * Iterate over the "resolution points". We skip unneeded queries in the
         * case the current interval is longer than the resolution.
         */
        for (ts = t1; ts <= tEnd; ts += ((currentInterval.getEndTime() - ts) / resolution + 1) * resolution) {
            if (mon.isCanceled()) {
                return intervals;
            }
            currentInterval = ss.querySingleState(ts, attributeQuark);
            intervals.add(currentInterval);
        }

        /* Add the interval at t2, if it wasn't included already. */
        if (currentInterval != null && currentInterval.getEndTime() < tEnd) {
            currentInterval = ss.querySingleState(tEnd, attributeQuark);
            intervals.add(currentInterval);
        }
        return intervals;
    }

    /**
     * Queries intervals in the state system for a given attribute, starting at
     * time t1, until we obtain a non-null value.
     *
     * @param ss
     *            The state system on which to query intervals
     * @param attributeQuark
     *            The attribute quark to query
     * @param t1
     *            Start time of the query
     * @param t2
     *            Time limit of the query. Use {@link Long#MAX_VALUE} for no
     *            limit.
     * @return The first interval from t1 for which the value is not a null
     *         value, or <code>null</code> if no interval was found once we
     *         reach either t2 or the end time of the state system.
     */
    public static @Nullable ITmfStateInterval queryUntilNonNullValue(ITmfStateSystem ss,
            int attributeQuark, long t1, long t2) {

        long current = t1;
        /* Make sure the range is ok */
        if (t1 < ss.getStartTime()) {
            current = ss.getStartTime();
        }
        long end = t2;
        if (end < ss.getCurrentEndTime()) {
            end = ss.getCurrentEndTime();
        }
        /* Make sure the time range makes sense */
        if (end < current) {
            return null;
        }

        try {
            while (current < t2) {
                ITmfStateInterval currentInterval = ss.querySingleState(current, attributeQuark);
                @Nullable Object value = currentInterval.getValue();

                if (value != null) {
                    return currentInterval;
                }
                current = currentInterval.getEndTime() + 1;
            }
        } catch (StateSystemDisposedException | TimeRangeException e) {
            /* Nothing to do */
        }
        return null;
    }

    /**
     * "Offer" helper method for a FIFO queue. This is similar to
     * {@link ITmfStateSystemBuilder#pushAttribute(long, Object, int)}, but it
     * represents a queue. An element is added to the end of the queue as if it
     * were a stack.
     *
     * To get the size of the queue later, use the following code:
     *
     * <pre>
     * Integer size = (Integer) ss.queryOngoing(attributeQuark);
     * if (size == null) {
     *     size = 0;
     * }
     * </pre>
     *
     * @param ss
     *            The state system
     * @param t
     *            Timestamp of the state change
     * @param value
     *            State value to add to the queue.
     * @param attributeQuark
     *            The base attribute to use as a queue. If it does not exist if
     *            will be created (with depth = 1)
     * @throws StateValueTypeException
     *             If the attribute 'attributeQuark' already exists, but is not
     *             of integer type.
     * @since 4.2
     */
    public static void queueOfferAttribute(ITmfStateSystemBuilder ss, long t, Object value, int attributeQuark) {
        ss.pushAttribute(t, value, attributeQuark);
    }

    /**
     * Antagonist of
     * {@link #queueOfferAttribute(ITmfStateSystemBuilder, long, Object, int)}.
     * This is similar to
     * {@link ITmfStateSystemBuilder#popAttributeObject(long, int)}, but it
     * represents a FIFO queue. An element is effectively removed from the top
     * of the queue by shifting everything towards the top and popping the last.
     * An empty queue will return <code>null</code>
     *
     * @param ss
     *            The state system
     * @param t
     *            Timestamp of the state change
     * @param attributeQuark
     *            Quark of the queue-attribute to pop
     * @return The state value that was removed, or <code>null</code> if the
     *         queue is empty
     * @throws StateValueTypeException
     *             If the target attribute already exists, but its state value
     *             type is invalid (not an integer)
     * @since 4.2
     */
    public static @Nullable Object queuePollAttribute(ITmfStateSystemBuilder ss, long t, int attributeQuark)
            throws StateValueTypeException {
        // Get children quarks
        List<@NonNull Integer> subAttributes = ss.getSubAttributes(attributeQuark, false);
        int size = subAttributes.size();

        // Abort if there are no children
        if (size == 0) {
            return null;
        }

        // If there is only one child, do a simple pop
        if (size == 1) {
            return ss.popAttributeObject(t, attributeQuark);
        }

        // Otherwise, keep element that will be effectively removed
        Object poppedValue = ss.queryOngoing(subAttributes.get(0));
        // And go through elements starting from the top
        for (int i = 0; i < (size - 1); ++i) {
            int childQuark = subAttributes.get(i);
            int nextChildQuark = subAttributes.get(i + 1);

            // Get value from the next element
            Object nextValue = ss.queryOngoing(nextChildQuark);

            // And replace above value with that
            ss.modifyAttribute(t, nextValue, childQuark);
        }

        // Pop last element (that was moved up)
        ss.popAttributeObject(t, attributeQuark);

        return poppedValue;
    }

    /**
     * "Peek" helper method for a FIFO queue. This will return the element that
     * is currently at the head of the queue without removing it, or
     * <code>null</code> if that queue is currently empty. This works similarly
     * to
     * {@link StateSystemUtils#querySingleStackTop(ITmfStateSystem, long, int)}.
     *
     * @param ss
     *            The state system
     * @param t
     *            Timestamp of the state change
     * @param attributeQuark
     *            Quark of the queue-attribute to peek (that was the target of
     *            queueAddAttribute() at creation time)
     * @return The state value at the head of the queue, or <code>null</code> if
     *         the queue is empty.
     * @since 4.2
     */
    public static @Nullable Object queuePeekAttribute(ITmfStateSystemBuilder ss, long t, int attributeQuark) {
        List<@NonNull Integer> subAttributes = ss.getSubAttributes(attributeQuark, false);
        if (subAttributes.size() == 0) {
            return null;
        }
        return ss.queryOngoing(subAttributes.get(0));
    }

    /**
     * Iterator class to allow 2-way iteration over intervals of a given
     * attribute. Not thread-safe!
     *
     * @since 2.3
     */
    public static class QuarkIterator implements Iterator<ITmfStateInterval> {

        private final ITmfStateSystem fSS;
        private final int fQuark;
        private final long fInitialTime;
        private final long fEndTime;
        private final long fResolution;

        /**
         * The last returned interval
         */
        private @Nullable ITmfStateInterval fCurrent;
        /**
         * The previous interval (pre-fetched)
         */
        private @Nullable ITmfStateInterval fPrevious;
        /**
         * The next interval (pre-fetched)
         */
        private @Nullable ITmfStateInterval fNext;

        /**
         * Constructor
         *
         * @param ss
         *            The state system on which to query intervals
         * @param quark
         *            The key to the attribute to iterate over
         * @param initialTime
         *            The timestamp that the first returned interval will
         *            intersect. This timestamp can be smaller than the
         *            StateSystem's start time, in which case, iteration will
         *            start at the StateSystem's start, on bigger than the
         *            StateSystem's current end time, in which case iteration
         *            will start at the StateSystem's current end time.
         * @since 2.1
         */
        public QuarkIterator(ITmfStateSystem ss, int quark, long initialTime) {
            this (ss, quark, initialTime, Long.MAX_VALUE, 1);
        }

        /**
         * Constructor
         *
         * @param ss
         *            The state system on which to query intervals
         * @param quark
         *            The key to the attribute to iterate over
         * @param initialTime
         *            The timestamp that the first returned interval will
         *            intersect. This timestamp can be smaller than the
         *            StateSystem's start time, in which case, iteration will
         *            start at the StateSystem's start, on bigger than the
         *            StateSystem's current end time, in which case iteration
         *            will start at the StateSystem's current end time.
         * @param endTime
         *            The end of the range of intervals to iterate over, it can be
         *            greater than the current state system's end time, iteration
         *            will end at the smallest of the two.
         * @throws TimeRangeException
         *             If endTime < initialTime.
         * @since 3.2
         */
        public QuarkIterator(ITmfStateSystem ss, int quark, long initialTime, long endTime) {
            this (ss, quark, initialTime, endTime, 1);
        }

        /**
         * Constructor
         *
         * @param ss
         *            The state system on which to query intervals
         * @param quark
         *            The key to the attribute to iterate over
         * @param initialTime
         *            The timestamp that the first returned interval will intersect.
         *            This timestamp can be smaller than the StateSystem's start time,
         *            in which case, iteration will start at the StateSystem's start, on
         *            bigger than the StateSystem's current end time, in which case
         *            iteration will start at the StateSystem's current end time.
         * @param endTime
         *            The end of the range of intervals to iterate over, it can be
         *            greater than the current state system's end time, iteration will
         *            end at the smallest of the two.
         * @param resolution
         *            The resolution, ie the number of nanoseconds between kernel status
         *            queries. A value lower or equal to 1 will return all intervals
         * @throws TimeRangeException
         *             If endTime < initialTime.
         * @since 4.0
         */
        public QuarkIterator(ITmfStateSystem ss, int quark, long initialTime, long endTime, long resolution) {
            if (endTime < initialTime) {
                throw new TimeRangeException("iterateOverQuark: end < start !"); //$NON-NLS-1$
            }
            fSS = ss;
            fQuark = quark;
            fInitialTime = initialTime;
            fEndTime = endTime;
            fResolution = Math.max(1, resolution);
        }

        private long getNextQueryTime() {
            if (fCurrent != null) {
                long endTime = fCurrent.getEndTime();
                return (fResolution == 1 ? endTime + 1 : fInitialTime + ((endTime - fInitialTime) / fResolution + 1) * fResolution);
            }
            /* Iteration has not started yet */
            return Long.max(fInitialTime, fSS.getStartTime());
        }

        private long getPreviousQueryTime() {
            if (fCurrent != null) {
                long startTime = fCurrent.getStartTime();
                return (fResolution == 1 ? startTime - 1 : fInitialTime - ((fInitialTime - startTime) / fResolution + 1) * fResolution);
            }
            /* Iteration has not started yet */
            return Long.min(fInitialTime, fSS.getCurrentEndTime());
        }

        @Override
        public boolean hasNext() {
            if (fNext != null) {
                return true;
            }
            /*
             * Compute the query's real end time here, to update it if the
             * iterator is used during state system build
             */
            long end = Long.min(fEndTime, fSS.getCurrentEndTime());

            /*
             * Ensure that the next query time falls within state system and
             * query time range. By definition getNextQueryTime() is larger than
             * the state system's start time.
             */
            long nextQueryTime = getNextQueryTime();
            if (nextQueryTime <= end) {
                try {
                    fNext = fSS.querySingleState(nextQueryTime, fQuark);
                } catch (StateSystemDisposedException e) {
                    fNext = null;
                    return false;
                }
            }
            return fNext != null;
        }

        @Override
        public ITmfStateInterval next() {
            if (hasNext()) {
                ITmfStateInterval next = Objects.requireNonNull(fNext, "Inconsistent state, should be non null if hasNext returned true"); //$NON-NLS-1$
                fPrevious = fCurrent;
                fCurrent = next;
                fNext = null;
                return next;
            }
            throw new NoSuchElementException();
        }

        /**
         * Returns true if the iteration has more previous elements. (In other
         * words, returns true if previous() would return an element rather than
         * throwing an exception.)
         *
         * @return true if the iteration has more previous elements
         */
        public boolean hasPrevious() {
            if (fPrevious != null) {
                return true;
            }
            /*
             * Ensure that the next query time falls within state system and
             * query time range. By definition getPreviousQueryTime() is smaller
             * than the state system's end time.
             */
            long previousQueryTime = getPreviousQueryTime();
            if (previousQueryTime >= fSS.getStartTime()) {
                try {
                    fPrevious = fSS.querySingleState(previousQueryTime, fQuark);
                } catch (StateSystemDisposedException e) {
                    fPrevious = null;
                    return false;
                }
            }
            return fPrevious != null;
        }

        /**
         * Returns the previous element in the iteration.
         *
         * @return the previous element in the iteration
         */
        public ITmfStateInterval previous() {
            if (hasPrevious()) {
                ITmfStateInterval prev = Objects.requireNonNull(fPrevious, "Inconsistent state, should be non null if hasPrevious returned true"); //$NON-NLS-1$
                fNext = fCurrent;
                fCurrent = prev;
                fPrevious = null;
                return prev;
            }
            throw new NoSuchElementException();
        }
    }

    /**
     * Build a sorted list of time stamps separated by resolution between
     * bounds, including the upper bound.
     *
     * @param from
     *            lower bound of the list of time stamps.
     * @param to
     *            upper bound of the list of time stamps.
     * @param resolution
     *            positive duration between two consecutive time stamps.
     * @return a sorted list of time stamps from start to end separated by
     *         resolution, or consecutive timestamps if resolution == 0.
     * @throws IllegalArgumentException
     *             if end < start or resolution < 0.
     * @since 3.0
     */
    public static List<Long> getTimes(long from, long to, long resolution) {
        if (to < from || resolution < 0) {
            throw new IllegalArgumentException();
        }
        /*
         * If resolution is 0, adjust increment to return consecutive
         * timestamps.
         */
        long increment = Math.max(resolution, 1L);
        List<Long> times = new ArrayList<>((int) ((to - from) / increment + 1));
        for (long t = from; t < to; t += increment) {
            times.add(t);
        }
        times.add(to);
        return times;
    }

}
