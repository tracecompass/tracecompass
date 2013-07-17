/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace.location;

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;

/**
 * A concrete implementation of TmfLocation based on ITmfTimestamp:s
 *
 * @author Francois Chouinard
 * @since 3.0
 */
public final class TmfTimestampLocation extends TmfLocation {

    /**
     * The normal constructor
     *
     * @param locationInfo the concrete location
     */
    public TmfTimestampLocation(final ITmfTimestamp locationInfo) {
        super(locationInfo);
    }

    /**
     * The copy constructor
     *
     * @param other the other location
     */
    public TmfTimestampLocation(final TmfTimestampLocation other) {
        super(other.getLocationInfo());
    }

    @Override
    public ITmfTimestamp getLocationInfo() {
        return (ITmfTimestamp) super.getLocationInfo();
    }

}
