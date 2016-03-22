/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.resources;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;

/**
 * Caching iterator of time events with a couple extras.
 * <ul>
 * <li>peek() allows reading the head without removing it</li>
 * <li>trim() allows removing a bit of the first event</li>
 * </ul>
 *
 * @author Matthew Khouzam
 */
class CachingIterator implements Iterator<@NonNull ITimeEvent>, Comparable<CachingIterator> {
    private ITimeEvent fEvent;
    private @NonNull Iterator<@NonNull ? extends ITimeEvent> fIterator;
    private final Comparator<ITimeEvent> fComparator;

    public CachingIterator(@NonNull Iterator<@NonNull ? extends ITimeEvent> iterator, Comparator<ITimeEvent> comparator) {
        fIterator = iterator;
        fComparator = comparator;
        fEvent = iterator.hasNext() ? iterator.next() : null;
    }

    @Override
    public ITimeEvent next() {
        ITimeEvent retVal = fEvent;
        fEvent = fIterator.hasNext() ? fIterator.next() : null;
        if (retVal == null) {
            throw new NoSuchElementException("Iterator is empty"); //$NON-NLS-1$
        }
        return retVal;
    }

    @Override
    public boolean hasNext() {
        return fEvent != null;
    }

    /**
     * Retrieves, but does not remove, the next element of this iterator, or
     * returns {@code null} if this iterator does not have a next.
     *
     * @return the next element of the iterator
     */
    public ITimeEvent peek() {
        return fEvent;
    }

    @Override
    public int compareTo(CachingIterator o) {
        final ITimeEvent myEvent = peek();
        final ITimeEvent otherEvent = o.peek();
        return fComparator.compare(myEvent, otherEvent);
    }

    /**
     * Trims the next element in the iterator to be after a cut-off time.
     *
     * @param time
     *            the cut-off time
     * @return true if there was a trim
     */
    public boolean trim(long time) {
        if (time <= fEvent.getTime()) {
            return false;
        }
        if (time < fEvent.getTime() + fEvent.getDuration()) {
            fEvent = fEvent.splitAfter(time);
            return true;
        }
        fEvent = fIterator.hasNext() ? fIterator.next() : null;
        return true;
    }

}