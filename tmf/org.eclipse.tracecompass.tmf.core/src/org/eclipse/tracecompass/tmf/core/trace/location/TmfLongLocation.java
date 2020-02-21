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

/**
 * A concrete implementation of TmfLocation based on Long:s
 *
 * @author Francois Chouinard
 */
public final class TmfLongLocation extends TmfLocation {

    /**
     * Constructor
     *
     * @param locationInfo
     *            The concrete location
     */
    public TmfLongLocation(long locationInfo) {
        super(Long.valueOf(locationInfo));
    }

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

    /**
     * Construct the location from the ByteBuffer.
     *
     * @param bufferIn
     *            the buffer to read from
     */
    public TmfLongLocation(ByteBuffer bufferIn) {
        this(bufferIn.getLong());
    }

    @Override
    public Long getLocationInfo() {
        return (Long) super.getLocationInfo();
    }

    @Override
    public void serialize(ByteBuffer bufferOut) {
        bufferOut.putLong(getLocationInfo().longValue());
    }

}
