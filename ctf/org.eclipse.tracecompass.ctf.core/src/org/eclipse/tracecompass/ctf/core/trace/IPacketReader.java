/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.trace;

import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.event.types.ICompositeDefinition;

/**
 * Packet reader interface, allows for more flexible packet readers. A packet
 * reader must be able to do one thing only: read a ctf packet.
 *
 * In order to do that, it will have access to the packet and can then iterate
 * over the packet with two main functions {@link #readNextEvent()} and
 * {@link #hasMoreEvents()}. The packet reader must also have a notion of
 * whether a given packet has an assigned CPU or not, which will be given
 * by @link {@link #getCPU()}, as well as the event header, defined in
 * {@link #getCurrentPacketEventHeader()}. The packet description in the reader
 * can be obtained by calling {@link #getCurrentPacket()}
 *
 * @author Matthew Khouzam
 *
 * @since 2.0
 */
public interface IPacketReader {

    /**
     * The value of a cpu if it is unknown to the packet reader
     */
    int UNKNOWN_CPU = -1;

    /**
     * Gets the CPU (core) number
     *
     * @return the CPU (core) number
     */
    int getCPU();

    /**
     * Returns whether it is possible to read any more events from this packet.
     *
     * @return True if it is possible to read any more events from this packet.
     */
    boolean hasMoreEvents();

    /**
     * Reads the next event of the packet into the right event definition.
     *
     * @return The event definition containing the event data that was just
     *         read.
     * @throws CTFException
     *             If there was a problem reading the trace
     */
    IEventDefinition readNextEvent() throws CTFException;

    /**
     * Get the packet being read
     *
     * @return the packet being read
     */
    ICTFPacketDescriptor getCurrentPacket();

    /**
     * Get the current event header definition
     *
     * @return the current event header definition
     */
    ICompositeDefinition getCurrentPacketEventHeader();

}