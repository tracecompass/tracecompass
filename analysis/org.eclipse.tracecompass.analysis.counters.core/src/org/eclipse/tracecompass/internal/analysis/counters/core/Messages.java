/**********************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.counters.core;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for the Counters module
 *
 * @author Yonni Chen
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.counters.core.messages"; //$NON-NLS-1$

    /**
     * Help prefix for the counter aspect
     */
    public static @Nullable String CounterAspect_HelpPrefix;
    /**
     * Chart's title for counters view
     */
    public static @Nullable String CounterDataProvider_ChartTitle;

    /**
     * Name text for the data provider to be shown to the user
     */
    public static @Nullable String CounterDataProviderFactory_Title;
    /**
     * Description text for the data provider
     */
    public static @Nullable String CounterDataProviderFactory_DescriptionText;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
