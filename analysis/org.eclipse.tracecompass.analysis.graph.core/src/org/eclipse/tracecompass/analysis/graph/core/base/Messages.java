/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francis Giraldeau - Initial implementation and API
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.graph.core.base;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;

/**
 * Packages external string files
 *
 * @deprecated This class has been internalized in package
 *             {@link org.eclipse.tracecompass.internal.analysis.graph.core.base.Messages}
 */
@Deprecated
@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.analysis.graph.core.base.messages"; //$NON-NLS-1$

    public static @Nullable String TmfGraph_FromNotInGraph;

    public static @Nullable String TmfVertex_ArgumentTimestampLower;

    public static @Nullable String TmfVertex_CannotLinkToSelf;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
