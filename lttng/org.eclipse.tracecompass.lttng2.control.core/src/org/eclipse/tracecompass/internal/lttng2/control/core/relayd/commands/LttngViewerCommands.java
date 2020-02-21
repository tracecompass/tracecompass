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
 *   Matthew Khouzam - Initial implementation
 **********************************************************************/

package org.eclipse.tracecompass.internal.lttng2.control.core.relayd.commands;

/**
 * LTTng Relay Daemon API. needs a TCP connection, API is defined from
 * BSD-licensed implementation in Lttng tools: <a href=
 * "http://git.lttng.org/?p=lttng-tools.git;a=blob;f=src/bin/lttng-relayd/lttng-viewer-abi.h"
 * > src/bin/lttng-relayd/lttng-viewer-abi.h</a>
 *
 * @author Matthew Khouzam
 */
public interface LttngViewerCommands {

    /** Maximum path name length */
    static final int LTTNG_VIEWER_PATH_MAX = 4096;
    /** Maximum name length */
    static final int LTTNG_VIEWER_NAME_MAX = 255;
    /** Maximum host name length */
    static final int LTTNG_VIEWER_HOST_NAME_MAX = 64;
    /** New stream in the trace */
    static final int NEW_STREAM = (1 << 1);
    /** New metadata in the trace */
    static final int NEW_METADATA = (1 << 0);

}
