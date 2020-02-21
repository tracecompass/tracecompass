/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.profiling.core.callgraph;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for the call stack analysis module
 *
 * @author Sonia Farrah
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$
    /**
     * Analysis description for the help
     */
    public static @Nullable String CallGraphAnalysis_Description;
    /**
     * The call stack event's name
     */
    public static @Nullable String CallStack_FunctionName;
    /**
     * Querying state system error's message
     */
    public static @Nullable String QueringStateSystemError;
    /**
     * Segment's start time exceeding its end time Error message
     */
    public static @Nullable String TimeError;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
