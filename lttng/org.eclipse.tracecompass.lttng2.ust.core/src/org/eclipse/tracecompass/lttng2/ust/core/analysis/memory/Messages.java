/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Guilliano Molaire - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.lttng2.ust.core.analysis.memory;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for the ust memory analysis module
 *
 * @author Guilliano Molaire
 * @deprecated This class does not need to be API. It has been moved to
 *             {@link org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.memory.Messages}
 */
@Deprecated
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.lttng2.ust.core.analysis.memory.messages"; //$NON-NLS-1$

    /** Information regarding events loading prior to the analysis execution */
    public static String UstMemoryAnalysisModule_EventsLoadingInformation;

    /** Example of how to execute the application with the libc wrapper */
    public static String UstMemoryAnalysisModule_EventsLoadingExampleInformation;

    /**
     * Chart title
     *
     * @since 3.1
     */
    public static String MemoryUsageDataProvider_Title;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
