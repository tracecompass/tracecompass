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
 * Get viewer connection type
 *
 * @author Matthew Khouzam
 */
public enum ConnectionType implements IBaseCommand {

    /** Command sent */
    VIEWER_CLIENT_COMMAND(1),
    /** Notification sent */
    VIEWER_CLIENT_NOTIFICATION(2);

    private final int fCode;

    private ConnectionType(int c) {
        fCode = c;
    }

    @Override
    public int getCommand() {
        return fCode;
    }

}