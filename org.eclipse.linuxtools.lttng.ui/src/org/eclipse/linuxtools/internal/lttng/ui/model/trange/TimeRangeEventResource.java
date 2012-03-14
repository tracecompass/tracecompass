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

import org.eclipse.linuxtools.internal.lttng.core.state.model.LttngTraceState;


/**
 * @author alvaro
 * 
 */
public abstract class TimeRangeEventResource extends TimeRangeComposite
		implements
		Comparable<TimeRangeEventResource> {

	// ========================================================================
	// Data
	// =======================================================================
	public static enum ResourceTypes {
		UNKNOWN, IRQ, TRAP, SOFT_IRQ, BDEV, CPU
	}

	private ResourceTypes type = ResourceTypes.UNKNOWN;
	private Long resourceId = null;

	// ========================================================================
	// Constructor
	// =======================================================================
	/**
	 * Constructor<br>
	 * 
	 * @param newId            Id used by the UI
	 * @param newStartTime     normally set to the Trace start time
	 * @param newStopTime      normally set to the Trace end time
	 * @param newName          the name of this resource
	 * @param newGroupName     the group name of this resource. Should be same as the traceId
	 * @param newClassName     the classname of this resource.
	 * @param newType          the type of the resource, as defined in the ResourceTypes enum
	 * @param newResourceId    the resourceId, unique id identifying this resource 
	 * 
	 */
	public TimeRangeEventResource(int newId, long newStartTime,
			long newStopTime, String newName, String newGroupName,
			String newClassName, ResourceTypes newType, Long newResourceId,
			long insertionTime) {

		super(newId, newStartTime, newStopTime, newName, newGroupName,
				newClassName, CompositeType.RESOURCE, insertionTime);

		type = newType;
		resourceId = newResourceId;
	}

	// ========================================================================
	// Methods
	// =======================================================================

	/**
	 * Interface to add children to this resource
	 * 
	 * @param newEvent
	 */
	public void addChildren(TimeRangeEvent newEvent) {
		if ((newEvent != null)) {
			this.ChildEventLeafs.add(newEvent);
		}
	}

	/**
	 * @return
	 */
	public Long getResourceId() {
		return resourceId;
	}

	/**
	 * @param newResId
	 */
	public void setResourceId(Long newResId) {
		this.resourceId = newResId;
	}

	/**
	 * @return
	 */
	public ResourceTypes getType() {
		return type;
	}

	/**
	 * @param type
	 */
	public void setType(ResourceTypes type) {
		this.type = type;
	}

	/**
	 * Getter for traceId.<br>
	 * Note : traceId and groupName are the same for EventResource
	 * 
	 * @return String
	 */
	public String getTraceId() {
		return groupName;
	}

	/**
	 * Getter for traceId.<br>
	 * Note : traceId and groupName are the same for EventResource
	 * 
	 * @return String
	 */
	public void setTraceId(String traceId) {
		this.groupName = traceId;
	}

	// @Override
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
//	@Override
//	public String toString() {
//		return getResourceId().toString() + ":" + getTraceId().toString() + ":"
//				+ getType().toString();
//	}

    @Override
    @SuppressWarnings("nls")
    public String toString() {
		return "[TimeRangeEventResource: " + super.toString() +
		",type=" + type + ",resourceId=" + resourceId + "]";
    }

    /**
	 * Compare function to implement Comparable<br>
	 * <br>
	 * Compare by traceId THEN IF EQUAL by resourceType THEN IF EQUAL by
	 * resourceId
	 * 
	 * @param comparedResource
	 *            The resource to compare to
	 * 
	 * @return int 0 if equals, negative number if "smaller", positive if
	 *         "bigger".
	 */
	// @Override
	@Override
	public int compareTo(TimeRangeEventResource comparedResource) {
		int returnedValue = 0;

		if (comparedResource != null) {
			// Compare by trace id first
			returnedValue = this.getTraceId().compareTo(
					comparedResource.getTraceId());

			// If same, compare by resourceName
			if (returnedValue == 0) {
				returnedValue = this.getName().compareTo(
						comparedResource.getName());

				// Finally, if same, compare by ResourceId
				if (returnedValue == 0) {
					returnedValue = this.getResourceId().compareTo(
							comparedResource.getResourceId());
				}
			}

		}

		return returnedValue;
	}

	public abstract String getStateMode(LttngTraceState traceState);
}
