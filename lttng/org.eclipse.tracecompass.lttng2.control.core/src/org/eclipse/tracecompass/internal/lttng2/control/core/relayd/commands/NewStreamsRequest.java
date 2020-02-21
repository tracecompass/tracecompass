/**********************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial implementation and API
 *   Marc-Andre Laperle - Initial implementation and API
 **********************************************************************/

package org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * LTTNG_VIEWER_GET_NEW_STREAMS payload.
 *
 * @author Matthew Khouzam
 */
public class NewStreamsRequest implements IRelayCommand {

    /**
     * Command size (fSessionId)
     */
    public static final int SIZE = Long.SIZE / 8;

    /** session ID */
    private final long fSessionId;

    /**
     * Constructor
     *
     * @param sessionId
     *            the session id we want
     */
    public NewStreamsRequest(long sessionId) {
        fSessionId = sessionId;
    }

    @Override
    public byte[] serialize() {
        byte data[] = new byte[SIZE];
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putLong(fSessionId);
        return data;
    }
}
