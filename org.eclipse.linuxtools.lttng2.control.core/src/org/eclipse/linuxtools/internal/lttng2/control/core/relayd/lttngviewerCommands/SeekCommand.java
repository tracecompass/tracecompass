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
 * Seek command
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
public enum SeekCommand implements IBaseCommand {

    /** Receive the trace packets from the beginning. */
    VIEWER_SEEK_BEGINNING(1),
    /** Receive the trace packets from now. */
    VIEWER_SEEK_LAST(2);

    private final int fCode;

    /**
     * Size of the enum
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