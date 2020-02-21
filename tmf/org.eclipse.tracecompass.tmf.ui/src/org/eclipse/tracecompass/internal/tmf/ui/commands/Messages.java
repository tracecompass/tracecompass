/**********************************************************************
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
 *   Marc-Andre Laperle - Initial API and implementation
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.commands;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * Messages for commands
 *
 * @author Marc-Andre Laperle
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.ui.commands.messages"; //$NON-NLS-1$

    /** Select trace directory */
    public static String OpenDirHandler_SelectTraceDirectory;
    /** Select trace file */
    public static String OpenFileHandler_SelectTraceFile;
    /** Export to TSV message */
    public static @Nullable String AbstractSegmentStoreTableView_exportToTsv;
    /** Export to TSV tooltip */
    public static @Nullable String ExportToTsvAction_exportToTsvToolTip;
    /** Export to TSV tooltip */
    public static @Nullable String AbstractSegmentStoreScatterView_Type;
    /** Export to TSV tooltip */
    public static @Nullable String AbstractSegmentStoreScatterView_Legend;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
