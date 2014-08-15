/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.pcap.core.signal;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.internal.tmf.pcap.core.event.TmfPacketStream;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;

/**
 * TmfSignal that is broadcasted when a new packet stream is chosen. Views that
 * are network-specific can uses this signal to track when the user selects a
 * new stream.
 *
 * @author Vincent Perot
 */
public class TmfPacketStreamSelectedSignal extends TmfSignal {

    private final @Nullable TmfPacketStream fStream;

    /**
     * Standard constructor
     *
     * @param source
     *            Object sending this signal
     * @param reference
     *            Reference index to assign to this signal
     * @param stream
     *            The new stream. It can be null if the user cleared the
     *            selection.
     */
    public TmfPacketStreamSelectedSignal(Object source, int reference, @Nullable TmfPacketStream stream) {
        super(source, reference);
            fStream = stream;
    }

    /**
     * Getter method that returns the stream.
     *
     * @return The stream.
     */
    public @Nullable TmfPacketStream getStream() {
        return fStream;
    }
}
