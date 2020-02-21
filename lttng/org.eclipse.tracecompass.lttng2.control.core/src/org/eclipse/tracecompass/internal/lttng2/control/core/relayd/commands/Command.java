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

/**
 * Viewer commands
 *
 * @author Matthew Khouzam
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