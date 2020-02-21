/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.datastore.core.interval;

import org.eclipse.tracecompass.datastore.core.serialization.ISafeByteBufferWriter;

/**
 * An object that can be serialized
 *
 * @author Geneviève Bastien
 * @since 1.1
 */
public interface ISerializableObject {

    /**
     * Get the size on disk in bytes of an object
     *
     * @return the size occupied by this segment when stored in a Segment
     *         History Tree (in bytes)
     */
    int getSizeOnDisk();

    /**
     * Method to serialize an object to a safe byte buffer
     *
     * @param buffer
     *            The safe byte buffer to write to
     */
    void writeSegment(ISafeByteBufferWriter buffer);
}
