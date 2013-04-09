/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial API and Implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.statistics;

import org.eclipse.osgi.util.NLS;

/**
 * Messages file for statistics view strings.
 *
 * @author Mathieu Denis
 * @since 2.0
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.ui.viewers.statistics.messages"; //$NON-NLS-1$

    /**
     * String for unknown trace name.
     */
    public static String TmfStatisticsView_UnknownTraceName;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
