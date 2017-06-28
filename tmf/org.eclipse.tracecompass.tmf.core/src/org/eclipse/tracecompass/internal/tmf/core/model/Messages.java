/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.model;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for the common status messages
 *
 * @author Yonni Chen
 * @since 3.0
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.core.model.messages"; //$NON-NLS-1$

    /**
     * Detailed message for running status
     */
    public static @Nullable String CommonStatusMessage_Running;

    /**
     * Detailed message for completed status
     */
    public static @Nullable String CommonStatusMessage_Completed;

    /**
     * Detailed message for cancelled status cause by a progress monitor
     */
    public static @Nullable String CommonStatusMessage_TaskCancelled;

    /**
     * Detailed message for failed status cause by an analysis initialization failure
     */
    public static @Nullable String CommonStatusMessage_AnalysisInitializationFailed;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
