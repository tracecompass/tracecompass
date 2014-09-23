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

/**
 * Viewer commands
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
public enum Command implements IBaseCommand {

    /** get version */
    VIEWER_CONNECT(1),
    /** list all lttng sessions */
    VIEWER_LIST_SESSIONS(2),
    /** attach to a session */
    VIEWER_ATTACH_SESSION(3),
    /** get the next index */
    VIEWER_GET_NEXT_INDEX(4),
    /** get packet */
    VIEWER_GET_PACKET(5),
    /** get metadata */
    VIEWER_GET_METADATA(6),
    /** get new streams */
    VIEWER_GET_NEW_STREAMS(7),
    /** create a new session */
    VIEWER_CREATE_SESSION(8);

    /**
     * Command size (fCode)
     */
    public static final int SIZE = Integer.SIZE / 8;

    private final int fCode;

    private Command(int c) {
        fCode = c;
    }

    @Override
    public int getCommand() {
        return fCode;
    }
}