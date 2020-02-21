/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Vincent Perot - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.pcap.core.trace;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.pcap.core.trace.messages"; //$NON-NLS-1$

    public static @Nullable String PcapTrace_FileEndianness;
    public static @Nullable String PcapTrace_LinkLayerHeaderType;
    public static @Nullable String PcapTrace_MaxSnapLength;
    public static @Nullable String PcapTrace_TimestampAccuracy;
    public static @Nullable String PcapTrace_TimeZoneCorrection;
    public static @Nullable String PcapTrace_Version;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
