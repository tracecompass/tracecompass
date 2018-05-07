/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Tassé - Initial API and implementation
 *     Mahdi Zolnouri - Add new String for ControlFlowView
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui;

import org.eclipse.osgi.util.NLS;

@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.os.linux.ui.messages"; //$NON-NLS-1$

    public static String ControlFlowView_birthTimeColumn;
    public static String ControlFlowView_threadPresentation;

    public static String ControlFlowView_tidColumn;
    public static String ControlFlowView_ptidColumn;
    public static String ControlFlowView_processColumn;
    public static String ControlFlowView_traceColumn;
    public static String ControlFlowView_invisibleColumn;

    public static String ControlFlowView_stateTypeName;
    public static String ControlFlowView_multipleStates;
    public static String ControlFlowView_nextProcessActionNameText;
    public static String ControlFlowView_nextProcessActionToolTipText;
    public static String ControlFlowView_previousProcessActionNameText;
    public static String ControlFlowView_previousProcessActionToolTipText;
    public static String ControlFlowView_followCPUBwdText;
    public static String ControlFlowView_followCPUFwdText;
    public static String ControlFlowView_checkActiveLabel;
    public static String ControlFlowView_checkActiveToolTip;
    public static String ControlFlowView_uncheckInactiveLabel;
    public static String ControlFlowView_uncheckInactiveToolTip;
    public static String ControlFlowView_attributeSyscallName;
    public static String ControlFlowView_flatViewLabel;
    public static String ControlFlowView_flatViewToolTip;
    public static String ControlFlowView_hierarchicalViewLabel;
    public static String ControlFlowView_hierarchicalViewToolTip;
    public static String ControlFlowView_optimizeLabel;
    public static String ControlFlowView_optimizeToolTip;

    public static String ResourcesView_stateTypeName;
    public static String ResourcesView_multipleStates;
    public static String ResourcesView_nextResourceActionNameText;
    public static String ResourcesView_nextResourceActionToolTipText;
    public static String ResourcesView_previousResourceActionNameText;
    public static String ResourcesView_previousResourceActionToolTipText;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
