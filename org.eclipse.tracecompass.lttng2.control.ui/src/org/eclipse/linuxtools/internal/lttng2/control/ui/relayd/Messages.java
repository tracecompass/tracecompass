/**********************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc-Andre Laperle - Initial implementation
 **********************************************************************/

package org.eclipse.linuxtools.internal.lttng2.control.ui.relayd;

import org.eclipse.osgi.util.NLS;

/**
 * Messages for the relayd connection.
 *
 * @author Marc-Andre Laperle
 */
public final class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.lttng2.control.ui.relayd.messages"; //$NON-NLS-1$

    /**
     * Error occurred establishing the connection.
     */
    public static String LttngRelaydConnectionManager_ConnectionError;

    /**
     * Error occurred attaching to the session.
     */
    public static String LttngRelaydConsumer_AttachSessionError;

    /**
     * Error occurred creating the viewer session.
     */
    public static String LttngRelaydConsumer_CreateViewerSessionError;

    /**
     * Error occurred connecting to the relayd.
     */
    public static String LttngRelaydConsumer_ErrorConnecting;

    /**
     * Error (generic) during live reading.
     */
    public static String LttngRelaydConsumer_ErrorLiveReading;

    /**
     * No metadata for this trace session.
     */
    public static String LttngRelaydConsumer_NoMetadata;

    /**
     * No streams for this trace session.
     */
    public static String LttngRelaydConsumer_NoStreams;

    /**
     * The session was not found by the relayd.
     */
    public static String LttngRelaydConsumer_SessionNotFound;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
