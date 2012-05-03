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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.linuxtools.lttng2.kernel.core.trace.CtfKernelTrace;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

public class ControlFlowEntry implements ITimeGraphEntry, Comparable<ControlFlowEntry> {
    private CtfKernelTrace fTrace;
    private ITimeGraphEntry parent = null;
    private ITimeGraphEntry[] children = null;
    private String fName;
    private int fThreadId;
    private int fPpid;
    private long fStartTime = -1;
    private long fEndTime = -1;
    List<ITimeEvent> list = new LinkedList<ITimeEvent>();

    public ControlFlowEntry(CtfKernelTrace trace, String execName, int threadId, int ppid, long startTime, long endTime) {
        fTrace = trace;
        fName = execName;
        fThreadId = threadId;
        fPpid = ppid;
        fStartTime = startTime;
        fEndTime = endTime;
    }

    @Override
    public ITimeGraphEntry getParent() {
        return parent;
    }

    @Override
    public boolean hasChildren() {
        return children != null && children.length > 0;
    }

    @Override
    public ITimeGraphEntry[] getChildren() {
        return children;
    }

    @Override
    public String getName() {
        return fName;
    }

    public int getThreadId() {
        return fThreadId;
    }

    public int getPPID() {
        return fPpid;
    }

    @Override
    public long getStartTime() {
        return fStartTime;
    }

    @Override
    public long getStopTime() {
        return fEndTime;
    }

    @Override
    public Iterator<ITimeEvent> getTimeEventsIterator() {
        return list.iterator();
    }

    @Override
    public Iterator<ITimeEvent> getTimeEventsIterator(long startTime, long stopTime, long visibleDuration) {
        return getTimeEventsIterator();
    }

    public void addTraceEvent(ITimeEvent event) {
        long time = event.getTime();
        list.add(event);
        if (fStartTime == -1 || time < fStartTime) {
            fStartTime = time;
        }
        if (fEndTime == -1 || time > fEndTime) {
            fEndTime = time;
        }
    }

    @Override
    public int compareTo(ControlFlowEntry other) {
        int result = this.fThreadId < other.fThreadId ? -1 : this.fThreadId > other.fThreadId ? 1 : 0;
        if (result == 0) {
            result = this.fStartTime < other.fStartTime ? -1 : this.fStartTime > other.fStartTime ? 1 : 0;
        }
        return result;
    }

}
