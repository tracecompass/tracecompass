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

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.controlflow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.common.EventIterator;
import org.eclipse.linuxtools.lttng2.kernel.core.trace.CtfKernelTrace;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

public class ControlFlowEntry implements ITimeGraphEntry {
    private int fThreadQuark;
    private CtfKernelTrace fTrace;
    private ControlFlowEntry fParent = null;
    private ArrayList<ControlFlowEntry> fChildren = new ArrayList<ControlFlowEntry>();
    private String fName;
    private int fThreadId;
    private int fParentThreadId;
    private long fBirthTime = -1;
    private long fStartTime = -1;
    private long fEndTime = -1;
    private List<ITimeEvent> fEventList = new ArrayList<ITimeEvent>();
    private List<ITimeEvent> fZoomedEventList = null;

    public ControlFlowEntry(int threadQuark, CtfKernelTrace trace, String execName, int threadId, int parentThreadId, long birthTime, long startTime, long endTime) {
        fThreadQuark = threadQuark;
        fTrace = trace;
        fName = execName;
        fThreadId = threadId;
        fParentThreadId = parentThreadId;
        fBirthTime = birthTime;
        fStartTime = startTime;
        fEndTime = endTime;
    }

    @Override
    public ITimeGraphEntry getParent() {
        return fParent;
    }

    @Override
    public boolean hasChildren() {
        return fChildren != null && fChildren.size() > 0;
    }

    @Override
    public ControlFlowEntry[] getChildren() {
        return fChildren.toArray(new ControlFlowEntry[0]);
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

    public int getThreadQuark() {
        return fThreadQuark;
    }

    public CtfKernelTrace getTrace() {
        return fTrace;
    }

    public int getThreadId() {
        return fThreadId;
    }

    public int getParentThreadId() {
        return fParentThreadId;
    }

    public long getBirthTime() {
        return fBirthTime;
    }

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

    public void setEventList(List<ITimeEvent> eventList) {
        fEventList = eventList;
    }

    public void setZoomedEventList(List<ITimeEvent> eventList) {
        fZoomedEventList = eventList;
    }

    public void addChild(ControlFlowEntry child) {
        child.fParent = this;
        fChildren.add(child);
    }
}
