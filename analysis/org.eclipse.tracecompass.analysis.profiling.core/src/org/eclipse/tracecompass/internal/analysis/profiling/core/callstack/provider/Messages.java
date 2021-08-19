/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.profiling.core.callstack.provider;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for the call stack state provider.
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.profiling.core.callstack.provider.messages"; //$NON-NLS-1$

    /**
     * The value popped from a 'func_exit' event doesn't match the current
     * function name.
     */
    public static @Nullable String CallStackStateProvider_UnmatchedPoppedValue;

    /**
     * There have been issues with the callstack, show the amount of errors.
     */
    public static @Nullable String CallStackStateProvider_IncoherentCallstack;

    public static String CallStackDataProvider_toolTipAddress;

    public static String CallStackDataProvider_toolTipState;

    /**
     * Name of the data provider shown to the user
     */
    public static @Nullable String CallStackDataProviderFactory_title;
    /**
     * Help text for the data descriptor
     */
    public static @Nullable String CallStackDataProviderFactory_descriptionText;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
