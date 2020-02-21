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
 * Get metadata return code
 *
 * @author Matthew Khouzam
 */
public enum GetMetadataReturnCode implements IBaseCommand {

    /** Response was OK */
    VIEWER_METADATA_OK(1),
    /** Response was nothing new */
    VIEWER_NO_NEW_METADATA(2),
    /** Response was Error */
    VIEWER_METADATA_ERR(3);

    private final int fCode;

    private GetMetadataReturnCode(int c) {
        fCode = c;
    }

    @Override
    public int getCommand() {
        return fCode;
    }

}