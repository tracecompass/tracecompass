/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;

/**
 * The nugget of information that is unique to a location in a CTF trace.
 *
 * It can be copied and used to restore a position in a given trace.
 *
 * @version 1.0
 * @author Matthew Khouzam
 */
public final class CtfLocation implements ITmfLocation {

    private final CtfLocationData fLocation;

    /**
     * An invalid location
     */
    public static final CtfLocationData INVALID_LOCATION = new CtfLocationData(-1, -1);

    /**
     * Constructor for CtfLocation. Uses a default index of 0.
     *
     * @param timestamp
     *            The timestamp of this location
     */
    public CtfLocation(final ITmfTimestamp timestamp) {
        this(timestamp.getValue(), 0);
    }

    /**
     * Standard constructor
     *
     * @param timestamp
     *            The timestamp of this location
     * @param index
     *            The index of this location for this timestamp
     * @since 2.0
     */
    public CtfLocation(final ITmfTimestamp timestamp, long index) {
        this(timestamp.getValue(), index);
    }

    /**
     * Change this location's timestamp and index values.
     *
     * @param timestampValue
     *            The new timestamp
     * @param index
     *            The new index
     * @since 2.0
     */
    public CtfLocation(final long timestampValue, final long index) {
       this(new CtfLocationData(timestampValue, index));
    }

    /**
     * Copy constructor
     *
     * @param location
     *            Other location to copy
     * @since 2.0
     */
    public CtfLocation(final CtfLocationData location) {
        fLocation = location;
    }

    /**
     * Get the Location Data of this location
     *
     * @return The CtfLocationData
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfLocation#getLocationInfo()
     * @since 2.0
     */
    @Override
    public CtfLocationData getLocationInfo() {
        return fLocation;
    }

    @Override
    public CtfLocation clone() {
        return new CtfLocation(new CtfLocationData(fLocation.getTimestamp(), fLocation.getIndex()));
    }


    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result)
                + ((fLocation == null) ? 0 : fLocation.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CtfLocation)) {
            return false;
        }
        CtfLocation other = (CtfLocation) obj;
        if (fLocation == null) {
            if (other.fLocation != null) {
                return false;
            }
        } else if (!fLocation.equals(other.fLocation)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if( this.getLocationInfo().equals(CtfLocation.INVALID_LOCATION )) {
            return "CtfLocation: INVALID"; //$NON-NLS-1$
        }
        return "CtfLocation: " + getLocationInfo().toString(); //$NON-NLS-1$
    }

}
