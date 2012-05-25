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
 * The ctflocation is the nugget of information that is unique to a location in a trace.
 * it can be copied and used to restore a position in a given trace.
 */
public class CtfLocation implements ITmfLocation<Long>, Cloneable {

    public static final Long INVALID_LOCATION = -1L;

    /**
     * Constructor for CtfLocation.
     * @param location Long
     */
    public CtfLocation(Long location) {
        setLocation(location);
    }

    /**
     * Constructor for CtfLocation.
     * @param timestamp ITmfTimestamp
     */
    public CtfLocation(ITmfTimestamp timestamp) {
        setLocation(timestamp.getValue());
    }

    private Long fTimestamp;

    /**
     * Method setLocation.
     * @param location Long
     */
    public void setLocation(Long location) {
        this.fTimestamp = location;
    }

    /**
     * Method getLocation.
     * @return Long
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfLocation#getLocation()
     */
    @Override
    public Long getLocation() {
        return this.fTimestamp;
    }

    /**
     * Method clone.
     * @return CtfLocation
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfLocation#clone()
     */
    @Override
    public CtfLocation clone() {
        return new CtfLocation(getLocation());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result)
                + ((fTimestamp == null) ? 0 : fTimestamp.hashCode());
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
        if (fTimestamp == null) {
            if (other.fTimestamp != null) {
                return false;
            }
        } else if (!fTimestamp.equals(other.fTimestamp)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if( this.getLocation().equals(CtfLocation.INVALID_LOCATION )) {
            return "CtfLocation: INVALID"; //$NON-NLS-1$
        }
        return "CtfLocation: " + getLocation().toString(); //$NON-NLS-1$
    }

}
