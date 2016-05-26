/*******************************************************************************
 * Copyright (c) 2012, 2016 Ericsson, École Polytechnique de Montréal
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

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.SWT;

import com.google.common.collect.Iterables;

/**
 * An entry for use in the time graph views
 */
public class TimeGraphEntry implements ITimeGraphEntry {

    /** Entry's parent */
    private TimeGraphEntry fParent = null;

    /** List of child entries */
    private final List<@NonNull TimeGraphEntry> fChildren = new CopyOnWriteArrayList<>();

    /** Name of this entry (text to show) */
    private String fName;
    private long fStartTime = SWT.DEFAULT;
    private long fEndTime = SWT.DEFAULT;
    private @NonNull List<ITimeEvent> fEventList = new ArrayList<>();
    private @NonNull List<ITimeEvent> fZoomedEventList = new ArrayList<>();
    private Comparator<ITimeGraphEntry> fComparator;

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

    /**
     * @since 2.0
     */
    @Override
    public TimeGraphEntry getParent() {
        return fParent;
    }

    /**
     * Sets the entry's parent
     *
     * @param entry The new parent entry
     * @since 2.0
     */
    public void setParent(TimeGraphEntry entry) {
        fParent = entry;
    }

    @Override
    public synchronized boolean hasChildren() {
        return !fChildren.isEmpty();
    }

    @Override
    public synchronized List<@NonNull TimeGraphEntry> getChildren() {
        return fChildren;
    }

    /**
     * Clear the children of the entry
     *
     * @since 2.0
     */
    public synchronized void clearChildren() {
        fChildren.clear();
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
     */
    public void updateEndTime(long endTime) {
        fEndTime = Math.max(endTime, fEndTime);
    }

    @Override
    public boolean hasTimeEvents() {
        return true;
    }

    @Override
    public Iterator<@NonNull ITimeEvent> getTimeEventsIterator() {
        if (hasTimeEvents()) {
            return new EventIterator(fEventList, fZoomedEventList);
        }
        return null;
    }

    @Override
    public Iterator<@NonNull ITimeEvent> getTimeEventsIterator(long startTime, long stopTime, long visibleDuration) {
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
        int lastIndex = fEventList.size() - 1;
        if (lastIndex >= 0 && fEventList.get(lastIndex).getTime() == event.getTime()) {
            fEventList.set(lastIndex, event);
        } else {
            fEventList.add(event);
        }
        updateEntryBounds(event);
    }

    /**
     * Set the general event list of this entry. The list should be modifiable
     * but will only increase in size over time.
     *
     * @param eventList
     *            The modifiable list of time events, or null to clear the list
     */
    public void setEventList(List<ITimeEvent> eventList) {
        if (eventList != null) {
            fEventList = eventList;
        } else {
            fEventList = new ArrayList<>();
        }
    }

    /**
     * Set the zoomed event list of this entry. The list should be modifiable
     * but will only increase in size over time.
     *
     * @param eventList
     *            The modifiable list of time events, or null to clear the list
     */
    public void setZoomedEventList(List<ITimeEvent> eventList) {
        if (eventList != null) {
            fZoomedEventList = eventList;
        } else {
            fZoomedEventList = new ArrayList<>();
        }
    }

    /**
     * Add an event to this entry's zoomed event list. If necessary, update the
     * start and end time of the entry. If the zoomed event list's last event
     * starts at the same time as the event to add, it is replaced by the new
     * event. If the new event starts before the zoomed event list's last event,
     * the new event is ignored and is assumed to be already part of the list.
     * If the new event starts before the zoomed event list's first event, the
     * list is assumed to be incomplete and is cleared, and the event is added.
     *
     * @param event
     *            The time event to add
     * @since 1.1
     */
    public void addZoomedEvent(ITimeEvent event) {
        long start = event.getTime();
        int lastIndex = fZoomedEventList.size() - 1;
        long lastStart = lastIndex >= 0 ? fZoomedEventList.get(lastIndex).getTime() : Long.MIN_VALUE;
        if (start > lastStart) {
            fZoomedEventList.add(event);
        } else if (start == lastStart) {
            fZoomedEventList.set(lastIndex, event);
        } else if (start < fZoomedEventList.get(0).getTime()) {
            fZoomedEventList.clear();
            fZoomedEventList.add(event);
        }
        updateEntryBounds(event);
    }

    private void updateEntryBounds(ITimeEvent event) {
        if (event instanceof NullTimeEvent) {
            /* A NullTimeEvent should not affect the entry bounds */
            return;
        }
        long start = event.getTime();
        if (fStartTime == SWT.DEFAULT || start < fStartTime) {
            fStartTime = start;
        }
        long end = start + event.getDuration();
        if (fEndTime == SWT.DEFAULT || end > fEndTime) {
            fEndTime = end;
        }
    }

    /**
     * Method to insert event to entry even if it isn't the last
     *
     * @param event
     *            event to insert
     * @param zoom
     *            insert in ZoomedEventList if true, else insert in EventList
     * @since 3.1
     */
    public void insertEvent(ITimeEvent event, boolean zoom) {
        List<ITimeEvent> eventList = zoom ? fZoomedEventList : fEventList;
        if (eventList.isEmpty() || Iterables.getLast(eventList).getTime() < event.getTime()) {
            /* Optimize for most common case, when the event is after the events in the list. */
            eventList.add(event);
        } else {
            int insertionIndex = Collections.binarySearch(eventList, event, Comparator.comparing(ITimeEvent::getTime));
            if (insertionIndex >= 0) {
                /*
                 * This can happen as we are only comparing event start times, update the
                 * eventList with the more recent one.
                 */
                eventList.set(insertionIndex, event);
            } else {
                insertionIndex = -insertionIndex - 1;
                eventList.add(insertionIndex, event);
            }
        }
        updateEntryBounds(event);
    }

    /**
     * Method to add time events between gaps in the zoomed events list.
     *
     * @since 3.1
     */
    public void fillZoomedEventList() {
        List<ITimeEvent> incomplete = fZoomedEventList;
        List<ITimeEvent> full = new ArrayList<>(incomplete.size());
        ITimeEvent prev = null;
        for (ITimeEvent event : incomplete) {
            if (prev != null) {
                long prevEnd = prev.getTime() + prev.getDuration();
                if (prevEnd < event.getTime()) {
                    full.add(new TimeEvent(this, prevEnd, event.getTime() - prevEnd));
                }
            }
            prev = event;
            full.add(event);
        }
        fZoomedEventList = full;
    }

    /**
     * Add a child entry to this one. If a comparator was previously set with
     * {@link #sortChildren(Comparator)}, the entry will be inserted in its
     * sort-order position. Otherwise it will be added to the end of the list.
     *
     * @param child
     *            The child entry
     */
    public synchronized void addChild(@NonNull TimeGraphEntry child) {
        if (fComparator == null) {
            addChild(fChildren.size(), child);
        } else {
            int i = Collections.binarySearch(fChildren, child, fComparator);
            /* Deal with negative insertion points from binarySearch */
            i = i >= 0 ? i : -i - 1;
            addChild(i, child);
        }
    }

    /**
     * Add a child entry to this one at the specified position
     *
     * @param index
     *            Index at which the specified entry is to be inserted
     * @param child
     *            The child entry
     * @since 2.0
     */
    public synchronized void addChild(int index, @NonNull TimeGraphEntry child) {
        if (child.getParent() == this) {
            return;
        }
        if (child.getParent() != null) {
            child.getParent().removeChild(child);
        }
        child.setParent(this);
        fChildren.add(index, child);
    }

    /**
     * Remove a child entry from this one.
     *
     * @param child
     *            The child entry
     * @since 2.0
     */
    public synchronized void removeChild(@NonNull TimeGraphEntry child) {
        if (child.getParent() == this) {
            child.setParent(null);
        }
        fChildren.remove(child);
    }

    /**
     * Sort the children of this entry using the provided comparator. Subsequent
     * calls to {@link #addChild(TimeGraphEntry)} will use this comparator to
     * maintain the sort order.
     *
     * @param comparator
     *            The entry comparator
     */
    public synchronized void sortChildren(Comparator<ITimeGraphEntry> comparator) {
        fComparator = comparator;
        if (comparator == null) {
            return;
        }
        List<@NonNull TimeGraphEntry> copy = new ArrayList<>(fChildren);
        copy.sort(comparator);
        fChildren.clear();
        fChildren.addAll(copy);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '(' + fName + ')';
    }

    /**
     * @since 2.0
     */
    @Override
    public boolean matches(@NonNull Pattern pattern) {
        // Default implementation
        return pattern.matcher(fName).find();
    }

}
