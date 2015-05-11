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
package org.eclipse.tracecompass.internal.tmf.remote.ui.preferences;

import org.eclipse.osgi.util.NLS;

/**
 * Messages file for the TMF remote UI preferences package.
 *
 * @author Bernd Hufmann
 */
@SuppressWarnings("javadoc")
public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.remote.ui.preferences.messages"; //$NON-NLS-1$

    public static String TraceControl_CommandTimeout;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
