/*******************************************************************************
 * Copyright (c) 2010, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation, based on article by Nick Zhang
 *                   (http://www.javaworld.com/javatips/jw-javatip26.html)
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.core.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;

/**
 * A class to mitigate the Java I/O inefficiency of RandomAccessFile.
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class BufferedRandomAccessFile extends RandomAccessFile {

    private static final int DEFAULT_BUF_SIZE = 8192;
    private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8"); //$NON-NLS-1$

    private final int BUF_SIZE;
    private final byte buffer[];
    private int buf_end = 0;
    private int buf_pos = 0;
    private long real_pos = 0;
    private final StringBuilder sb = new StringBuilder();

    /**
     * Constructor using the default buffer size
     *
     * @param name
     *            File path. This is passed as-is to the RandomAccessFile's
     *            constructor.
     * @param mode
     *            File open mode ("r", "rw", etc.). This is passed as-is to
     *            RandomAccessFile's constructor.
     * @throws IOException
     *             If the file was not found or couldn't be opened with the
     *             request permissions
     */
    public BufferedRandomAccessFile(String name, String mode) throws IOException {
        this(name, mode, DEFAULT_BUF_SIZE);
    }

    /**
     * Constructor using the default buffer size
     *
     * @param file
     *            File object. This is passed as-is to the RandomAccessFile's
     *            constructor.
     * @param mode
     *            File open mode ("r", "rw", etc.). This is passed as-is to
     *            RandomAccessFile's constructor.
     * @throws IOException
     *             If the file was not found or couldn't be opened with the
     *             request permissions
     */
    public BufferedRandomAccessFile(File file, String mode) throws IOException {
        this(file, mode, DEFAULT_BUF_SIZE);
    }

    /**
     * Standard constructor.
     *
     * @param name
     *            File path. This is passed as-is to the RandomAccessFile's
     *            constructor.
     * @param mode
     *            File open mode ("r", "rw", etc.). This is passed as-is to
     *            RandomAccessFile's constructor.
     * @param bufsize
     *            Buffer size to use, in bytes
     * @throws IOException
     *             If the file was not found or couldn't be opened with the
     *             request permissions
     */
    public BufferedRandomAccessFile(String name, String mode, int bufsize) throws IOException {
        super(name, mode);
        invalidate();
        BUF_SIZE = bufsize;
        buffer = new byte[BUF_SIZE];
    }

    /**
     * Standard constructor.
     *
     * @param file
     *            File object. This is passed as-is to the RandomAccessFile's
     *            constructor.
     * @param mode
     *            File open mode ("r", "rw", etc.). This is passed as-is to
     *            RandomAccessFile's constructor.
     * @param bufsize
     *            Buffer size to use, in bytes
     * @throws IOException
     *             If the file was not found or couldn't be opened with the
     *             request permissions
     */
    public BufferedRandomAccessFile(File file, String mode, int bufsize) throws IOException {
        super(file, mode);
        invalidate();
        BUF_SIZE = bufsize;
        buffer = new byte[BUF_SIZE];
    }

    @Override
    public final int read() throws IOException {
        if (buf_pos >= buf_end) {
            if (fillBuffer() < 0) {
                return -1;
            }
        }
        if (buf_end == 0) {
            return -1;
        }
        return (buffer[buf_pos++] & 0xff);
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        int leftover = buf_end - buf_pos;
        if (len <= leftover) {
            System.arraycopy(buffer, buf_pos, b, off, len);
            buf_pos += len;
            return len;
        }
        for (int i = 0; i < len; i++) {
            int c = this.read();
            if (c != -1) {
                b[off + i] = (byte) c;
            } else {
                return i;
            }
        }
        return len;
    }

    @Override
    public long getFilePointer() throws IOException {
        long l = real_pos;
        return (l - buf_end + buf_pos);
    }

    @Override
    public void seek(long pos) throws IOException {
        int n = (int) (real_pos - pos);
        if (n >= 0 && n <= buf_end) {
            buf_pos = buf_end - n;
        } else {
            super.seek(pos);
            invalidate();
        }
    }

    /**
     * Read the next line from the buffer (ie, until the next '\n'). The bytes
     * are interpreted as UTF-8 characters.
     *
     * @return The String that was read
     * @throws IOException
     *             If we failed reading the file
     */
    public final String getNextLine() throws IOException {
        String str = null;
        if (buf_end - buf_pos <= 0) {
            if (fillBuffer() < 0) {
                return null;
            }
        }
        int lineend = -1;
        for (int i = buf_pos; i < buf_end; i++) {
            if (buffer[i] == '\n') {
                lineend = i;
                break;
            }
        }
        if (lineend < 0) {
            sb.delete(0, sb.length());
            int c;
            while (((c = read()) != -1) && (c != '\n')) {
                sb.append((char) c);
            }
            if ((c == -1) && (sb.length() == 0)) {
                return null;
            }
            if (sb.charAt(sb.length() - 1) == '\r') {
                sb.deleteCharAt(sb.length() - 1);
            }
            return sb.toString();
        }
        if (lineend > 0 && buffer[lineend - 1] == '\r' && lineend > buf_pos) {
            str = new String(buffer, buf_pos, lineend - buf_pos - 1, CHARSET_UTF8);
        } else {
            str = new String(buffer, buf_pos, lineend - buf_pos, CHARSET_UTF8);
        }
        buf_pos = lineend + 1;
        return str;
    }

    private int fillBuffer() throws IOException {
        int n = super.read(buffer, 0, BUF_SIZE);
        if (n >= 0) {
            real_pos += n;
            buf_end = n;
            buf_pos = 0;
        }
        return n;
    }

    private void invalidate() throws IOException {
        buf_end = 0;
        buf_pos = 0;
        real_pos = super.getFilePointer();
    }
}
