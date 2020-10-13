/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.cpuusage;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for the CPU usage module
 *
 * @author Yonni Chen
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.os.linux.core.cpuusage.messages"; //$NON-NLS-1$

    /**
     * CPU Usage title
     */
    public static @Nullable String CpuUsageDataProvider_title;
    /**
     * Data provider help text
     */
    public static @Nullable String CpuUsageProviderFactory_DescriptionText;
    /**
     * Percentage format string
     */
    public static @Nullable String CpuUsageDataProvider_TextPercent;
    /**
     * Total label
     */
    public static @Nullable String CpuUsageDataProvider_Total;
    /**
     * Percent column text header
     */
    public static @Nullable String CpuUsageDataProvider_ColumnPercent;
    /**
     * Process column text header
     */
    public static @Nullable String CpuUsageDataProvider_ColumnProcess;
    /**
     * Time column text header
     */
    public static @Nullable String CpuUsageDataProvider_ColumnTime;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
