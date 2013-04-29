/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.linuxtools.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.EventIterator;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

/**
 * An entry, or row, in the resource view
 *
 * @author Patrick Tasse
 */
public class ResourcesEntry implements ITimeGraphEntry {

    /** Type of resource */
    public static enum Type {
        /** Null resources (filler rows, etc.) */
        NULL,
        /** Entries for CPUs */
        CPU,
        /** Entries for IRQs */
        IRQ,
        /** Entries for Soft IRQ */
        SOFT_IRQ }

    private final int fQuark;
    private final LttngKernelTrace fTrace;
    private ITimeGraphEntry fParent = null;
    private final List<ITimeGraphEntry> children = null;
    private final String fName;
    private final Type fType;
    private final int fId;
    private long fStartTime;
    private long fEndTime;
    private List<ITimeEvent> fEventList = new ArrayList<ITimeEvent>();
    private List<ITimeEvent> fZoomedEventList = null;

    /**
     * Standard constructor
     *
     * @param quark
     *            The quark of the state system attribute whose state is shown
     *            on this row
     * @param trace
     *            The trace that this view is talking about
     * @param type
     *            Type of entry, see the Type enum
     * @param id
     *            The integer id associated with this entry or row
     */
    public ResourcesEntry(int quark, LttngKernelTrace trace, Type type, int id) {
        fQuark = quark;
        fTrace = trace;
        fType = type;
        fId = id;
        fName = type.toString() + ' ' + Integer.toString(id);
    }

    @Override
    public ITimeGraphEntry getParent() {
        return fParent;
    }

    @Override
    public boolean hasChildren() {
        return children != null && children.size() > 0;
    }

    @Override
    public List<ITimeGraphEntry> getChildren() {
        return children;
    }

    @Override
    public String getName() {
        return fName;
    }

    @Override
    public long getStartTime() {
        return fStartTime;
    }

    @Override
    public long getEndTime() {
        return fEndTime;
    }

    @Override
    public boolean hasTimeEvents() {
        return true;
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
     * Retrieve the trace that is associated to this Resource view.
     *
     * @return The LTTng 2 kernel trace
     */
    public LttngKernelTrace getTrace() {
        return fTrace;
    }

    /**
     * Get the entry Type of this entry. Uses the inner Type enum.
     *
     * @return The entry type
     */
    public Type getType() {
        return fType;
    }

    /**
     * Get the integer ID associated with this entry.
     *
     * @return The ID
     */
    public int getId() {
        return fId;
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
}
