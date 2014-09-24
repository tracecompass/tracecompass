/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alvaro Sanchez-Leon - Initial API and implementation
 *   Patrick Tasse - Refactoring
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model;

import java.util.Iterator;
import java.util.List;

/**
 * Interface for an entry (row) in the time graph view
 *
 * @version 1.0
 * @author Alvaro Sanchez-Leon
 * @author Patrick Tasse
 */
public interface ITimeGraphEntry {

    /**
     * Returns the parent of this entry, or <code>null</code> if it has none.
     *
     * @return the parent element, or <code>null</code> if it has none
     */
    ITimeGraphEntry getParent();

    /**
     * Returns whether this entry has children.
     *
     * @return <code>true</code> if the given element has children,
     *  and <code>false</code> if it has no children
     */
    boolean hasChildren();

    /**
     * Returns the child elements of this entry.
     *
     * @return an array of child elements
     * @since 2.0
     */
    List<? extends ITimeGraphEntry> getChildren();

    /**
     * Returns the name of this entry.
     *
     * @return the entry name
     */
    String getName();

    /**
     * Returns the start time of this entry in nanoseconds.
     *
     * @return the start time
     */
    long getStartTime();

    /**
     * Returns the end time of this entry in nanoseconds.
     *
     * @return the end time
     */
    long getEndTime();

    /**
     * Returns whether this entry has time events.
     * If true, the time events iterator should not be null.
     *
     * @return true if the entry has time events
     *
     * @see #getTimeEventsIterator
     * @see #getTimeEventsIterator(long, long, long)
     */
    boolean hasTimeEvents();

    /**
     * Get an iterator which returns all time events.
     *
     * @return the iterator
     */
    <T extends ITimeEvent> Iterator<T> getTimeEventsIterator();

    /**
     * Get an iterator which only returns events that fall within the start time and the stop time.
     * The visible duration is the event duration below which further detail is not discernible.
     * If no such iterator is implemented, provide a basic iterator which returns all events.
     *
     * @param startTime start time in nanoseconds
     * @param stopTime stop time in nanoseconds
     * @param visibleDuration duration of one pixel in nanoseconds
     *
     * @return the iterator
     */
    <T extends ITimeEvent> Iterator<T> getTimeEventsIterator(long startTime, long stopTime, long visibleDuration);
}
