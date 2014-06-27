/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.pcap.core.tests.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;

import org.eclipse.linuxtools.pcap.core.tests.shared.PcapTestTrace;
import org.eclipse.linuxtools.pcap.core.trace.BadPcapFileException;
import org.eclipse.linuxtools.pcap.core.trace.PcapFile;
import org.junit.Test;

/**
 * JUnit Class that tests the opening of non-valid pcap files.
 *
 * @author Vincent Perot
 */
public class PcapFileOpenFailTest {

    /**
     * Test that tries to open a pcap with a bad magic number
     *
     * @throws IOException
     *             Thrown when an IO error occurs. Fails the test.
     */
    @Test
    public void FileOpenBadPcapTest() throws IOException {
        PcapTestTrace trace = PcapTestTrace.BAD_PCAPFILE;
        assumeTrue(trace.exists());

        String path = trace.getPath();
        try (PcapFile file = new PcapFile(path);) {
            fail("The pcap was accepted even though the magic number is invalid!");
        } catch (BadPcapFileException e) {
            assertEquals("c3d4a1b2 is not a known magic number.", e.getMessage());
        }
    }

    /**
     * Test that tries to open a non-pcap binary file
     *
     * @throws IOException
     *             Thrown when an IO error occurs. Fails the test.
     */
    @Test
    public void FileOpenBinaryFile() throws IOException {
        PcapTestTrace trace = PcapTestTrace.KERNEL_TRACE;
        assumeTrue(trace.exists());

        String path = trace.getPath();
        try (PcapFile file = new PcapFile(path);) {
            fail("The file was accepted even though it is not a pcap file!");
        } catch (BadPcapFileException e) {
            assertEquals("Bad Pcap File.", e.getMessage());
        }
    }

    /**
     * Test that tries to open a directory
     *
     * @throws IOException
     *             Thrown when an IO error occurs. Fails the test.
     */
    @Test
    public void FileOpenDirectory() throws IOException {
        PcapTestTrace trace = PcapTestTrace.KERNEL_DIRECTORY;
        assumeTrue(trace.exists());

        String path = trace.getPath();
        try (PcapFile file = new PcapFile(path);) {
            fail("The file was accepted even though it is not a pcap file!");
        } catch (BadPcapFileException e) {
            assertEquals("Bad Pcap File.", e.getMessage());
        }
    }
}
