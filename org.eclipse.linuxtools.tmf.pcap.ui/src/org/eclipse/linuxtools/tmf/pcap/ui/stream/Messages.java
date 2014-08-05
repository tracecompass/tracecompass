/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.pcap.ui.stream;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.pcap.ui.stream.messages"; //$NON-NLS-1$
    public static @Nullable String StreamListView_BPSAtoB;
    public static @Nullable String StreamListView_BPSBtoA;
    public static @Nullable String StreamListView_BytesAtoB;
    public static @Nullable String StreamListView_BytesBtoA;
    public static @Nullable String StreamListView_Clear;
    public static @Nullable String StreamListView_Duration;
    public static @Nullable String StreamListView_EndpointA;
    public static @Nullable String StreamListView_EndpointB;
    public static @Nullable String StreamListView_ExtractAsFilter;
    public static @Nullable String StreamListView_FilterName_Stream;
    public static @Nullable String StreamListView_FollowStream;
    public static @Nullable String StreamListView_ID;
    public static @Nullable String StreamListView_PacketsAtoB;
    public static @Nullable String StreamListView_PacketsBtoA;
    public static @Nullable String StreamListView_StartTime;
    public static @Nullable String StreamListView_StopTime;
    public static @Nullable String StreamListView_TotalBytes;
    public static @Nullable String StreamListView_TotalPackets;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
