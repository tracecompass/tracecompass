/*******************************************************************************
 * Copyright (c) 2011 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Polytechnique Montréal - Initial API and implementation
 *   Bernd Hufmann - Productification, enhancements and fixes
 *   
 *******************************************************************************/
package org.eclipse.linuxtools.lttng.core.tracecontrol.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.linuxtools.lttng.core.LttngConstants;
import org.eclipse.linuxtools.lttng.core.tracecontrol.model.ProviderResource;
import org.eclipse.linuxtools.lttng.core.tracecontrol.model.TargetResource;
import org.eclipse.linuxtools.lttng.core.tracecontrol.model.TraceResource;
import org.eclipse.rse.core.subsystems.AbstractResource;
import org.eclipse.rse.core.subsystems.ISubSystem;

/**
 * <b><u>TargetResource</u></b>
 * <p>
 * This models a remote resource representing a target defined on a particular system.
 * </p>
 */
public class TargetResource extends AbstractResource implements Comparable<TargetResource> {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private String fName;
    private List<TraceResource> fTraces;
    private ProviderResource fParent;
    private String fCanCreateNewTrace;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor for TargetResource when given fParent subsystem.
     */
    public TargetResource(ISubSystem parentSubSystem) {
        super(parentSubSystem);
        fCanCreateNewTrace = LttngConstants.Rse_Resource_Action_Enabled;
        fTraces = new ArrayList<TraceResource>();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Returns the name of the target resource.
     * 
     * @return String
     */
    public String getName() {
        return fName;
    }

    /**
     * Sets the name of the target resource.
     * 
     * @param fName The fName to set
     */
    public void setName(String name) {
        fName = name;
    }

    /**
     * Sets the traces (children).
     * 
     * @param newTraces The new traces to set
     */
    public void setTraces(TraceResource[] newTraces) {
        fTraces.clear();
        fTraces.addAll(Arrays.asList(newTraces));
    }

    /**
     * Gets the traces (children).
     * 
     * @return traces (children)
     */
    public TraceResource[] getTraces() {
    	TraceResource[] traces = fTraces.toArray(new TraceResource[0]);
    	Arrays.sort(traces);
        return traces;
    }

    /**
     * Gets the trace for a given name.
     * 
     * @param name The name of trace to search for.
     * @return trace if exists else null
     */
    public TraceResource getTrace(String name) {
        for (TraceResource trace : fTraces) {
            if (trace.getName().equals(name)) {
                return trace;
            }
        }
        return null;
    }
    
    /**
     * Adds a new trace (child) to the existing list of traces.
     * 
     * @param trace The new trace to add.
     */
    public void addTrace(TraceResource trace) {
        fTraces.add(trace);
    }

    /**
     * Removes a new trace (child) from the existing list of traces.
     * 
     * @param trace The new trace to add.
     */
    public void removeTrace(TraceResource trace) {
        fTraces.remove(trace);
    }
    
    /**
     * Removes all traces (children).
     */
    public void removeAllTraces() {
        fTraces.clear();
    }

    /**
     * Refreshes target with other traces list. If trace already exists in this
     * target, reuse the trace from this target and don't override.   
     * 
     * @param otherTargets
     */
    public void refreshTraces(TraceResource[] otherTraces) {
        List<TraceResource>  newTraces = new ArrayList<TraceResource>();
        for (int i = 0; i < otherTraces.length; i++) {
            boolean added = false;
            for (TraceResource trace : fTraces) {
                if (otherTraces[i].equals(trace)) {
                    newTraces.add(trace);
                    added = true;
                    break;
                }
            }
            if (!added) {
                newTraces.add(otherTraces[i]);
            }
        }
        fTraces = newTraces;
    }

    /**
     * Returns the parent provider resource.
     * 
     * @return parent provider resource
     */
    public ProviderResource getParent() {
        return fParent;
    }

    /**
     * Sets the parent provider resource.
     * @param provider
     */
    public void setParent(ProviderResource provider) {
        fParent = provider;
    }

    /**
     * Returns whether the target is for UST or kernel traces. 
     * 
     * @return true if UST, false for kernel 
     */
    public boolean isUst() {
        return fParent.isUst();
    }
    
    /**
     * Gets property whether target can create new trace or not 
     * @return fCanCreateNewTrace
     */
    public String getCanCreateNewTrace() {
        return fCanCreateNewTrace;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {

        if (this == other) {
            return true;
        }

        // We only check the name because the target name has to be unique
        if (other instanceof TargetResource) {
            TargetResource otherTarget = (TargetResource) other;
            if (fName != null) {
                return fName.equals(otherTarget.fName);
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override 
    public int hashCode() {
        // We only use the name because the target name has to be unique
        return fName.hashCode();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(TargetResource o) {
        // We only check the name because the trace name has to be unique
        return fName.toLowerCase().compareTo(o.fName.toLowerCase());
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "[TargetResource (" + fName + ")]";
    }
}
