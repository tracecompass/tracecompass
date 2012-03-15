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
package org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.linuxtools.internal.lttng.core.LttngConstants;
import org.eclipse.linuxtools.internal.lttng.core.tracecontrol.model.TargetResource;
import org.eclipse.rse.core.subsystems.AbstractResource;
import org.eclipse.rse.core.subsystems.ISubSystem;

/**
 * <b><u>ProviderResource</u></b>
 * <p>
 * This models a remote resource representing a provider defined on a particular system.
 * </p>
 */
public class ProviderResource extends AbstractResource {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    private String fName;
    private TargetResource[] fTargets;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     */
    public ProviderResource() {
        super();
    }

    /**
     * Constructor for ProviderResource when given a parent subsystem.
     */
    public ProviderResource(ISubSystem parentSubSystem) {
        super(parentSubSystem);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Returns the name of the provider resource.
     * 
     * @return String
     */
    public String getName() {
        return fName;
    }

    /**
     * Sets the name of the provider resource.
     * 
     * @param name The fName to set
     */
    public void setName(String name) {
        fName = name;
    }

    /**
     * Returns the targets (children).
     * 
     * @return TargetResource[]
     */
    public TargetResource[] getTargets() {
    	Arrays.sort(fTargets);
        return fTargets;
    }

    /**
     * Returns whether the provider is for UST or kernel traces. 
     * 
     * @return true if UST, false for kernel 
     */
    public boolean isUst() {
        return fName.equals(LttngConstants.Lttng_Provider_Ust);        
    }
    
    /**
     * Sets the targets (children).
     * 
     * @param newTargets The new targets to set
     */
    public void setTargets(TargetResource[] newTargets) {
        fTargets = newTargets;
    }

    /**
     * Removes all targets (children).
     */
    public void removeAllTargets() {
        for (int i = 0; i < fTargets.length; i++) {
            fTargets[i].removeAllTraces();
        }
        fTargets = null;
    }

    /**
     * Refreshes provider with other targets list. If target already exists in this
     * provider, reuse the target from this provider and don't override.   
     * 
     * @param otherTargets
     */
    public void refreshTargets(TargetResource[] otherTargets) {
        List<TargetResource>  newTargets = new ArrayList<TargetResource>();
        for (int i = 0; i < otherTargets.length; i++) {
            boolean added = false;
            for (int j = 0; j < fTargets.length; j++) {
                if (otherTargets[i].equals(fTargets[j])) {
                    newTargets.add(fTargets[j]);
                    fTargets[j].refreshTraces(otherTargets[i].getTraces());
                    added = true;
                    break;
                }
            }
            if (!added) {
                newTargets.add(otherTargets[i]);
            }
        }
        fTargets = newTargets.toArray(new TargetResource[0]);
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "[ProviderResource (" + fName + ")]";
    }
}
