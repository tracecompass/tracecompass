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
 * Return codes for "viewer attach" command
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
public enum AttachReturnCode implements IBaseCommand {

    /** If the attach command succeeded. */
    VIEWER_ATTACH_OK(1),
    /** If a viewer is already attached. */
    VIEWER_ATTACH_ALREADY(2),
    /** If the session ID is unknown. */
    VIEWER_ATTACH_UNK(3),
    /** If the session is not live. */
    VIEWER_ATTACH_NOT_LIVE(4),
    /** Seek error. */
    VIEWER_ATTACH_SEEK_ERR(5),
    /** No session */
    VIEWER_ATTACH_NO_SESSION(6);

    private final int fCode;

    private AttachReturnCode(int c) {
        fCode = c;
    }

    @Override
    public int getCommand() {
        return fCode;
    }
}