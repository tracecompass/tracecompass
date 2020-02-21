/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.trace;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility methods for traces in the CTF format.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
@NonNullByDefault
public final class CtfUtils {

    private CtfUtils() {
    }

    /**
     * Convenience method to get the tracer name from the trace's metadata. The
     * leading and trailing '"' will be stripped from the returned string.
     *
     * @param trace
     *            The trace to query
     * @return The tracer's name, or null if it is not defined in the metadata.
     */
    public static @Nullable String getTracerName(CtfTmfTrace trace) {
        String str = trace.getEnvironment().get("tracer_name"); //$NON-NLS-1$
        if (str == null) {
            return null;
        }
        /* Remove the '"' at the start and end of the string */
        return str.replaceAll("^\"|\"$", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Convenience method to get the tracer's major version from the trace
     * metadata.
     *
     * @param trace
     *            The trace to query
     * @return The tracer's major version, or -1 if it is not defined.
     */
    public static int getTracerMajorVersion(CtfTmfTrace trace) {
        String str = trace.getEnvironment().get("tracer_major"); //$NON-NLS-1$
        if (str == null) {
            return -1;
        }

        /* Remove the '"' at the start and end of the string (ex:"2") */
        str = str.replaceAll("^\"|\"$", ""); //$NON-NLS-1$ //$NON-NLS-2$

        try {
            int ret = Integer.parseInt(str);
            return ret;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Convenience method to get the tracer's minor version from the trace
     * metadata.
     *
     * @param trace
     *            The trace to query
     * @return The tracer's minor version, or -1 if it is not defined.
     */
    public static int getTracerMinorVersion(CtfTmfTrace trace) {
        String str = trace.getEnvironment().get("tracer_minor"); //$NON-NLS-1$
        if (str == null) {
            return -1;
        }
        /* Remove the '"' at the start and end of the string (ex:"9") */
        str = str.replaceAll("^\"|\"$", ""); //$NON-NLS-1$ //$NON-NLS-2$
        try {
            int ret = Integer.parseInt(str);
            return ret;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
