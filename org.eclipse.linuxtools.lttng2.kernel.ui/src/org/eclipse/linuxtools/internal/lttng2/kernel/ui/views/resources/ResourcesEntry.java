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

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.resources;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.linuxtools.lttng2.kernel.core.trace.CtfKernelTrace;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

public class ResourcesEntry implements ITimeGraphEntry {
    private CtfKernelTrace fTrace;
    private ITimeGraphEntry fParent = null;
    private ITimeGraphEntry[] children = null;
    private String fName;
    private long fStartTime;
    private long fEndTime;
    List<ITimeEvent> list = new LinkedList<ITimeEvent>();

    public ResourcesEntry(ITimeGraphEntry parent, CtfKernelTrace trace, String cpuName) {
        fParent = parent;
        fTrace = trace;
        fName = cpuName;
    }

    @Override
    public ITimeGraphEntry getParent() {
        return fParent;
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
}
