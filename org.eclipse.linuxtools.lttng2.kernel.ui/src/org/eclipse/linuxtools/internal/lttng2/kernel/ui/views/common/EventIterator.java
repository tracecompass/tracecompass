/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.common;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeEvent;

/**
 * An iterator for time events
 */
public class EventIterator implements Iterator<ITimeEvent> {

    private final long fStartTime;
    private final long fEndTime;
    private List<ITimeEvent> fEventList;
    private List<ITimeEvent> fZoomedEventList;
    private long fZoomedStartTime;
    private long fZoomedEndTime;
    private int fIndex = 0;
    private int fZoomedIndex= 0;
    private ITimeEvent fNext = null;
    private ITimeEvent fZoomedNext = null;

    /**
     * Basic constructor, with start time and end times equal to the lowest and
     * highest values possible, respectively.
     *
     * @param eventList
     *            The list on which this iterator will iterate
     * @param zoomedEventList
     *            The "zoomed" list
     */
    public EventIterator(List<ITimeEvent> eventList, List<ITimeEvent> zoomedEventList) {
        this(eventList, zoomedEventList, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    /**
     * Complete constructor, where we specify start and end times.
     *
     * @param eventList
     *            The list on which this iterator will iterate
     * @param zoomedEventList
     *            The "zoomed" list
     * @param startTime
     *            The start time
     * @param endTime
     *            The end time
     */
    public EventIterator(List<ITimeEvent> eventList,
            List<ITimeEvent> zoomedEventList, long startTime, long endTime) {
        fEventList = eventList;
        fZoomedEventList = zoomedEventList;
        if (zoomedEventList != null && zoomedEventList.size() > 0) {
            fZoomedStartTime = zoomedEventList.get(0).getTime();
            ITimeEvent lastEvent = zoomedEventList.get(zoomedEventList.size() - 1);
            fZoomedEndTime = lastEvent.getTime() + lastEvent.getDuration();
        } else {
            fZoomedStartTime = Long.MAX_VALUE;
            fZoomedEndTime = Long.MIN_VALUE;
        }
        fStartTime = startTime;
        fEndTime = endTime;
    }

    @Override
    public boolean hasNext() {
        if (fNext == null && fEventList != null) {
            while (fIndex < fEventList.size()) {
                ITimeEvent event = fEventList.get(fIndex++);
                if (event.getTime() + event.getDuration() >= fStartTime && event.getTime() <= fEndTime) {
                    fNext = event;
                    break;
                }
            }
            if (fNext == null) {
                fEventList = null;
            }
        }

        if (fZoomedNext == null && fZoomedEventList != null) {
            while (fZoomedIndex < fZoomedEventList.size()) {
                ITimeEvent event = fZoomedEventList.get(fZoomedIndex++);
                if (event.getTime() + event.getDuration() >= fStartTime && event.getTime() <= fEndTime) {
                    fZoomedNext = event;
                    break;
                }
            }
            if (fZoomedNext == null) {
                fZoomedEventList = null;
            }
        }

        return fNext != null || fZoomedNext != null;
    }

    @Override
    public ITimeEvent next() {
        if (hasNext()) {
            if (fZoomedNext != null && (fNext == null || fZoomedNext.getTime() <= fNext.getTime())) {
                if (fNext != null && fNext.getTime() == fZoomedNext.getTime()) {
                    long duration = fNext.getTime() + fNext.getDuration() - fZoomedEndTime;
                    if (duration > 0) {
                        fNext = new TimeEvent(fNext.getEntry(), fZoomedEndTime, duration);
                    } else {
                        fNext = null;
                    }
                }
                ITimeEvent event = fZoomedNext;
                fZoomedNext = null;
                return event;
            }
            if (fNext.getTime() < fZoomedEndTime && fNext.getTime() + fNext.getDuration() > fZoomedStartTime) {
                ITimeEvent event = new TimeEvent(fNext.getEntry(), fNext.getTime(), fZoomedStartTime - fNext.getTime());
                long duration = fNext.getTime() + fNext.getDuration() - fZoomedEndTime;
                if (duration > 0) {
                    fNext = new TimeEvent(fNext.getEntry(), fZoomedEndTime, duration);
                } else {
                    fNext = null;
                }
                return event;
            }
            ITimeEvent event = fNext;
            fNext = null;
            return event;
        }
        throw new NoSuchElementException();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
