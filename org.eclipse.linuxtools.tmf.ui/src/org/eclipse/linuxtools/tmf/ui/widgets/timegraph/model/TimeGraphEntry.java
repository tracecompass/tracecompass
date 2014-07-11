/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Geneviève Bastien - Move code to provide base classes for time graph view
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * An entry for use in the time graph views
 *
 * @since 2.1
 */
public class TimeGraphEntry implements ITimeGraphEntry {

    /** Entry's parent */
    private ITimeGraphEntry fParent = null;

    /** List of child entries */
    private final List<ITimeGraphEntry> fChildren = new CopyOnWriteArrayList<>();

    /** Name of this entry (text to show) */
    private String fName;
    private long fStartTime = -1;
    private long fEndTime = -1;
    private List<ITimeEvent> fEventList = new ArrayList<>();
    private List<ITimeEvent> fZoomedEventList = new ArrayList<>();

    /**
     * Constructor
     *
     * @param name
     *            The name of this entry
     * @param startTime
     *            The start time of this entry
     * @param endTime
     *            The end time of this entry
     */
    public TimeGraphEntry(String name, long startTime, long endTime) {
        fName = name;
        fStartTime = startTime;
        fEndTime = endTime;
    }

    // ---------------------------------------------
    // Getters and setters
    // ---------------------------------------------

    @Override
    public ITimeGraphEntry getParent() {
        return fParent;
    }

    /**
     * Sets the entry's parent
     *
     * @param entry The new parent entry
     */
    /*
     * TODO: This method can be removed in the next major API version.
     */
    protected void setParent(TimeGraphEntry entry) {
        fParent = entry;
    }

    /**
     * Sets the entry's parent
     *
     * @param entry The new parent entry
     * @since 3.1
     */
    /*
     * TODO: This method should be added to the interface in the next major API version.
     */
    protected void setParent(ITimeGraphEntry entry) {
        fParent = entry;
    }

    @Override
    public boolean hasChildren() {
        return fChildren.size() > 0;
    }

    @Override
    public List<? extends ITimeGraphEntry> getChildren() {
        return fChildren;
    }

    @Override
    public String getName() {
        return fName;
    }

    /**
     * Update the entry name
     *
     * @param name
     *            the updated entry name
     */
    public void setName(String name) {
        fName = name;
    }

    @Override
    public long getStartTime() {
        return fStartTime;
    }

    @Override
    public long getEndTime() {
        return fEndTime;
    }

    /**
     * Updates the end time
     *
     * @param endTime
     *            the end time
     *
     * @since 3.0
     */
    public void updateEndTime(long endTime) {
        fEndTime = Math.max(endTime, fEndTime);
    }

    @Override
    public boolean hasTimeEvents() {
        return true;
    }

    @Override
    public Iterator<ITimeEvent> getTimeEventsIterator() {
        if (hasTimeEvents()) {
            return new EventIterator(fEventList, fZoomedEventList);
        }
        return null;
    }

    @Override
    public Iterator<ITimeEvent> getTimeEventsIterator(long startTime, long stopTime, long visibleDuration) {
        if (!hasTimeEvents()) {
            return null;
        }
        return new EventIterator(fEventList, fZoomedEventList, startTime, stopTime);
    }

    /**
     * Add an event to this entry's event list. If necessary, update the start
     * and end time of the entry. If the event list's last event starts at the
     * same time as the event to add, it is replaced by the new event.
     *
     * @param event
     *            The time event to add
     */
    public void addEvent(ITimeEvent event) {
        long start = event.getTime();
        long end = start + event.getDuration();
        synchronized (fEventList) {
            int lastIndex = fEventList.size() - 1;
            if (lastIndex >= 0 && fEventList.get(lastIndex).getTime() == event.getTime()) {
                fEventList.set(lastIndex, event);
            } else {
                fEventList.add(event);
            }
            if (fStartTime == -1 || start < fStartTime) {
                fStartTime = start;
            }
            if (fEndTime == -1 || end > fEndTime) {
                fEndTime = end;
            }
        }
    }

    /**
     * Set the general event list of this entry.
     *
     * @param eventList
     *            The list of time events
     */
    public void setEventList(List<ITimeEvent> eventList) {
        if (eventList != null) {
            fEventList = new ArrayList<>(eventList);
        } else {
            fEventList = new ArrayList<>();
        }
    }

    /**
     * Set the zoomed event list of this entry.
     *
     * @param eventList
     *            The list of time events
     */
    public void setZoomedEventList(List<ITimeEvent> eventList) {
        if (eventList != null) {
            fZoomedEventList = new ArrayList<>(eventList);
        } else {
            fZoomedEventList = new ArrayList<>();
        }
    }

    /**
     * Add a child entry to this one
     *
     * @param child
     *            The child entry
     */
    /*
     * TODO: This method can be removed in the next major API version.
     */
    public void addChild(TimeGraphEntry child) {
        child.fParent = this;
        fChildren.add(child);
    }

    /**
     * Add a child entry to this one
     *
     * @param child
     *            The child entry
     * @since 3.1
     */
    public void addChild(ITimeGraphEntry child) {
        if (child instanceof TimeGraphEntry) {
            ((TimeGraphEntry) child).fParent = this;
        }
        fChildren.add(child);
    }

    /**
     * Add a child entry to this one at the specified position
     *
     * @param index
     *            Index at which the specified entry is to be inserted
     * @param child
     *            The child entry
     * @since 3.1
     */
    public void addChild(int index, ITimeGraphEntry child) {
        if (child instanceof TimeGraphEntry) {
            ((TimeGraphEntry) child).fParent = this;
        }
        fChildren.add(index, child);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '(' + fName + ')';
    }

}
