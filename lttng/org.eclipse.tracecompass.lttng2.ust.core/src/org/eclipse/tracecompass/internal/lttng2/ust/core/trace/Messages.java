/*******************************************************************************
 * Copyright (c) 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.internal.lttng2.ust.core.trace;

import org.eclipse.osgi.util.NLS;

/**
 * Messages for aspects
 *
 * @author Matthew Khouzam
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.lttng2.ust.core.trace.messages"; //$NON-NLS-1$

    /**
     * tracef name
     */
    public static String UstTracefAspect_Name;

    /**
     * tracef description
     */
    public static String UstTracefAspect_Description;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // do nothing
    }
}
