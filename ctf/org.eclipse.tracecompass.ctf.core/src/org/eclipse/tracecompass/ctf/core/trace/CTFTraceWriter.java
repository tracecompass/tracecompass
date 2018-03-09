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
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.CTFException;

/**
 * A CTF trace reader. Reads the events of a trace.
 *
 * @author Bernd Hufmann
 * @since 1.0
 */
public class CTFTraceWriter {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The trace to read from.
     */
    private final @Nullable CTFTrace fInTrace;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a TraceReader to read a trace.
     *
     * @param trace
     *            The trace to read from.
     * @throws CTFException
     *             if an error occurs
     */
    public CTFTraceWriter(@NonNull CTFTrace trace) throws CTFException {
        fInTrace = trace;
        try (CTFTraceReader fTraceReader = new CTFTraceReader(fInTrace)) {
            fTraceReader.populateIndex();
        }
    }

    /**
     * Copies packets from the relevant input to the output trace based on a given
     * time range. The following condition has to be met so that a packet is written
     * to the output trace:
     *
     * startTime <= packet.getTimestampBegin() <= endTime
     *
     * @param startTime
     *            start time of packets to be included in output trace
     * @param endTime
     *            end time of packets to be included in the output trace
     * @param newTracePath
     *            the path of the new trace to be written
     * @throws CTFException
     *             If a reading or writing error occurs
     */
    public void copyPackets(long startTime, long endTime, String newTracePath) throws CTFException {
        CTFTrace trace = fInTrace;
        if (trace != null) {
            long adjustedStart = trace.timestampNanoToCycles(startTime);
            long adjustedEnd = trace.timestampNanoToCycles(endTime);
            File out = new File(newTracePath);
            if (out.exists()) {
                if (!out.isDirectory() || out.listFiles().length != 0) {
                    throw new CTFIOException("Trace segment cannot be created since trace already exists: " + newTracePath); //$NON-NLS-1$
                }
            } else {
                // create new directory
                if (!out.mkdirs()) {
                    throw new CTFIOException("Creating trace directory failed: " + newTracePath); //$NON-NLS-1$
                }
            }
            // copy metadata
            Metadata metadata = new Metadata(fInTrace);
            try {
                metadata.copyTo(out);
            } catch (IOException e) {
                throw new CTFIOException("Error copying metadata: " + e.toString(), e); //$NON-NLS-1$
            }

            // Copy packets
            for (ICTFStream stream : trace.getStreams()) {
                Set<CTFStreamInput> inputs = stream.getStreamInputs();
                for (CTFStreamInput s : inputs) {
                    CTFStreamOutputWriter streamOutputwriter = new CTFStreamOutputWriter(checkNotNull(s), out);
                    streamOutputwriter.copyPackets(adjustedStart, adjustedEnd);
                }
            }
        }
    }
}
