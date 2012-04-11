/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.ui.widgets.timeAnalysis.test.stub.model;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITmfTimeAnalysisEntry;

@SuppressWarnings("nls")
public class TraceImpl implements ITmfTimeAnalysisEntry {
	// ========================================================================
	// Data
	// ========================================================================
	private int id = 0;
	private String name = "traceDefaultName";
	private long startTime = 0;
	private long stopTime = 1;
	private String groupName = "defaultGroupName";
	private String className = "defaultClassName";
	private Vector<ITimeEvent> traceEvents = new Vector<ITimeEvent>();
	
	// ========================================================================
	// Constructor
	// ========================================================================

	public TraceImpl(int id, String name, long sTime, long stopTime,
			String groupName, String className) {
		this.id = id;
		this.name = name;
		this.startTime = sTime;
		this.stopTime = stopTime;
		this.groupName = groupName;
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

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public void setId(int id) {
		this.id = id;
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
	public String getGroupName() {
		return groupName;
	}

	@Override
	public int getId() {
		return id;
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
	public long getStopTime() {
		return stopTime;
	}

	@Override
	@Deprecated public Vector<ITimeEvent> getTraceEvents() {
		return traceEvents;
	}
	
    @Override
    public Iterator<ITimeEvent> getTraceEventsIterator() {
        return traceEvents.iterator();
    }

    @Override
    public Iterator<ITimeEvent> getTraceEventsIterator(long startTime, long stopTime, long maxDuration) {
        return traceEvents.iterator();
    }

    @Override
    public void addTraceEvent(ITimeEvent event) {
        traceEvents.add(event);
    }

}
