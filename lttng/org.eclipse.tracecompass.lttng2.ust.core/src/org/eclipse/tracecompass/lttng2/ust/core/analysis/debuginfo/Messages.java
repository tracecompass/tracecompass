/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * Message bundle
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo.messages"; //$NON-NLS-1$

    public static @Nullable String UstDebugInfoAnalysis_BinaryAspectName;
    public static @Nullable String UstDebugInfoAnalysis_BinaryAspectHelpText;
    public static @Nullable String UstDebugInfoAnalysis_FunctionAspectName;
    public static @Nullable String UstDebugInfoAnalysis_FunctionAspectHelpText;
    public static @Nullable String UstDebugInfoAnalysis_SourceAspectName;
    public static @Nullable String UstDebugInfoAnalysis_SourceAspectHelpText;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
