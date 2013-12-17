/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.statesystem;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.IStateHistoryBackend;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.interval.TmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.TmfStateValue;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue.Type;

/**
 * The Transient State is used to build intervals from punctual state changes. It
 * contains a "state info" vector similar to the "current state", except here we
 * also record the start time of every state stored in it.
 *
 * We can then build StateInterval's, to be inserted in the State History when
 * we detect state changes : the "start time" of the interval will be the
 * recorded time we have here, and the "end time" will be the timestamp of the
 * new state-changing event we just read.
 *
 * @author alexmont
 *
 */
class TransientState {

    /* Indicates where to insert state changes that we generate */
    private final IStateHistoryBackend backend;

    private boolean isActive;
    private long latestTime;

    private List<ITmfStateValue> ongoingStateInfo;
    private List<Long> ongoingStateStartTimes;
    private List<Type> stateValueTypes;

    TransientState(IStateHistoryBackend backend) {
        this.backend = backend;
        isActive = true;
        ongoingStateInfo = new ArrayList<>();
        ongoingStateStartTimes = new ArrayList<>();
        stateValueTypes = new ArrayList<>();

        if (backend != null) {
            latestTime = backend.getStartTime();
        } else {
            latestTime = 0;
        }
    }

    long getLatestTime() {
        return latestTime;
    }

    ITmfStateValue getOngoingStateValue(int index) throws AttributeNotFoundException {
        checkValidAttribute(index);
        return ongoingStateInfo.get(index);
    }

    long getOngoingStartTime(int index) throws AttributeNotFoundException {
        checkValidAttribute(index);
        return ongoingStateStartTimes.get(index);
    }

    void changeOngoingStateValue(int index, ITmfStateValue newValue)
            throws AttributeNotFoundException {
        checkValidAttribute(index);
        ongoingStateInfo.set(index, newValue);
    }

    /**
     * Return the "ongoing" value for a given attribute as a dummy interval
     * whose end time = -1 (since we don't know its real end time yet).
     *
     * @param quark
     * @throws AttributeNotFoundException
     */
    ITmfStateInterval getOngoingInterval(int quark) throws AttributeNotFoundException {
        checkValidAttribute(quark);
        return new TmfStateInterval(ongoingStateStartTimes.get(quark), -1, quark,
                ongoingStateInfo.get(quark));
    }

    private void checkValidAttribute(int quark) throws AttributeNotFoundException {
        if (quark > ongoingStateInfo.size() - 1 || quark < 0) {
            throw new AttributeNotFoundException();
        }
    }

    /**
     * More advanced version of {@link #changeOngoingStateValue}. Replaces the
     * complete {@link #ongoingStateInfo} in one go, and updates the
     * {@link #ongoingStateStartTimes} and {@link #stateValuesTypes}
     * accordingly. BE VERY CAREFUL WITH THIS!
     *
     * @param newStateIntervals
     *            The List of intervals that will represent the new
     *            "ongoing state". Their end times don't matter, we will only
     *            check their value and start times.
     */
    synchronized void replaceOngoingState(List<ITmfStateInterval> newStateIntervals) {
        int size = newStateIntervals.size();
        ongoingStateInfo = new ArrayList<>(size);
        ongoingStateStartTimes = new ArrayList<>(size);
        stateValueTypes = new ArrayList<>(size);

        for (ITmfStateInterval interval : newStateIntervals) {
            ongoingStateInfo.add(interval.getStateValue());
            ongoingStateStartTimes.add(interval.getStartTime());
            stateValueTypes.add(interval.getStateValue().getType());
        }
    }

    /**
     * Add an "empty line" to both "ongoing..." vectors. This is needed so the
     * Ongoing... tables can stay in sync with the number of attributes in the
     * attribute tree, namely when we add sub-path attributes.
     */
    synchronized void addEmptyEntry() {
        /*
         * Since this is a new attribute, we suppose it was in the "null state"
         * since the beginning (so we can have intervals covering for all
         * timestamps). A null interval will then get added at the first state
         * change.
         */
        ongoingStateInfo.add(TmfStateValue.nullValue());
        stateValueTypes.add(Type.NULL);

        if (backend == null) {
            ongoingStateStartTimes.add(0L);
        } else {
            ongoingStateStartTimes.add(backend.getStartTime());
        }
    }

    /**
     * Ask if the state information about attribute 'quark' at time 'time' is
     * present in the Builder as it is right now. If it's not, it's either in
     * the History Tree, or not in the system at all.
     *
     * Note that this method does not return the value itself (we don't even
     * look for it, we can know by just looking at the timestamp)
     *
     * @param time
     *            The timestamp to look for
     * @param quark
     *            The quark of the attribute to look for
     * @return True if the value is present in the Transient State at this
     *         moment in time, false if it's not
     */
    boolean hasInfoAboutStateOf(long time, int quark) {
        return (this.isActive() && time >= ongoingStateStartTimes.get(quark));
    }

    /**
     * This is the lower-level method that will be called by the
     * StateHistorySystem (with already-built StateValues and timestamps)
     *
     * @param index
     *            The index in the vectors (== the quark of the attribute)
     * @param value
     *            The new StateValue associated to this attribute
     * @param eventTime
     *            The timestamp associated with this state change
     * @throws TimeRangeException
     * @throws AttributeNotFoundException
     * @throws StateValueTypeException
     */
    synchronized void processStateChange(long eventTime,
            ITmfStateValue value, int index) throws TimeRangeException,
            AttributeNotFoundException, StateValueTypeException {
        assert (this.isActive);

        Type expectedSvType = stateValueTypes.get(index);
        checkValidAttribute(index);

        /*
         * Make sure the state value type we're inserting is the same as the
         * one registered for this attribute.
         */
        if (expectedSvType == Type.NULL) {
            /*
             * The value hasn't been used yet, set it to the value
             * we're currently inserting (which might be null/-1 again).
             */
            stateValueTypes.set(index, value.getType());
        } else if ((value.getType() != Type.NULL) && (value.getType() != expectedSvType)) {
            /*
             * We authorize inserting null values in any type of attribute,
             * but for every other types, it needs to match our expectations!
             */
            throw new StateValueTypeException();
        }

        /* Update the Transient State's lastestTime, if needed */
        if (latestTime < eventTime) {
            latestTime = eventTime;
        }

        if (ongoingStateInfo.get(index).equals(value)) {
            /*
             * This is the case where the new value and the one already present
             * in the Builder are the same. We do not need to create an
             * interval, we'll just keep the current one going.
             */
            return;
        }

        if (backend != null && ongoingStateStartTimes.get(index) < eventTime) {
            /*
             * These two conditions are necessary to create an interval and
             * update ongoingStateInfo.
             */
            backend.insertPastState(ongoingStateStartTimes.get(index),
                    eventTime - 1, /* End Time */
                    index, /* attribute quark */
                    ongoingStateInfo.get(index)); /* StateValue */

            ongoingStateStartTimes.set(index, eventTime);
        }
        ongoingStateInfo.set(index, value);
        return;
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
    void doQuery(List<ITmfStateInterval> stateInfo, long t) {
        ITmfStateInterval interval;

        if (!this.isActive) {
            return;
        }
        assert (stateInfo.size() == ongoingStateInfo.size());

        for (int i = 0; i < ongoingStateInfo.size(); i++) {
            /*
             * We build a dummy interval with end time = -1 to put in the answer
             * to the query.
             */
            if (this.hasInfoAboutStateOf(t, i)) {
                interval = new TmfStateInterval(ongoingStateStartTimes.get(i), -1,
                        i, ongoingStateInfo.get(i));
                stateInfo.set(i, interval);
            }
        }
    }

    /**
     * Close off the Transient State, used for example when we are done reading a
     * static trace file. All the information currently contained in it will be
     * converted to intervals and "flushed" to the State History.
     */
    void closeTransientState(long endTime) {
        assert (this.isActive);

        for (int i = 0; i < ongoingStateInfo.size(); i++) {
            if (ongoingStateStartTimes.get(i) > endTime) {
                /*
                 * Handle the cases where trace end > timestamp of last state
                 * change. This can happen when inserting "future" changes.
                 */
                continue;
            }
            try {
                backend.insertPastState(ongoingStateStartTimes.get(i),
                        endTime, /* End Time */
                        i, /* attribute quark */
                        ongoingStateInfo.get(i)); /* StateValue */

            } catch (TimeRangeException e) {
                /*
                 * This shouldn't happen, since we control where the interval's
                 * start time comes from
                 */
                throw new IllegalStateException(e);
            }
        }

        ongoingStateInfo.clear();
        ongoingStateStartTimes.clear();
        this.isActive = false;
        return;
    }

    /**
     * Simply returns if this Transient State is currently being used or not
     *
     * @return
     */
    boolean isActive() {
        return this.isActive;
    }

    void setInactive() {
        isActive = false;
    }

    /**
     * Debugging method that prints the contents of both 'ongoing...' vectors
     *
     * @param writer
     */
    void debugPrint(PrintWriter writer) {
        /* Only used for debugging, shouldn't be externalized */
        writer.println("------------------------------"); //$NON-NLS-1$
        writer.println("Info stored in the Builder:"); //$NON-NLS-1$
        if (!this.isActive) {
            writer.println("Builder is currently inactive"); //$NON-NLS-1$
            writer.println('\n');
            return;
        }
        writer.println("\nAttribute\tStateValue\tValid since time"); //$NON-NLS-1$
        for (int i = 0; i < ongoingStateInfo.size(); i++) {
            writer.format("%d\t\t", i); //$NON-NLS-1$
            writer.print(ongoingStateInfo.get(i).toString() + "\t\t"); //$NON-NLS-1$
            writer.println(ongoingStateStartTimes.get(i).toString());
        }
        writer.println('\n');
        return;
    }

}
