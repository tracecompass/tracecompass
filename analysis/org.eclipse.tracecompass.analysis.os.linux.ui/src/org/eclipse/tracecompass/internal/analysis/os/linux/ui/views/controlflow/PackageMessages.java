/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for the package
 *
 * @noreference Messages class
 */
@SuppressWarnings("javadoc")
public class PackageMessages extends NLS {

    private static final String BUNDLE_NAME = PackageMessages.class.getPackage().getName() + ".messages"; //$NON-NLS-1$

    public static String ControlFlowView_NextEventActionName;
    public static String ControlFlowView_NextEventActionTooltip;
    public static String ControlFlowView_NextEventJobName;

    public static String ControlFlowView_PreviousEventActionName;
    public static String ControlFlowView_PreviousEventActionTooltip;
    public static String ControlFlowView_PreviousEventJobName;

    public static String ControlFlowView_DynamicFiltersActiveThreadToggleLabel;
    public static String ControlFlowView_DynamicFiltersActiveThreadToggleToolTip;
    public static String ControlFlowView_DynamicFiltersConfigureLabel;
    public static String ControlFlowView_DynamicFiltersMenuLabel;

    static {
        NLS.initializeMessages(BUNDLE_NAME, PackageMessages.class);
    }

    private PackageMessages() {
    }
}
