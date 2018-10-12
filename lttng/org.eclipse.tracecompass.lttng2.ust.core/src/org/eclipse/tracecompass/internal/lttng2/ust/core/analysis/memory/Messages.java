/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Guilliano Molaire - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.memory;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for the ust memory analysis module
 *
 * @author Guilliano Molaire
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.memory.messages"; //$NON-NLS-1$

    /** The help text for unfreed memory tid aspect */
    public static String SegmentAspectHelpText_PotentialLeakTid;
    /** Information regarding events loading prior to the analysis execution */
    public static String UstMemoryAnalysisModule_EventsLoadingInformation;

    /** Example of how to execute the application with the libc wrapper */
    public static String UstMemoryAnalysisModule_EventsLoadingExampleInformation;

    /**
     * Chart title
     */
    public static String MemoryUsageDataProvider_Title;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Do nothing
    }
}
