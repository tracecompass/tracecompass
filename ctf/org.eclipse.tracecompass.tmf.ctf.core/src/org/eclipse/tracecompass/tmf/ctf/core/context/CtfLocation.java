/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Alexandre Montplaisir - Extends TmfLocation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.context;

import java.nio.ByteBuffer;

import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.location.TmfLocation;

/**
 * The nugget of information that is unique to a location in a CTF trace.
 *
 * It can be copied and used to restore a position in a given trace.
 *
 * @author Matthew Khouzam
 */
public final class CtfLocation extends TmfLocation {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * An invalid location
     */
    public static final CtfLocationInfo INVALID_LOCATION = new CtfLocationInfo(-1, -1);

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Basic constructor for CtfLocation. Uses a default index of 0.
     *
     * @param timestamp
     *            The timestamp of this location
     */
    public CtfLocation(final ITmfTimestamp timestamp) {
        this(timestamp.getValue(), 0);
    }

    /**
     * Constructor using timestamp object and index
     *
     * @param timestamp
     *            The timestamp of this location
     * @param index
     *            The index of this location for this timestamp
     */
    public CtfLocation(final ITmfTimestamp timestamp, long index) {
        this(timestamp.getValue(), index);
    }

    /**
     * Constructor using a long value for the timestamp, and an index
     *
     * @param timestampValue
     *            The new timestamp
     * @param index
     *            The new index
     */
    public CtfLocation(final long timestampValue, final long index) {
       super(new CtfLocationInfo(timestampValue, index));
    }

    /**
     * Constructor using a pre-made locationInfo object
     *
     * @param locationInfo
     *            The locationInfo object to use
     */
    public CtfLocation(CtfLocationInfo locationInfo) {
        super(locationInfo);
    }

    /**
     * Copy constructor
     *
     * @param location
     *            Other location to copy
     */
    public CtfLocation(final CtfLocation location) {
        super(location);
    }

    // ------------------------------------------------------------------------
    // TmfLocation
    // ------------------------------------------------------------------------

    /**
     * Construct the location from the ByteBuffer.
     *
     * @param bufferIn
     *            the buffer to read from
     */
    public CtfLocation(ByteBuffer bufferIn) {
        super(new CtfLocationInfo(bufferIn));
    }

    @Override
    public CtfLocationInfo getLocationInfo() {
        return (CtfLocationInfo) super.getLocationInfo();
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        if (getLocationInfo().equals(CtfLocation.INVALID_LOCATION )) {
            return getClass().getSimpleName() + " [INVALID]"; //$NON-NLS-1$
        }
        return super.toString();
    }

    /**
     * Constructs the location from the ByteBuffer. This typically happens when reading from disk.
     */
    @Override
    public void serialize(ByteBuffer bufferOut) {
        getLocationInfo().serialize(bufferOut);
    }
}
