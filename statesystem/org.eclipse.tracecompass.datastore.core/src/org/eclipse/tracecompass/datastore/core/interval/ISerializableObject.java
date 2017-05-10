/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.datastore.core.interval;

import org.eclipse.tracecompass.internal.provisional.datastore.core.serialization.ISafeByteBufferWriter;

/**
 * An object that can be serialized
 *
 * @author Geneviève Bastien
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
