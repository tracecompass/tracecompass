package org.eclipse.linuxtools.tmf.ui.widgets.timeAnalysis.test.stub.model;

import java.util.Vector;

import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITmfTimeAnalysisEntry;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.TimeEvent;


public class TraceImpl implements ITmfTimeAnalysisEntry {
	private int id = 0;
	private String name = "traceDefaultName";
	private long startTime = 0;
	private long stopTime = 1;
	private String groupName = "defaultGroupName";
	private String className = "defaultClassName";
	
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	private Vector<TimeEvent> traceEvents = new Vector<TimeEvent>();

	public TraceImpl(int id, String name, long sTime, long stopTime,
			String groupName, String className) {
		this.id = id;
		this.name = name;
		this.startTime = sTime;
		this.stopTime = stopTime;
		this.groupName = groupName;
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

	public String getGroupName() {
		return groupName;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getStopTime() {
		return stopTime;
	}

	@SuppressWarnings("unchecked")
	public Vector<TimeEvent> getTraceEvents() {
		return traceEvents;
	}

}
