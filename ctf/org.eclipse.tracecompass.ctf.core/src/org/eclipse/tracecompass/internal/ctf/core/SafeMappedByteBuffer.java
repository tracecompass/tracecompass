/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.ctf.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.CTFException;

/**
 * A common utility for mapping a ByteBuffer safely to work around a bug on
 * Windows which prevents deleting a file after it was mapped. On Windows, the
 * ByteBuffer will be allocated and the file will be read instead of being
 * mapped.
 *
 * http://bugs.java.com/view_bug.do?bug_id=4715154
 */
public final class SafeMappedByteBuffer {

    private static final boolean IS_WIN32 = System.getProperty("os.name").startsWith("Windows");  //$NON-NLS-1$//$NON-NLS-2$

    private SafeMappedByteBuffer(){}

    /**
     * Maps a region of this channel's file directly into memory. On Windows, this
     * will allocate a new ByteBuffer and read the file.
     *
     * @param fc
     *            the file channel
     * @param mode
     *            the mapping mode
     * @param position
     *            the position within the file
     * @param size
     *            the size of the region to be mapped (or read)
     * @return the mapped ByteBuffer
     * @throws IOException
     *             on FileChannel operations failures
     * @throws CTFException
     *             the file mapping refused to map
     */
    public static @NonNull ByteBuffer map(FileChannel fc, FileChannel.MapMode mode, long position, long size) throws IOException, CTFException {
        ByteBuffer byteBuffer = null;
        long fileSize = fc.size();
        if (IS_WIN32) {
            byteBuffer = ByteBuffer.allocate((int) size);
            fc.read(byteBuffer, position);
            byteBuffer.flip();
        } else {
            if (position + size <= fileSize) {
                byteBuffer = fc.map(mode, position, size);
            }
            if (byteBuffer == null) {
                throw new CTFException("Failed to allocate mapped byte buffer at " + position + " of size " + size + " on a file of size " + fileSize); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }
        return byteBuffer;
    }
}
