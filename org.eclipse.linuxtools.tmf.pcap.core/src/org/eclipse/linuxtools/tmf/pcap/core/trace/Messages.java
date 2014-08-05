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

package org.eclipse.linuxtools.tmf.pcap.core.trace;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.pcap.core.trace.messages"; //$NON-NLS-1$
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
