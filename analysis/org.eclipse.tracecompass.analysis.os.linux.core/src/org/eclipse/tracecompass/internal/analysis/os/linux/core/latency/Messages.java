/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.latency;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * Messages for Syscall latency analysis.
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.messages"; //$NON-NLS-1$

    /** System Call latency analysis aspect name */
    public static @Nullable String SegmentAspectName_SystemCall;

    /** System Call latency analysis aspect help text */
    public static @Nullable String SegmentAspectHelpText_SystemCall;

    /** System Call TID aspect help text */
    public static @Nullable String SegmentAspectHelpText_SystemCallTid;

    /** System Call return value aspect name */
    public static @Nullable String SegmentAspectName_SystemCallRet;

    /** System Call return value aspect help text */
    public static @Nullable String SegmentAspectHelpText_SystemCallRet;

    /** System Call component aspect name */
    public static @Nullable String SystemCallLatencyAnalysis_componentName;

    /** System Call component aspect help text */
    public static @Nullable String SystemCallLatencyAnalysis_componentDescription;

    /** System Call file aspect name */
    public static @Nullable String SystemCallLatencyAnalysis_fileName;

    /** System Call file aspect help text */
    public static @Nullable String SystemCallLatencyAnalysis_fileDescription;

    /** System Call lookup aspect name */
    public static @Nullable String SystemCallLatencyAnalysis_sourceLookupName;

    /** System Call lookup aspect help text */
    public static @Nullable String SystemCallLatencyAnalysis_sourceLookupDescription;

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
