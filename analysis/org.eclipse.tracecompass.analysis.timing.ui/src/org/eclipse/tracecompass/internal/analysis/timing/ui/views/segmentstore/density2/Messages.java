/*******************************************************************************
 * Copyright (c) 2015, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.density2;

import org.eclipse.osgi.util.NLS;

/**
 * @author Marc-Andre Laperle
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.density2.messages"; //$NON-NLS-1$

    /**
     * Tooltip count message
     */
    public static String SimpleTooltipProvider_count;

    /**
     * Tooltip duration message
     */
    public static String SimpleTooltipProvider_duration;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
