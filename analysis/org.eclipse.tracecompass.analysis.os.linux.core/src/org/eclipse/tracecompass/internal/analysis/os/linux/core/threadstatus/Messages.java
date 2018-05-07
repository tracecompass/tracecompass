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

class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus.messages"; //$NON-NLS-1$

    /** attribute cpu name */
    public static String ThreadStatusDataProvider_attributeCpuName;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
