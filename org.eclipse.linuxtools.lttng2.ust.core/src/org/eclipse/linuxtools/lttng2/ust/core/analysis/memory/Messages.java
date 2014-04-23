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
package org.eclipse.linuxtools.lttng2.ust.core.analysis.memory;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for the ust memory analysis module
 *
 * @author Guilliano Molaire
 * @since 3.0
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.lttng2.ust.core.analysis.memory.messages"; //$NON-NLS-1$

    /** Information regarding events loading prior to the analysis execution */
    public static String UstMemoryAnalysisModule_EventsLoadingInformation;

    /** Example of how to execute the application with the libc wrapper */
    public static String UstMemoryAnalysisModule_EventsLoadingExampleInformation;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
