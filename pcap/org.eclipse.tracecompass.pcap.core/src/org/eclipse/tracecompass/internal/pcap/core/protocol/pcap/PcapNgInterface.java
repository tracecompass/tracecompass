/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.pcap.core.protocol.pcap;

/**
 * Class that represents a PcapNg interface description
 */

public class PcapNgInterface {

    private final long fPosition;
    private final short fLinkType;
    private final int fSnapLen;
    private final byte fTsResol;
    private final long fTsOffset;

    /**
     * Constructor
     *
     * @param position
     *            position (file offset) of this IDB in the file
     * @param linkType
     *            link type
     * @param snapLen
     *            snapshot length
     * @param tsResol
     *            timestamp resolution
     * @param tsOffset
     *            timestamp offset
     *
     */
    public PcapNgInterface(long position, short linkType, int snapLen, byte tsResol, long tsOffset) {
        fPosition = position;
        fLinkType = linkType;
        fSnapLen = snapLen;
        fTsResol = tsResol;
        fTsOffset = tsOffset;
    }

    /**
     * Get the position (file offset) of this IDB in the file
     *
     * @return the position (file offset)
     */
    public long getPosition() {
        return fPosition;
    }

    /**
     * Get the link type
     *
     * @return the link type
     */
    public short getLinkType() {
        return fLinkType;
    }

    /**
     * Get the snapshot length, in octets
     *
     * @return the snapshot length
     */
    public int getSnapLen() {
        return fSnapLen;
    }

    /**
     * Get the timestamp resolution, in negative power of 10 (if MSB is zero) or
     * in negative power of 2 (if MSB is one)
     *
     * @return the timstamp resolution
     */
    public byte getTsResol() {
        return fTsResol;
    }

    /**
     * Get the timestamp offset, in seconds
     *
     * @return the timstamp offset
     */
    public long getTsOffset() {
        return fTsOffset;
    }

}
