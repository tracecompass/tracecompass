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

package org.eclipse.tracecompass.internal.ctf.core.trace;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.types.ICompositeDefinition;
import org.eclipse.tracecompass.ctf.core.trace.ICTFPacketDescriptor;
import org.eclipse.tracecompass.ctf.core.trace.IPacketReader;
import org.eclipse.tracecompass.internal.ctf.core.event.EventDefinition;

/**
 * Null packet reader, used for unset packets
 */
@NonNullByDefault
public final class NullPacketReader implements IPacketReader {

    /**
     * Instance of a null packet reader
     */
    public static final NullPacketReader INSTANCE = new NullPacketReader();

    private NullPacketReader() { }

    @Override
    public int getCPU() {
        return UNKNOWN_CPU;
    }

    @Override
    public boolean hasMoreEvents() {
        return false;
    }

    @Override
    public @Nullable EventDefinition readNextEvent() throws CTFException {
        return null;
    }

    @Override
    public @Nullable ICTFPacketDescriptor getCurrentPacket() {
        return null;
    }

    @Override
    public @Nullable ICompositeDefinition getCurrentPacketEventHeader() {
        return null;
    }
}
