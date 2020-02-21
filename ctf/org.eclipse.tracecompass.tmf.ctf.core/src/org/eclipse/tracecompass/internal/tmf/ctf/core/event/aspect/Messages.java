/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ctf.core.event.aspect;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.osgi.util.NLS;

/**
 * Messages
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.ctf.core.event.aspect.messages"; //$NON-NLS-1$

    /** Packet header name */
    public static String CtfPacketHeaderAspect_name;
    /** Packet header description */
    public static String CtfPacketHeaderAspect_description;
    /** Packet context name */
    public static String CtfPacketContextAspect_name;
    /** Packet context description */
    public static String CtfPacketContextAspect_description;
    /** Stream context name */
    public static String CtfStreamContextAspect_name;
    /** Stream context description */
    public static String CtfStreamContextAspect_description;
    /** Event context name */
    public static String CtfEventContextAspect_name;
    /** Event context description */
    public static String CtfEventContextAspect_description;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    /**
     * NonNull wrapper
     *
     * @param msg
     *            message
     * @return message or "" if msg is null
     */
    public static @NonNull String getMessage(String msg) {
        if (msg == null) {
            return ""; //$NON-NLS-1$
        }
        return msg;
    }

    private Messages() {
        // Do nothing
    }
}
