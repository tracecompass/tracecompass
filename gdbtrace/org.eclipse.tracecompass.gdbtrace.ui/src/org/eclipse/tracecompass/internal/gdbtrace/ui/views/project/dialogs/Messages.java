/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.gdbtrace.ui.views.project.dialogs;

import org.eclipse.osgi.util.NLS;

@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.gdbtrace.ui.views.project.dialogs.messages"; //$NON-NLS-1$
    public static String SelectTraceExecutableDialog_BinaryError;
    public static String SelectTraceExecutableDialog_Browse;
    public static String SelectTraceExecutableDialog_ExecutableName;
    public static String SelectTraceExecutableDialog_ExecutablePrompt;
    public static String SelectTraceExecutableDialog_Message;
    public static String SelectTraceExecutableDialog_Title;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
