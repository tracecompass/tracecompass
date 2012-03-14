/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon (alvsan09@gmail.com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.lttng.ui.model.trange;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITmfTimeAnalysisEntry;

public class TimeRangeComposite extends TimeRangeComponent implements
ITmfTimeAnalysisEntry {

	// ========================================================================
	// Data
	// =======================================================================
	/**
	 * Type of Composites or Containers
	 * <p>
	 * PROPERTY: Refers to a sub-composite of a RESOURCE or a PROCESS e.g the
	 * cpu which can vary over time and can have time range events associated to
	 * it, and at the same time PROPERTY is associated to a Composite parent
	 * like a PROCESS
	 * </p>
	 * <p>
	 * PROCESS: A composite of time range events representing a Process
	 * </p>
	 * <p>
	 * RESOURCE: A composite of time range events representing a resource i.g.
	 * irq, softIrq, trap, bdev, cpu
	 * </p>
	 * 
	 * @author alvaro
	 * 
	 */
	public static enum CompositeType {
		UNKNOWN, PROPERTY, PROCESS, RESOURCE
	}

	protected final Vector<TimeRangeComponent> ChildEventLeafs = new Vector<TimeRangeComponent>();
	protected final Vector<TimeRangeComponent> ChildEventComposites = new Vector<TimeRangeComponent>();
	protected Integer id = 0;
	protected String name;
	protected String groupName = ""; //$NON-NLS-1$
	protected String className = ""; //$NON-NLS-1$
	protected CompositeType contType = CompositeType.UNKNOWN;
	protected Long next_good_time = -1L;
	/*Time of first event which trigger the creation of this local resource */
	protected Long insertionTime = -1L; 

	// ========================================================================
	// Constructors
	// =======================================================================
	public TimeRangeComposite(Integer id, Long stime, Long etime, String name,
			CompositeType type, long insertionTime) {
		super(stime, etime, null);
		this.id = id;
		this.name = name;
		contType = type;
		this.insertionTime = insertionTime;
		// Adjust the first good drawing position to the event time creating this resource
		next_good_time = insertionTime;
	}

	public TimeRangeComposite(Integer id, Long stime, Long etime, String name,
			String groupName, String className, CompositeType type,
			long insertionTime) {
		this(id, stime, etime, name, type, insertionTime);
		this.groupName = groupName;
		this.className = className;
    }
	
	// ========================================================================
	// Methods
	// =======================================================================

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeComponent#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.
	 * ITmfTimeAnalysisEntry#getGroupName()
	 */
	@Override
	public String getGroupName() {
		return groupName;
	}

	/**
	 * @param groupName
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.
	 * ITmfTimeAnalysisEntry#getId()
	 */
	@Override
	public int getId() {
		return id;
	}

	/**
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * @param className
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.
	 * ITmfTimeAnalysisEntry#getTraceEvents()
	 */
	@Override
	@SuppressWarnings("unchecked")
	@Deprecated public Vector<TimeRangeComponent> getTraceEvents() {
		return ChildEventLeafs;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Iterator<TimeRangeComponent> getTraceEventsIterator() {
		Vector<TimeRangeComponent> clone = (Vector<TimeRangeComponent>) ChildEventLeafs.clone();
		return clone.iterator();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Iterator<TimeRangeComponent> getTraceEventsIterator(long startTime, long stopTime, long visibleDuration) {
		return getTraceEventsIterator();
	}

	@Override
	public void addTraceEvent(ITimeEvent event) {
		if (event instanceof TimeRangeComponent) {
			ChildEventLeafs.add((TimeRangeComponent) event);
		}
	}

    /**
	 * @return
	 */
	public Vector<TimeRangeComponent> getChildEventComposites() {
		return ChildEventComposites;
	}

	/**
	 * Represents the time where the next time range can start the drawing i.e.
	 * right after previous time range.
	 * 
	 * @return
	 */
	public long getNext_good_time() {
		return next_good_time;
	}

	/**
	 * Represents the time where the next time range can start the drawing i.e.
	 * right after previous time range.
	 * 
	 * @param nextGoodTime
	 */
	public void setNext_good_time(long nextGoodTime) {
		next_good_time = nextGoodTime;
	}

	/**
	 * Reset this resource to the construction state
	 */
	public void reset() {
		getChildEventComposites().clear();
		getTraceEvents().clear();
		next_good_time = insertionTime;
	}

	/**
	 * Event Time reflecting the creation of this local resource e.g. at Reception of Fork, etc.
	 * 
	 * @return
	 */
	public long getInsertionTime() {
		return insertionTime;
	}

    @Override
    @SuppressWarnings("nls")
    public String toString() {
		return "[TimeRangeComposite:" + super.toString() +
		",id=" + id + ",name=" + name + ",group=" + groupName + ",class=" + className +
		",ctype=" + contType + ",itime=" + insertionTime + 
		",leaves=" + ChildEventLeafs + ",composites=" + ChildEventComposites + "]";
    }

}
