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

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.memory;

import org.eclipse.osgi.util.NLS;

/**
 * Messages for the shared {@link MemoryUsageView}.
 *
 * @since 2.2
 */
@SuppressWarnings("javadoc")
public class Messages {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.memory.messages"; //$NON-NLS-1$

    public static String MemoryUsageTree_ColumnProcess;
    public static String MemoryUsageTree_ColumnTID;
    public static String MemoryUsageTree_Legend;
    public static String MemoryUsageTree_Total;

    public static String MemoryView_FilterAction_Text;
    public static String MemoryView_FilterAction_FilteredTooltipText;
    public static String MemoryView_FilterAction_UnfilteredTooltipText;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
