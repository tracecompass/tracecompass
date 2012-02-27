/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon - Initial API and implementation
 * 	 Michel Dagenais (michel.dagenais@polymtl.ca) - Reference C implementation, used with permission
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.resources.model;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.linuxtools.lttng.core.TraceDebug;
import org.eclipse.linuxtools.lttng.ui.model.trange.ItemContainer;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeEventResource;
import org.eclipse.linuxtools.lttng.ui.model.trange.TimeRangeEventResource.ResourceTypes;

/**
 * Common location to allocate the resources in use by the resource view
 * 
 * @author alvaro
 * 
 */
public class ResourceContainer implements ItemContainer<TimeRangeEventResource> {
	// ========================================================================
	// Data
	// ========================================================================
	private final HashMap<ResourceKey, TimeRangeEventResource> resources = new HashMap<ResourceKey, TimeRangeEventResource>();
	private static Integer uniqueId = 0;
	
	
	// ========================================================================
	// Constructor
	// ========================================================================
	/**
	 * Package level constructor
	 */
	public ResourceContainer() { }
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.linuxtools.lttng.ui.views.resources.model.ItemContainer#addItem
	 * (org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.
	 * ITmfTimeAnalysisEntry)
	 */
	@Override
	public void addItem(TimeRangeEventResource newItem) {
		if (newItem != null) {
		    resources.put( new ResourceKey(newItem),newItem);
		}
	}
	
	// ========================================================================
	// Methods
	// ========================================================================
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.ui.views.resources.model.ItemContainer#getUniqueId()
	 */
    @Override
	public Integer getUniqueId() {
        return uniqueId++;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.ui.views.resources.model.ItemContainer#readItems()
	 */
	@Override
	public TimeRangeEventResource[] readItems() {
		return resources.values().toArray(
				new TimeRangeEventResource[resources.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.ui.views.resources.model.ItemContainer#clearChildren()
	 */
	@Override
	public void clearChildren() {
	    TimeRangeEventResource newRes = null;
        Iterator<ResourceKey> iterator = resources.keySet().iterator();
        
        while (iterator.hasNext()) {
			newRes = resources.get(iterator.next());
			newRes.reset();
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.ui.views.resources.model.ItemContainer#clearItems()
	 */
	@Override
	public void clearItems() {
		resources.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.lttng.ui.views.resources.model.ItemContainer#removeItems(java.lang.String)
	 */
	@Override
	public void removeItems(String traceId) {
	    ResourceKey newKey = null;

	    Iterator<ResourceKey> iterator = resources.keySet().iterator();
        while (iterator.hasNext()) {
            newKey = iterator.next();
            
            if (resources.get(newKey).getTraceId().equals(traceId)) {
                resources.remove(newKey);
            }
        }
	}
	
	
	/**
	 * Search by keys (resourceId, traceId and type)
	 * <p>
	 * 
	 * A match is returned if the three arguments received match an entry
	 * Otherwise null is returned
	 * 
	 * @param searchedId
	 *            The ressourceId we are looking for
	 * @param searchedType
	 *            The ressourceType we are looking for
	 * @param searchedTraceId
	 *            The traceId (trace name?) we are looking for
	 * 
	 * @return TimeRangeEventResource
	 */
    public TimeRangeEventResource findItem(Long searchedId, ResourceTypes searchedType, String searchedTraceId) {
		// Get the EventResource associated to a key we create here
        TimeRangeEventResource foundResource = resources.get( new ResourceKey(searchedId, searchedTraceId, searchedType) );
        
        return foundResource;
    }
}

class ResourceKey {
    
    private TimeRangeEventResource valueRef = null;
    
    private Long       resourceId = null;
    private String        traceId = null;
    private ResourceTypes type = null;
    
    @SuppressWarnings("unused")
    private ResourceKey() { }
    
    public ResourceKey(TimeRangeEventResource newRef) {
        valueRef = newRef;
    }
    
    public ResourceKey(Long newId, String newTraceId, ResourceTypes newType) {
        resourceId = newId;
        traceId = newTraceId;
        type = newType;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null)
            return false;
        
        boolean isSame = false;
        
        if ( obj instanceof ResourceKey ) {
        	if ( valueRef != null )  {
	            if ( (  ((ResourceKey)obj).getResourceId().equals(valueRef.getResourceId()) ) &&
	                 (  ((ResourceKey)obj).getTraceId().equals(valueRef.getTraceId()) ) &&
	                 (  ((ResourceKey)obj).getType().equals(valueRef.getType()) ) )
	            {
	                isSame = true;
	            }
        	}
        	else {
        		if ( (  ((ResourceKey)obj).getResourceId().equals(this.resourceId)) &&
   	                 (  ((ResourceKey)obj).getTraceId().equals(this.traceId)) &&
   	                 (  ((ResourceKey)obj).getType().equals(this.type)) )
   	            {
   	                isSame = true;
   	            }
        	}
        }
        else {
        	TraceDebug.debug("ERROR : The given key is not of the type ProcessKey!" + obj.getClass().toString()); //$NON-NLS-1$
        }
        
        return isSame;
    }
    
    // *** WARNING : Everything in there work because the check "valueRef != null" is the same for ALL getter
    // Do NOT change this check without checking.
    public Long getResourceId() {
        if ( valueRef != null ) {
            return valueRef.getResourceId();
        }
        else {
            return resourceId;
        }
    }

    public String getTraceId() {
        if ( valueRef != null ) {
            return valueRef.getTraceId();
        }
        else {
            return traceId;
        }
    }

    public ResourceTypes getType() {
        if ( valueRef != null ) {
            return valueRef.getType();
        }
        else {
            return type;
        }
    }

    @Override
    public int hashCode() { 
        return this.toString().hashCode();
    }
    
    
    @Override
	@SuppressWarnings("nls")
	public String toString() {
        if ( valueRef != null ) {
            return (valueRef.getResourceId().toString() + ":" + valueRef.getTraceId().toString() + ":" + valueRef.getType().toString());
        }
        return (resourceId + ":" + traceId + ":" + type);
    }
}
