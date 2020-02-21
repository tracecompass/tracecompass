/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.remote.core.messages;

import org.eclipse.osgi.util.NLS;

/**
 * Messages file for the trace control package.
 *
 * @author Bernd Hufmann
 */
@SuppressWarnings("javadoc")
public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.remote.core.messages.messages"; //$NON-NLS-1$

    // Failures
    public static String RemoteConnection_ExecutionCancelled;
    public static String RemoteConnection_ExecutionFailure;
    public static String RemoteConnection_ExecutionTimeout;
    public static String RemoteConnection_ShellNotConnected;
    public static String RemoteConnection_CommandShellError;
    public static String RemoteConnection_ServiceNotDefined;

    public static String RemoteConnection_ConnectionError;
    public static String RemoteConnection_DuplicateConnectionError;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
