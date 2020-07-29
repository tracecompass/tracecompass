/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.ctf.core.tests.trace;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.trace.Metadata;
import org.junit.Test;

/**
 * There are so many it makes sense to move them to their own file
 */
public class MetadataPrevalidationTest {

    private static final String GOOD_TSDL = "/* CTF 1.8 */\ntrace {\n major = 1 ;\n minor = 8 ;\n byte_order = le ; \n};";

    /**
     * Test a null should return false
     *
     * @throws CTFException
     *             if an exception occurs, shouldn't happen
     */
    @Test
    public void testTraceNull() throws CTFException {
        assertFalse(Metadata.preValidate(null));
    }

    /**
     * Test a non-existing file should return false
     *
     * @throws CTFException
     *             if an exception occurs, shouldn't happen
     */
    @Test
    public void testTraceFileDoesNotExist() throws CTFException {
        assertFalse(Metadata.preValidate("abcdefghijklmnopqrstuvwxyz"));
    }

    /**
     * Test a trace file should return false
     *
     * @throws IOException
     *             A file error occurs, shouldn't happen
     * @throws CTFException
     *             if an exception occurs, shouldn't happen
     */
    @Test
    public void testTraceFile() throws CTFException, IOException {
        File f = File.createTempFile("test", ".log");
        try (PrintWriter pw = new PrintWriter(f)) {
            pw.println("2 hello world");
        }
        assertFalse(Metadata.preValidate(f.getAbsolutePath()));
    }

    /**
     * Test an empty directory should return false
     *
     * @throws IOException
     *             A file error occurs, shouldn't happen
     * @throws CTFException
     *             if an exception occurs, shouldn't happen
     */
    @Test
    public void testTraceDirectoryWithNoFiles() throws IOException, CTFException {
        Path dir = Files.createTempDirectory("trace");
        assertFalse(Metadata.preValidate(dir.toAbsolutePath().toString()));
    }

    /**
     * Test a directory with no metadata file should return false
     *
     * @throws IOException
     *             A file error occurs, shouldn't happen
     * @throws CTFException
     *             if an exception occurs, shouldn't happen
     */
    @Test
    public void testTraceDirectoryWithNoMetadataButFiles() throws CTFException, IOException {
        Path dir = Files.createTempDirectory("trace");
        Path f = Files.createFile(dir.resolve("metadata"));
        try (PrintWriter pw = new PrintWriter(f.toFile())) {
            pw.println("2 hello world");
        }
        assertFalse(Metadata.preValidate(dir.toAbsolutePath().toString()));
    }

    /**
     * Test a valid trace with packetized little endian metadata should return
     * true
     *
     * @throws IOException
     *             A file error occurs, shouldn't happen
     * @throws CTFException
     *             if an exception occurs, shouldn't happen
     */
    @Test
    public void testTraceDirectoryWithLittleEndianMetadata() throws CTFException, IOException {
        Path dir = Files.createTempDirectory("trace");
        Path f = Files.createFile(dir.resolve("metadata"));
        Files.write(f, packetize(GOOD_TSDL, ByteOrder.BIG_ENDIAN));
        assertTrue(Metadata.preValidate(dir.toAbsolutePath().toString()));
    }

    /**
     * Test a valid trace with packetized big endian metadata should return true
     *
     * @throws IOException
     *             A file error occurs, shouldn't happen
     * @throws CTFException
     *             if an exception occurs, shouldn't happen
     */
    @Test
    public void testTraceDirectoryWithBigEndianMetadata() throws CTFException, IOException {
        Path dir = Files.createTempDirectory("trace");
        Path f = Files.createFile(dir.resolve("metadata"));
        Files.write(f, packetize(GOOD_TSDL, ByteOrder.BIG_ENDIAN));
        assertTrue(Metadata.preValidate(dir.toAbsolutePath().toString()));
    }

    /**
     * Test a valid trace with text metadata should return true
     *
     * @throws IOException
     *             A file error occurs, shouldn't happen
     * @throws CTFException
     *             if an exception occurs, shouldn't happen
     */
    @Test
    public void testTraceDirectoryWithTextMetadata() throws IOException, CTFException {
        Path dir = Files.createTempDirectory("trace");
        Path f = Files.createFile(dir.resolve("metadata"));
        try (PrintWriter pw = new PrintWriter(f.toFile())) {
            pw.println(GOOD_TSDL);
        }
        assertTrue(Metadata.preValidate(dir.toAbsolutePath().toString()));
    }

    /**
     * Test a valid trace with text invalid metadata should return false
     *
     * @throws IOException
     *             A file error occurs, shouldn't happen
     * @throws CTFException
     *             if an exception occurs, shouldn't happen
     */
    @Test
    public void testTraceDirectoryWithInvalidMetadata() throws IOException, CTFException {
        Path dir = Files.createTempDirectory("trace");
        Path f = Files.createFile(dir.resolve("metadata"));
        try (PrintWriter pw = new PrintWriter(f.toFile())) {
            // no header
            pw.println("trace { major =1 ; minor = 8 ; byte_order = le;};");
        }
        assertFalse(Metadata.preValidate(dir.toAbsolutePath().toString()));
    }

    /**
     * Test a valid trace with an empty metadata should return false
     *
     * @throws IOException
     *             A file error occurs, shouldn't happen
     * @throws CTFException
     *             if an exception occurs, shouldn't happen
     */
    @Test
    public void testTraceDirectoryWithEmptyMetadata() throws IOException, CTFException {
        Path dir = Files.createTempDirectory("trace");
        Files.createFile(dir.resolve("metadata"));
        assertFalse(Metadata.preValidate(dir.toAbsolutePath().toString()));
    }

    /**
     * Test a valid trace with 1 byte long metadata should return false
     *
     * @throws IOException
     *             A file error occurs, shouldn't happen
     * @throws CTFException
     *             if an exception occurs, shouldn't happen
     */
    @Test
    public void testTraceDirectoryWith1ByteMetadata() throws IOException, CTFException {
        Path dir = Files.createTempDirectory("trace");
        Path f = Files.createFile(dir.resolve("metadata"));
        try (FileWriter pw = new FileWriter(f.toFile())) {
            pw.append('x');
        }
        assertFalse(Metadata.preValidate(dir.toAbsolutePath().toString()));
    }

    private static byte[] packetize(String body, ByteOrder bo) {
        byte[] retVal = new byte[40 + body.length()];
        ByteBuffer bb = ByteBuffer.wrap(retVal);
        bb.order(bo);
        generateMetadataPacketHeader(bb, body);
        return retVal;
    }

    private static void generateMetadataPacketHeader(ByteBuffer headerByteBuffer, String body) {
        /* Read from the ByteBuffer */
        headerByteBuffer.putInt(0x75D11D57);
        final UUID randomUUID = UUID.randomUUID();
        headerByteBuffer.putLong(randomUUID.getMostSignificantBits());
        headerByteBuffer.putLong(randomUUID.getLeastSignificantBits());
        headerByteBuffer.putInt(0); // checksum
        headerByteBuffer.putInt(body.length());
        headerByteBuffer.putInt(body.length());
        headerByteBuffer.put((byte) 0);
        headerByteBuffer.put((byte) 0);
        headerByteBuffer.putInt(0);
        headerByteBuffer.put((byte) 1);
        headerByteBuffer.put((byte) 8);
        headerByteBuffer.put(body.getBytes());
    }
}
