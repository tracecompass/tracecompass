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

package org.eclipse.linuxtools.internal.tmf.pcap.core.util;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.pcap.core.protocol.Protocol;
import org.eclipse.linuxtools.tmf.pcap.core.protocol.TmfProtocol;

/**
 * Helper class that allows the conversion between Protocol and TmfProtocol.
 * This is only used by this project and thus is internal (not API).
 *
 * @author Vincent Perot
 */
public class ProtocolConversion {

    /**
     * Wrap a Protocol into a TmfProtocol.
     *
     * @param protocol
     *            The Protocol.
     * @return The TmfProtocol.
     */
    public static TmfProtocol wrap(Protocol protocol) {
        @SuppressWarnings("null")
        @NonNull String name = protocol.name();

        @NonNull TmfProtocol wrappedProtocol = TmfProtocol.valueOf(name);
        return wrappedProtocol;
    }

    /**
     * Unwrap a TmfProtocol into a Protocol.
     *
     * @param protocol
     *            The TmfProtocol.
     * @return The Protocol.
     */
    public static Protocol unwrap(TmfProtocol protocol) {
        @SuppressWarnings("null")
        @NonNull String name = protocol.name();
        return Protocol.valueOf(name);
    }

}
