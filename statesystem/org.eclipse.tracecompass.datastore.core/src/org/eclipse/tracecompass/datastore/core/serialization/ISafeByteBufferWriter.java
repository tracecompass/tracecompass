/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.datastore.core.serialization;

/**
 * Interface for a safe ByteBuffer for writing purposes. This interface allows
 * only to write data from a buffer, no other operation is allowed on it. The
 * implementation needs to make sure that the buffer does not write over the
 * limits of the buffer.
 *
 * @author Geneviève Bastien
 * @noimplement This interface is not intended to be implemented by clients.
 * @since 1.1
 */
public interface ISafeByteBufferWriter {

    /**
     * Writes a byte at the buffer's current position
     *
     * @param value
     *            The byte to write
     */
    void put(byte value);

    /**
     * Transfers the bytes from the src array in the buffer at the current
     * position
     *
     * @param src
     *            the byte array to write
     */
    void put(byte[] src);

    /**
     * Writes a char at the buffer's current position
     *
     * @param value
     *            The char to write
     */
    void putChar(char value);

    /**
     * Writes a double at the buffer's current position
     *
     * @param value
     *            The double to write
     */
    void putDouble(double value);

    /**
     * Writes a float at the buffer's current position
     *
     * @param value
     *            The float to write
     */
    void putFloat(float value);

    /**
     * Writes an int at the buffer's current position
     *
     * @param value
     *            The int to write
     */
    void putInt(int value);

    /**
     * Writes a long at the buffer's current position
     *
     * @param value
     *            The long to write
     */
    void putLong(long value);

    /**
     * Writes a short at the buffer's current position
     *
     * @param value
     *            The short to write
     */
    void putShort(short value);

    /**
     * Writes a string value in the byte buffer. The implementation can decide
     * what format it will use. They can also have a maximum size, in which case
     * string should be truncated if they are larger than that.
     *
     * @param value
     *            The String value to write to the buffer
     */
    void putString(String value);

}
