/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial API and Implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.statistics.model;

import org.eclipse.osgi.util.NLS;

/**
 * Message strings for the statistics framework.
 *
 * @version 1.0
 * @author Mathieu Denis
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.ui.views.statistics.model.messages"; //$NON-NLS-1$

    /**
     * CPU statistic name.
     */
    public static String TmfStatisticsData_CPUs;

    /**
     * Event type statistic name.
     */
    public static String TmfStatisticsData_EventTypes;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
