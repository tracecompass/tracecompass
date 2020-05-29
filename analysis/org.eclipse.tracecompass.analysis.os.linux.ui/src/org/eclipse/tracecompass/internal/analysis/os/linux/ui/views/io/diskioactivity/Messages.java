/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.io.diskioactivity;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * Translatable strings for the Disk I/O Activity View
 *
 * @author Houssem Daoud
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.io.diskioactivity.messages"; //$NON-NLS-1$
    /** Title of the Disk I/O view */
    public static @Nullable String DiskIOActivityView_Title;
    /** Title of the Disk I/O activity viewer */
    public static @Nullable String DiskIOActivityViewer_Title;
    /** X axis caption */
    public static @Nullable String DiskIOActivityViewer_XAxis;
    /** Disk Name column */
    public static @Nullable String DiskIOActivityTreeViewer_DiskName;
    /** Legend Column*/
    public static @Nullable String DiskIOActivityTreeViewer_Legend;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
