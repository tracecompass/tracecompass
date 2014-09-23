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
 * Create new session return code
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
public enum CreateSessionReturnCode implements IBaseCommand {

    /** If new streams are being sent. */
    LTTNG_VIEWER_CREATE_SESSION_OK(1),
    /** Fatal error on the server-side. */
    LTTNG_VIEWER_CREATE_SESSION_ERR(2);

    private final int fCode;

    private CreateSessionReturnCode(int c) {
        fCode = c;
    }

    @Override
    public int getCommand() {
        return fCode;
    }
}
