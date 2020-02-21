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
 * Create new session return code
 *
 * @author Matthew Khouzam
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
