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
 * Return codes for "new streams" command
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
public enum NewStreamsReturnCode implements IBaseCommand {

    /** If new streams are being sent. */
    LTTNG_VIEWER_NEW_STREAMS_OK(1),
    /** If no new streams are available. */
    LTTNG_VIEWER_NEW_STREAMS_NO_NEW(2),
    /** Error. */
    LTTNG_VIEWER_NEW_STREAMS_ERR(3),
    /** Session closed. */
    LTTNG_VIEWER_NEW_STREAMS_HUP(4);

    private final int fCode;

    private NewStreamsReturnCode(int c) {
        fCode = c;
    }

    @Override
    public int getCommand() {
        return fCode;
    }
}
