/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.graph.ui.criticalpath.view;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tracecompass.common.core.NonNullUtils;

/**
 * Externalized strings for the critical path view
 *
 * @author Geneviève Bastien
 */
@SuppressWarnings("javadoc")
public class Messages {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.graph.ui.criticalpath.view.messages"; //$NON-NLS-1$

    public static @Nullable String CriticalFlowView_multipleStates;

    public static @Nullable String CriticalFlowView_stateTypeName;

    public static @Nullable String CriticalFlowView_columnProcess;

    public static @Nullable String CriticalFlowView_columnElapsed;

    public static @Nullable String CriticalFlowView_columnPercent;

    public static @Nullable String CriticalPathModule_waitingForGraph;

    public static @Nullable String CriticalPathView_selectAlgorithm;
    public static @Nullable String CriticalPathView_followArrowFwdText;
    public static @Nullable String CriticalPathView_followArrowBwdText;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }

    /**
     * Helper method to expose externalized strings as non-null objects.
     */
    static String getMessage(@Nullable String msg) {
        return NonNullUtils.nullToEmptyString(msg);
    }

}
