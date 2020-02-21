/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.trace;

import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.trace.ICTFPacketDescriptor;

/**
 * Internal implementation to test packet index
 *
 * @author Matthew Khouzam
 */
class PacketStub implements ICTFPacketDescriptor {

    private final long fOffsetBytes;
    private final long fTsStart;
    private final long fTsEnd;

    public PacketStub(long packetNumber, long start, long end) {
        fOffsetBytes = packetNumber * getPacketSizeBits();
        fTsStart = start;
        fTsEnd = end;
    }

    @Override
    public boolean includes(long ts) {
        return ts >= fTsStart && ts <= fTsEnd;
    }

    @Override
    public long getOffsetBits() {
        return fOffsetBytes * 8;
    }

    @Override
    public long getPacketSizeBits() {
        return 3;
    }

    @Override
    public long getContentSizeBits() {
        return 2;
    }

    @Override
    public long getTimestampBegin() {
        return fTsStart;
    }

    @Override
    public long getTimestampEnd() {
        return fTsEnd;
    }

    @Override
    public long getLostEvents() {
        return 0;
    }

    @Override
    public @NonNull Map<String, Object> getAttributes() {
        return Collections.emptyMap();
    }

    @Override
    public String getTarget() {
        return "";
    }

    @Override
    public long getTargetId() {
        return 0;
    }

    @Override
    public long getOffsetBytes() {
        return fOffsetBytes;
    }

    @Override
    public long getPayloadStartBits() {
        return 1;
    }

    @Override
    public String toString() {
        return "[" + fOffsetBytes + ", " + fTsStart + " - " + fTsEnd + "]";
    }

}