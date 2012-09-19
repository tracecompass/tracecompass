/*******************************************************************************
 * Copyright (c) 2009, 2010, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Francois Chouinard - Initial API and implementation
 * Francois Chouinard - Updated as per TMF Trace Model 1.0
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.trace;

import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;

/**
 * The experiment location in TMF.
 * <p>
 * An experiment location is actually the set of locations of the traces it
 * contains. By setting the individual traces to their corresponding locations,
 * the experiment can be positioned to read the next chronological event.
 * <p>
 * It is the responsibility of the user the individual trace locations are valid
 * and that they are matched to the correct trace.
 *
 * @version 1.0
 * @author Francois Chouinard
 *
 * @see TmfLocationArray
 */
public class TmfExperimentLocation implements ITmfLocation {

    TmfLocationArray fLocation;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * The standard constructor
     *
     * @param locations the set of trace locations
     */
    public TmfExperimentLocation(TmfLocationArray locations) {
        fLocation = locations;
    }

    /**
     * The copy constructor
     *
     * @param location the other experiment location
     */
    public TmfExperimentLocation(TmfExperimentLocation location) {
        this(location.getLocationData());
    }

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.TmfLocation#clone()
     */
    @Override
    public TmfExperimentLocation clone() {
//        super.clone(); // To keep FindBugs happy
        TmfLocationArray array = getLocationData();
        TmfLocationArray clones = array.clone();
        return new TmfExperimentLocation(clones);
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.TmfLocation#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        StringBuilder result = new StringBuilder("[TmfExperimentLocation");
        ITmfLocation[] locations = getLocationData().getLocations();
        for (ITmfLocation location : locations) {
            result.append("[" + location + "]");
        }
        result.append("]");
        return result.toString();
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.TmfLocation#hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.TmfLocation#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfLocation#getLocationData()
     */
    @Override
    public TmfLocationArray getLocationData() {
        return fLocation;
    }

}
