/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;

/**
 * A concrete implementation of TmfLocation based on Long:s
 *
 * @author Francois Chouinard
 * @since 2.0
 */
public class TmfLongLocation extends TmfLocation {

    /**
     * The normal constructor
     *
     * @param locationInfo the concrete location
     */
    public TmfLongLocation(final Long locationInfo) {
        super(locationInfo);
    }

    /**
     * The copy constructor
     *
     * @param other the other location
     */
    public TmfLongLocation(final TmfLongLocation other) {
        super(other.getLocationInfo());
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfLocation#getLocationInfo()
     */
    @Override
    public Long getLocationInfo() {
        return (Long) super.getLocationInfo();
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.TmfLocation#clone()
     */
    @Override
    public TmfLongLocation clone() {
        return (TmfLongLocation) super.clone();
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.TmfLocation#cloneValue()
     */
    @Override
    protected Long cloneLocationInfo() {
        // No need to clone a Long
        return getLocationInfo();
    }

}
