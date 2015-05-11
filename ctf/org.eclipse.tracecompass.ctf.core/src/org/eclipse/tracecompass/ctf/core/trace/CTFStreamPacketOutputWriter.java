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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * CTF trace packet writer.
 *
 * @author Bernd Hufmann
 * @since 1.0
 */
public class CTFStreamPacketOutputWriter {

    /**
     * Writes a stream packet to the output file channel based on the packet
     * descriptor information.
     *
     * @param byteBuffer
     *            a byte buffer with packet to write
     * @param fc
     *            a file channel
     * @throws IOException
     *            if a reading or writing error occurs
     */
    public void writePacket(ByteBuffer byteBuffer, FileChannel fc) throws IOException {
        fc.write(byteBuffer);
    }

}
