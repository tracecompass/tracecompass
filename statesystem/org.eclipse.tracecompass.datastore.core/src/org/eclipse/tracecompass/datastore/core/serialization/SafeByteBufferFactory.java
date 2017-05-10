/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.datastore.core.serialization;

import java.nio.ByteBuffer;

import org.eclipse.tracecompass.internal.datastore.core.serialization.SafeByteBufferWrapper;

/**
 * Class that creates instances of safe byte buffers wrappers from a part of a
 * ByteBuffer instance
 *
 * @author Geneviève Bastien
 * @since 1.1
 */
public final class SafeByteBufferFactory {

    private SafeByteBufferFactory() {

    }

    /**
     * Creates a new safe byte buffer reader from the ByteBuffer's current
     * position with a size limited to 'size'.
     *
     * @param buffer
     *            The big ByteBuffer to safely wrap for reading
     * @param size
     *            The size of the new sub-buffer
     * @return The safe byte buffer reader instance
     */
    public static ISafeByteBufferReader wrapReader(ByteBuffer buffer, int size) {
        int pos = buffer.position();
        // Slice the main buffer, so that position 0 is the current position
        // set it as read-only also
        ByteBuffer readOnlyBuffer = buffer.slice().asReadOnlyBuffer();
        readOnlyBuffer.order(buffer.order());
        // Set its limit to the request limit
        readOnlyBuffer.limit(size);
        // Operations on fBuffer will not affect the main buffer's position, so
        // we set its position to after the limit
        buffer.position(pos + size);
        return new SafeByteBufferWrapper(readOnlyBuffer);
    }

    /**
     * Creates a new safe byte buffer writer from the ByteBuffer's current
     * position with a size limited to 'size'.
     *
     * @param buffer
     *            The big ByteBuffer to safely wrap for reading
     * @param size
     *            The size of the new sub-buffer
     * @return The safe byte buffer writer instance
     */
    public static ISafeByteBufferWriter wrapWriter(ByteBuffer buffer, int size) {
        int pos = buffer.position();
        // Slice the main buffer, so that position 0 is the current position
        ByteBuffer readWriteBuffer = buffer.slice();
        readWriteBuffer.order(buffer.order());
        // Set its limit to the request limit
        readWriteBuffer.limit(size);
        // Operations on fBuffer will not affect the main buffer's position, so
        // we set its position to after the limit
        buffer.position(pos + size);
        return new SafeByteBufferWrapper(readWriteBuffer);
    }

    /**
     * Get the serialized of a string object if it uses the
     * {@link ISafeByteBufferWriter#putString(String)} method
     *
     * @param string
     *            The string to write to the buffer
     * @return The size of the string serialized by the
     *         {@link ISafeByteBufferWriter#putString(String)} method, or -1 if
     *         the string cannot be serialized
     */
    public static int getStringSizeInBuffer(String string) {
        return SafeByteBufferWrapper.getStringSizeInBuffer(string);
    }

}
