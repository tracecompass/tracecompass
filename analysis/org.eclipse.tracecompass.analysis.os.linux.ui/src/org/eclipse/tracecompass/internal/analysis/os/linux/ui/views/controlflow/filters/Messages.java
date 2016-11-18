/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow.filters;

import org.eclipse.osgi.util.NLS;

/**
 * The messages for
 * {@link org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow.filters}
 *
 * @author Jonathan Rajotte Julien
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow.filters.messages"; //$NON-NLS-1$
    /** The CPUs ranges example */
    public static String DynamicFilterDialog_CpuRangesExamples;
    /** The CPUs radio button label*/
    public static String DynamicFilterDialog_CpuRangesLabel;
    /** The CPUs radio button tooltip  */
    public static String DynamicFilterDialog_CpuRangesTooltip;
    /** The active thread filter name */
    public static String DynamicFilterDialog_ActiveThreadsFilterName;
    /** Thrown error if the radios button are in a invalid state */
    public static String DynamicFilterDialog_InvalidRadioButtonState;
    /** The error message displayed when input ranges are invalid */
    public static String DynamicFilterDialog_InvalidRangesErrorMsg;
    /** The Options sub groups label */
    public static String DynamicFilterDialog_OptionsGroupLabel;
    /** The All CPUs option label */
    public static String DynamicFilterDialog_RadioButtonAllActiveThreads;
    /** The All CPUs option tooltip */
    public static String DynamicFilterDialog_RadioButtonAllActiveThreadsToolTip;
    /** The dialog title */
    public static String DynamicFilterDialog_Title;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
