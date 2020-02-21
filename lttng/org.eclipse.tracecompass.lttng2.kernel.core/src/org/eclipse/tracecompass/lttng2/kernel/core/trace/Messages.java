/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.trace;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for lttng2.kernel.core.trace
 *
 * @author Matthew Khouzam
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.lttng2.kernel.core.trace.messages"; //$NON-NLS-1$

    /**
     * The domain is not "kernel"
     */
    public static String LttngKernelTrace_DomainError;
    /**
     * Malformed trace (buffer overflow maybe?)
     */
    public static String LttngKernelTrace_MalformedTrace;
    /**
     * Trace read error
     */
    public static String LttngKernelTrace_TraceReadError;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
