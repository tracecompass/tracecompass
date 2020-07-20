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

package org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for the Disks I/O module
 *
 * @author Yonni Chen
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.messages"; //$NON-NLS-1$

    /** I/O aspect name: disk */
    public static @Nullable String IoAspect_DiskName;
    /** I/O aspect help text: disk */
    public static @Nullable String IoAspect_DiskHelpText;
    /** I/O aspect help text: request type */
    public static @Nullable String IoAspect_TypeHelpText;
    /** I/O aspect name: request type */
    public static @Nullable String IoAspect_TypeName;

    /**
     * Used for disk read series
     */
    public static @Nullable String DisksIODataProvider_read;

    /**
     * XY chart title
     */
    public static @Nullable String DisksIODataProvider_title;

    /**
     * Used for disk write series
     */
    public static @Nullable String DisksIODataProvider_write;

    /**
     * Data provider help text
     */
    public static @Nullable String DisksIODataProviderFactory_descriptionText;

    /** Help text for the IO analysis */
    public static @Nullable String LttngInputOutputModule_Help;
    /** Y axis caption */
    public static @Nullable String DiskIODataProvider_YAxis;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}