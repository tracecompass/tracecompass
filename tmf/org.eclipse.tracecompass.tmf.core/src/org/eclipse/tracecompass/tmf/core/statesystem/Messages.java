/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.statesystem;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for org.eclipse.tracecompass.tmf.core.statesystem
 *
 * @author Geneviève Bastien
 * @since 2.0
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.tmf.core.statesystem.messages"; //$NON-NLS-1$

    /**
     * Analysis not executed text
     */
    public static @Nullable String TmfStateSystemAnalysisModule_PropertiesAnalysisNotExecuted;
    /**
     * Backend property text
     */
    public static @Nullable String TmfStateSystemAnalysisModule_PropertiesBackend;
    /**
     * File size property text
     */
    public static @Nullable String TmfStateSystemAnalysisModule_PropertiesFileSize;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
