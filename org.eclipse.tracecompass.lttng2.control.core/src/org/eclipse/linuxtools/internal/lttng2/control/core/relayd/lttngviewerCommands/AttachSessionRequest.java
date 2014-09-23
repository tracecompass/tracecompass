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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * VIEWER_ATTACH_SESSION payload.
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
public class AttachSessionRequest implements IRelayCommand {

    /**
     * Command size
     *
     * fSessionId + fOffset + fSeek
     */
    public static final int SIZE = (Long.SIZE + Long.SIZE) / 8 + SeekCommand.SIZE;
    /** the id of a session */
    private final long fSessionId;
    /** unused for now */
    private final long fOffset;
    /** enum lttng_viewer_seek */
    private final SeekCommand fSeek;

    /**
     * Attach session request constructor
     *
     * @param id
     *            the session id
     * @param seekCommand
     *            the seek command
     */
    public AttachSessionRequest(long id, SeekCommand seekCommand) {
        this(id, 0, seekCommand);
    }

    /**
     * Attach session request constructor
     *
     * @param id
     *            the session id
     * @param offset
     *            unused for now
     * @param seekCommand
     *            the seek command
     */

    public AttachSessionRequest(long id, int offset, SeekCommand seekCommand) {
        fSessionId = id;
        fOffset = offset;
        fSeek = seekCommand;

    }

    @Override
    public byte[] serialize() {
        byte data[] = new byte[SIZE];
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putLong(fSessionId);
        bb.putLong(fOffset);
        bb.putInt(fSeek.getCommand());
        return data;
    }

}