/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.dialogs.offset;

import org.eclipse.osgi.util.NLS;

/**
 * Messages for the offset dialog
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.tmf.ui.project.dialogs.offset.messages"; //$NON-NLS-1$

    /**
     * Advanced mode button
     */
    public static String OffsetDialog_AdvancedButton;
    /**
     * Advanced mode dialog message
     */
    public static String OffsetDialog_AdvancedMessage;
    /**
     * Basic mode button
     */
    public static String OffsetDialog_BasicButton;
    /**
     * Basic mode dialog message
     */
    public static String OffsetDialog_BasicMessage;
    /**
     * Offset time
     */
    public static String OffsetDialog_OffsetTime;
    /**
     * Reference time
     */
    public static String OffsetDialog_ReferenceTime;
    /**
     * Target time
     */
    public static String OffsetDialog_TargetTime;
    /**
     * Dialog title
     */
    public static String OffsetDialog_Title;
    /**
     * Trace name
     */
    public static String OffsetDialog_TraceName;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
