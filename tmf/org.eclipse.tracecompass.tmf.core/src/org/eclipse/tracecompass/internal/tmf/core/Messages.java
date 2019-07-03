/*******************************************************************************
 * Copyright (c) 2013, 2018 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * TMF Core message bundle
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.core.messages"; //$NON-NLS-1$
    public static String TmfCheckpointIndexer_EventsPerSecond;
    public static String TmfCheckpointIndexer_Indexing;

    /**
     * String to refer to the start time
     */
    public static @Nullable String TmfStrings_StartTime;
    /**
     * String to refer to the end time
     */
    public static @Nullable String TmfStrings_EndTime;
    /**
     * String to refer to the time
     */
    public static @Nullable String TmfStrings_Time;
    /**
     * String to refer to the duration
     */
    public static @Nullable String TmfStrings_Duration;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
