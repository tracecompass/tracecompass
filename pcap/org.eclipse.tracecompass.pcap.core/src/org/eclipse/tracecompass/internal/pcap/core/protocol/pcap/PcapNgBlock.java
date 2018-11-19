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

package org.eclipse.tracecompass.internal.pcap.core.protocol.pcap;

import java.nio.ByteBuffer;

/**
 * Class that represents a PcapNg block where the body content is depending on
 * the block type
 *
 * @author Viet-Hung Phan
 */

public class PcapNgBlock {

    private final long fPosition;
    private final int fBlockType;
    private final int fBlockLength;
    private final ByteBuffer fBlockBody;

    /**
     * Constructor of the PcapNgBlock Class.
     *
     * @param position
     *            the position (file offset) of this block in the file
     * @param blockType
     *            block type
     * @param blockLength
     *            block total length
     * @param blockBody
     *            block body
     *
     */
    public PcapNgBlock(long position, int blockType, int blockLength, ByteBuffer blockBody) {
        fPosition = position;
        fBlockType = blockType;
        fBlockLength = blockLength;
        fBlockBody = blockBody;
    }

    /**
     * Get the position (file offset) of this block in the file
     *
     * @return the position (file offset)
     */
    public long getPosition() {
        return fPosition;
    }

    /**
     * Get the block type
     *
     * @return the block type
     */
    public int getBlockType() {
        return fBlockType;
    }

    /**
     * Get the block total length
     *
     * @return the block total length
     */
    public int getBlockLength() {
        return fBlockLength;
    }

    /**
     * Get the block body
     *
     * @return the block body
     */
    public ByteBuffer getBlockBody() {
        return fBlockBody;
    }

}
