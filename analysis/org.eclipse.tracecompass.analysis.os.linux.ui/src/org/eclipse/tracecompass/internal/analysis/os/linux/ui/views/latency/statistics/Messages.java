/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency.statistics;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.osgi.util.NLS;

/**
 * Messages used in the LTTng kernel CPU usage view and viewers.
 *
 * @author Bernd Hufmann
 */
@NonNullByDefault({})
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency.statistics.messages"; //$NON-NLS-1$

    /** Name of the system call level in statistics tree */
    public static String LatencyStatistics_SyscallLevelName;
    /** Name of level column */
    public static String LatencyStatistics_LevelLabel;
    /** Name of the minimum column */
    public static String LatencyStatistics_MinLabel;
    /** Name of maximum column */
    public static String LatencyStatistics_MaxLabel;
    /** Name of average column */
    public static String LatencyStatistics_AverageLabel;
    /** Name of Total statistics */
    public static String LatencyStatistics_TotalLabel;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
