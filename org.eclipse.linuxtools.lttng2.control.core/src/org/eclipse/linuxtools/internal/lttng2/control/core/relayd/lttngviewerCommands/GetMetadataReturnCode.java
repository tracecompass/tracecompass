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
 * Get metadata return code
 *
 * @author Matthew Khouzam
 * @since 3.0
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