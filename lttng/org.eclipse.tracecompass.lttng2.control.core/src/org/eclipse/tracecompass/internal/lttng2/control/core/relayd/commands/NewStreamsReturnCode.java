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
 * Return codes for "new streams" command
 *
 * @author Matthew Khouzam
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
