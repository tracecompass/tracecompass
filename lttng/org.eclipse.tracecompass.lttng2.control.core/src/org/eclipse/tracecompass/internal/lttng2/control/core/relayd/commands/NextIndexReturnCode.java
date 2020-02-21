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
 * Get next index return code (hope it's viewer_index_ok)
 *
 * @author Matthew Khouzam
 */
public enum NextIndexReturnCode implements IBaseCommand {

    /** Index is available. */
    VIEWER_INDEX_OK(1),
    /** Index not yet available. */
    VIEWER_INDEX_RETRY(2),
    /** Index closed (trace destroyed). */
    VIEWER_INDEX_HUP(3),
    /** Unknown error. */
    VIEWER_INDEX_ERR(4),
    /** Inactive stream beacon. */
    VIEWER_INDEX_INACTIVE(5),
    /** End of index file. */
    VIEWER_INDEX_EOF(6);

    private final int fCode;

    private NextIndexReturnCode(int c) {
        fCode = c;
    }

    @Override
    public int getCommand() {
        return fCode;
    }
}