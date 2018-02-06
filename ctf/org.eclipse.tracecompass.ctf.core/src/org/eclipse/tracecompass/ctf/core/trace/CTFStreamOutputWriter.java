/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.trace;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.CTFStrings;
import org.eclipse.tracecompass.internal.ctf.core.trace.StreamInputPacketIndex;

/**
 * A CTF Stream output writer. Reads the packets of a given CTFStreamInput and
 * writes packets that are within a given time range to output stream file.
 *
 * @author Bernd Hufmann
 * @since 1.0
 */
public class CTFStreamOutputWriter {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    // Stream input when copying stream from an input
    // It is @Nullable for future implementations that doesn't use an input
    // stream
    @Nullable
    private final CTFStreamInput fStreamInput;
    @NonNull
    private final CTFStreamPacketOutputWriter fStreamPacketOutputWriter;
    @NonNull
    private final File fOutFile;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a StreamInput.
     *
     * @param streamInput
     *                        The stream to which this StreamInput belongs to.
     * @param file
     *                        The output trace directory.
     * @throws CTFException
     *                          If a reading or writing error occurs
     */
    public CTFStreamOutputWriter(@NonNull CTFStreamInput streamInput, @NonNull File file) throws CTFException {
        fStreamInput = streamInput;
        String inFileName = streamInput.getFile().getName();
        Path outFilePath = FileSystems.getDefault().getPath(file.getAbsolutePath(), inFileName);

        try {
            fOutFile = checkNotNull(Files.createFile(outFilePath).toFile());
        } catch (IOException e) {
            throw new CTFIOException("Output file can't be created: " + outFilePath, e); //$NON-NLS-1$
        }

        fStreamPacketOutputWriter = new CTFStreamPacketOutputWriter(streamInput);
    }

    /**
     * Copies packets from the relevant input this input stream to a corresponding
     * output stream based on a given time range. The following condition has to be
     * met so that a packet is written to the output stream:
     *
     * startTime <= packet.getTimestampEnd() && packet.getTimestampStart() <=
     * endTime
     *
     * @param startTime
     *                      the start time for packets to be written
     * @param endTime
     *                      the end time for packets to be written
     * @throws CTFException
     *                          if a reading or writing error occurs
     * @since 1.0
     */
    public void copyPackets(long startTime, long endTime) throws CTFException {
        CTFStreamInput streamInput = fStreamInput;
        if (streamInput == null) {
            throw new CTFIOException("StreamInput is null. Can't copy packets"); //$NON-NLS-1$
        }

        try (FileChannel fc = checkNotNull(FileChannel.open(fOutFile.toPath(), StandardOpenOption.WRITE));
                FileChannel source = FileChannel.open(streamInput.getFile().toPath(), StandardOpenOption.READ);) {
            StreamInputPacketIndex index = streamInput.getIndex();
            int count = 0;
            long initialLost = 0;
            for (int i = 0; i < index.size(); i++) {
                ICTFPacketDescriptor entry = index.getElement(i);
                /*
                 * Entire packet is contained
                 */
                long packetStart = entry.getTimestampBegin();
                long packetEnd = entry.getTimestampEnd();
                if (count == 0) {
                    initialLost = (long) entry.getAttributes().getOrDefault(CTFStrings.EVENTS_DISCARDED, 0);
                }
                if (startTime <= packetStart && endTime >= packetEnd) {
                    // MUCH faster
                    fStreamPacketOutputWriter.writePacket(entry, fc, initialLost);
                    count++;
                } else if (startTime <= packetEnd && endTime >= packetStart) {
                    fStreamPacketOutputWriter.writePacket(entry, startTime, endTime, initialLost, fc);
                    count++;
                } else if (entry.getTimestampBegin() > endTime) {
                    break;
                }
            }

            if (count == 0 && fOutFile.exists() || fOutFile.length() == 0) {
                boolean deleteResult = fOutFile.delete();
                if (!deleteResult) {
                    throw new CTFIOException("Could not delete " + fOutFile.getAbsolutePath()); //$NON-NLS-1$
                }
            }
        } catch (IOException e) {
            throw new CTFIOException("Error copying packets: " + e.toString(), e); //$NON-NLS-1$
        }
    }

    /**
     * Get the stream file to write.
     *
     * @return the stream file to write
     */
    public File getOutFile() {
        return fOutFile;
    }

}
