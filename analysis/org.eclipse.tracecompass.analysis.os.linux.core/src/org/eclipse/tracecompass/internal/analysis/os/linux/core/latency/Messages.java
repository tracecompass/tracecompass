/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.latency;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * Messages for Syscall latency analysis.
 * @since 2.0
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.messages"; //$NON-NLS-1$

    /** System Call latency analysis aspect name */
    public static @Nullable String SegmentAspectName_SystemCall;

    /** System Call latency analysis aspect help text */
    public static @Nullable String SegmentAspectHelpText_SystemCall;

    /** System Call latency analysis aspect help text */
    public static @Nullable String SegmentAspectHelpText_SystemCallTid;

    /** System Call latency analysis aspect name */
    public static @Nullable String SegmentAspectName_SystemCallRet;

    /** System Call latency analysis aspect help text */
    public static @Nullable String SegmentAspectHelpText_SystemCallRet;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }

    /**
     * Helper method to expose externalized strings as non-null objects.
     */
    static String getMessage(@Nullable String msg) {
        if (msg == null) {
            return StringUtils.EMPTY;
        }
        return msg;
    }
}
