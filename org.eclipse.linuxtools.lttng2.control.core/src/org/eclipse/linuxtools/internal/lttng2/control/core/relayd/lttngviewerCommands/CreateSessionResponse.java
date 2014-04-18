/**********************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial implementation and API
 *   Marc-Andre Laperle - Initial implementation and API
 **********************************************************************/

package org.eclipse.linuxtools.internal.lttng2.control.core.relayd.lttngviewerCommands;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Response to a "create session" command
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
public class CreateSessionResponse implements IRelayResponse {

    /**
     * Response size
     */
    public static final int SIZE = Integer.SIZE / 8;

    /** enum lttng_viewer_create_session_return_code */
    private final CreateSessionReturnCode fStatus;

    /**
     * Create session response network constructor
     *
     * @param inNet
     *            network input stream
     * @throws IOException
     *             network error
     */
    public CreateSessionResponse(DataInputStream inNet) throws IOException {
        byte[] data = new byte[SIZE];
        inNet.readFully(data);
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.BIG_ENDIAN);
        fStatus = (CreateSessionReturnCode.values()[bb.getInt() - 1]);
    }

    /**
     * Get status
     *
     * @return the status
     */
    public CreateSessionReturnCode getStatus() {
        return fStatus;
    }

}