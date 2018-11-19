/*******************************************************************************
 * Copyright (c) 2018, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Viet-Hung Phan - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.pcap.core.util;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.tracecompass.internal.pcap.core.trace.BadPcapFileException;
import org.eclipse.tracecompass.internal.pcap.core.trace.PcapFile;
import org.eclipse.tracecompass.internal.pcap.core.trace.PcapFileValues;
import org.eclipse.tracecompass.internal.pcap.core.trace.PcapNgFile;
import org.eclipse.tracecompass.internal.pcap.core.trace.PcapNgFileValues;
import org.eclipse.tracecompass.internal.pcap.core.trace.PcapOldFile;

/**
 * Class for helping with the conversion of data.
 */
public final class PcapHelper {

    private PcapHelper() {
        // Do nothing
    }

    /**
     * Method that allows to instantiate a pcap or pcapNg object.
     *
     * @param filePath
     *            the path file where the file locates
     * @return PcapFile which is a pcap/pcapNg object
     * @throws IOException
     *             Thrown when the path file does not exist or permission issues
     * @throws BadPcapFileException
     *             Thrown when a packet header is invalid.
     */
    public static PcapFile getPcapFile(Path filePath) throws IOException, BadPcapFileException {

        // Check file validity
        if (Files.notExists(filePath) || !Files.isRegularFile(filePath) ||
                Files.size(filePath) < PcapFileValues.GLOBAL_HEADER_SIZE) {
            throw new BadPcapFileException("Bad Pcap File."); //$NON-NLS-1$
        }

        if (!Files.isReadable(filePath)) {
            throw new BadPcapFileException("File is not readable."); //$NON-NLS-1$
        }

        try (FileChannel fileChannel = (FileChannel) checkNotNull(Files.newByteChannel(filePath))) {
            ByteBuffer header = ByteBuffer.allocate(PcapNgFileValues.BLOCK_HEADER_SIZE);
            // Read the packet header to get a magic number
            fileChannel.read(header);
            header.flip();
            // Read 4 bytes from the packet header to verify if it is a true PCAPNG block type via a magic number
            int blockType = header.getInt();
            // If the magic number is 0x0A0D0D0A
            if (blockType == PcapNgFileValues.SHB) {
                return new PcapNgFile(filePath);
            }

            return new PcapOldFile(filePath);
        }
    }

}
