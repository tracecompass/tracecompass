/*******************************************************************************
 * Copyright (c) 2018, 2019 Ericsson, Draeger, Auriga
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.views;

import org.eclipse.osgi.util.NLS;

/**
 * Messages class
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.ui.views.messages"; //$NON-NLS-1$
    /**
     * Toggle message for the many entries selected dialog
     */
    public static String ManyEntriesSelectedDialogPreCheckedListener_ManyEntriesSelectedDontShowAgain;
    /**
     * Message for the many entries selected dialog
     */
    public static String ManyEntriesSelectedDialogPreCheckedListener_ManyEntriesSelectedMessage;
    /**
     * Title for the many entries selected dialog
     */
    public static String ManyEntriesSelectedDialogPreCheckedListener_ManyEntriesSelectedTitle;
    /**
     * Title for the "Lock Y Axis" dialog
     */
    public static String TmfChartView_LockYAxis;
    /**
     * Text for the checkbox in the "Lock Y Axis" dialog
     */
    public static String TmfChartView_LockButton;
    /**
     * Label for the lower range input in the "Lock Y Axis" dialog
     */
    public static String TmfChartView_LowerYAxisRange;
    /**
     * Label for the upper range input in the "Lock Y Axis" dialog
     */
    public static String TmfChartView_UpperYAxisRange;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
