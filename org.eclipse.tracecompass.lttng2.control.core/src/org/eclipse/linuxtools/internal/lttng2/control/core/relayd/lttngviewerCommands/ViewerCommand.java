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
 * The LTTng command
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
public class ViewerCommand implements IRelayCommand {

    /**
     * Command size
     *
     * fDataSize + fCmdVersion + fCmd
     */
    public static final int SIZE = (Long.SIZE + Integer.SIZE) / 8 + Command.SIZE;
    /**
     * data size following this header, you normally attach a payload that one,
     * in bytes
     */
    private final long fDataSize;
    /** enum lttcomm_relayd_command */
    private final Command fCmd;
    /** command version */
    private final int fCmdVersion;

    /**
     * Sets the packet command
     *
     * @param viewerConnect
     *            the command
     * @param size size of the command
     * @param version the version number
     */
    public ViewerCommand(Command viewerConnect, long size, int version) {
        fCmd = viewerConnect;
        fDataSize = size;
        fCmdVersion = version;
    }

    /**
     * Get the data size
     *
     * @return the DataSize
     */
    public long getDataSize() {
        return fDataSize;
    }

    /**
     * Get the command
     *
     * @return the Cmd
     */
    public Command getCmd() {
        return fCmd;
    }

    /**
     * Get the command version
     *
     * @return the CmdVersion
     */
    public int getCmdVersion() {
        return fCmdVersion;
    }

    @Override
    public byte[] serialize() {
        byte data[] = new byte[SIZE];
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putLong(getDataSize());
        bb.putInt(getCmd().getCommand());
        bb.putInt(fCmdVersion);
        return data;
    }


}