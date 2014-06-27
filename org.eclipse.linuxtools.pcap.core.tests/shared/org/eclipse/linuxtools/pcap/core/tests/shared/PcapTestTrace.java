/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.pcap.core.tests.shared;

import java.io.File;
import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.pcap.core.trace.BadPcapFileException;
import org.eclipse.linuxtools.pcap.core.trace.PcapFile;

/**
 * Here is the list of the available test traces for the Pcap parser.
 *
 * @author Vincent Perot
 */
public enum PcapTestTrace {

    /** A bad pcap file. */
    BAD_PCAPFILE("../org.eclipse.linuxtools.pcap.core.tests/rsc/BadPcapFile.pcap"),

    /** A Valid Pcap that is empty. */
    EMPTY_PCAP("../org.eclipse.linuxtools.pcap.core.tests/rsc/EmptyPcap.pcap"),

    /** A Pcap that mostly contains TCP packets. */
    MOSTLY_TCP("../org.eclipse.linuxtools.pcap.core.tests/rsc/mostlyTCP.pcap"),

    /** A Pcap that mostly contains UDP packets. */
    MOSTLY_UDP("../org.eclipse.linuxtools.pcap.core.tests/rsc/mostlyUDP.pcap"),

    /** A big-endian trace that contains two packets. */
    SHORT_BIG_ENDIAN("../org.eclipse.linuxtools.pcap.core.tests/rsc/Short_BigEndian.pcap"),

    /** A little-endian trace that contains two packets. */
    SHORT_LITTLE_ENDIAN("../org.eclipse.linuxtools.pcap.core.tests/rsc/Short_LittleEndian.pcap"),

    /** A Kernel trace directory. */
    KERNEL_DIRECTORY("../org.eclipse.linuxtools.pcap.core.tests/rsc/kernel/"),

    /** A Kernel trace file. */
    KERNEL_TRACE("../org.eclipse.linuxtools.pcap.core.tests/rsc/kernel/channel0_0");

    private final @NonNull String fPath;

    private PcapTestTrace(@NonNull String path) {
        fPath = path;
    }

    /** @return The path to the test trace */
    public @NonNull String getPath() {
        return fPath;
    }

    /**
     * Get a Pcap Trace instance of a test trace. Make sure to call
     * {@link #exists()} before calling this!
     *
     * @return The PcapFile object
     * @throws IOException
     *             Thrown when some IO error occurs.
     * @throws BadPcapFileException
     *             Thrown when the file is not a valid Pcap File.
     */
    public PcapFile getTrace() throws BadPcapFileException, IOException {
        return new PcapFile(fPath);
    }

    /**
     * Check if this test trace actually exists on disk.
     *
     * @return If the trace exists
     */
    public boolean exists() {
        File file = new File(fPath);
        if (!file.exists()) {
            return false;
        }
        return true;
    }

}
