/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.aspect;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.tmf.core.event.aspect.messages"; //$NON-NLS-1$

    public static @Nullable String AspectName_Timestamp;
    public static @Nullable String AspectName_EventType;
    public static @Nullable String AspectName_Contents;
    public static @Nullable String AspectName_TraceName;
    public static @Nullable String AspectName_CPU;

    public static @Nullable String AspectHelpText_EventType;
    public static @Nullable String AspectHelpText_Contents;
    public static @Nullable String AspectHelpText_TraceName;
    public static @Nullable String AspectHelpText_CPU;
    public static @Nullable String AspectHelpText_Statesystem;
    /**
     * Nanosecond normalized timestamp
     * @since 6.1
     */
    public static @Nullable String AspectName_Timestamp_Nanoseconds;
    /**
     * Explanation of why use a nanosecond normalized timestamp
     * @since 6.1
     */
    public static @Nullable String AspectName_Timestamp_Nanoseconds_Help;

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
            return ""; //$NON-NLS-1$
        }
        return msg;
    }
}
