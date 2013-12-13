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
public class TransientState {

    /* Indicates where to insert state changes that we generate */
    private final IStateHistoryBackend backend;

    private boolean isActive;
    private long latestTime;

    private List<ITmfStateValue> ongoingStateInfo;
    private List<Long> ongoingStateStartTimes;
    private List<Type> stateValueTypes;

    /**
     * Constructor
     *
     * @param backend
     *            The back-end in which to insert the generated state intervals
     */
    public TransientState(IStateHistoryBackend backend) {
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

    /**
     * Get the latest time we have seen so far.
     *
     * @return The latest time seen in the transient state
     */
    public long getLatestTime() {
        return latestTime;
    }

    /**
     * Retrieve the ongoing state value for a given index (attribute quark).
     *
     * @param quark
     *            The quark of the attribute to look for
     * @return The corresponding state value
     * @throws AttributeNotFoundException
     *             If the quark is invalid
     */
    public ITmfStateValue getOngoingStateValue(int quark) throws AttributeNotFoundException {
        checkValidAttribute(quark);
        return ongoingStateInfo.get(quark);
    }

    /**
     * Retrieve the start time of the state in which the given attribute is in.
     *
     * @param quark
     *            The quark of the attribute to look for
     * @return The start time of the current state for this attribute
     * @throws AttributeNotFoundException
     *             If the quark is invalid
     */
    public long getOngoingStartTime(int quark) throws AttributeNotFoundException {
        checkValidAttribute(quark);
        return ongoingStateStartTimes.get(quark);
    }

    /**
     * Modify the current state for a given attribute. This will not update the
     * "ongoing state start time" in any way, so be careful when using this.
     *
     * @param quark
     *            The quark of the attribute to modify
     * @param newValue
     *            The state value the attribute should have
     * @throws AttributeNotFoundException
     *             If the quark is invalid
     */
    public void changeOngoingStateValue(int quark, ITmfStateValue newValue)
            throws AttributeNotFoundException {
        checkValidAttribute(quark);
        ongoingStateInfo.set(quark, newValue);
    }

    /**
     * Convenience method to return the "ongoing" value for a given attribute as
     * a dummy interval whose end time = -1 (since we don't know its real end
     * time yet).
     *
     * @param quark
     *            The quark of the attribute
     * @return An interval representing the current state (but whose end time is
     *         meaningless)
     * @throws AttributeNotFoundException
     *             If the quark is invalid
     */
    public ITmfStateInterval getOngoingInterval(int quark) throws AttributeNotFoundException {
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
     * complete ongoingStateInfo in one go, and updates the
     * ongoingStateStartTimes and #stateValuesTypes accordingly. BE VERY CAREFUL
     * WITH THIS!
     *
     * @param newStateIntervals
     *            The List of intervals that will represent the new
     *            "ongoing state". Their end times don't matter, we will only
     *            check their value and start times.
     */
    public synchronized void replaceOngoingState(List<ITmfStateInterval> newStateIntervals) {
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
    public synchronized void addEmptyEntry() {
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
    public boolean hasInfoAboutStateOf(long time, int quark) {
        return (this.isActive() && time >= ongoingStateStartTimes.get(quark));
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
     * @throws AttributeNotFoundException
     *             IF 'quark' does not represent an existing attribute
     * @throws StateValueTypeException
     *             If the state value to be inserted is of a different type of
     *             what was inserted so far for this attribute.
     */
    public synchronized void processStateChange(long eventTime,
            ITmfStateValue value, int quark) throws TimeRangeException,
            AttributeNotFoundException, StateValueTypeException {
        assert (this.isActive);

        Type expectedSvType = stateValueTypes.get(quark);
        checkValidAttribute(quark);

        /*
         * Make sure the state value type we're inserting is the same as the
         * one registered for this attribute.
         */
        if (expectedSvType == Type.NULL) {
            /*
             * The value hasn't been used yet, set it to the value
             * we're currently inserting (which might be null/-1 again).
             */
            stateValueTypes.set(quark, value.getType());
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

        if (ongoingStateInfo.get(quark).equals(value)) {
            /*
             * This is the case where the new value and the one already present
             * in the Builder are the same. We do not need to create an
             * interval, we'll just keep the current one going.
             */
            return;
        }

        if (backend != null && ongoingStateStartTimes.get(quark) < eventTime) {
            /*
             * These two conditions are necessary to create an interval and
             * update ongoingStateInfo.
             */
            backend.insertPastState(ongoingStateStartTimes.get(quark),
                    eventTime - 1, /* End Time */
                    quark, /* attribute quark */
                    ongoingStateInfo.get(quark)); /* StateValue */

            ongoingStateStartTimes.set(quark, eventTime);
        }
        ongoingStateInfo.set(quark, value);
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
    public void doQuery(List<ITmfStateInterval> stateInfo, long t) {
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
     * @return True if this transient state is active
     */
    public boolean isActive() {
        return this.isActive;
    }

    /**
     * Mark this transient state as inactive
     */
    public void setInactive() {
        isActive = false;
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
