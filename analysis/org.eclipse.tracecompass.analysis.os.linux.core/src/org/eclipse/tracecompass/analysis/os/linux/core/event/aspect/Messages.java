/*******************************************************************************
 * Copyright (c) 2015  École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.event.aspect;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * @since 1.0
 * @noreference Messages class
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.analysis.os.linux.core.event.aspect.messages"; //$NON-NLS-1$

    public static @Nullable String AspectName_Tid;

    public static @Nullable String AspectHelpText_Tid;

    public static @Nullable String AspectName_Prio;

    public static @Nullable String AspectHelpText_Prio;

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }

    /**
     * Helper method to expose externalized strings as non-null objects.
     */
    static String getMessage(@Nullable String msg) {
        if (msg == null) {
            return ""; //$NON-NLS-1$
        }
        return msg;
    }
}
