/*******************************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus;

import org.eclipse.osgi.util.NLS;

/**
 * Externalized Strings for the {@link ThreadStatusDataProvider} package
 */
class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus.messages"; //$NON-NLS-1$

    /** The data provider title text */
    public static String ThreadStatusDataProviderFactory_title;
    /**
     * The data provider description text
     */
    public static String ThreadStatusDataProviderFactory_descriptionText;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
