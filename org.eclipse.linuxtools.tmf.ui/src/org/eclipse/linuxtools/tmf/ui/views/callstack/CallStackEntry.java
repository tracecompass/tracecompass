/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.callstack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.statesystem.core.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.EventIterator;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

/**
 * An entry, or row, in the Call Stack view
 *
 * @author Patrick Tasse
 * @since 2.0
 */
public class CallStackEntry implements ITimeGraphEntry {

    private final int fQuark;
    private final int fStackLevel;
    private final ITmfTrace fTrace;
    private ITimeGraphEntry fParent = null;
    private String fName;
    private String fFunctionName;
    private long fStartTime;
    private long fEndTime;
    private List<ITimeEvent> fEventList = new ArrayList<>(1);
    private List<ITimeEvent> fZoomedEventList = null;
    private @NonNull ITmfStateSystem fSS;

    /**
     * Standard constructor
     *
     * @param quark
     *            The call stack quark
     * @param stackLevel
     *            The stack level
     * @param trace
     *            The trace that this view is talking about
     * @deprecated Use {@link #CallStackEntry(int, int, ITmfTrace, ITmfStateSystem)}
     */
    @Deprecated
    public CallStackEntry(int quark, int stackLevel, ITmfTrace trace) {
        throw new UnsupportedOperationException();
    }

    /**
     * Standard constructor
     *
     * @param quark
     *            The call stack quark
     * @param stackLevel
     *            The stack level
     * @param trace
     *            The trace that this view is talking about
     * @param ss
     *            The call stack state system
     * @since 3.1
     */
    public CallStackEntry(int quark, int stackLevel, ITmfTrace trace, @NonNull ITmfStateSystem ss) {
        fQuark = quark;
        fStackLevel = stackLevel;
        fTrace = trace;
        fFunctionName = ""; //$NON-NLS-1$
        fSS = ss;
    }

    @Override
    public ITimeGraphEntry getParent() {
        return fParent;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public List<CallStackEntry> getChildren() {
        return null;
    }

    @Override
    public String getName() {
        return ""; //$NON-NLS-1$
    }

    /**
     * Get the function name of the call stack entry
     * @return the function name
     */
    public String getFunctionName() {
        return fFunctionName;
    }

    /**
     * Set the function name of the call stack entry
     * @param functionName the function name
     */
    public void setFunctionName(String functionName) {
        fFunctionName = functionName;
    }

    @Override
    public long getStartTime() {
        return fStartTime;
    }

    /**
     * Set the start time of the call stack entry
     * @param startTime the start time
     */
    public void setStartTime(long startTime) {
        fStartTime = startTime;
    }

    @Override
    public long getEndTime() {
        return fEndTime;
    }

    /**
     * Set the end time of the call stack entry
     * @param endTime the end time
     */
    public void setEndTime(long endTime) {
        fEndTime = endTime;
    }

    @Override
    public boolean hasTimeEvents() {
        return fEventList != null;
    }

    @Override
    public Iterator<ITimeEvent> getTimeEventsIterator() {
        return new EventIterator(fEventList, fZoomedEventList);
    }

    @Override
    public Iterator<ITimeEvent> getTimeEventsIterator(long startTime, long stopTime, long visibleDuration) {
        return new EventIterator(fEventList, fZoomedEventList, startTime, stopTime);
    }

    /**
     * Assign a parent entry to this one, to organize them in a tree in the
     * view.
     *
     * @param parent
     *            The parent entry
     */
    public void setParent(ITimeGraphEntry parent) {
        fParent = parent;
    }

    /**
     * Retrieve the attribute quark that's represented by this entry.
     *
     * @return The integer quark
     */
    public int getQuark() {
        return fQuark;
    }

    /**
     * Retrieve the stack level associated with this entry.
     *
     * @return The stack level or 0
     */
    public int getStackLevel() {
        return fStackLevel;
    }

    /**
     * Retrieve the trace that is associated to this view.
     *
     * @return The trace
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * Retrieve the call stack state system associated with this entry.
     *
     * @return The call stack state system
     * @since 3.1
     */
    public @NonNull ITmfStateSystem getStateSystem() {
        return fSS;
    }

    /**
     * Assign the target event list to this view.
     *
     * @param eventList
     *            The list of time events
     */
    public void setEventList(List<ITimeEvent> eventList) {
        fEventList = eventList;
        if (eventList != null && eventList.size() > 0) {
            fStartTime = eventList.get(0).getTime();
            ITimeEvent lastEvent = eventList.get(eventList.size() - 1);
            fEndTime = lastEvent.getTime() + lastEvent.getDuration();
        }
    }

    /**
     * Assign the zoomed event list to this view.
     *
     * @param eventList
     *            The list of "zoomed" time events
     */
    public void setZoomedEventList(List<ITimeEvent> eventList) {
        fZoomedEventList = eventList;
    }

    /**
     * Add an event to the event list
     *
     * @param timeEvent
     *          The event
     */
    public void addEvent(ITimeEvent timeEvent) {
        fEventList.add(timeEvent);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " name=" + fName; //$NON-NLS-1$
    }
}
