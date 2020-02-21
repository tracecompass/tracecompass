/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.test.stub.model;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

@SuppressWarnings("javadoc")
public class TraceImpl implements ITimeGraphEntry {

    // ========================================================================
    // Data
    // ========================================================================

    private String name = "traceDefaultName";
    private long startTime = 0;
    private long stopTime = 1;
    private String className = "defaultClassName";
    private Vector<@NonNull ITimeEvent> traceEvents = new Vector<>();

    // ========================================================================
    // Constructor
    // ========================================================================

    public TraceImpl(String name, long sTime, long stopTime, String className) {
        this.name = name;
        this.startTime = sTime;
        this.stopTime = stopTime;
        this.className = className;
    }

    // ========================================================================
    // Methods
    // ========================================================================

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public long getEndTime() {
        return stopTime;
    }

    @Override
    public boolean hasTimeEvents() {
        return traceEvents != null;
    }

    @Override
    public Iterator<@NonNull ITimeEvent> getTimeEventsIterator() {
        return traceEvents.iterator();
    }

    @Override
    public Iterator<@NonNull ITimeEvent> getTimeEventsIterator(long aStartTime, long aStopTime, long maxDuration) {
        return traceEvents.iterator();
    }

    public void addTraceEvent(@NonNull ITimeEvent event) {
        traceEvents.add(event);
    }

    @Override
    public List<@NonNull ITimeGraphEntry> getChildren() {
        return null;
    }

    @Override
    public ITimeGraphEntry getParent() {
        return null;
    }

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public boolean matches(@NonNull  Pattern pattern) {
        return pattern.matcher(name).find();
    }

}
