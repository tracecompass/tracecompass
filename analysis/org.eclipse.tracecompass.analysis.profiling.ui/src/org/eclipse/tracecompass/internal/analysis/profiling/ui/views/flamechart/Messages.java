/*******************************************************************************
 * Copyright (c) 2012, 2018 Ericsson and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.profiling.ui.views.flamechart;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * TMF message bundle
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.profiling.ui.views.flamechart.messages"; //$NON-NLS-1$

    public static @Nullable String CallStackPresentationProvider_Process;
    public static @Nullable String CallStackPresentationProvider_Thread;
    public static @Nullable String CallStackView_FunctionColumn;
    public static @Nullable String CallStackView_PidTidColumn;
    public static @Nullable String CallStackView_DepthColumn;
    public static @Nullable String CallStackView_EntryTimeColumn;
    public static @Nullable String CallStackView_ExitTimeColumn;
    public static @Nullable String CallStackView_DurationColumn;
    public static @Nullable String CallStackView_ThreadColumn;
    public static @Nullable String CallStackView_StackInfoNotAvailable;
    public static @Nullable String CallStackView_SortByThreadName;
    public static @Nullable String CallStackView_SortByThreadId;
    public static @Nullable String CallStackView_SortByThreadTime;

    public static @Nullable String CallStackView_ConfigureSymbolProvidersText;
    public static @Nullable String CallStackView_ConfigureSymbolProvidersTooltip;

    public static @Nullable String FlameChartView_NextItemActionNameText;
    public static @Nullable String FlameChartView_NextItemActionToolTipText;
    public static @Nullable String FlameChartView_PreviousItemActionNameText;
    public static @Nullable String FlameChartView_PreviousItemActionToolTipText;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
