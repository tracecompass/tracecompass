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

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.controlflow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.linuxtools.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.EventIterator;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

/**
 * An entry in the Control Flow view
 */
public class ControlFlowEntry implements ITimeGraphEntry {
    private final int fThreadQuark;
    private final LttngKernelTrace fTrace;
    private ControlFlowEntry fParent = null;
    private final ArrayList<ControlFlowEntry> fChildren = new ArrayList<ControlFlowEntry>();
    private String fName;
    private final int fThreadId;
    private final int fParentThreadId;
    private long fStartTime = -1;
    private long fEndTime = -1;
    private List<ITimeEvent> fEventList = new ArrayList<ITimeEvent>();
    private List<ITimeEvent> fZoomedEventList = null;

    /**
     * Constructor
     *
     * @param threadQuark
     *            The attribute quark matching the thread
     * @param trace
     *            The trace on which we are working
     * @param execName
     *            The exec_name of this entry
     * @param threadId
     *            The TID of the thread
     * @param parentThreadId
     *            the Parent_TID of this thread
     * @param startTime
     *            The start time of this process's lifetime
     * @param endTime
     *            The end time of this process
     */
    public ControlFlowEntry(int threadQuark, LttngKernelTrace trace, String execName, int threadId, int parentThreadId, long startTime, long endTime) {
        fThreadQuark = threadQuark;
        fTrace = trace;
        fName = execName;
        fThreadId = threadId;
        fParentThreadId = parentThreadId;
        fStartTime = startTime;
        fEndTime = endTime;
    }

    @Override
    public ITimeGraphEntry getParent() {
        return fParent;
    }

    @Override
    public boolean hasChildren() {
        return fChildren.size() > 0;
    }

    @Override
    public List<ControlFlowEntry> getChildren() {
        return fChildren;
    }

    @Override
    public String getName() {
        return fName;
    }

    /**
     * Update the entry name
     * @param execName the updated entry name
     */
    public void setName(String execName) {
        fName = execName;
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
     * Get the quark of the attribute matching this thread's TID
     *
     * @return The quark
     */
    public int getThreadQuark() {
        return fThreadQuark;
    }

    /**
     * Get the CTF trace object
     *
     * @return The trace
     */
    public LttngKernelTrace getTrace() {
        return fTrace;
    }

    /**
     * Get this entry's thread ID
     *
     * @return The TID
     */
    public int getThreadId() {
        return fThreadId;
    }

    /**
     * Get this thread's parent TID
     *
     * @return The "PTID"
     */
    public int getParentThreadId() {
        return fParentThreadId;
    }

    /**
     * Add an event to this process's timeline
     *
     * @param event
     *            The time event
     */
    public void addEvent(ITimeEvent event) {
        long start = event.getTime();
        long end = start + event.getDuration();
        synchronized (fEventList) {
            fEventList.add(event);
            if (fStartTime == -1 || start < fStartTime) {
                fStartTime = start;
            }
            if (fEndTime == -1 || end > fEndTime) {
                fEndTime = end;
            }
        }
    }

    /**
     * Set the general event list of this entry
     *
     * @param eventList
     *            The list of time events
     */
    public void setEventList(List<ITimeEvent> eventList) {
        fEventList = eventList;
    }

    /**
     * Set the zoomed event list of this entry
     *
     * @param eventList
     *            The list of time events
     */
    public void setZoomedEventList(List<ITimeEvent> eventList) {
        fZoomedEventList = eventList;
    }

    /**
     * Add a child entry to this one (to show relationships between processes as
     * a tree)
     *
     * @param child
     *            The child entry
     */
    public void addChild(ControlFlowEntry child) {
        child.fParent = this;
        fChildren.add(child);
    }
}
