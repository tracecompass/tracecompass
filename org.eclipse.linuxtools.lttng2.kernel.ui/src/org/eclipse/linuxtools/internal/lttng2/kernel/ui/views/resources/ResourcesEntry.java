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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.common.EventIterator;
import org.eclipse.linuxtools.lttng2.kernel.core.trace.CtfKernelTrace;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

public class ResourcesEntry implements ITimeGraphEntry {
    public static enum Type { NULL, CPU, IRQ, SOFT_IRQ };

    private int fQuark;
    private CtfKernelTrace fTrace;
    private ITimeGraphEntry fParent = null;
    private ITimeGraphEntry[] children = null;
    private String fName;
    private Type fType;
    private int fId;
    private long fStartTime;
    private long fEndTime;
    private List<ITimeEvent> fEventList = new ArrayList<ITimeEvent>();
    private List<ITimeEvent> fZoomedEventList = null;

    public ResourcesEntry(int quark, CtfKernelTrace trace, Type type, int id) {
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
    public long getEndTime() {
        return fEndTime;
    }

    @Override
    public Iterator<ITimeEvent> getTimeEventsIterator() {
        return new EventIterator(fEventList, fZoomedEventList);
    }

    @Override
    public Iterator<ITimeEvent> getTimeEventsIterator(long startTime, long stopTime, long visibleDuration) {
        return new EventIterator(fEventList, fZoomedEventList, startTime, stopTime);
    }

    public void setParent(ITimeGraphEntry parent) {
        fParent = parent;
    }

    public int getQuark() {
        return fQuark;
    }

    public CtfKernelTrace getTrace() {
        return fTrace;
    }

    public Type getType() {
        return fType;
    }

    public int getId() {
        return fId;
    }

    public void setEventList(List<ITimeEvent> eventList) {
        fEventList = eventList;
        if (eventList != null && eventList.size() > 0) {
            fStartTime = eventList.get(0).getTime();
            ITimeEvent lastEvent = eventList.get(eventList.size() - 1);
            fEndTime = lastEvent.getTime() + lastEvent.getDuration();
        }
    }

    public void setZoomedEventList(List<ITimeEvent> eventList) {
        fZoomedEventList = eventList;
    }
}
