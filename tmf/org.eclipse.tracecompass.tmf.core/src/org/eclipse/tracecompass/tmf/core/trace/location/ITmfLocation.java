/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
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
 *   Francois Chouinard - Updated as per TMF Trace Model 1.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace.location;

import java.nio.ByteBuffer;

/**
 * The generic trace location in TMF.
 * <p>
 * An ITmfLocation is the equivalent of a random-access file position, holding
 * enough information to allow the positioning of the trace 'pointer' to read an
 * arbitrary event.
 * <p>
 * This location is trace-specific, must be comparable and immutable.
 *
 * @author Francois Chouinard
 */
public interface ITmfLocation {

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * Returns the concrete trace location information
     *
     * @return the location information
     */
    Comparable<?> getLocationInfo();

    /**
     * Write the location to the ByteBuffer so that it can be saved to disk.
     * @param bufferOut the buffer to write to
     */
    void serialize(ByteBuffer bufferOut);
}
