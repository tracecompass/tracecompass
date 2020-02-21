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
 * Seek command
 *
 * @author Matthew Khouzam
 */
public enum SeekCommand implements IBaseCommand {

    /** Receive the trace packets from the beginning. */
    VIEWER_SEEK_BEGINNING(1),
    /** Receive the trace packets from now. */
    VIEWER_SEEK_LAST(2);

    private final int fCode;

    /**
     * Command size (fCode)
     */
    public static final int SIZE = Integer.SIZE / 8;

    private SeekCommand(int c) {
        fCode = c;
    }

    @Override
    public int getCommand() {
        return fCode;
    }
}