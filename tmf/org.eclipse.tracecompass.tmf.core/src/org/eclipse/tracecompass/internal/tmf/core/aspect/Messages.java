/*******************************************************************************
 * Copyright (c) 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.aspect;

import org.eclipse.osgi.util.NLS;

/**
 * Messages
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.tmf.core.aspect.messages"; //$NON-NLS-1$

    /**
     * Callsite name
     */
    public static String TmfCallsiteAspect_name;

    /**
     * Callsite description
     */
    public static String TmfCallsiteAspect_description;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
