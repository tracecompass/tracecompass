/*******************************************************************************
 * Copyright (c) 2012, 2016 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Add message to exceptions
 *******************************************************************************/

package org.eclipse.tracecompass.internal.statesystem.core;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.TimeRangeCondition;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.interval.TmfStateInterval;

/**
 * The Transient State is used to build intervals from punctual state changes.
 * It contains a "state info" vector similar to the "current state", except here
 * we also record the start time of every state stored in it.
 *
 * We can then build {@link ITmfStateInterval}'s, to be inserted in a
 * {@link IStateHistoryBackend} when we detect state changes : the "start time"
 * of the interval will be the recorded time we have here, and the "end time"
 * will be the timestamp of the new state-changing event we just read.
 *
 * @author Alexandre Montplaisir
 */
@NonNullByDefault
public class TransientState {

    private static final Logger LOGGER = TraceCompassLog.getLogger(TransientState.class);

    /* Indicates where to insert state changes that we generate */
    private final IStateHistoryBackend fBackend;

    private final ReentrantReadWriteLock fRWLock = new ReentrantReadWriteLock(false);

    private volatile boolean fIsActive;
    private volatile long fLatestTime;

    /* A method accessing these arrays will have to go through the lock */
    private List<@Nullable Object> fOngoingStateInfo;
    private List<Long> fOngoingStateStartTimes;
    private List<@Nullable Class<?>> fStateValueTypes;

    /**
     * Constructor
     *
     * @param backend
     *            The back-end in which to insert the generated state intervals
     */
    public TransientState(IStateHistoryBackend backend) {
        fBackend = backend;
        fIsActive = true;
        fOngoingStateInfo = new ArrayList<>();
        fOngoingStateStartTimes = new ArrayList<>();
        fStateValueTypes = new ArrayList<>();

        fLatestTime = backend.getStartTime();
    }

    /**
     * Get the latest time we have seen so far.
     *
     * @return The latest time seen in the transient state
     */
    public long getLatestTime() {
        return fLatestTime;
    }

    /**
     * Retrieve the ongoing state value for a given index (attribute quark).
     *
     * @param quark
     *            The quark of the attribute to look for
     * @return The corresponding state value
     * @throws IndexOutOfBoundsException
     *             If the quark is out of range
     */
    public @Nullable Object getOngoingStateValue(int quark) {
        fRWLock.readLock().lock();
        try {
            return fOngoingStateInfo.get(quark);
        } finally {
            fRWLock.readLock().unlock();
        }
    }

    /**
     * Retrieve the start time of the state in which the given attribute is in.
     *
     * @param quark
     *            The quark of the attribute to look for
     * @return The start time of the current state for this attribute
     * @throws IndexOutOfBoundsException
     *             If the quark is out of range
     */
    public long getOngoingStartTime(int quark) {
        fRWLock.readLock().lock();
        try {
            return fOngoingStateStartTimes.get(quark);
        } finally {
            fRWLock.readLock().unlock();
        }
    }

    /**
     * Modify the current state for a given attribute. This will not update the
     * "ongoing state start time" in any way, so be careful when using this.
     *
     * @param quark
     *            The quark of the attribute to modify
     * @param newValue
     *            The state value the attribute should have
     * @throws IndexOutOfBoundsException
     *             If the quark is out of range
     */
    public void changeOngoingStateValue(int quark, Object newValue) {
        fRWLock.writeLock().lock();
        try {
            fOngoingStateInfo.set(quark, newValue);
        } finally {
            fRWLock.writeLock().unlock();
        }
    }

    /**
     * Convenience method to return the "ongoing" value for a given attribute as
     * a dummy interval whose end time = the current latest time.
     *
     * @param quark
     *            The quark of the attribute
     * @return An interval representing the current state (but whose end time is
     *         the current one, and probably not the "final" one)
     * @throws IndexOutOfBoundsException
     *             If the quark is out of range
     */
    public ITmfStateInterval getOngoingInterval(int quark) {
        fRWLock.readLock().lock();
        try {
            return new TmfStateInterval(fOngoingStateStartTimes.get(quark), fLatestTime,
                    quark, fOngoingStateInfo.get(quark));
        } finally {
            fRWLock.readLock().unlock();
        }
    }

    /**
     * Try to get the state interval valid for time/quark, if it is present in
     * this transient state. If it is not (for example, a new value is active
     * since after the specified timestamp) then null will be returned.
     *
     * @param time
     *            The timestamp to look for
     * @param quark
     *            The quark of the attribute to look for
     * @return The corresponding TmfStateInterval object if we could find it in
     *         this transient state, or null if we couldn't.
     * @throws IndexOutOfBoundsException
     *             If the quark is out of range
     */
    public @Nullable ITmfStateInterval getIntervalAt(long time, int quark) {
        fRWLock.readLock().lock();
        try {
            if (!isActive() || time < fOngoingStateStartTimes.get(quark)) {
                return null;
            }
            return new TmfStateInterval(fOngoingStateStartTimes.get(quark),
                    fLatestTime, quark, fOngoingStateInfo.get(quark));
        } finally {
            fRWLock.readLock().unlock();
        }
    }

    /**
     * More advanced version of {@link #changeOngoingStateValue}. Replaces the
     * complete ongoingStateInfo in one go, and updates the
     * ongoingStateStartTimes and #stateValuesTypes accordingly. BE VERY CAREFUL
     * WITH THIS!
     *
     * @param newStateIntervals
     *            The List of intervals that will represent the new
     *            "ongoing state". Their end times don't matter, we will only
     *            check their value and start times.
     */
    public void replaceOngoingState(List<ITmfStateInterval> newStateIntervals) {
        final int size = newStateIntervals.size();

        fRWLock.writeLock().lock();
        try {
            fOngoingStateInfo = new ArrayList<>(size);
            fOngoingStateStartTimes = new ArrayList<>(size);
            fStateValueTypes = new ArrayList<>(size);

            for (ITmfStateInterval interval : newStateIntervals) {
                Object value = interval.getValue();
                fOngoingStateInfo.add(value);
                fOngoingStateStartTimes.add(interval.getStartTime());
                Class<?> objectClass = value != null ? value.getClass() : null;
                fStateValueTypes.add(objectClass);
            }
        } finally {
            fRWLock.writeLock().unlock();
        }
    }

    /**
     * Add an "empty line" to both "ongoing..." vectors. This is needed so the
     * Ongoing... tables can stay in sync with the number of attributes in the
     * attribute tree, namely when we add sub-path attributes.
     */
    public void addEmptyEntry() {
        fRWLock.writeLock().lock();
        try {
            /*
             * Since this is a new attribute, we suppose it was in the
             * "null state" since the beginning (so we can have intervals
             * covering for all timestamps). A null interval will then get added
             * at the first state change.
             */
            fOngoingStateInfo.add(null);
            fStateValueTypes.add(null);

            fOngoingStateStartTimes.add(fBackend.getStartTime());
        } finally {
            fRWLock.writeLock().unlock();
        }
    }

    /**
     * Process a state change to be inserted in the history.
     *
     * @param eventTime
     *            The timestamp associated with this state change
     * @param value
     *            The new StateValue associated to this attribute
     * @param quark
     *            The quark of the attribute that is being modified
     * @throws TimeRangeException
     *             If 'eventTime' is invalid
     * @throws IndexOutOfBoundsException
     *             If the quark is out of range
     * @throws StateValueTypeException
     *             If the state value to be inserted is of a different type of
     *             what was inserted so far for this attribute.
     */
    public void processStateChange(long eventTime, @Nullable Object value, int quark)
            throws TimeRangeException, StateValueTypeException {
        if (!this.fIsActive) {
            return;
        }

        fRWLock.writeLock().lock();
        try {
            Class<?> expectedSvType = fStateValueTypes.get(quark);

            /*
             * Make sure the state value type we're inserting is the same as the
             * one registered for this attribute.
             */
            if (expectedSvType == null) {
                /*
                 * The value hasn't been used yet, set it to the value we're
                 * currently inserting (which might be null/-1 again).
                 */
                fStateValueTypes.set(quark, value != null ? value.getClass() : null);
            } else if ((value != null) && (value.getClass() != expectedSvType)) {
                /*
                 * We authorize inserting null values in any type of attribute,
                 * but for every other types, it needs to match our
                 * expectations!
                 */
                throw new StateValueTypeException(fBackend.getSSID() + " Quark:" + quark + ", Type:" + value.getClass() + ", Expected:" + expectedSvType); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }

            if (Objects.equals(fOngoingStateInfo.get(quark),value)) {
                /*
                 * This is the case where the new value and the one already
                 * present in the Builder are the same. We do not need to create
                 * an interval, we'll just keep the current one going.
                 */
                return;
            }

            if (fOngoingStateStartTimes.get(quark) < eventTime) {
                /*
                 * These two conditions are necessary to create an interval and
                 * update ongoingStateInfo.
                 */
                fBackend.insertPastState(fOngoingStateStartTimes.get(quark),
                        eventTime - 1, /* End Time */
                        quark, /* attribute quark */
                        fOngoingStateInfo.get(quark)); /* StateValue */

                fOngoingStateStartTimes.set(quark, eventTime);
            }
            fOngoingStateInfo.set(quark, value);

            /* Update the Transient State's lastestTime, if needed */
            if (fLatestTime < eventTime) {
                fLatestTime = eventTime;
            }

        } finally {
            fRWLock.writeLock().unlock();
        }
    }

    /**
     * Run a "get state at time" query on the Transient State only.
     *
     * @param stateInfo
     *            The stateInfo object in which we will put our relevant
     *            information
     * @param t
     *            The requested timestamp
     */
    public void doQuery(List<@Nullable ITmfStateInterval> stateInfo, long t) {
        fRWLock.readLock().lock();
        try {
            if (!this.fIsActive) {
                return;
            }
            if (stateInfo.size() > fOngoingStateInfo.size()) {
                throw new IllegalArgumentException();
            }

            for (int i = 0; i < stateInfo.size(); i++) {
                /*
                 * We build a dummy interval whose end time =
                 * "current transient state end time" to put in the answer to
                 * the query.
                 */
                final ITmfStateInterval interval = getIntervalAt(t, i);
                if (interval != null) {
                    stateInfo.set(i, interval);
                }
            }
        } finally {
            fRWLock.readLock().unlock();
        }
    }

    /**
     * Generalized 2D iterable query method. Iterates over intervals that match
     * the conditions on quarks and times in the Transient State.
     *
     * @param quarks
     *            Collection of quarks for returned intervals.
     * @param timeCondition
     *            Condition on the times for returned intervals
     * @return An iterable over the queried intervals, ordered by quarks.
     * @since 2.1
     */
    public Iterable<ITmfStateInterval> query2D(Collection<Integer> quarks, TimeRangeCondition timeCondition) {
        fRWLock.readLock().lock();
        try (TraceCompassLogUtils.ScopeLog log = new TraceCompassLogUtils.ScopeLog(LOGGER, Level.FINEST, "TransientState:query2D", //$NON-NLS-1$
                "ssid", fBackend.getSSID(), //$NON-NLS-1$
                "quarks", quarks, //$NON-NLS-1$
                "time", timeCondition)) { //$NON-NLS-1$
            if (!fIsActive) {
                return Collections.EMPTY_LIST;
            }
            long end = timeCondition.max();
            Collection<ITmfStateInterval> iterable = new ArrayList<>();
            for (Integer quark : quarks) {
                ITmfStateInterval interval = getIntervalAt(end, quark);
                if (interval != null) {
                    iterable.add(interval);
                }
            }
            return iterable;
        } finally {
            fRWLock.readLock().unlock();
        }
    }

    /**
     * Close off the Transient State, used for example when we are done reading
     * a static trace file. All the information currently contained in it will
     * be converted to intervals and "flushed" to the state history.
     *
     * @param endTime
     *            The timestamp to use as end time for the state history (since
     *            it may be different than the timestamp of the last state
     *            change)
     */
    public void closeTransientState(long endTime) {
        if (!this.fIsActive) {
            return;
        }

        fRWLock.writeLock().lock();
        try {
            for (int i = 0; i < fOngoingStateInfo.size(); i++) {
                if (fOngoingStateStartTimes.get(i) > endTime) {
                    /*
                     * Handle the cases where trace end > timestamp of last
                     * state change. This can happen when inserting "future"
                     * changes.
                     */
                    continue;
                }
                try {
                    fBackend.insertPastState(fOngoingStateStartTimes.get(i),
                            endTime, /* End Time */
                            i, /* attribute quark */
                            fOngoingStateInfo.get(i)); /* StateValue */

                } catch (TimeRangeException e) {
                    /*
                     * This shouldn't happen, since we control where the
                     * interval's start time comes from
                     */
                    throw new IllegalStateException(e);
                }
            }

            fOngoingStateInfo.clear();
            fOngoingStateStartTimes.clear();
            this.fIsActive = false;

        } finally {
            fRWLock.writeLock().unlock();
        }
    }

    /**
     * Simply returns if this Transient State is currently being used or not
     *
     * @return True if this transient state is active
     */
    public boolean isActive() {
        return this.fIsActive;
    }

    /**
     * Mark this transient state as inactive
     */
    public void setInactive() {
        fIsActive = false;
    }

    /**
     * Debugging method that prints the contents of the transient state
     *
     * @param writer
     *            The writer to which the output should be written
     */
    public void debugPrint(PrintWriter writer) {
        /* Only used for debugging, shouldn't be externalized */
        writer.println("------------------------------"); //$NON-NLS-1$
        writer.println("Info stored in the Builder:"); //$NON-NLS-1$
        if (!this.fIsActive) {
            writer.println("Builder is currently inactive"); //$NON-NLS-1$
            writer.println('\n');
            return;
        }
        writer.println("\nAttribute\tStateValue\tValid since time"); //$NON-NLS-1$
        for (int i = 0; i < fOngoingStateInfo.size(); i++) {
            writer.format("%d\t\t", i); //$NON-NLS-1$
            writer.print(String.valueOf(fOngoingStateInfo.get(i)) + "\t\t"); //$NON-NLS-1$
            writer.println(fOngoingStateStartTimes.get(i).toString());
        }
        writer.println('\n');
        return;
    }

}
