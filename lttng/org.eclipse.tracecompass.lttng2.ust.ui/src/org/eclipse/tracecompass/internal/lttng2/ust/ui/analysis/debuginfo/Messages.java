/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.ui.analysis.debuginfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.osgi.util.NLS;

/**
 * Message bundle
 *
 * @noreference Messages class
 */
@NonNullByDefault({})
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private Messages() {}

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.lttng2.ust.ui.analysis.debuginfo.messages"; //$NON-NLS-1$

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    public static String PreferencePage_WindowDescription;

    public static String PreferencePage_CheckboxLabel;
    public static String PreferencePage_CheckboxTooltip;

    public static String PreferencePage_ButtonBrowse;
    public static String PreferencePage_ButtonClear;

    public static String PreferencePage_BrowseDialogTitle;
    public static String PreferencePage_ErrorDirectoryDoesNotExists;

}
