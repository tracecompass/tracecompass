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

import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITmfTimeAnalysisEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeEvent;

public class EventImpl extends TimeEvent {
	// ========================================================================
	// Data
	// ========================================================================
	public static enum Type {ERROR, WARNING, TIMEADJUSTMENT, ALARM, EVENT, INFORMATION, UNKNOWN, INFO1, INFO2, INFO3, INFO4, INFO5, INFO6, INFO7, INFO8, INFO9}

	private long time = 0;
	private ITmfTimeAnalysisEntry trace = null;
	private Type myType = Type.UNKNOWN; 
	private long duration; 
	
	// ========================================================================
	// Constructor
	// ========================================================================
	public EventImpl(long time, ITmfTimeAnalysisEntry trace, Type type) {
		this.time = time;
		this.trace = trace;
		this.myType = type;
		this.setDuration(super.getDuration());
	}
	
	// ========================================================================
	// Methods
	// ========================================================================
	public Type getType() {
		return myType;
	}

	public void setType(Type myType) {
		this.myType = myType;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public void setTrace(ITmfTimeAnalysisEntry trace) {
		this.trace = trace;
	}
	
	@Override
	public long getTime() {
		return time;
	}

	@Override
	public ITmfTimeAnalysisEntry getEntry() {
		return trace;
	}

	/**
	 * @param duration the duration to set
	 */
	public void setDuration(long duration) {
		this.duration = duration;
	}

	/**
	 * @return the duration
	 */
	@Override
	public long getDuration() {
		return duration;
	}

}
