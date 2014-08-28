/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.ctf.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.eclipse.core.runtime.Platform;

/**
 * A common utility for mapping a ByteBuffer safely to work around a bug on
 * Windows which prevents deleting a file after it was mapped. On Windows, the
 * ByteBuffer will be allocated and the file will be read instead of being
 * mapped.
 *
 * http://bugs.java.com/view_bug.do?bug_id=4715154
 */
public class SafeMappedByteBuffer {

    private static final boolean IS_WIN32 = Platform.OS_WIN32.equals(Platform.getOS());

    /**
     * Maps a region of this channel's file directly into memory. On Windows,
     * this will allocate a new ByteBuffer and read the file.
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
     */
    public static ByteBuffer map(FileChannel fc, FileChannel.MapMode mode, long position, long size) throws IOException {
        ByteBuffer byteBuffer;
        if (IS_WIN32) {
            byteBuffer = ByteBuffer.allocate((int) size);
            fc.read(byteBuffer, position);
        } else {
            byteBuffer = fc.map(mode, position, size);
        }

        return byteBuffer;
    }
}
