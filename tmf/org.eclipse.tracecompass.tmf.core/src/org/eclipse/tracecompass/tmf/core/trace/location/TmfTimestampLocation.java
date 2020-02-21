/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace.location;

import java.nio.ByteBuffer;

import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;

/**
 * A concrete implementation of TmfLocation based on ITmfTimestamp:s
 *
 * @author Francois Chouinard
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

    /**
     * Construct the location from the ByteBuffer.
     *
     * @param bufferIn
     *            the buffer to read from
     */
    public TmfTimestampLocation(ByteBuffer bufferIn) {
        super(TmfTimestamp.create(bufferIn));
    }

    @Override
    public ITmfTimestamp getLocationInfo() {
        return (ITmfTimestamp) super.getLocationInfo();
    }

    @Override
    public void serialize(ByteBuffer bufferOut) {
        TmfTimestamp.serialize(bufferOut, getLocationInfo());
    }
}
