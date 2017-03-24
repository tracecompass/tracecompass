/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.io;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.tracecompass.tmf.core.io.BufferedRandomAccessFile;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for class {@link BufferedRandomAccessFile}
 */
public class BufferedRandomAccessFileTest {

    private static final int FILESIZE = 256;
    private static final String LINE = "123456789abcdef\n";
    private static final int LENGTH = LINE.length();
    private static File testFile;

    /**
     * Setup
     *
     * @throws IOException
     *             if an exception occurs
     */
    @BeforeClass
    public static void beforeClass() throws IOException {
        testFile = File.createTempFile("test", ".txt");
        FileWriter fw = new FileWriter(testFile);
        try (BufferedWriter bw = new BufferedWriter(fw)) {
            for (int i = 0; i < FILESIZE / LENGTH; i++) {
                bw.write(LINE);
            }
        }
    }

    /**
     * Test read methods
     *
     * @throws IOException
     *             if an exception occurs
     */
    @Test
    public void testRead() throws IOException {
        try (BufferedRandomAccessFile file = new BufferedRandomAccessFile(testFile, "r", 16)) {
            assertEquals(FILESIZE, file.length());
            for (int pos = 0; pos < file.length(); pos++) {
                assertEquals(pos, file.getFilePointer());
                int c = file.read();
                assertEquals(LINE.getBytes()[pos % 16], c);
            }
            assertEquals(file.length(), file.getFilePointer());
            assertEquals(-1, file.read());
        }
        try (BufferedRandomAccessFile file = new BufferedRandomAccessFile(testFile, "r", 24)) {
            assertEquals(FILESIZE, file.length());
            for (int pos = 0; pos < file.length(); pos += LENGTH) {
                byte[] b = new byte[LENGTH];
                assertEquals(pos, file.getFilePointer());
                int num = file.read(b);
                assertEquals(LENGTH, num);
                assertArrayEquals(LINE.getBytes(), b);
            }
            assertEquals(file.length(), file.getFilePointer());
            assertEquals(-1, file.read(new byte[LENGTH]));
        }
        try (BufferedRandomAccessFile file = new BufferedRandomAccessFile(testFile, "r", 16)) {
            assertEquals(FILESIZE, file.length());
            int off = LENGTH / 4;
            int len = LENGTH / 2;
            for (int pos = 0; pos < file.length(); pos += len) {
                byte[] b = new byte[LENGTH];
                assertEquals(pos, file.getFilePointer());
                int num = file.read(b, off, len);
                assertEquals(len, num);
                byte[] expected = new byte[LENGTH];
                System.arraycopy(LINE.getBytes(), pos % 16, expected, off, len);
                assertArrayEquals(expected, b);
            }
            assertEquals(file.length(), file.getFilePointer());
            assertEquals(-1, file.read(new byte[LENGTH], off, len));
        }
    }

    /**
     * Test readLine method
     *
     * @throws IOException
     *             if an exception occurs
     */
    @Test
    public void testReadLine() throws IOException {
        try (BufferedRandomAccessFile file = new BufferedRandomAccessFile(testFile, "r", 8)) {
            assertEquals(FILESIZE, file.length());
            for (int pos = 0; pos < file.length(); pos += LENGTH) {
                assertEquals(pos, file.getFilePointer());
                String line = file.readLine();
                assertEquals(LINE.trim(), line);
            }
            assertEquals(file.length(), file.getFilePointer());
            assertEquals(null, file.readLine());
        }
    }

    /**
     * Test seek followed by read
     *
     * @throws IOException
     *             if an exception occurs
     */
    @Test
    public void testSeekAndRead() throws IOException {
        try (BufferedRandomAccessFile file = new BufferedRandomAccessFile(testFile, "r", 8)) {
            assertEquals(FILESIZE, file.length());
            for (int i = 0; i < file.length(); i++) {
                /* alternate seeking forward and backward */
                /* 0, 255, 1, 254, ..., 127, 128 */
                int seek = (i % 2 == 0 ? i / 2 : FILESIZE - 1 - i / 2);
                file.seek(seek);
                /* read from seek position to end of file */
                for (int pos = seek; pos < file.length(); pos++) {
                    assertEquals(pos, file.getFilePointer());
                    int c = file.read();
                    assertEquals(LINE.getBytes()[pos % 16], c);
                }
            }
            file.seek(file.length());
            assertEquals(file.length(), file.getFilePointer());
            assertEquals(-1, file.read());
        }
    }

    /**
     * Test seek followed by write
     *
     * @throws IOException
     *             if an exception occurs
     */
    @Test
    public void testSeekAndWrite() throws IOException {
        byte[] expected = new byte[FILESIZE + 1];
        expected[FILESIZE] = -1;
        try (BufferedRandomAccessFile file = new BufferedRandomAccessFile(File.createTempFile("test", ".txt"), "rw", 8)) {
            assertEquals(0, file.length());
            for (int i = 0; i < FILESIZE; i++) {
                /* alternate seeking forward and backward */
                /* 255, 0, 254, 1, ..., 128, 127 */
                /* first iteration write sets the file length */
                int seek = (i % 2 == 0 ? FILESIZE - 1 - i / 2 : i / 2);
                file.seek(seek);
                /* write one byte at seek position */
                byte b = LINE.getBytes()[seek % 16];
                file.write(b);
                expected[seek] = b;
                assertEquals(FILESIZE, file.length());
                assertEquals(seek + 1, file.getFilePointer());
                /* read next position in the file */
                int c = file.read();
                assertEquals(expected[seek + 1], c);
            }
            /* read back the whole written file */
            file.seek(0);
            for (int pos = 0; pos < file.length(); pos++) {
                assertEquals(pos, file.getFilePointer());
                int c = file.read();
                assertEquals(expected[pos], c);
            }
            assertEquals(file.length(), file.getFilePointer());
            assertEquals(-1, file.read());
        }
    }
}
