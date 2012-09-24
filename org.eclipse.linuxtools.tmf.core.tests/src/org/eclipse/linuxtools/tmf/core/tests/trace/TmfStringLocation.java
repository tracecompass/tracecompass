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

package org.eclipse.linuxtools.tmf.core.tests.trace;

import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;

/**
 * <b><u>TmfStringLocation</u></b>
 * <p>
 * Implement me. Please.
 * <p>
 */
public class TmfStringLocation extends TmfLocation {

    /**
     * @param location the concrete trace location
     */
    public TmfStringLocation(String location) {
        super(location);
    }

    /**
     * @param other the other location
     */
    public TmfStringLocation(TmfStringLocation other) {
        super(other.getLocationInfo());
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfLocation#getLocationInfo()
     */
    @Override
    public String getLocationInfo() {
        return (String) super.getLocationInfo();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public TmfStringLocation clone() {
        TmfStringLocation clone = null;
        clone = (TmfStringLocation) super.clone();
        return clone;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.TmfLocation#cloneValue()
     */
    @Override
    protected String cloneLocationInfo() {
        // No need to clone a String
        return getLocationInfo();
    }

}
