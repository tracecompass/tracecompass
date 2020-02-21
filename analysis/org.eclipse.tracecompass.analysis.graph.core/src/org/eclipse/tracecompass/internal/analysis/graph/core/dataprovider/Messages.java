/*******************************************************************************
 * Copyright (c) 2020 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.graph.core.dataprovider;

import org.eclipse.osgi.util.NLS;

/**
 * Messages for the critical path data provider
 *
 * @author Geneviève Bastien
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.graph.core.dataprovider.messages"; //$NON-NLS-1$
    /** Label for the arrow group */
    public static String CriticalPathDataProvider_GroupArrows;
    /** Label for network arrows */
    public static String CriticalPathDataProvider_NetworkArrow;
    /** Label for unknown arrows */
    public static String CriticalPathDataProvider_UnknownArrow;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Do nothing
    }
}
