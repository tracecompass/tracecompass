package org.eclipse.linuxtools.tmf.ui.widgets.timeAnalysis.test.stub.model;

import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITmfTimeAnalysisEntry;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.TimeEvent;

public class EventImpl extends TimeEvent {
	public static enum Type {ERROR, WARNING, TIMEADJUSTMENT, ALARM, EVENT, INFORMATION, UNKNOWN, INFO1, INFO2, INFO3, INFO4, INFO5, INFO6, INFO7, INFO8, INFO9}

	private long time = 0;
	private ITmfTimeAnalysisEntry trace = null;
	private Type myType = Type.UNKNOWN; 
	private long duration; 
	
	public EventImpl(long time, ITmfTimeAnalysisEntry trace, Type type) {
		this.time = time;
		this.trace = trace;
		this.myType = type;
		this.setDuration(super.getDuration());
	}
	
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
	public long getDuration() {
		return duration;
	}

}
